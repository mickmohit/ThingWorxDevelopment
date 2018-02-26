package com.thingworx.sdk.practise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.relationships.RelationshipTypes;

import java.nio.file.Paths;


public class Main {
	
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args)  {
		
		try
		{
		String serverUri= "ws://10.196.110.141:80/Thingworx/WS";
		//String appkey= "558b1f52-6fde-432a-853c-276ceff2da0f";
		ClientConfigurator config = new ClientConfigurator();
		config.setUri(serverUri);
		//config.getSecurityClaims().addClaim("appkey",appkey);
		
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");
		
		
	
		config.ignoreSSLErrors(true);
		
		
		//config.fileTransferEnabled(true);
		//config.addVirtualDirectory("in", Paths.get("/Users","abc"));
		
		ConnectedThingClient client = new ConnectedThingClient(config, null);
		client.start();
		
		LOG.debug("Connecting to" + serverUri + "using appkey" );
		
		while(!client.getEndpoint().isConnected())
		{Thread.sleep(5000);}
		LOG.debug("Connected to" + serverUri + "using appkey" );
		
		String thecurrentuser= getCurrentUser(client);
		LOG.info("current user is "+ thecurrentuser);
		
		
		EcgThing ecg = new EcgThing("EcgThing", "A heart rate Monitor", client);
		
		FileTransferThing Fth= new FileTransferThing("FileTransferThing","File Transfer Thing", client);
		
		LOG.debug("Binding To" + serverUri + "using key to register Thing" + ecg.getName()  );
		
		client.bindThing(ecg);
		client.bindThing(Fth);
		while(!ecg.isBound())
		{
			Thread.sleep(5000);
		}
		LOG.debug("Binded To" + serverUri + "using key to register Thing" + ecg.getName()  );
				
		while(true)
		{
			if(client.getEndpoint().isConnected())
			{ecg.processScanRequest();}
			//double currentHeartrate= ecg.getHeartRate();
			
			int currentHeartRate = (int)ecg.getHeartRate();
			Thread.sleep(80*5000/ecg.ecgdata.length/currentHeartRate);
			
		//Thread.sleep(5000);
		}
		
		}
		
		catch(Exception e)
		{
			LOG.error("An Error Occured", e);
		}
	}

	
	protected static String getCurrentUser(ConnectedThingClient client) throws Exception 
	{
		ValueCollection paramvalues= new ValueCollection();
		int remoteTimeout = 10000;
		InfoTable infotable = client.invokeService(RelationshipTypes.ThingworxEntityTypes.Resources, "CurrentSessionInfo", "GetCurrentUser", paramvalues, remoteTimeout);
		
		if(infotable!=null)
		{
			if(infotable.getRowCount()>0)
			{
				return infotable.getRow(0).getStringValue("result");
			}
		}
		
		return null;
	}
}
