package com.thingworx.sdk.delivery;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.DatetimePrimitive;
import com.thingworx.types.primitives.LocationPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;
import com.thingworx.types.primitives.structs.Location;

//Refer to the "Delivery Truck Example" section of the documentation
//for a detailed explanation of this example's operation 

// Property Definitions
@SuppressWarnings("serial")
@ThingworxPropertyDefinitions(properties = {	
		@ThingworxPropertyDefinition(name="Driver", description="The name of the driver", baseType="STRING", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="DeliveriesLeft", description="The number of deliveries left for this truck", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="Speed", description="The speed of the truck", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="Location", description="The location of the truck", baseType="LOCATION", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="TotalDeliveries", description="The number of deliveries the truck has to carry out.", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="DeliveriesMade", description="The number of deliveries the truck has made.", baseType="NUMBER", aspects={"isReadOnly:false"}),
})

// Event Definitions
@ThingworxEventDefinitions(events = {
	@ThingworxEventDefinition(name="DeliveryStop", description="The event of a delivery truck stopping to deliver a package.", dataShape="DeliveryTruckShape", isInvocable=true, isPropertyEvent=false)
})

// Delivery Truck virtual thing class that simulates a Delivery Truck
public class DeliveryTruckThing extends VirtualThing implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(DeliveryTruckThing.class);
	private Thread _shutdownThread = null;
	private String thingName = null;
	List<String> drivers;

	private final static String ACTIV_TIME_FIELD = "ActivationTime";
	private final static String TOTAL_DELIVERIES_FIELD = "TotalDeliveries";
	private final static String REMAIN_DELIVERIES_FIELD = "RemainingDeliveries";
	private final static String DRIVER_NAME_FIELD = "DriverName";
	private final static String TRUCK_NAME_FIELD = "Truck";
	private final static String LOCATION_FIELD = "Location";

	private Double deliveriesMade;
	private Double deliveriesLeft;
	private Double totalDeliveries;
	private String driver;
	private Double speed;
	private Location location;

	public DeliveryTruckThing(String name, String description, ConnectedThingClient client) {
		super(name, description, client);
		thingName = name;
		// Populate the thing shape with the properties, services, and events that are annotated in this code
		super.initializeFromAnnotations();
		this.init();
	}

	// From the VirtualThing class
	// This method will get called when a connect or reconnect happens
	// Need to send the values when this happens
	// This is more important for a solution that does not send its properties on a regular basis
	public void synchronizeState() {
		// Be sure to call the base class
		super.synchronizeState();
		// Send the property values to ThingWorx when a synchronization is required
		super.syncProperties();
	}

	private void init() {
		// Data Shape definition that is used by the delivery stop event
		// The event only has one field, the message
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition(ACTIV_TIME_FIELD, BaseTypes.DATETIME));
        fields.addFieldDefinition(new FieldDefinition(DRIVER_NAME_FIELD, BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition(TRUCK_NAME_FIELD, BaseTypes.BOOLEAN));
        fields.addFieldDefinition(new FieldDefinition(TOTAL_DELIVERIES_FIELD, BaseTypes.NUMBER));
        fields.addFieldDefinition(new FieldDefinition(REMAIN_DELIVERIES_FIELD, BaseTypes.NUMBER));
        fields.addFieldDefinition(new FieldDefinition(LOCATION_FIELD, BaseTypes.LOCATION));
        defineDataShapeDefinition("DeliveryTruckShape", fields);

        drivers = new ArrayList<String>();
        drivers.add("Max");
        drivers.add("Mellissa");
        drivers.add("Mathew");
        drivers.add("Megan");
        drivers.add("Merv");
        drivers.add("Michelle");
        drivers.add("Merideth");
        drivers.add("Mona");
        drivers.add("Maxine");

        //Get the current values from the ThingWorx composer
        deliveriesMade = getDeliveriesMade();
    	deliveriesLeft = getDeliveriesLeft();
    	totalDeliveries = getTotalDeliveries();
    	driver = getDriver();
    	speed = getSpeed();
    	location = getLocation();

        // If the truck made all of it's deliveries
 		// Send the truck back out
 		if((deliveriesMade >= totalDeliveries) || (deliveriesLeft <= 0)) {
 			System.out.println("Reset Deliveries For " + thingName +"!");
 			totalDeliveries = 500d;
 			deliveriesLeft = 500d;
 			deliveriesMade = 0d;
 			driver = drivers.get((int) (0 + 9 * Math.random()));

 			try {
 				setTotalDeliveries();
				setDeliveriesLeft();
				setDeliveriesMade();
	 			setDriver();
			} catch (Exception e) {
				LOG.error("Failed to write to the ThingWorx composer.");
			}
 		}
	}

	// The processScanRequest is called by the DeliveryTruckClient every scan cycle
	@Override
	public void processScanRequest() throws Exception {
		// Execute the code for this simulation every scan
		this.scanDevice();
		this.updateSubscribedProperties(1000);
		this.updateSubscribedEvents(1000);
	}

	// Performs the logic for the delivery truck, occurs every scan cycle
	public void scanDevice() throws Exception {
		int counter = (int) (0 + 100000 * Math.random());

		// If the truck made all of it's deliveries
 		// Send the truck back out
 		if((deliveriesMade >= totalDeliveries) || (deliveriesLeft <= 0)) {
 			System.out.println("Reset Deliveries For " + thingName +"!");
 			totalDeliveries = 500d;
 			deliveriesLeft = 500d;
 			deliveriesMade = 0d;
 			driver = drivers.get((int) (0 + 9 * Math.random()));

 			try {
 				setTotalDeliveries();
				setDeliveriesLeft();
				setDeliveriesMade();
	 			setDriver();
			} catch (Exception e) {
				LOG.error("Failed to write to the ThingWorx composer.");
			}
 		}

		if((counter % 3) == 0 || (counter % 5) == 0) { // A truck delivery stop
			System.out.println(thingName +" Is Making A Delivery!!");
			// Set the Speed property value if the DeliveriesMade value
			// is equal to zero, raise speed
			// is good enough, lower speed
			if(deliveriesMade == 0){
				// Set the Speed property value in the range of 80-140
				speed = 80 + 140 * Math.random();
				setSpeed();
			}
			else {
				// Set the Speed property value in the range of 60-100
				speed = 60 + 100 * Math.random();
				setSpeed();
			}

			// Get the last location of the truck 
			// Set location value based on new values
			Double latitude = 40 + 45 * Math.random();
			Double longitude = (70 + 80 * Math.random()) * -1;
			location.setLatitude(latitude);
			location.setLongitude(longitude);
			setLocation();

			// Update deliveries
			deliveriesMade++;
			deliveriesLeft--;
			setDeliveriesMade();
			setDeliveriesLeft();

			// Set the event information of the defined data shape for a truck stop event
			ValueCollection payload = new ValueCollection();

			// Set values to the fields
			payload.put(LOCATION_FIELD, new LocationPrimitive(location));
			payload.put(REMAIN_DELIVERIES_FIELD, new NumberPrimitive(deliveriesLeft));
			payload.put(ACTIV_TIME_FIELD, new DatetimePrimitive(DateTime.now()));
			payload.put(TOTAL_DELIVERIES_FIELD, new NumberPrimitive(totalDeliveries));
			payload.put(DRIVER_NAME_FIELD, new StringPrimitive(driver));
			payload.put(TRUCK_NAME_FIELD, new StringPrimitive(super.getBindingName()));

			// This will trigger the 'DeliveryStop' of a remote thing 
			// on the platform.
			super.queueEvent("DeliveryStop", new DateTime(), payload);
		}
		else if((counter % 4) == 0) { // Delivery truck stopped for other reason
			System.out.println(thingName +" Has Stopped!");
			// Set the Speed property value to 0
			speed = 0d;
			setSpeed();
		}
		else if((counter % 2) == 0) { // Delivery truck running
			System.out.println(thingName +" Is Moving!");
			// Set the Speed property value in the range of 0-60
			speed = 0 + 60 * Math.random();
			setSpeed();
		}
	}

	@ThingworxServiceDefinition(name="DeliveriesCalc", description="Subtract two numbers to set property")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="NUMBER")
	public Double DeliveriesCalc( 
		@ThingworxServiceParameter( name="totalDeliveries", description="Value 1", baseType="NUMBER") Double totalDeliveries,
		@ThingworxServiceParameter( name="deliveriesMade", description="Value 2", baseType="NUMBER") Double deliveriesMade) throws Exception {

		return totalDeliveries - deliveriesMade;
	}

	@ThingworxServiceDefinition(name="GetBigString", description="Example string service.")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="STRING")
	public String GetBigString() {
		StringBuilder sbValue = new StringBuilder();

		for(int index = 0; index < 24000; index++) {
			sbValue.append('0');
		}

		return sbValue.toString();
	}

	@ThingworxServiceDefinition(name="Shutdown", description="Shutdown service.")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="", baseType="NOTHING")
	public synchronized void Shutdown() throws Exception {
		// Should not have to do this, but guard against this method being called more than once.
		if(this._shutdownThread == null) {
			// Create a thread for shutting down and start the thread
			this._shutdownThread = new Thread(this);
			this._shutdownThread.start();
		}
	}

	@ThingworxServiceDefinition(name="GetTruckReadings", description="Get Truck Readings")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="INFOTABLE", aspects={"dataShape:DeliveryTruckShape"})
	public InfoTable GetTruckReadings(String truck, String driver) {		
		InfoTable result = new InfoTable(getDataShapeDefinition("DeliveryTruckShape"));
		ValueCollection entry = new ValueCollection();
		DateTime now = DateTime.now();
		Location loc = new Location(40.8447819d, -73.8648268d, 14d);
		LocationPrimitive location = new LocationPrimitive(loc);

		try {			
			//entry 1
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(1));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 521);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			loc = new Location(40.71499674, -73.95378113d, 4d);
			location.setValue(loc);

			//entry 2
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(2));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 515);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			loc = new Location(40.73685215d, -74.19410706d, 54d);
			location.setValue(loc);

			//entry 3
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(3));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 500);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			loc = new Location(41.02549938d, -73.64341736d, 43d);
			location.setValue(loc);

			//entry 4
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(4));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 440);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			loc = new Location(40.91662589d, -72.66700745d, 3d);
			location.setValue(loc);

			//entry 5
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(5));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 315);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public void run() {
		try {
			// Delay for a period to verify that the Shutdown service will return
			Thread.sleep(1000);
			// Shutdown the client
			this.getClient().shutdown();
		} catch (Exception x) {
			// Not much can be done if there is an exception here
			// In the case of production code should at least log the error
		}
	}

	public Double getDeliveriesMade() {
		return (Double) getProperty("DeliveriesMade").getValue().getValue();
	}

	public void setDeliveriesMade() throws Exception {
		setProperty("DeliveriesMade", new NumberPrimitive(this.deliveriesMade));
	}

	public Double getDeliveriesLeft() {
		return (Double) getProperty("DeliveriesLeft").getValue().getValue();
	}

	public void setDeliveriesLeft() throws Exception {
		setProperty("DeliveriesLeft", new NumberPrimitive(this.deliveriesLeft));
	}

	public Double getTotalDeliveries() {
		return (Double) getProperty("TotalDeliveries").getValue().getValue();
	}

	public void setTotalDeliveries() throws Exception {
		setProperty("TotalDeliveries", new NumberPrimitive(this.totalDeliveries));
	}

	public String getDriver() {
		return getProperty("Driver").getValue().getStringValue();
	}

	public void setDriver() throws Exception {
		setProperty("Driver", new StringPrimitive(this.driver));
	}

	public Double getSpeed() {
		return (Double) getProperty("Speed").getValue().getValue();
	}

	public void setSpeed() throws Exception {
		setProperty("Speed", new NumberPrimitive(this.speed));
	}

	public Location getLocation() {
		return (Location) getProperty("Location").getValue().getValue();
	}

	public void setLocation() throws Exception {
		setProperty("Location", new LocationPrimitive(this.location));
	}
}
