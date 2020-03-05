package cs455.overlay.util;

import cs455.overlay.transport.TCPConnection;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class keeps a record of and manages all the connections between all nodes and contains all helper methods needed to manage connections and put them in the cache or remove them from the cache
 */
public class ConnectionManager {
    private ArrayList<Connection> connections;

    public ConnectionManager() {
        connections = new ArrayList<Connection>();
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public Connection getConnection(Socket socket) {
        for (Connection c : connections) {
            if (c.getTCPConnection().getSocket().equals(socket)) {
                return c;
            }
        }
        return null;
    }


    public Connection getConnection(int id) {
        for (Connection c : connections) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public void sort() {
        Collections.sort(connections);
    }

    public boolean hasConnection(Socket socket) {
        for (Connection c : connections) {
            if (c.getTCPConnection().getSocket().equals(socket)) {
                return true;
            }
        }
        return false;
    }


    public boolean hasID(int id) {
        for (Connection c : connections) {
            if (c.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void addConnection(byte[] destinationIP, int listeningPort, int id, TCPConnection tcpConnection) {
        connections.add(new Connection(destinationIP, listeningPort, id, tcpConnection));
    }

    public Connection removeConnection(Socket socket) {
        Connection returnedConnection = null;
        for (Connection connection : connections) {
            returnedConnection = connection;
            if (returnedConnection.getTCPConnection().getSocket().equals(socket)) {
                connections.remove(returnedConnection);
                break;
            }
        }
        return returnedConnection;
    }

    public boolean isReadyToInitiateTask() {
        for (Connection c : connections) {
            if (!c.isRoutingTablePresent()) {
                return false;
            }
        }
        return true;
    }

    public int[] getIDs() {
        int[] ids = new int[size()];
        for (int i = 0; i < size(); i++) {
            ids[i] = connections.get(i).getId();
        }

        return ids;
    }

    public boolean isEmpty() {
        return connections.isEmpty();

    }

    public int size() {
        return connections.size();
    }

    public Connection get(int index) {
        return connections.get(index);
    }


    public void showConnections() {
        for (Connection c : connections) {
            try {
                System.out.println("Hostname: " + InetAddress.getByAddress(c.getDestIP()) + " Port Number: " + c.getListenPort() + " Node ID: " + c.getId());
            } catch (UnknownHostException e) {
                System.out.println("Could not resolve hostname for Node with ID: " + c.getId() + " with destination port " + c.getListenPort());
            }
        }
    }
}
