package com.thingworx.sdk.practise;

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
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.relationships.RelationshipTypes;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.AspectCollection;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.InfoTablePrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;


@ThingworxPropertyDefinitions
( properties=
		{
	@ThingworxPropertyDefinition(name="heartRate" , description="Current heart rate in beats per minute", category="", baseType="NUMBER", aspects ={"isReadOnly:false", "defaultValue:80.0"}),	
	@ThingworxPropertyDefinition(name="ecgvalue" , description="ecg value", category="", baseType="NUMBER", aspects ={"isReadOnly:true","dataChangeType:ALWAYS", "defaultValue:0.0"}),
	@ThingworxPropertyDefinition(name="PatientName" , description="Patient Name", category="", baseType="STRING", aspects ={"isReadOnly:false", "defaultValue:unknown"})
	
		}
		)


@ThingworxEventDefinitions
( events=
		{
	@ThingworxEventDefinition(name="highHeartRate" , description="heart rate too high", dataShape="HeartRateEvent" , category="Faults" , isInvocable=true, isPropertyEvent=true ),	
	@ThingworxEventDefinition(name="lowHeartRate" , description="heart rate too low", dataShape="HeartRateEvent" , category="Faults" , isInvocable=true, isPropertyEvent=true )
		}
		)

public class EcgThing extends VirtualThing {

	private static final Logger LOG = LoggerFactory.getLogger(EcgThing.class);

	private boolean stopHeart=false;
	
	public EcgThing(String EcgThing, String description, ConnectedThingClient client) {
		// TODO Auto-generated constructor stub
		super(EcgThing,description, client);
		this.initializeFromAnnotations();
		
		/*AspectCollection coll=new AspectCollection();
		coll.setBooleanAspect("isReadOnly", true);
		this.defineProperty("myProperty", "my cool property", BaseTypes.NUMBER, coll);
		*/
	
		try
		{
			setProperty("heartRate", getProperty("heartRate").getPropertyDefinition().getDefaultValue().getValue());
			setProperty("PateintName", getProperty("PateintName").getPropertyDefinition().getDefaultValue().getValue());
		}catch(Exception e)
		{
			LOG.error("Failed to set default value", e);
		}
		
		try {
			loadSettingsFromServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("Failed to get value from setting server");
		}
		
	}
	@Override
	public void processScanRequest() throws Exception
	{
		//super.processScanRequest();
		if(stopHeart)
		{setEcgvalue(0.0);
		}
	else{
		setEcgvalue(ecgdata[LastecgIndex]);
		LastecgIndex++;
		if(LastecgIndex==ecgdata.length)
		{LastecgIndex=0;}
		}
			
		
		boolean highHeartRateFired = false;
		if(getHeartRate()>80 && !highHeartRateFired)
		{
			
			highHeartRateFired=true;
			ValueCollection representdes=new ValueCollection();
			representdes.setValue("PatientName", getProperty("PatientName").getValue());
			representdes.setValue("heartRate", getProperty("heartRate").getValue());
			queueEvent("highHeartRate", "heartRate", new DateTime(),representdes);
			LOG.info("****EVENT***");
			super.updateSubscribedEvents(5000);
		}
		
		boolean lowHeartRateFired = false;
		if(getHeartRate()<=50 && !lowHeartRateFired)
		{
			
			lowHeartRateFired=true;
			ValueCollection representdes=new ValueCollection();
			representdes.setValue("PatientName", getProperty("PatientName").getValue());
			representdes.setValue("heartRate", getProperty("heartRate").getValue());
			queueEvent("lowHeartRate", "heartRate", new DateTime(),representdes);
			LOG.info("****EVENT***");
			super.updateSubscribedEvents(5000);
		}
		
		if(getHeartRate()>=50 && getHeartRate()<80 )
		{
			lowHeartRateFired=false;
			highHeartRateFired=false;
		}
		
		super.updateSubscribedProperties(5000);
	}
	
	public double getHeartRate(){
		return (double) getProperty("heartRate").getValue().getValue();
	}
	public void setHeartRate(double heartRate)
	{
	try
	{
		setProperty("heartRate", new NumberPrimitive(heartRate));	
	}
	catch(Exception e)
	{
		LOG.error("Failed to set default value", e);
	}
	}

	
	
	
	public void setPateintName(String PateintName)
	{
	try
	{
		setProperty("PateintName", new StringPrimitive(PateintName));	
		super.updateSubscribedProperties(5000);
	}
	catch(Exception e)
	{
		LOG.error("Failed to set default value", e);
	}
	}
	
	public void setEcgvalue(double ecgvalue)
	{
	try
	{
		setProperty("ecgvalue", new NumberPrimitive(ecgvalue));	
	}
	catch(Exception e)
	{
		LOG.error("Failed to set default value", e);
	}
	}
	
	public void loadSettingsFromServer() throws Exception{
		
		Double serverHeartRate= (Double) getPropertyFromServer("heartRate");
		if(serverHeartRate != null)
		{
			setProperty("heartRate",serverHeartRate);
		}
	}
	

	
	
	
	private Object getPropertyFromServer(String propertyName) {
		// TODO Auto-generated method stub
try{		
	InfoTable it= new InfoTable();
	it.getDataShape().addFieldDefinition(new FieldDefinition("name", BaseTypes.STRING));
	ValueCollection vc= new ValueCollection();
	vc.put("name", new StringPrimitive(propertyName));
	it.addRow(vc);
	ValueCollection param= new ValueCollection();
	param.setValue("propertyNames", new InfoTablePrimitive(it));
	InfoTable infot= this.getClient().invokeService(RelationshipTypes.ThingworxEntityTypes.Things, this.getName(), "GetNamedPropertyValues", param, Integer.valueOf(10000));
	return infot!= null && infot.getRowCount().intValue()>0? infot.getRow(0).getPrimitive(propertyName).getValue(): null;
}
catch(Exception e){return null;}
	}

	
	
	@ThingworxServiceDefinition(name ="stopHeart", description="Stop Heart Beating")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result" , baseType="NOTHING")
	public void stopHeart(){this.stopHeart=true;}
	
	@ThingworxServiceDefinition(name ="defibrilate", description="Stop Heart Beating")
	@ThingworxServiceResult(name= CommonPropertyNames.PROP_RESULT, description="Result" , baseType="NOTHING")
	public void defibrilate(){this.stopHeart=false;}

	
	
	
	
	private int LastecgIndex=0;
	
	public static double ecgdata[] ={
			931,932,933, 934,935,936,937,938,939,940,941,950
	}; 
	

}
