For Graders:

Once the tar file has been extracted, inside the root directory I used the command "gradlew clean build" to build the project. The built jar can be found in the folder build/libs/ and it will be called overlay-1.0-SNAPSHOT.jar
This jar can now be used for all execution/grading purposes.


Package Descriptions:

1) cs455.overlay.node
This package contains the implementations of the executable classes Registry and Messaging Node.


	Class Descriptions in Package cs455.overlay.node:

	1a) cs455.overlay.node.MessagingNode

	This class begins by establishing a connection to the registry whose hostname and socket are provided as command line arguments 	to the class. Then this class subscribes to the event manager and also starts up a server socket to listen for connections once 	the overlay needs to be set up. On arrival of any message the event manager passes this event to the class and it executes the 		actions accordingly. The names of the methods in the class should be quite self-explanatory. This class also handles the 		interactive command line arguments.


	1b) cs455.overlay.node.Registry

	This class begins by registering a server on the port that is provided to this class as a command line argument. Then it listens 	 for incoming connections over that socket and initiates all functionalities in the overlay. This class will initialize give 		commands to set up the overlay where it sends each node its routing table and IDs of all nodes that are supposed to be part of 		the overlay.The registry is responsible for initiating the propagation of messages over the overlay and outputs the final 		tracking results. The registry also maintains a record of all routing tables 

	1c) cs455.overlay.node.Node
	
	This interface is implemented by the Registry and Messaging Nodes in order to be notified of the events.


2) cs455.overlay.routing
This package contains the classes that are used to store routing information at the nodes.


	Class Descriptions in Package cs455.overlay.routing

	2a) cs455.overlay.routing.RoutingEntry

	This class encapsulates all the data that is needed for each routing entry, such as ID and hops of nodes that make up each entry 	 of the routing table.
	
	2b) cs455.overlay.routing.RoutingTable

	This class encapsulates the routing table data for each node. This class also has methods to add to the routing table and find 		the most appropriate node(farthest node to origin node if destination id is not present in the routing table entries) to route a 	 packet based on the entries in that particular instance of the routing table. 


3) cs455.overlay.transport
This package contains all classes that handle TCP connections, receiver and sender and also a cache to store connections.


	Class Descriptions in Package cs455.overlay.transport

	3a) cs455.overlay.transport.TCPConnection

	This class starts the receiver thread on the mesasging node and connect it to the registry and also encapsulates all the 		TCPConnections used in this project.

	3b) cs455.overlay.transport.TCPConnectionsCache

	This class provides methods that caches TCP connections and provides methods to add and remove connections if and when they are 	made/removed.

	3c) cs455.overlay.transport.TCPReceiverThread

	The Receiver Thread started by TCP connection class for the nodes. Checks if data stream has any data. If yes, then queues the 		event so that it can be reconized by the EventFactory using the type.

	3d) cs455.overlay.transport.TCPSender

	TCPSender sends messsages over the socket specified at the constructor.

	3e) cs455.overlay.transport.TCPServerThread

	Encapsulates a server thread on which the nodes listen for incoming connections when the thread is started. Also this calss 		provides functionalities by Waiting for a connection and adding it to the cache once the connection is made.


4) cs455.overlay.util
This package contains all the utility classes that are used for parsing interactive console commands, manages connections, manages and queues events and also displays the tracking information that is required for correctness verification.


	Class Descriptions in Package cs455.overlay.util
	
	4a) cs455.overlay.util.CommandParser
	
	The Interactive command parser that takes user input and sends the message to the appropriate node. It takes the instance of the 	 node(Registry/MessagingNode) and accordingly sends the correct information (user command) to the appropriate instance.

	4b) cs455.overlay.util.Connection

	Encapsulates all attributes of a particular connection between any two nodes.

	4c) cs455.overlay.util.ConnectionManager

	This class keeps a record of and manages all the connections between all nodes and contains all helper methods needed to manage 	connections and put them in the cache or remove them from the cache. This class also has the methods to display all connections.

	4d) cs455.overlay.util.EventManager
	
	In a separate thread, listens for incoming events(Denoted by the Type in wireformats.Protocol), and invokes the onEvent method 		on the subscribed node(Registry/MessagingNode)

	4e) cs455.overlay.util.StatisticsCollectorAndDisplay

	Used by registry to collect and display final statistics(for correctness verification) once all nodes have finished their 		individual tasks.


5) cs455.overlay.wireformats
This package contains all the data message format classes to encapsulate data passed into and received from each message.


	Class Descriptions in Package cs455.overlay.wireformats

	5a) cs455.overlay.wireformats.Event

	Interface implemented by all wireformat classes in order to load and extract bytes from that object in order to send/receive 		them over the network.

	5b) cs455.overlay.wireformats.EventFactory

	Singleton class for propagating events accurately to the correct class so that the data is received correctly.

	5c) cs455.overlay.wireformats.SocketAndEvent

	A wrapper for event and the socket it is/was bounded to.

	5d) cs455.overlay.wireformats.Protocol

	This interface stores all the message type codes to avoid repitition of raw numbers and also takes care the DEBUG mode
	environment.

	5e) cs455.overlay.wireformats.NodeReportsOverlaySetupStatus

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the NodeReportsOverlaySetupStatus message.
	
	5f) cs455.overlay.wireformats.OverlayNodeReportsTaskFinished

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the OverlayNodeReportsTaskFinished 		message.

	5g) cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the OverlayNodeReportsTrafficSummary 		message.

	5h) cs455.overlay.wireformats.OverlayNodeSendsData

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the OverlayNodeSendsData message.

	5i) cs455.overlay.wireformats.OverlayNodeSendsDeregistration

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the OverlayNodeSendsDeregistration 		message.

	5j) cs455.overlay.wireformats.OverlayNodeSendsRegistration

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the OverlayNodeSendsRegistration message.

	5k) cs455.overlay.wireformats.RegistryReportsDeregistrationStatus

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the RegistryReportsDeregistrationStatus 		message.

	5l) cs455.overlay.wireformats.RegistryReportsRegistrationStatus

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the RegistryReportsRegistrationStatus 		message.

	5m) cs455.overlay.wireformats.RegistryRequestsTaskInitiate

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the RegistryRequestsTaskInitiate message.

	5n) cs455.overlay.wireformats.RegistryRequestsTrafficSummary

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the RegistryRequestsTrafficSummary 		message.

	5o) cs455.overlay.wireformats.RegistrySendsNodeManifest

	Implements the Event interface in order to marshall bytes(so that they aren't implicitly serialized) and return the correct 		message type. It also encapsulates, receives and processes all data that is related to the RegistrySendsNodeManifest message. 		Also provides a secondary contructor in order to populate values using the routing tables passed to it.
