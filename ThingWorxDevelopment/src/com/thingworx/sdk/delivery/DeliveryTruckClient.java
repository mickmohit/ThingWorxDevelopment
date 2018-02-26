package com.thingworx.sdk.delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.communications.common.SecurityClaims;

//Refer to the "Delivery Truck Example" section of the documentation
//for a detailed explanation of this example's operation 
public class DeliveryTruckClient extends ConnectedThingClient {
	private static final Logger LOG = LoggerFactory.getLogger(DeliveryTruckClient.class);

	public DeliveryTruckClient(ClientConfigurator config) throws Exception {
		super(config);
	}

	// Test example
	public static void main(String[] args) throws Exception {
		//URI Example ws://localhost:80/Thingworx/WS
		//If not working, change the port
		//Based on Tomcat configuration, ie ws://localhost:8080/Thingworx/WS
		String uri = "ws://localhost:8080/Thingworx/WS";

		//The keyId found in the default_key that was
		//imported into the ThingWorx Composer
		//Updated based on that value
		String appKey = "b3d06be7-c9e1-4a9c-b967-28cd4c49fa80";

		// Set the required configuration information
		ClientConfigurator config = new ClientConfigurator();
		// The uri for connecting to ThingWorx
		config.setUri(uri);

		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs

		// Set the security using an Application Key
		//Login can be performed using a username/password combo
		//Belonging to a user that has been added within the ThingWorx Composer
		//This is not recommended
		//SecurityClaims claims = SecurityClaims.fromCredentials("default_user", "admin");
		SecurityClaims claims = SecurityClaims.fromAppKey(appKey);
		config.setSecurityClaims(claims);
		
		// Create the client passing in the configuration from above
		DeliveryTruckClient client = new DeliveryTruckClient(config);

		DeliveryTruckThing truckThing1 = new DeliveryTruckThing("DeliveryTruck_1", "Delivery Truck thing in ThingWorx composer", client);
		DeliveryTruckThing truckThing2 = new DeliveryTruckThing("DeliveryTruck_2", "Delivery Truck thing in ThingWorx composer", client);
		DeliveryTruckThing truckThing3 = new DeliveryTruckThing("DeliveryTruck_3", "Delivery Truck thing in ThingWorx composer", client);
		client.bindThing(truckThing1);
		client.bindThing(truckThing2);
		client.bindThing(truckThing3);

		try {
			// Start the client. The client will connect to the server and 
	        // authenticate, using the Application Key specified above.
	        client.start();

	        LOG.info("The client is now connected.");

            // As long as the client has not been shutdown, continue
			while(!client.isShutdown()) {
				// Only process the Virtual Things if the client is connected
				if(client.isConnected()) {
					// Loop over all the Virtual Things and process them
					for(VirtualThing thing : client.getThings().values()) {
						try {
							thing.processScanRequest();
						}
						catch(Exception eProcessing) {
							System.out.println("Error Processing Scan Request for [" + thing.getName() + "] : " + eProcessing.getMessage());
						}
					}
				}

				// Suspend processing at the scan rate interval
				Thread.sleep(1000);
			}
		}
		catch(Exception eStart) {
			System.out.println("Initial Start Failed : " + eStart.getMessage());
		}
	}
}
