package cs455.overlay.util;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.Protocol;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The Interactive command parser that takes user input and sends the message to the appropriate node
 */
public class CommandParser implements Runnable {
    private static final boolean DEBUG = Protocol.DEBUG;
    private Registry registry;
    private MessagingNode messagingNode;
    private volatile LinkedBlockingQueue<String> commandQueue;


    public CommandParser(Node node, LinkedBlockingQueue<String> commandQueue) {
        if (node instanceof Registry) {
            this.registry = (Registry) node;
        } else if (node instanceof MessagingNode) {
            this.messagingNode = (MessagingNode) node;
        } else {
            if (DEBUG) {
                System.out.println("Parser received a Node that was not a Registry nor a MessagingNode");
            }
        }
        this.commandQueue = commandQueue;
    }

    @Override
    public void run() {
        if (registry != null && messagingNode == null) {
            acceptRegistryCommands();
        } else if (registry == null && messagingNode != null) {
            acceptMessagingNodeCommands();
        } else
            System.out.println("CommandParser: Both Registry and MessagingNode were instantiated or both were null. This is definitely an error");

    }

    private void acceptRegistryCommands() {
        Scanner scanner = new Scanner(System.in);
        boolean acceptCommands = true;
        String parsableCommand;

        System.out.println("Ready to accept commands of the form:\n1) setup-overlay <numberOfRoutingTableEntries>\n2) list-messaging-nodes\n3) list-routing-tables (After Setup Overlay)\n4) start <rounds> (After Setup Overlay)\n");

        while (acceptCommands) {
            parsableCommand = scanner.next();
            switch (parsableCommand) {
                case "list-messaging-nodes":
                case "list-routing-tables":
                    commandQueue.add(parsableCommand);
                    break;
                case "setup-overlay":
                    if (scanner.hasNext()) {
                        String nr = scanner.next();
                        commandQueue.add(parsableCommand);
                        commandQueue.add(nr);
                    }
                    break;
                case "start":
                    if (scanner.hasNext()) {
                        String noOfPackets = scanner.next();
                        commandQueue.add(parsableCommand);
                        commandQueue.add(noOfPackets);
                    } else
                        System.out.println("Please enter an argument for the number of rounds");
                    break;
                case "exit":
                    acceptCommands = false;
                    break;
                default:
                    System.out.println("Invalid Command");
                    break;
            }

        }
        scanner.close();

    }


    private void acceptMessagingNodeCommands() {
        Scanner scanner = new Scanner(System.in);
        boolean acceptCommands = true;
        String parsableCommand;

        System.out.println("Ready to accept commands of the form:\n1) print-counters-and-diagnostics\n2) exit-overlay(Before Setup Overlay at Registry)\n");

        while (acceptCommands) {
            parsableCommand = scanner.next();
            switch (parsableCommand) {
                case "print-counters-and-diagnostics":
                    commandQueue.add(parsableCommand);
                    break;
                case "exit-overlay":
                    commandQueue.add(parsableCommand);
                    break;
                case "exit":
                    acceptCommands = false;
                    break;
            }
        }

        scanner.close();

    }

}
