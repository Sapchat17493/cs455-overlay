package cs455.overlay.wireformats;

import cs455.overlay.routing.RoutingTable;

import java.io.*;

/**
 * byte: Message type; REGISTRY_SENDS_NODE_MANIFEST
 * byte: routing table size NR
 * int: Node ID of node 1 hop away
 * byte: length of following "IP address" field
 * byte[^^]: IP address of node 1 hop away; from InetAddress.getAddress()
 * int: Port number of node 1 hop away
 * int: Node ID of node 2 hops away
 * byte: length of following "IP address" field
 * byte[^^]: IP address of node 2 hops away; from InetAddress.getAddress()
 * int: Port number of node 2 hops away
 * int: Node ID of node 4 hops away
 * byte: length of following "IP address" field
 * byte[^^]: IP address of node 4 hops away; from InetAddress.getAddress()
 * int: Port number of node 4 hops away
 * byte: Number of node IDs in the system
 * int[^^]: List of all node IDs in the system [Note no IPs are included]
 */

public class RegistrySendsNodeManifest implements Event {
    private byte nr;
    private int[] nodesToConnect;
    private byte[][] ip_addrs;
    private int[] ports;
    private int[] allIDs;
    private byte numIDs;

    public RegistrySendsNodeManifest(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(baInputStream);

        byte msg_type = din.readByte();

        if(msg_type != Protocol.REGISTRY_SENDS_NODE_MANIFEST) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        nr = din.readByte();
        nodesToConnect = new int[(int) nr];
        ip_addrs = new byte[(int) nr][4];
        ports = new int[(int) nr];

        for(int i = 0; i < (int) nr; i++) {
            nodesToConnect[i] = din.readInt();
            byte ip_len = din.readByte();
            din.readFully(ip_addrs[i], 0, (int) ip_len);
            ports[i] = din.readInt();
        }

        numIDs = din.readByte();
        allIDs = new int[(int) numIDs];
        for(int i = 0; i < (int) numIDs; i++) {
            allIDs[i] = din.readInt();
        }


        baInputStream.close();
        din.close();
    }

    /**
     *
     * @param routingTable Construct all manifest fields based on the routing table and store all NodeIDs in the system
     * @param nodeIDs
     */
    public RegistrySendsNodeManifest(RoutingTable routingTable, int[] nodeIDs) {
        nr = (byte) routingTable.getNr();
        nodesToConnect = new int[(int) nr];
        ip_addrs = new byte[(int) nr][4];
        ports = new int[(int) nr];

        allIDs = new int[nodeIDs.length];
        numIDs = (byte) nodeIDs.length;

        for(int i = 0; i < (int) nr; i++) {
            nodesToConnect[i] = routingTable.getIndex(i).getId();
            ip_addrs[i] = routingTable.getIndex(i).getIpAddr();
            ports[i] = routingTable.getIndex(i).getPort();
        }

        System.arraycopy(nodeIDs, 0, allIDs, 0, nodeIDs.length);
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(getType());
        dout.writeByte(nr);

        for(int i = 0; i < (int) nr; i++) {
            dout.writeInt(nodesToConnect[i]);
            dout.writeByte((byte) ip_addrs[i].length);
            dout.write(ip_addrs[i]);
            dout.writeInt(ports[i]);
        }

        dout.writeByte((byte) allIDs.length);
        for (int id : allIDs) {
            dout.writeInt(id);
        }

        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.REGISTRY_SENDS_NODE_MANIFEST;
    }


    public byte getTableSize() {
        return nr;
    }

    public int[] getNodesToConnect() {
        return nodesToConnect;
    }

    public byte[][] getIp_addrs() {
        return ip_addrs;
    }

    public int[] getPorts() {
        return ports;
    }

    public int[] getAllIDs() {
        return allIDs;
    }

    public byte getNumIDs() {
        return numIDs;
    }
}
