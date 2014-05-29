package com.baidu.aqueducts.dashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;

public class AttributesReader implements Runnable {
    private static final Logger LOG = LogManager.getLogger();
	private ArrayList<AttributeValueHolder> attributes = new ArrayList<AttributeValueHolder>();;
	private MBeanServerConnection mbeanServerConnection;

	private StatsdClient statsd;
	
	public AttributesReader(MBeanServerConnection mbeanServerConnection){
		this.mbeanServerConnection = mbeanServerConnection;
		try {
			statsd = new StatsdClient(Config.statsdHost, Config.statsdPort);
		} catch (IOException e) {
			LOG.error("Com tin wong!");
		}
	}
	
    public void run() {
//		String myName = Config.getJmxHost();
//		if ( myName.equals("localhost") || myName.startsWith("127.") ) {
//			try {
//				myName = InetAddress.getLocalHost().getHostName();
//			} catch (UnknownHostException e) {
//				myName = Config.getJmxHost();
//			}
//		}
        String myName = "out_of_date";
    	
		while (true) {
			try {
				Thread.sleep(Config.flushPeriod);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

	    	for (AttributeValueHolder attribute : attributes) {
	    			try {
	    				
	    				ObjectName mbeanName = attribute.getMbean();
	    				
	    				Object attributeValue = mbeanServerConnection.getAttribute(attribute.getMbean(), attribute.getAttribute().getName());
	    				String statsName = getStatsName(myName, attribute, mbeanName);
	    				
	    				statsd.gauge(statsName, Double.parseDouble(attributeValue.toString()));
	    				
	    			} catch (Exception e) {}	
			}
		}
    }

    public void addAttribute(AttributeValueHolder attributeValueHolder){
    	this.attributes.add(attributeValueHolder);
    }

	public ArrayList<AttributeValueHolder> getAttributes() {
		return attributes;
	}
	
	public String getStatsName(String jmxHost, AttributeValueHolder attribute, ObjectName mbeanName){
		return "jmx." 
				+ jmxHost.replace(".", "_") + "_" +
				//+ Config.getJmxPort()  + "."
                ""
				+ mbeanName.toString().replace("\"", "").replace(".","_").replace(",",".").replace(":",".").replace("=", "") + "." 
				+ attribute.getAttribute().getName();
	}
 
}
