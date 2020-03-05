package cs455.overlay.wireformats;

import java.io.*;

/**
 * byte: Message type; REGISTRY_REQUESTS_TASK_INITIATE
 * int: Number of data packets to send
 */
public class RegistryRequestsTaskInitiate implements Event {
    private int noOfPackets;

    public RegistryRequestsTaskInitiate() {
    }

    public RegistryRequestsTaskInitiate(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(baInputStream);

        byte msg_type = din.readByte();

        if (msg_type != Protocol.REGISTRY_REQUESTS_TASK_INITIATE) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        noOfPackets = din.readInt();

        baInputStream.close();
        din.close();
    }


    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(getType());
        dout.writeInt(noOfPackets);
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.REGISTRY_REQUESTS_TASK_INITIATE;
    }

    public int getNoOfPackets() {
        return noOfPackets;
    }

    public void setNoOfPackets(int noOfPackets) {
        this.noOfPackets = noOfPackets;
    }
}
