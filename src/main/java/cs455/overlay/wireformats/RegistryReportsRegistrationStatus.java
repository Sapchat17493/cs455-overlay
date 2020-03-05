package cs455.overlay.wireformats;

import java.io.*;

/**
 * byte: Message type (REGISTRY_REPORTS_REGISTRATION_STATUS)
 * int: Success status; Assigned ID if successful, -1 in case of a failure
 * byte: Length of following "Information string" field
 * byte[^^]: Information string; ASCII charset
 */

public class RegistryReportsRegistrationStatus implements Event {
    private int status;
    private byte info_len;
    private byte[] info;

    public RegistryReportsRegistrationStatus() {
    }

    public RegistryReportsRegistrationStatus(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(baInputStream);

        byte msg_type = din.readByte();

        if (msg_type != Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        status = din.readInt();
        info_len = din.readByte();
        info = new byte[info_len];
        din.readFully(info);

        baInputStream.close();
        din.close();
    }

    /**
     * @return Bytes marshalled (not to be serialized)
     * @throws IOException
     */
    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(getType());
        dout.writeInt(status);
        dout.writeByte(info_len);
        dout.write(info);
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte[] getInfo() {
        return this.info;
    }

    public void setInfo(byte[] info) {
        this.info = info;
    }

    public byte getInfoLen() {
        return this.info_len;
    }

    public void setInfo_len(byte info_len) {
        this.info_len = info_len;
    }
}
