package cs455.overlay.wireformats;

import java.io.*;


/**
 * byte: Message type; OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY
 * int: Assigned node ID
 * int: Total number of packets sent (only the ones that were started/initiated by the node)
 * int: Total number of packets relayed (received from a different node and forwarded)
 * long: Sum of packet data sent (only the ones that were started by the node)
 * int: Total number of packets received (packets with this node as final destination)
 * long: Sum of packet data received (only packets that had this node as final destination)
 */
public class OverlayNodeReportsTrafficSummary implements Event {
    private int id;
    private int packetsSent;
    private int packetsRelayed;
    private long totalPacketsSent;
    private int packetsReceived;
    private long totalPacketsReceived;

    public OverlayNodeReportsTrafficSummary() {
    }

    public OverlayNodeReportsTrafficSummary(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(baInputStream);

        byte msg_type = din.readByte();

        if (msg_type != Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }


        id = din.readInt();
        packetsSent = din.readInt();
        packetsRelayed = din.readInt();
        totalPacketsSent = din.readLong();
        packetsReceived = din.readInt();
        totalPacketsReceived = din.readLong();

        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(getType());
        dout.writeInt(id);
        dout.writeInt(packetsSent);
        dout.writeInt(packetsRelayed);
        dout.writeLong(totalPacketsSent);
        dout.writeInt(packetsReceived);
        dout.writeLong(totalPacketsReceived);

        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public void setPacketsSent(int packetsSent) {
        this.packetsSent = packetsSent;
    }

    public int getPacketsRelayed() {
        return packetsRelayed;
    }

    public void setPacketsRelayed(int packetsRelayed) {
        this.packetsRelayed = packetsRelayed;
    }

    public int getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(int packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public long getTotalPacketsSent() {
        return totalPacketsSent;
    }

    public void setTotalPacketsSent(long totalPacketsSent) {
        this.totalPacketsSent = totalPacketsSent;
    }

    public long getTotalPacketsReceived() {
        return totalPacketsReceived;
    }

    public void setTotalPacketsReceived(long totalPacketsReceived) {
        this.totalPacketsReceived = totalPacketsReceived;
    }
}
