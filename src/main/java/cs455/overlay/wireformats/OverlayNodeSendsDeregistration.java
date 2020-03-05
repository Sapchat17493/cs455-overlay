package cs455.overlay.wireformats;

import java.io.*;

/**
 * byte: Message Type (OVERLAY_NODE_SENDS_DEREGISTRATION)
 * byte: length of following "IP address" field
 * byte[^^]: IP address; from InetAddress.getAddress()
 * int: Port number
 * int: assigned Node ID
 */
public class OverlayNodeSendsDeregistration implements Event {
    private byte ip_len;
    private byte[] ip_addr;
    private int port;
    private int id;

    public OverlayNodeSendsDeregistration() {
    }

    public OverlayNodeSendsDeregistration(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        byte msg_type = din.readByte();

        if (msg_type != Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        ip_len = din.readByte();
        ip_addr = new byte[ip_len];
        din.readFully(ip_addr, 0, ip_len);

        port = din.readInt();
        id = din.readInt();

        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.write(getType());

        byte[] identifierBytes = ip_addr;
        dout.writeByte(ip_len);
        dout.write(identifierBytes);
        dout.writeInt(port);
        dout.writeInt(id);
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION;
    }

    public byte getIpLen() {
        return ip_len;
    }

    public void setIp_len(byte ip_len) {
        this.ip_len = ip_len;
    }

    public byte[] getIpAddr() {
        return ip_addr;
    }

    public void setIp_addr(byte[] ip_addr) {
        this.ip_addr = ip_addr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
