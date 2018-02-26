package com.thingworx.sdk.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

public class ExampleClient extends ConnectedThingClient {

	private static final Logger LOG = LoggerFactory.getLogger(ExampleClient.class);
	
	private static String ThingName = "SimpleThing_1";

	public ExampleClient(ClientConfigurator config) throws Exception {
		super(config);
	}

	public static void main(String[] args) {
		ClientConfigurator config = new ClientConfigurator();

		// Set the URI of the server that we are going to connect to.
		// The port is based on configurations set during tomcat setup.
		// Port 80 is the default.
		//config.setUri("ws://100.122.143.29:80/Thingworx/WS");
		config.setUri("ws://10.196.110.141:80/Thingworx/WS");
		
		// Set the Application Key. This will allow the client to authenticate with the server.
		// It will also dictate what the client is authorized to do once connected.
		// This application key should match that of imported default_user User.
	//	config.setAppKey("b3d06be7-c9e1-4a9c-b967-28cd4c49fa80");
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");
		//config.setAppKey("1c6d618d-61a7-4b29-a565-9c8ac01d6208");
		
		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs

		try {
			// Create our client that will communication with the ThingWorx composer.
			ExampleClient client = new ExampleClient(config);

			// Create a new VirtualThing. The name parameter should correspond with the 
			// name of a RemoteThing on the Platform. In this example, the SimpleThing_1 is used.
			VirtualThing thing = new VirtualThing(ThingName, "A basic virtual thing", client);

			// Bind the VirtualThing to the client. This will tell the Platform that
			// the RemoteThing 'SimpleThing_1' is now connected and that it is ready to 
			// receive requests.
			client.bindThing(thing);

			// Start the client. The client will connect to the server and authenticate
			// using the ApplicationKey specified above.
			client.start();

			// Lets wait to get connected
			LOG.debug("****************Connecting to ThingWorx Server****************");

			while(!client.getEndpoint().isConnected()) {
				Thread.sleep(5000);
			}

			LOG.debug("****************Connected to ThingWorx Server****************");

			// This will prevent the main thread from exiting. It will be up to another thread
			// of execution to call client.shutdown(), allowing this main thread to exit.
			while (!client.isShutdown()) {
				Thread.sleep(15000);

				// Every 15 seconds we tell the thing to process a scan request. This is
				// an opportunity for the thing to query a data source, update property
				// values, and push new property values to the server.
				//
				// This loop demonstrates how to iterate over multiple VirtualThings
				// that have bound to a client. In this simple example the things
				// collection only contains one VirtualThing.
				for (VirtualThing vt : client.getThings().values()) {
					vt.processScanRequest();
				}
			}
		} catch (Exception e) {
			LOG.error("An exception occured during execution.", e);
		}

		LOG.info("ExampleClient is done. Exiting");
	}
}
