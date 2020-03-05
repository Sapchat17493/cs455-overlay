package cs455.overlay.wireformats;


import java.io.*;

/**
 * byte: Message Type (OVERLAY_NODE_SENDS_REGISTRATION)
 * byte: length of following "IP address" field
 * byte[]: IP address; from InetAddress.getAddress()
 * int: Port number
 */
public class OverlayNodeSendsRegistration implements Event {
    private byte ip_len;
    private byte[] ip_addr;
    private int port;

    public OverlayNodeSendsRegistration() {
    }

    public OverlayNodeSendsRegistration(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        byte msg_type = din.readByte();

        if (msg_type != Protocol.OVERLAY_NODE_SENDS_REGISTRATION) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        ip_len = din.readByte();
        ip_addr = new byte[ip_len];
        din.readFully(ip_addr, 0, ip_len);

        port = din.readInt();

        baInputStream.close();
        din.close();
    }


    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.write(getType());
        dout.writeByte(ip_len);
        dout.write(ip_addr);
        dout.writeInt(port);
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
    }

    public byte getIpLen() {
        return ip_len;
    }

    public void setIp_len(byte ip_len) {
        this.ip_len = ip_len;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getIpAddr() {
        return ip_addr;
    }

    public void setIp_addr(byte[] ip_addr) {
        this.ip_addr = ip_addr;
    }
}
