package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * TCPSender sends messsages over the socket specified at the constructor
 */
class TCPSender {
    private DataOutputStream dout;

    TCPSender(Socket socket) throws IOException {
        dout = new DataOutputStream(socket.getOutputStream());
    }

    void sendData(byte[] dataToSend) throws IOException {
        dout.writeInt(dataToSend.length);
        dout.write(dataToSend, 0, dataToSend.length);
        dout.flush();
    }
}
