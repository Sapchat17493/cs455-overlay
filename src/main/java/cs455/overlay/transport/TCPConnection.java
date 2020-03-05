package cs455.overlay.transport;

import cs455.overlay.wireformats.Protocol;

import java.io.IOException;
import java.net.Socket;


/**
 * This class starts the receiver thread on the mesasging node and connect it to the registry and also encapsulates all the TCPConnections used in this project
 */
public class TCPConnection {
    private static final boolean DEBUG = Protocol.DEBUG;
    private Socket socket;
    private TCPSender tcpSender;
    private TCPReceiverThread tcpReceiverThread;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        tcpSender = new TCPSender(socket);
        tcpReceiverThread = new TCPReceiverThread(socket);
        (new Thread(tcpReceiverThread,"ThreadToReceive")).start();

    }

    public Socket getSocket() {
        return socket;
    }

    public void send(byte[] data) throws IOException {
        if(DEBUG && data.length == 0)
            System.out.println("Zero length data");

        tcpSender.sendData(data);
    }

    public byte[] getDestinationAddress() {
        return socket.getInetAddress().getAddress();
    }

    public int getDestinationPort() {
        return socket.getPort();
    }

    public byte[] getLocalAddress() {
        return socket.getLocalAddress().getAddress();
    }

    private int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String toString() {
        return "Destination Address: " + new String(getDestinationAddress()) + "\n Local Address: " + new String(getLocalAddress()) + "\n Destination Port: " + getDestinationPort() + "\n Local Port: " + getLocalPort() + "\n\n";
    }
}
