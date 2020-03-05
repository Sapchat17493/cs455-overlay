package cs455.overlay.util;

import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;

import java.util.ArrayList;


/**
 * Collects and displays final statistics(for correctness verification) once all nodes have finished their individual tasks
 */
public class StatisticsCollectorAndDisplay {

    private ArrayList<TrafficSummary> summaries;
    private int totalNumNodes;
    private int totalPacketsSent;
    private int totalPacketsReceived;
    private int totalPacketsRelayed;
    private long totalSumOfPacketsSent;
    private long totalSumOfPacketsReceived;

    public StatisticsCollectorAndDisplay(int numNodes) {
        this.summaries = new ArrayList<TrafficSummary>(numNodes);
        this.totalNumNodes = numNodes;
    }

    public void addStats(OverlayNodeReportsTrafficSummary trafficSummary) {
        summaries.add(new TrafficSummary(trafficSummary.getId(), trafficSummary.getPacketsSent(), trafficSummary.getPacketsReceived(), trafficSummary.getPacketsRelayed(), trafficSummary.getTotalPacketsSent(), trafficSummary.getTotalPacketsReceived()));
        if (summaries.size() == totalNumNodes) {
            System.out.println("  Node   |Packets | Packets  | Packets | Sum of Values | Sum of Values");
            System.out.println("  ID     |Sent    | Received | Relayed | Sent          | Received");
            System.out.println("----------------------------------------------------------------------------");
            for (TrafficSummary o : summaries) {
                System.out.format(" %8s|%8s|%10s|%9s|%15s|%15s%n", o.nodeID, o.packetsSent, o.packetsReceived, o.packetsRelayed, o.totalSumPacketsSent, o.totalSumPacketsReceived);
                System.out.println("------------------------------------------------------------------------");
                totalPacketsSent += o.packetsSent;
                totalPacketsReceived += o.packetsReceived;
                totalPacketsRelayed += o.packetsRelayed;
                totalSumOfPacketsSent += o.totalSumPacketsSent;
                totalSumOfPacketsReceived += o.totalSumPacketsReceived;
            }
            System.out.format("Sum      |%8s|%10s|%9s|%15s|%15s%n", totalPacketsSent, totalPacketsReceived, totalPacketsRelayed, totalSumOfPacketsSent, totalSumOfPacketsReceived);


        }
    }

    public void clearStats() {
        summaries.clear();
        totalPacketsSent = 0;
        totalPacketsReceived = 0;
        totalPacketsRelayed = 0;
        totalSumOfPacketsSent = 0L;
        totalSumOfPacketsReceived = 0L;
    }


    private class TrafficSummary {
        int nodeID;
        int packetsSent;
        int packetsReceived;
        int packetsRelayed;
        long totalSumPacketsSent;
        long totalSumPacketsReceived;

        TrafficSummary(int nodeID, int packetsSent, int packetsReceived, int packetsRelayed, long totalSumPacketsSent, long totalSumPacketsReceived) {
            this.nodeID = nodeID;
            this.packetsSent = packetsSent;
            this.packetsReceived = packetsReceived;
            this.packetsRelayed = packetsRelayed;
            this.totalSumPacketsSent = totalSumPacketsSent;
            this.totalSumPacketsReceived = totalSumPacketsReceived;
        }
    }

}
