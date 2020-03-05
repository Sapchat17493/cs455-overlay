package cs455.overlay.node;

import cs455.overlay.wireformats.SocketAndEvent;

/**
 * The interface implemented by Messaging Node and Registry and invoked by the event manager when it receives an event.
 */
public interface Node {
    public void onEvent(SocketAndEvent socketAndEvent);
}
