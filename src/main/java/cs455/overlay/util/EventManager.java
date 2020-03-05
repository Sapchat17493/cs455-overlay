package cs455.overlay.util;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.SocketAndEvent;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * In a separate thread, listens for incoming events(Denoted by the Type in wireformats.Protocol), and invokes the onEvent method on the subscribed node
 */
public class EventManager extends Thread {
    private static final boolean DEBUG = Protocol.DEBUG;
    private static LinkedBlockingQueue<SocketAndEvent> eventQueue; //Thread-safe Queue for incoming events for all nodes
    private Node node;
    private volatile boolean acceptEvents;

    public EventManager(Node node) {
        eventQueue = new LinkedBlockingQueue<SocketAndEvent>();
        this.node = node;
        acceptEvents = true;

    }

    public static void queueEvent(Event event, Socket socket) {
        eventQueue.add(new SocketAndEvent(socket, event));
    }

    public void run() {
        while (acceptEvents) {
            try {
                node.onEvent(eventQueue.take()); //invoke on event for the subscribed nodes, both registry and all instances of messaging nodes
            } catch (InterruptedException ie) {
                if (DEBUG) {
                    ie.printStackTrace();
                }
            }
        }
    }
}
