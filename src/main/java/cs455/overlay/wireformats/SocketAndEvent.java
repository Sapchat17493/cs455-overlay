package cs455.overlay.wireformats;

import java.net.Socket;

/**
 * A wrapper for socket and event
 */
public class SocketAndEvent {
    private Event event;
    private Socket socket;

    public SocketAndEvent(Socket socket, Event event) {
        this.socket = socket;
        this.event = event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public Socket getSocket() {
        return socket;
    }
}
