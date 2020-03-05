package cs455.overlay.transport;

import cs455.overlay.util.EventManager;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.Protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


/**
 * The Receiver Thread started by TCP connection class for the nodes
 */
public class TCPReceiverThread implements Runnable {
    private static final boolean DEBUG = Protocol.DEBUG;
    private Socket socket;
    private DataInputStream din;

    TCPReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                if (din.available() > 0) { //Check if data is available. Stops EOF exception when registry exits overlay and Messaging Node leaves after setup-overlay
                    dataLength = din.readInt();

                    byte[] data = new byte[dataLength];
                    din.readFully(data, 0, dataLength);

                    if (DEBUG && data.length == 0) {
                        System.out.println("Zero length data");
                    }
                    EventManager.queueEvent(EventFactory.getInstance().getEvent(data), socket); //Queue event that comes in so that it can be sent by the event manager to the appropriate node that has subscribed to it
                }

            } catch (SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch (Exception e) {
                e.getMessage();
                e.printStackTrace();
                if (DEBUG)
                    System.out.println("Should not happen");
            }
        }
    }
}
