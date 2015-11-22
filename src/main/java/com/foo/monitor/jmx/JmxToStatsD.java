package com.foo.monitor.jmx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JmxToStatsD {

    private static final Logger LOG = LogManager.getLogger();
	
	public static Config config = new Config();
    public static BlockingQueue<Map<String, Object>> metrics = new ArrayBlockingQueue<Map<String, Object>>(10240);
	public static ArrayList<JMXConnector> openJMXConnectors = new ArrayList<JMXConnector>();
	
//	public static MBeanServerConnection getNewMBeanServerConnection() throws Exception{
//		JMXServiceURL url = new JMXServiceURL(Config.getServiceURL());
//		JMXConnector jmxConnector = JMXConnectorFactory.connect(url);
//		openJMXConnectors.add(jmxConnector);
//
//		MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
//		return mbeanServerConnection;
//	}
	
	public static void closeOpenJMXConnectors() throws IOException{
		for(JMXConnector jmxConnector : openJMXConnectors){
			jmxConnector.close();
		}
	}

	public static void main(String[] args){
        try {
            Puller puller = new Puller();
            puller.start();

        } catch (Exception e) {
            //eat it
        }
	}

}
