package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.CommandParser;
import cs455.overlay.util.ConnectionManager;
import cs455.overlay.util.EventManager;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The class handling all functionalities of the messaging node instances
 */
public class MessagingNode implements Node {
    private static final boolean DEBUG = Protocol.DEBUG;
    private TCPConnection registryConnection;
    private TCPServerThread tcpServerThread; // for listening to incoming connections from other MessagingNodes
    private volatile TCPConnectionsCache tcpConnectionsCache;
    private CommandParser commandParser;
    private volatile LinkedBlockingQueue<String> commandQueue;

    private EventManager eventManager;

    private volatile boolean acceptCommands;
    private int assignedID;
    private volatile RoutingTable routingTable;
    private int[] nodeIDs;
    private ConnectionManager connectionManager;
    private static Random payloadGenerator;

    private volatile AtomicInteger sendTracker;
    private volatile AtomicInteger receiveTracker;
    private volatile AtomicInteger relayTracker;
    private volatile AtomicLong sentTotal;
    private volatile AtomicLong receivedTotal;

    public MessagingNode(Socket regSocket) throws IOException {
        sendTracker = new AtomicInteger(0);
        receiveTracker = new AtomicInteger(0);
        relayTracker = new AtomicInteger(0);
        sentTotal = new AtomicLong(0);
        receivedTotal = new AtomicLong(0);

        registryConnection = new TCPConnection(regSocket);

        tcpConnectionsCache = new TCPConnectionsCache();

        tcpServerThread = new TCPServerThread(tcpConnectionsCache, 0);
        (new Thread(tcpServerThread)).start();

        commandQueue = new LinkedBlockingQueue<String>();
        commandParser = new CommandParser(this, commandQueue);
        (new Thread(commandParser)).start();

        eventManager = new EventManager(this);
        eventManager.start();


        OverlayNodeSendsRegistration message = new OverlayNodeSendsRegistration();
        message.setIp_len((byte) regSocket.getLocalAddress().getAddress().length);
        message.setIp_addr(regSocket.getLocalAddress().getAddress());
        message.setPort(tcpServerThread.getListenPort());

        registryConnection.send(message.getBytes());

        acceptCommands = true;

        connectionManager = new ConnectionManager();

        payloadGenerator = new Random();
    }

