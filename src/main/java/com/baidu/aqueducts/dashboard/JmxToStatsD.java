package com.baidu.aqueducts.dashboard;
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


//		try {

//			LOG.info("Connecting to JMX host " + Config.jmxHost + " on port " + Config.jmxPort + ".");
//            //    连接到mbserver
//			MBeanServerConnection mbeanServerConnection = getNewMBeanServerConnection();
//            //    获得所有mb的名字
//			Set<ObjectName> mbeans = mbeanServerConnection.queryNames(null,null);
//			ArrayList<AttributeValueHolder> attributesList = new ArrayList<AttributeValueHolder>();
//
//			LOG.info("Reading numeric attributes from available MBeans...");
//            //    只记录数值信息
//			for (ObjectName mbean : mbeans) {
//                MBeanAttributeInfo[] attributes = mbeanServerConnection.getMBeanInfo(mbean).getAttributes();
//				for (MBeanAttributeInfo attribute : attributes) {
//					if (Config.getAttributetypes().contains(attribute.getType())) {
//						attributesList.add(new AttributeValueHolder(mbean, attribute));
//					}
//				}
//			}
//			LOG.info("Number of available attributes is " + attributesList.size() + ".");
//			//    分派attributes到线程。默认8个线程
//			int numberOfAttributesPerThread = attributesList.size() / Config.getNumberOfThreads() + 1;
//			LOG.info("Dividing attributes to " + Config.getNumberOfThreads() + " different attribute readers.");
//
//			ArrayList<AttributesReader> attributeReaders = new ArrayList<AttributesReader>();
//			attributeReaders.add(new AttributesReader(getNewMBeanServerConnection()));
//
//			int attributesPerThreadAdded = 0;
//			for (int j = 0; j < attributesList.size(); j++) {
//				if (attributesPerThreadAdded == numberOfAttributesPerThread) {
//					attributeReaders.add(new AttributesReader(getNewMBeanServerConnection()));
//					attributesPerThreadAdded = 0;
//				}
//				attributeReaders.get(attributeReaders.size() - 1).addAttribute(attributesList.get(j));
//				attributesPerThreadAdded++;
//			}
//
//			LOG.info("Sending values to " + Config.getStatsDHost());
//			ArrayList<Thread> threads = new ArrayList<Thread>();
//			for (int k = 0; k < attributeReaders.size(); k++) {
//				Thread t = new Thread(attributeReaders.get(0));
//				threads.add(t);
//				t.start();
//			}
//
//			for(Thread thread : threads){
//				thread.join();
//			}
//
//			closeOpenJMXConnectors();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

}
