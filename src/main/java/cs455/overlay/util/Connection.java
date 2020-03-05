package cs455.overlay.util;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;


/**
 * Encapsulates all attributes of a particular connection
 */
public class Connection implements Comparable<Connection> {
    private byte[] destIP;
    private int listenPort;
    private int id;
    private TCPConnection tcpConnection;
    private boolean registered;
    private RoutingTable routingTable;

    Connection(byte[] destIP, int listenPort, int id, TCPConnection tcpConnection) {
        this.destIP = destIP;
        this.listenPort = listenPort;
        this.id = id;
        this.tcpConnection = tcpConnection;
        this.registered = false;
        this.routingTable = null;

    }

    public byte[] getDestIP() {
        return destIP;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getId() {
        return id;
    }

    public boolean isRegistered() {
        return registered;
    }

    public TCPConnection getTCPConnection() {
        return tcpConnection;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public boolean isRoutingTablePresent() {
        return routingTable != null;
    }

    @Override
    public int compareTo(Connection o) {
        return Integer.compare(this.id, o.id);
    }
}
