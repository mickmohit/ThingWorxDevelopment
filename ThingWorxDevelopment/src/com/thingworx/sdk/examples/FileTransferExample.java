package com.thingworx.sdk.examples;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.filetransfer.FileTransferVirtualThing;
import com.thingworx.relationships.RelationshipTypes.ThingworxEntityTypes;

public class FileTransferExample {
	// Substitute your thing name here
	private static final String ThingName = "FileTransferExample";

	public static void main(String[] args) {
		// Create a client config
		ClientConfigurator config = new ClientConfigurator();

		// Basic configuration. See ExampleClient.java for additional info.
		config.setUri("ws://127.0.0.1:80/Thingworx/WS");		
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");		
		config.ignoreSSLErrors(true);

		try {
			// Create our client that will communication with the ThingWorx composer.			
			ConnectedThingClient client = new ConnectedThingClient(config);

			// Create a VirtualThing that will create the virtual directories
			// On the FileTransferExample thing and also make the connection 
			// Between the directories provided and the matching local directories.
			// This virtual thing is based on the FileTransferExample in the ThingWorx composer.
			FileTransferVirtualThing myThing = new FileTransferVirtualThing(ThingName, "File Transfer Example", client);

			// Add two virtual directories that will act as the root directories
			// in this application's virtual file system.
			// The first field is the name of the directory
			// The second field is the path
			myThing.addVirtualDirectory("in",  "transfer/incoming");
			myThing.addVirtualDirectory("out", "transfer/outgoing");

			client.bindThing(myThing);

			// Start the client and communication to the ThingWorx composer.
			client.start();

			// Wait for the client to connect.
			if (client.waitForConnection(30000)) {
				client.invokeService(ThingworxEntityTypes.Things, "SystemRepository", "CreateFolder", FileTransferExampleHelper.createSystemRepositoryIncomingPath(), 15000);
				client.invokeService(ThingworxEntityTypes.Things, "SystemRepository", "CreateTextFile", FileTransferExampleHelper.createSystemRepositoryOutgoingPath(), 15000);
				client.invokeService(ThingworxEntityTypes.Subsystems, "FileTransferSubsystem", "Copy", FileTransferExampleHelper.createTransferIncomingParameters(ThingName), 15000);
				client.invokeService(ThingworxEntityTypes.Subsystems, "FileTransferSubsystem", "Copy", FileTransferExampleHelper.createTransferOutgoingParameters(ThingName), 15000);
			} else {
				System.out.println("Client did not connect within 30 seconds. Exiting");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
