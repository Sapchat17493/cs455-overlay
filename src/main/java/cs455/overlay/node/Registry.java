package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.*;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The class for handling the registry node.
 */
public class Registry implements Node {
    private static final boolean DEBUG = Protocol.DEBUG;
    private static final int MAX_NUM_NODES = 127;
    private static TCPServerThread tcpServerThread;
    private static CommandParser commandParser;
    private volatile static LinkedBlockingQueue<String> commandQueue; // Blocks and "thread-safely" provides commands
    private volatile static TCPConnectionsCache tcpConnectionsCache;
    private static EventManager eventManager;
    private boolean acceptCommands;
    private int port;

    private static Random iDGenerator;
    private volatile ConnectionManager connectionManager;
    private int nodesReady;
    private int nodesFinished;
    private StatisticsCollectorAndDisplay stats;

    /**
     *
     * @param portNum Initialize the server socket on which the registry listens for connections. Subscribe to the event manager to get notified of events coming in
     * @throws IOException
     */
    private Registry(int portNum) throws IOException {
        this.port = portNum;
        commandQueue = new LinkedBlockingQueue<String>();
        commandParser = new CommandParser(this, commandQueue);
        (new Thread(commandParser)).start();

        eventManager = new EventManager(this);
        eventManager.start();

        tcpConnectionsCache = new TCPConnectionsCache();

        tcpServerThread = new TCPServerThread(tcpConnectionsCache, portNum);
        (new Thread(tcpServerThread)).start();

        iDGenerator = new Random();

        acceptCommands = true;
        connectionManager = new ConnectionManager();
        nodesReady = 0;
        nodesFinished = 0;

        stats = null;
    }

