package cs455.overlay.wireformats;

/**
 * This interface stores all the message type codes to avoid repitition of raw numbers and also takes care the DEBUG mode environment
 */
public interface Protocol {

    public final byte OVERLAY_NODE_SENDS_REGISTRATION = (byte) 2;
    public final byte REGISTRY_REPORTS_REGISTRATION_STATUS = (byte) 3;
    public final byte OVERLAY_NODE_SENDS_DEREGISTRATION = (byte) 4;
    public final byte REGISTRY_REPORTS_DEREGISTRATION_STATUS = (byte) 5;
    public final byte REGISTRY_SENDS_NODE_MANIFEST = (byte) 6;
    public final byte NODE_REPORTS_OVERLAY_SETUP_STATUS = (byte) 7;
    public final byte REGISTRY_REQUESTS_TASK_INITIATE = (byte) 8;
    public final byte OVERLAY_NODE_SENDS_DATA = (byte) 9;
    public final byte OVERLAY_NODE_REPORTS_TASK_FINISHED = (byte) 10;
    public final byte REGISTRY_REQUESTS_TRAFFIC_SUMMARY = (byte) 11;
    public final byte OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY = (byte) 12;
    public final boolean DEBUG = false;


    public static String getMsgTypeName(byte msg_type) {
        switch (msg_type) {
            case OVERLAY_NODE_SENDS_REGISTRATION:
                return "OVERLAY_NODE_SENDS_REGISTRATION";
            case REGISTRY_REPORTS_REGISTRATION_STATUS:
                return "REGISTRY_REPORTS_REGISTRATION_STATUS";
            case OVERLAY_NODE_SENDS_DEREGISTRATION:
                return "OVERLAY_NODE_SENDS_DEREGISTRATION";
            case REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                return "REGISTRY_REPORTS_DEREGISTRATION_STATUS";
            case REGISTRY_SENDS_NODE_MANIFEST:
                return "REGISTRY_SENDS_NODE_MANIFEST";
            case NODE_REPORTS_OVERLAY_SETUP_STATUS:
                return "NODE_REPORTS_OVERLAY_SETUP_STATUS";
            case REGISTRY_REQUESTS_TASK_INITIATE:
                return "REGISTRY_REQUESTS_TASK_INITIATE";
            case OVERLAY_NODE_SENDS_DATA:
                return "OVERLAY_NODE_SENDS_DATA";
            case OVERLAY_NODE_REPORTS_TASK_FINISHED:
                return "OVERLAY_NODE_REPORTS_TASK_FINISHED";
            case REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                return "REGISTRY_REQUESTS_TRAFFIC_SUMMARY";
            case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                return "OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY";
            default:
                return "Unknown Message Type";
        }
    }
}
