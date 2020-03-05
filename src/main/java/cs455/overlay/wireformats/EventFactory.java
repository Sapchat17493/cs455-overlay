package cs455.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Singleton class for propagating events
 */
public class EventFactory {
    private static final EventFactory event = new EventFactory();

    private EventFactory(){
    }

    public static EventFactory getInstance() {
        return event;
    }

    public Event getEvent(byte[] data) throws IOException {
        //try changing to data[0]
        switch(ByteBuffer.wrap(data).get(0)) {
            case Protocol.OVERLAY_NODE_SENDS_DATA:
                return new OverlayNodeSendsData(data);
            case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
                return new OverlayNodeSendsRegistration(data);
            case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
                return new RegistryReportsRegistrationStatus(data);
            case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                return new OverlayNodeSendsDeregistration(data);
            case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                return new RegistryReportsDeregistrationStatus(data);
            case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
                return new RegistrySendsNodeManifest(data);
            case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
                return new NodeReportsOverlaySetupStatus(data);
            case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
                return new RegistryRequestsTaskInitiate(data);
            case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
                return new OverlayNodeReportsTaskFinished(data);
            case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                return new RegistryRequestsTrafficSummary(data);
            case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                return new OverlayNodeReportsTrafficSummary(data);
            default:
                return null;
        }

    }
}
