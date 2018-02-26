package com.thingworx.sdk.examples;

import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.primitives.BooleanPrimitive;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.StringPrimitive;

/**
 * A helper class that simplifies code in the FileTransferExample. This class provides static methods to create 
 * virtual directories in the SystemRepository thing in the ThingWorx composer, creates a file located in the SystemRepository
 *  thing, and creates the virtual directories in a RemoteThingWithFileTransfer thing that is provided to the method.
 */
public class FileTransferExampleHelper {
	/**
	 * A helper method to create the INCOMING path for the SystemRepository thing in the ThingWorx composer. 
	 * This method will be used to provide the SystemRepository thing with a location to RECEIVE new files.
	 * Incoming is based on the SystemRepository thing's perspective.
	 * 
	 * @return The value collection used to provide the SystemRepository thing with a path to receive files from the client application.
	 */
	public static ValueCollection createSystemRepositoryIncomingPath(){
		// Create the payload for the directory that will accept files from the client application
		ValueCollection inPayload = new ValueCollection();
		inPayload.put("path", new StringPrimitive("incoming"));
		return inPayload;
	}

	/**
	 * A helper method to create the OUTGOING path for the SystemRepository thing in the ThingWorx composer. 
	 * This method will be used to provide the SystemRepository thing with a location to SEND new files.
	 * Outgoing is based on the SystemRepository thing's perspective.
	 * 
	 * @return The value collection used to provide the SystemRepository thing with a path to send the client application files
	 * and a newly created file that will be sent to the client.
	 */
	public static ValueCollection createSystemRepositoryOutgoingPath(){
		// Create the payload for the directory that will send files to the client application
		// This payload will also contain the information to create a file on the SystemRepository thing.
		ValueCollection outPayload = new ValueCollection();
		outPayload.put("path", new StringPrimitive("outgoing/incoming.txt"));
		outPayload.put("data", new StringPrimitive("Hello. This is a file coming from the ThingWorx platform."));
		return outPayload;
	}

	/**
	 * A helper method to use the INCOMING path for the provided RemoteThingWithFileTransfer thing (ThingName) in the ThingWorx composer. 
	 * This method will be used to provide the thing/client application with a location to RECEIVE new files. It will also establish the parameters used to 
	 * transfer a file from the SystemRepository to the thing provided (ThingName). Incoming is based on the perspective of the ThingName 
	 * and the client application.
	 * 
	 * @param thingName The name of the thing in which this payload will be created for
	 * 
	 * @return The value collection used to enable the RemoteThingWithFileTransfer thing provided with a path to receive files
	 *  from the ThingWorx composer and set the parameters needed to transfer a file.
	 */
	public static ValueCollection createTransferIncomingParameters(String thingName){
		ValueCollection inPayload = new ValueCollection();
		//Create the payload for the file transfer coming to the client application from the ThingWorx composer
		inPayload.put("sourceRepo", new StringPrimitive("SystemRepository"));
		inPayload.put("sourcePath", new StringPrimitive("/outgoing"));
		inPayload.put("sourceFile", new StringPrimitive("example.txt"));
		inPayload.put("targetRepo", new StringPrimitive(thingName));
		inPayload.put("targetPath", new StringPrimitive("in"));
		inPayload.put("targetFile", new StringPrimitive("example.txt"));
		inPayload.put("timeout", new IntegerPrimitive(15000));
		inPayload.put("async", new BooleanPrimitive(false));
		return inPayload;
	}

	/**
	 * A helper method to use the OUTGOING path for the provided RemoteThingWithFileTransfer thing (ThingName) in the ThingWorx composer. 
	 * This method will be used to provide the thing/client application with a location to SEND new files to. It will also establish the parameters used to 
	 * transfer a file from the thing provided (ThingName) to the SystemRepository thing. Outgoing is based on the perspective of the ThingName 
	 * and the client application.
	 * 
	 * @param thingName The name of the thing in which this payload will be created for
	 * 
	 * @return The value collection used to enable the RemoteThingWithFileTransfer thing provided with a path to send files
	 *  to the ThingWorx composer and set the parameters needed to transfer a file. 
	 */
	public static ValueCollection createTransferOutgoingParameters(String thingName){
		ValueCollection outPayload = new ValueCollection();

		//Create the payload for the file transfer going to the ThingWorx composer from the client application
		outPayload.put("sourceRepo", new StringPrimitive(thingName));
		outPayload.put("sourcePath", new StringPrimitive("out"));
		outPayload.put("sourceFile", new StringPrimitive("outgoing.txt"));
		outPayload.put("targetRepo", new StringPrimitive("SystemRepository"));
		outPayload.put("targetPath", new StringPrimitive("/incoming"));
		outPayload.put("targetFile", new StringPrimitive("outgoing.txt"));
		outPayload.put("timeout", new IntegerPrimitive(15000));
		outPayload.put("async", new BooleanPrimitive(false));
		return outPayload;
	}
}
