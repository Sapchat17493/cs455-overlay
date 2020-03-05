package cs455.overlay.wireformats;

import java.io.*;

/**
 * This class just needs its type to be defined to construct the message for the MessagingNodes
 */
public class RegistryRequestsTrafficSummary implements Event {

    public RegistryRequestsTrafficSummary() {
    }

    public RegistryRequestsTrafficSummary(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(baInputStream);

        byte msg_type = din.readByte();

        if (msg_type != Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        baInputStream.close();
        din.close();
    }


    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeByte(getType());
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY;
    }
}