    /**
     * Handle incoming events
     * @param socketAndEvent the socket and event on which the event/message arrived
     */
    @Override
    public void onEvent(SocketAndEvent socketAndEvent) {
        if (socketAndEvent.getEvent() instanceof OverlayNodeSendsRegistration) {
            try {
                executeOverlayNodeSendsRegistration(socketAndEvent);
            } catch (Exception e) {
                System.out.println("Could not send Registration status\n" + e.getMessage());
            }
        } else if (socketAndEvent.getEvent() instanceof OverlayNodeSendsDeregistration) {
            try {
                executeOverlayNodeSendsDeregistration(socketAndEvent);
            } catch (IOException ioe) {
                System.out.println("Could not send Deregistration status\n" + ioe.getMessage());
            }
        } else if (socketAndEvent.getEvent() instanceof NodeReportsOverlaySetupStatus) {
            executeNodeReportsOverlaySetupStatus(socketAndEvent);
        } else if (socketAndEvent.getEvent() instanceof OverlayNodeReportsTaskFinished) {
            try {
                executeOverlayNodeReportsTaskFinished(socketAndEvent);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else if (socketAndEvent.getEvent() instanceof OverlayNodeReportsTrafficSummary) {
            executeOverlayNodeReportsTrafficSummary(socketAndEvent);
        } else {
            System.out.println("Unknown message");
        }
    }

    private void executeOverlayNodeSendsRegistration(SocketAndEvent socketAndEvent) throws Exception {
        OverlayNodeSendsRegistration event = (OverlayNodeSendsRegistration) socketAndEvent.getEvent();

        RegistryReportsRegistrationStatus regStatus = new RegistryReportsRegistrationStatus();
        String info;

        if (!Arrays.equals(event.getIpAddr(), socketAndEvent.getSocket().getInetAddress().getAddress())) {
            if (DEBUG) {
                System.out.println("eventSocket.getAddress is: " + Arrays.toString(socketAndEvent.getSocket().getInetAddress().getAddress()));
                System.out.println("e.getmessageIP is: " + Arrays.toString(event.getIpAddr()));
            }

            info = "IP address and port within message did not match sender's IP Address and port";
            regStatus.setStatus(-1);
            regStatus.setInfo(info.getBytes());
            regStatus.setInfo_len((byte) info.length());
        }

        if (tcpConnectionsCache.containsConnection(socketAndEvent.getSocket()) && connectionManager.hasConnection(socketAndEvent.getSocket()) && connectionManager.getConnection(socketAndEvent.getSocket()).isRegistered()) {
            // if connected and already assigned ID, reply with failed registration

            regStatus.setStatus(-1);
            info = "This node has already been registered, cannot re-register";
            regStatus.setInfo(info.getBytes());
            regStatus.setInfo_len((byte) info.length());
        } else if (tcpConnectionsCache.containsConnection(socketAndEvent.getSocket()) && !connectionManager.hasConnection(socketAndEvent.getSocket())) {
            int assignedID;

            do {
                assignedID = iDGenerator.nextInt(MAX_NUM_NODES + 1);
            } while (connectionManager.hasID(assignedID));
            connectionManager.addConnection(socketAndEvent.getSocket().getInetAddress().getAddress(), event.getPort(), assignedID, tcpConnectionsCache.getConnection(socketAndEvent.getSocket()));
            info = "Registration successful. The number of messaging nodes that are currently part of the overlay is (" + connectionManager.size() + ")";
            regStatus.setStatus(assignedID);
            regStatus.setInfo(info.getBytes());
            regStatus.setInfo_len((byte) info.length());

        } else {
            System.out.println("Received a registration request from " + Arrays.toString(tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).getDestinationAddress()) + ":" + tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).getDestinationPort() + " that is not present in the ConnectionsCache");
        }

        try {
            tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).send(regStatus.getBytes());
        } catch (IOException ioe) {
            System.out.println("A messaging node has failed before a reply to the registration request could be sent. Removing node connection with Registry.");
            if (connectionManager.hasConnection(socketAndEvent.getSocket())) {
                connectionManager.removeConnection(socketAndEvent.getSocket());
            }
            if (tcpConnectionsCache.containsConnection(socketAndEvent.getSocket())) {
                tcpConnectionsCache.removeConnection(socketAndEvent.getSocket());
            }
        }

    }

    private void executeNodeReportsOverlaySetupStatus(SocketAndEvent socketAndEvent) {
        NodeReportsOverlaySetupStatus event = (NodeReportsOverlaySetupStatus) socketAndEvent.getEvent();

        if (event.getStatus() == -1) {
            System.out.println("Connection setup unsuccessful with a node");
            System.out.println(new String(event.getInfo()));
        } else if (event.getStatus() != connectionManager.getConnection(socketAndEvent.getSocket()).getId()) {
            System.out.println("Received an ID that is not the sender's assigned ID");
            return;
        } else {
            nodesReady += 1;
            if (nodesReady == connectionManager.size() && connectionManager.isReadyToInitiateTask()) {
                System.out.println("All messaging nodes have successfully initiated the connection. Registry now ready to initiate tasks.");
            }
        }
    }

    private void executeOverlayNodeSendsDeregistration(SocketAndEvent socketAndEvent) throws IOException {
        OverlayNodeSendsDeregistration event = (OverlayNodeSendsDeregistration) socketAndEvent.getEvent();
        RegistryReportsDeregistrationStatus deRegstatus = new RegistryReportsDeregistrationStatus();
        String info;

        if (!Arrays.equals(event.getIpAddr(), socketAndEvent.getSocket().getInetAddress().getAddress())) {
            info = "IP address and port within deregistration request did not match the sender's IP Address & port";
            deRegstatus.setStatus(-1);
            deRegstatus.setInfo(info.getBytes());
            deRegstatus.setInfo_len((byte) info.length());
            tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).send(deRegstatus.getBytes());
            return;
        }

        if (tcpConnectionsCache.containsConnection(socketAndEvent.getSocket())
                && connectionManager.hasConnection(socketAndEvent.getSocket())
                && event.getId() == connectionManager.getConnection(socketAndEvent.getSocket()).getId()) {
            //successful deregistration

            deRegstatus.setStatus(connectionManager.removeConnection(socketAndEvent.getSocket()).getId());
            info = "Deregistration request successful. The number of messaging nodes that are currently part of the overlay is (" + connectionManager.size() + ")";
            deRegstatus.setInfo(info.getBytes());
            deRegstatus.setInfo_len((byte) info.length());
            tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).send(deRegstatus.getBytes());

        } else if (tcpConnectionsCache.containsConnection(socketAndEvent.getSocket())
                && !connectionManager.hasConnection(socketAndEvent.getSocket())) {
            // if node is connected but not registered(which is strange), unsuccessful deregistration
            deRegstatus.setStatus(-1);
            info = "Deregistration request unsuccessful. You are connected but not registered.";
            deRegstatus.setInfo(info.getBytes());
            deRegstatus.setInfo_len((byte) info.length());
            connectionManager.getConnection(socketAndEvent.getSocket()).getTCPConnection().send(deRegstatus.getBytes());

        } else {
            System.out.println("Received a deregistration request from " + Arrays.toString(tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).getDestinationAddress()) + ":" + tcpConnectionsCache.getConnection(socketAndEvent.getSocket()).getDestinationPort() + " that is not present in the ConnectionsCache");
        }
    }

    /**
     * The command queue is linked to the event manager which provides us with the command line actions to be taken
     */
    private void executeIncomingCommands() {
        String command;
        while (acceptCommands) {
            try {
                command = commandQueue.take();
                switch (command) {
                    case "setup-overlay":
                        int nr = Integer.parseInt(commandQueue.take());
                        if (nr < 1 || Math.pow(2, nr) > connectionManager.size()) {
                            System.out.println("A valid number of entries e for a messaging node is bounded by 0 < e <= log_2<number of messaging nodes>. Please provide an appropriate number of routing table entries for the setup-overlay command.");
                        } else {
                            try {
                                setupOverlay(nr);
                            } catch (IOException ioe) {
                                System.out.println("Overlay Setup Failed");
                            }
                        }
                        break;
                    case "list-routing-tables":
                        if (!connectionManager.isEmpty() && !connectionManager.get(0).isRoutingTablePresent()) {
                            System.out.println("The overlay must be set up before executing this command, otherwise the overlay does not exist yet.");
                        } else {
                            for (Connection c : connectionManager.getConnections()) {
                                System.out.println("Node ID: " + c.getId() + " has the following directly connected peers");
                                System.out.println(c.getRoutingTable().toString());
                            }
                        }
                        break;
                    case "list-messaging-nodes":
                        connectionManager.showConnections();
                        break;
                    case "start":
                        int noOfPacketsToSend = Integer.parseInt(commandQueue.take());
                        try {
                            start(noOfPacketsToSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }


            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }


    }

    private void executeOverlayNodeReportsTaskFinished(SocketAndEvent socketAndEvent) throws IOException {
        nodesFinished += 1;
        if (nodesFinished == connectionManager.size()) {
            try {
                Thread.sleep(10000); //sleep so that registry doesn't overwhelm messaging nodes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Connection c : connectionManager.getConnections()) {
                c.getTCPConnection().send((new RegistryRequestsTrafficSummary()).getBytes());
            }
            nodesFinished = 0;
        }

    }


    private void executeOverlayNodeReportsTrafficSummary(SocketAndEvent socketAndEvent) {
        OverlayNodeReportsTrafficSummary event = (OverlayNodeReportsTrafficSummary) socketAndEvent.getEvent();
        stats.addStats(event);
    }


    private void start(int noOfPacketsToSend) throws IOException {
        RegistryRequestsTaskInitiate initiate = new RegistryRequestsTaskInitiate();
        if (stats == null) {
            stats = new StatisticsCollectorAndDisplay(connectionManager.size()); //create new instance of statistics collector
        }
        stats.clearStats();
        initiate.setNoOfPackets(noOfPacketsToSend);
        for (Connection c : connectionManager.getConnections()) {
            c.getTCPConnection().send(initiate.getBytes());
        }

    }


    private void setupOverlay(int numRoutingTableEntries) throws IOException {
        int nodeIDIndex;
        connectionManager.sort();

        for (int i = 0; i < connectionManager.size(); i++) {
            RoutingTable tempRoutingTable = new RoutingTable(numRoutingTableEntries);

            for (int j = 0; j < numRoutingTableEntries; j++) {

                // Each subsequent routing entry is supposed to be 2^0, 2^1, 2^2 ... 2^(log(n)) hops away from the current node,
                // i.e., or any particular messaging node instance which is the message source, where n is the number of routing entries
                nodeIDIndex = (i + (int) Math.pow((double) 2, (double) j)) % connectionManager.size();
                tempRoutingTable.add(new RoutingEntry(connectionManager.get(nodeIDIndex).getId(), connectionManager.get(nodeIDIndex).getListenPort(), connectionManager.get(nodeIDIndex).getDestIP(),
                        (int) Math.pow((double) 2, (double) j)));
            }

            connectionManager.get(i).addRoutingTable(tempRoutingTable);
        }
        for (Connection c : connectionManager.getConnections()) {
            // send each connection(node) its particular routing table and a list of all IDs in the system
            c.getTCPConnection().send((new RegistrySendsNodeManifest(c.getRoutingTable(), connectionManager.getIDs())).getBytes());
        }
    }


    public static void main(String args[]) {
        int portNum;

        if (args.length == 0) {
            System.out.println("Not enough arguments. Please provide a valid port number if you want to set this node as the Registry");
        }

        portNum = Integer.parseInt(args[0]);
        Registry reg;

        try {
            reg = new Registry(portNum);
            reg.executeIncomingCommands();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
