package cs455.overlay.routing;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class helps provide an underlying logic to the overlay. The wt variable stores the hops for that particular entry(id) in the routing table of the Messaging Node.
 * It implements the comparable interface in order to provide a logical order between the entries (low -> high)
 */
public class RoutingEntry implements Comparable<RoutingEntry> {
    private int id;
    private int port;
    private byte[] ip_addr;
    private int wt;

    public RoutingEntry(int id, int port, byte[] ip_addr, int wt){
        this.id = id;
        this.port = port;
        this.ip_addr = new byte[4];
        this.ip_addr = ip_addr;
        this.wt = wt;
    }

    public int getId(){
        return id;
    }

    public int getPort(){
        return port;
    }

    public byte[] getIpAddr(){
        return ip_addr;
    }

    int getWt(){
        return wt;
    }

    @Override
    public int compareTo(RoutingEntry o) {
        if(this.getId() > o.getId()) {
            return 1;
        }
        else {
            return -1;
        }
    }

    @Override
    public String toString() {
        try {
            return "Hostname: " + InetAddress.getByAddress(ip_addr) + " PortNumber: " + port + " Node ID: " + id + "\n";
        } catch (UnknownHostException e) {
            return "Failed to resolve hostname for the node with ID: " + id + " at port " + port;
        }
    }
}
