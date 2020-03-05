package cs455.overlay.wireformats;

import java.io.*;

/**
 * byte: Message type; OVERLAY_NODE_SENDS_DATA
 * int: Destination ID
 * int: Source ID
 * int: Payload
 * int: Dissemination trace field length (number of hops)
 * int[^^]: Dissemination trace comprising nodeIDs that the packet traversed through
 */
public class OverlayNodeSendsData implements Event {
    private byte msg_type;
    private int destId;
    private int srcId;
    private int payload;
    private int hops;
    private int[] intermediateIDs;


    public OverlayNodeSendsData() {
    }

    public OverlayNodeSendsData(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        msg_type = din.readByte();

        if (msg_type != Protocol.OVERLAY_NODE_SENDS_DATA) {
            System.out.println("Incorrect message type, received " + Protocol.getMsgTypeName(msg_type) + ", but expected " + Protocol.getMsgTypeName(getType()));
        }

        destId = din.readInt();
        srcId = din.readInt();
        payload = din.readInt();
        hops = din.readInt();
        intermediateIDs = new int[hops + 1];
        for (int i = 0; i < hops; i++) {
            intermediateIDs[i] = din.readInt();
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
        dout.writeInt(destId);
        dout.writeInt(srcId);
        dout.writeInt(payload);
        dout.writeInt(hops);
        for (int i = 0; i < hops; i++) {
            dout.writeInt(intermediateIDs[i]);
        }

        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return Protocol.OVERLAY_NODE_SENDS_DATA;
    }

    public int getDestId() {
        return destId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public byte getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(byte msg_type) {
        this.msg_type = msg_type;
    }

    public int getSrcId() {
        return srcId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public int[] getIntermediateIDs() {
        return intermediateIDs;
    }

    public void setIntermediateIDs(int[] intermediateIDs) {
        this.intermediateIDs = intermediateIDs;
    }
}
