package cs455.overlay.wireformats;

import java.io.IOException;


/**
 * Interface implemented by all wireformat classes in order to load and extract bytes from that object in order to send/receive them over the network
 */
public interface Event {
    public byte[] getBytes() throws IOException; // Bytes marshalled to avoid implicit Serialization

    public byte getType();
}
