package cs455.overlay.routing;

import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.Protocol;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class encapsulates all routing entries for a particular node into a single object and also provides
 * a method to locate the most appropriate next node to relay messages.
 */
public class RoutingTable {
    private static final boolean DEBUG = Protocol.DEBUG;
    private int nr;
    private ArrayList<RoutingEntry> routingEntries;


    public RoutingTable(int nr) {
        this.nr = nr;
        routingEntries = new ArrayList<RoutingEntry>(nr);
    }

    /**
     *
     * @param message A data message from an overlay that needs to be relayed
     * @param assignedID Assigned ID of the node invoking this method in order to relay to the appropriate node
     * @param nodeIDs All node IDs currently present in the system. Received from REGISTRY_SENDS_NODE_MANIFEST
     * @return The ID of the most appropriate node to relay the message to
     */
    public int findCorrectNode(OverlayNodeSendsData message, int assignedID, int[] nodeIDs) {
        int destId = message.getDestId();
        int hopsAway;
        Arrays.sort(nodeIDs);
        int assignedIDIndex = Arrays.binarySearch(nodeIDs, assignedID);
        int destIdIndex = Arrays.binarySearch(nodeIDs, destId);
        int candidateWt = -1;

        //Cannot be equal. Equal ID would imply destination and source are the same
        if (destIdIndex < assignedIDIndex) {
            hopsAway = nodeIDs.length - assignedIDIndex + destIdIndex;
        } else {
            hopsAway = destIdIndex - assignedIDIndex;
        }

        for (RoutingEntry r : routingEntries) {
            if (r.getWt() < hopsAway && r.getWt() > candidateWt) {
                candidateWt = r.getWt();
            }
        }

        if (candidateWt == -1) {
            if(DEBUG)
                System.out.println("Unexpected Results. Hop cannot be -1");
            return -1;
        } else {
            int candidateIndex = -1;
            for (RoutingEntry r : routingEntries) {
                if (r.getWt() == candidateWt) {
                    candidateIndex = Arrays.binarySearch(nodeIDs, r.getId());
                    break;
                }
            }
            return nodeIDs[candidateIndex];
        }
    }

    public int getNr() {
        return nr;
    }

    public int getSize() {
        return routingEntries.size();
    }

    public boolean containsID(int id) {
        for (RoutingEntry r : routingEntries) {
            if (r.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void add(RoutingEntry routingEntry) {
        routingEntries.add(routingEntry);
    }

    public RoutingEntry getIndex(int index) {
        return routingEntries.get(index);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (RoutingEntry r : routingEntries) {
            res.append(r.toString());
        }
        res.append("\n\n");
        return res.toString();
    }
}
