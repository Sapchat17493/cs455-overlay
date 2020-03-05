package cs455.overlay.transport;

import java.net.Socket;
import java.util.ArrayList;


/**
 * This classes caches the TCPConnections
 */
public class TCPConnectionsCache {
    private static volatile ArrayList<TCPConnection> tcpConnections;

    public TCPConnectionsCache () {
        tcpConnections = new ArrayList<TCPConnection>();
    }

    public synchronized boolean containsConnection(Socket socket) {
        for(TCPConnection tcpConnection : tcpConnections) {
            if(tcpConnection.getSocket().equals(socket)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addConnection(TCPConnection tcpConnection) {
        tcpConnections.add(tcpConnection);
    }

    public synchronized TCPConnection getConnection(Socket socket) {
        for(TCPConnection tcpConnection : tcpConnections) {
            if(tcpConnection.getSocket().equals(socket))
                return tcpConnection;
        }
        return null;
    }

    public TCPConnection removeConnection(Socket socket) {
        TCPConnection returnedConnection = null;
        if(containsConnection(socket)) {
            returnedConnection = getConnection(socket);
        }
        tcpConnections.remove(returnedConnection);
        return returnedConnection;
    }

    public String showConnections() {
        StringBuilder res = new StringBuilder();
        for(TCPConnection tcpConnection : tcpConnections) {
            res.append(tcpConnection.toString());
        }
        return res.toString();
    }
}
