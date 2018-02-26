package com.thingworx.sdk.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

public class TunnelExample {
	private static final Logger LOG = LoggerFactory.getLogger(TunnelExample.class);

	// Substitute your thing name here
	private static final String thingName = "TunnelExample";

	public static void main(String[] args) {
		// Create a client config
		ClientConfigurator config = new ClientConfigurator();

		// Basic configuration. See ExampleClient.java for additional info.
		config.setUri("ws://127.0.0.1:80/Thingworx/WS");		
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");	
		config.ignoreSSLErrors(true);

		// Ensure tunnels are enabled for this example
		config.tunnelsEnabled(true);

		try {
			// Create our client that will communication with the ThingWorx composer.			
			ConnectedThingClient client = new ConnectedThingClient(config);

			// Create a VirtualThing that will handle tunnelling.
			// This virtual thing is based on the TunnelExample in the ThingWorx composer.
			VirtualThing myThing = new VirtualThing(thingName, "Tunnel Example", client);
			client.bindThing(myThing);

			// Start the client and communication to the ThingWorx composer.
			client.start();

			// Lets wait to get connected
			LOG.debug("****************Connecting to ThingWorx Server****************");

			while(!client.getEndpoint().isConnected()) {
				Thread.sleep(5000);
			}

			LOG.debug("****************Connected to ThingWorx Server****************");

			// Wait for the client to connect.
			if (client.waitForConnection(30000)) {
				while (!client.isShutdown()) {
					Thread.sleep(5000);
				}
			} else {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
