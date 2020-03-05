package cs455.overlay.transport;

import cs455.overlay.wireformats.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Encapsulates a server thread on which the nodes listen for incoming connections when the thread is started
 */
public class TCPServerThread implements Runnable {
    private ServerSocket serverSocket;
    private volatile boolean acceptConnections;
    private volatile TCPConnectionsCache tcpConnectionsCache;
    private static final boolean DEBUG = Protocol.DEBUG;


    public TCPServerThread(TCPConnectionsCache tcpConnectionsCache, int portNum) throws IOException, IllegalArgumentException {
        this.serverSocket = new ServerSocket(portNum);
        this.tcpConnectionsCache = tcpConnectionsCache;
    }

    /**
     * Wait for connection and add to cache once connection is made
     */
    @Override
    public void run() {
        acceptConnections = true;

        while (acceptConnections) {
            try {
                Socket socket = serverSocket.accept();
                tcpConnectionsCache.addConnection(new TCPConnection(socket));

            } catch (IOException ioe) {
                System.out.println("I/O error waiting for connection");
                ioe.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void stopListening() {
        acceptConnections = false;
        if (DEBUG)
            System.out.println("Stopped accepting connections");
    }

    public int getListenPort() {
        return serverSocket.getLocalPort();
    }
}