    /**
     * The command queue is linked to the event manager which provides us with the command line actions to be taken
     * @throws IOException
     */
    private void executeIncomingCommands() throws IOException {
        String command;
        while (acceptCommands) {
            try {
                command = commandQueue.take();
                if (command.equalsIgnoreCase("print-counters-and-diagnostics")) {
                    System.out.println("This node has ID: " + assignedID);
                    System.out.println("Packets sent: " + sendTracker.get());
                    System.out.println("Packets relayed: " + relayTracker.get());
                    System.out.println("Sum of packet values sent: " + sentTotal.get());
                    System.out.println("Packets received: " + receiveTracker.get());
                    System.out.println("Sum of packet values received: " + receivedTotal.get());
                } else if (command.equalsIgnoreCase("exit-overlay")) {
                    OverlayNodeSendsDeregistration deRegMsg = new OverlayNodeSendsDeregistration();
                    deRegMsg.setIp_len((byte) InetAddress.getLocalHost().getAddress().length);
                    deRegMsg.setIp_addr(InetAddress.getLocalHost().getAddress());
                    deRegMsg.setPort(tcpServerThread.getListenPort());
                    deRegMsg.setId(assignedID);
                    registryConnection.send(deRegMsg.getBytes());
                } else {
                    System.out.println("Invalid Command\n");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Handle incoming events
     * @param socketAndEvent the socket and event on which the event/message arrived
     */
    @Override
    public void onEvent(SocketAndEvent socketAndEvent) {
        if (socketAndEvent.getEvent() instanceof OverlayNodeSendsData) {
            try {
                executeOverlayNodeSendsData(socketAndEvent);
            } catch (IOException e) {
                e.printStackTrace();
                if (DEBUG)
                    System.out.println("Error receiving data from overlay node");
            }
        } else if (socketAndEvent.getEvent() instanceof RegistryReportsRegistrationStatus) {
            executeRegistryReportsRegistrationStatus(socketAndEvent);
        } else if (socketAndEvent.getEvent() instanceof RegistryReportsDeregistrationStatus) {
            executeRegistryReportsDeregistrationStatus(socketAndEvent);
        } else if (socketAndEvent.getEvent() instanceof RegistryRequestsTaskInitiate) {
            try {
                executeRegistryRequestsTaskInitiate(socketAndEvent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (socketAndEvent.getEvent() instanceof RegistrySendsNodeManifest) {
            executeRegistrySendsNodeManifest(socketAndEvent);
        } else if (socketAndEvent.getEvent() instanceof RegistryRequestsTrafficSummary) {
            try {
                executeRegistryRequestsTrafficSummary();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Unknown Event found, type = " + socketAndEvent.getEvent().getType());

    }

    private void executeRegistryReportsRegistrationStatus(SocketAndEvent socketAndEvent) {
        RegistryReportsRegistrationStatus regStatus = (RegistryReportsRegistrationStatus) socketAndEvent.getEvent();
        int successStatus = regStatus.getStatus();
        if (successStatus == -1) {
            System.out.println("From Registry -> Registration Failed");
            System.out.println(String.valueOf(new String(regStatus.getInfo())));
            System.out.println("Try again please");
            System.exit(1);
        } else {
            assignedID = successStatus;
            System.out.println(new String(regStatus.getInfo()));
        }


    }

    private void executeRegistryReportsDeregistrationStatus(SocketAndEvent socketAndEvent) {
        RegistryReportsDeregistrationStatus deRegStatus = (RegistryReportsDeregistrationStatus) socketAndEvent.getEvent();
        int successStatus = (deRegStatus.getStatus());
        if (successStatus == assignedID) {
            System.out.println("From Registry -> Deregistration Successful");
            //registryConnection = null;
            System.exit(0);
        } else {
            System.out.println("From Registry -> Deregistration Unsuccessful");
            System.out.println(new String(deRegStatus.getInfo()));
        }
    }


    private void executeRegistrySendsNodeManifest(SocketAndEvent socketAndEvent) {
        System.out.println("Received Peer Node Manifest");
        RegistrySendsNodeManifest manifest = (RegistrySendsNodeManifest) socketAndEvent.getEvent();
        routingTable = new RoutingTable(manifest.getTableSize());

        for (int i = 0; i < routingTable.getNr(); i++) {
            routingTable.add(new RoutingEntry(manifest.getNodesToConnect()[i], manifest.getPorts()[i], manifest.getIp_addrs()[i], (int) Math.pow((double) 2, (double) i)));
        }

        nodeIDs = new int[manifest.getNumIDs()];
        nodeIDs = manifest.getAllIDs();

        try {
            initConnections();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Connection Initiation failed");
        }
    }

    private void initConnections() throws UnknownHostException, IOException {
        Socket socket;
        NodeReportsOverlaySetupStatus initiation = new NodeReportsOverlaySetupStatus();

        for (int i = 0; i < routingTable.getSize(); i++) {
            if (DEBUG) {
                System.out.println("Connecting to this IP from routing table entry " + i + " : " + InetAddress.getByAddress(routingTable.getIndex(i).getIpAddr()));
                System.out.println("Connecting to this port for above IP from routing table entry " + i + " : " + routingTable.getIndex(i).getPort());
            }
            socket = new Socket(InetAddress.getByAddress(routingTable.getIndex(i).getIpAddr()), routingTable.getIndex(i).getPort());
            tcpConnectionsCache.addConnection(new TCPConnection(socket));
            connectionManager.addConnection(socket.getInetAddress().getAddress(), socket.getPort(), routingTable.getIndex(i).getId(), tcpConnectionsCache.getConnection(socket));
        }

        initiation.setStatus(assignedID);
        String infoString = "Node has initiated connections successfully";
        initiation.setInfo_len((byte) infoString.length());
        initiation.setInfo(infoString.getBytes());

        registryConnection.send(initiation.getBytes());

    }


    private void executeOverlayNodeSendsData(SocketAndEvent socketAndEvent) throws IOException {
        OverlayNodeSendsData data = (OverlayNodeSendsData) socketAndEvent.getEvent();
        data.setHops(data.getHops() + 1);
        int[] intermediateNodeIDs = new int[data.getHops()];

        intermediateNodeIDs[intermediateNodeIDs.length - 1] = assignedID;
        data.setIntermediateIDs(intermediateNodeIDs);

        if (data.getDestId() == assignedID) {
            receivedTotal.addAndGet((long) data.getPayload());
            receiveTracker.incrementAndGet();
        } else if (routingTable.containsID(data.getDestId())) {
            connectionManager.getConnection(data.getDestId()).getTCPConnection().send(data.getBytes());
            relayTracker.incrementAndGet();
        } else {
            connectionManager.getConnection(routingTable.findCorrectNode(data, assignedID, nodeIDs)).getTCPConnection().send(data.getBytes());
            relayTracker.incrementAndGet();

        }
    }


    private void executeRegistryRequestsTaskInitiate(SocketAndEvent socketAndEvent) throws IOException {
        int numPacketsToSend;
        RegistryRequestsTaskInitiate event = (RegistryRequestsTaskInitiate) socketAndEvent.getEvent();
        numPacketsToSend = event.getNoOfPackets();
        int payloadToSend;
        int destIDIndex;
        OverlayNodeSendsData data;

        //Refreshing all counters
        sendTracker = new AtomicInteger(0);
        relayTracker = new AtomicInteger(0);
        sentTotal = new AtomicLong(0);
        receiveTracker = new AtomicInteger(0);
        receivedTotal = new AtomicLong(0);

        for (int i = 0; i < numPacketsToSend; i++) {
            data = new OverlayNodeSendsData();
            payloadToSend = payloadGenerator.nextInt();

            sendTracker.incrementAndGet();
            sentTotal.addAndGet((long) payloadToSend);

            // Selecting random nodeID to send randomly generated integer payload
            do {
                destIDIndex = payloadGenerator.nextInt(nodeIDs.length);
            } while (nodeIDs[destIDIndex] == assignedID);

            data.setMsg_type(Protocol.OVERLAY_NODE_SENDS_DATA);
            data.setPayload(payloadToSend);
            data.setDestId(nodeIDs[destIDIndex]);
            data.setSrcId(assignedID);
            data.setHops(0);
            data.setIntermediateIDs(new int[nodeIDs.length]);

            if (routingTable.containsID(data.getDestId())) {
                try {
                    connectionManager.getConnection(data.getDestId()).getTCPConnection().send(data.getBytes());
                } catch (NullPointerException | IOException ioe) {
                    ioe.getStackTrace();
                    ioe.getMessage();
                }
            } else {
                connectionManager.getConnection(routingTable.findCorrectNode(data, assignedID, nodeIDs)).getTCPConnection().send(data.getBytes());
            }

            //Tried this after switching trackers to atomic and everything looked fixed, so I am letting this be. Does not cause any visible slowdown. To be removed later-
            try {
                Thread.sleep(2L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // After all messages have been sent,rounds are done
        informRegistryOfFinishedTask();
    }


    private void informRegistryOfFinishedTask() throws IOException {
        OverlayNodeReportsTaskFinished finished = new OverlayNodeReportsTaskFinished();
        finished.setIp_len((byte) registryConnection.getLocalAddress().length);
        finished.setIp_addr(registryConnection.getLocalAddress());
        finished.setPort(tcpServerThread.getListenPort());
        registryConnection.send(finished.getBytes());

    }


    private void executeRegistryRequestsTrafficSummary() throws IOException, InterruptedException {
        OverlayNodeReportsTrafficSummary report = new OverlayNodeReportsTrafficSummary();

        report.setId(assignedID);
        report.setPacketsSent(sendTracker.get());
        report.setPacketsRelayed(relayTracker.get());
        report.setTotalPacketsSent(sentTotal.get());
        report.setPacketsReceived(receiveTracker.get());
        report.setTotalPacketsReceived(receivedTotal.get());

        if (DEBUG)
            System.out.println("Sending traffic summary to registry");
        registryConnection.send(report.getBytes());
    }


    public static void main(String args[]) {
        String regIP;
        int regPort;

        if (args.length != 2) {
            System.out.println("Not enough arguments. Please provide Registry hostname and port.");
            return;
        }

        regIP = args[0];
        regPort = Integer.parseInt(args[1]);

        MessagingNode mn = null;

        do {
            try {
                mn = new MessagingNode(new Socket(regIP, regPort));
                mn.executeIncomingCommands();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("\nFailed to connect to Registry Socket");
            } finally {
                System.out.println("Closing");
            }
            if (mn == null) {
                System.out.println("Failed to establish a connection. Make sure you have entered the correct port number.\n Enter port of Registry again:");
                regPort = Integer.parseInt((new Scanner(System.in)).next());
            }
        } while (mn == null);

    }
}
