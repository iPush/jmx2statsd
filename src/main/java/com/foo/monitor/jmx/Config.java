package com.foo.monitor.jmx;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.foo.monitor.jmx.monitor.AbstractMonitor;
import com.foo.monitor.jmx.monitor.MonitorFactory;



public class Config {
    private final static Logger LOG = LogManager.getLogger();

    static int flushPeriod;
    static String type;
    //    <name, Service>
    static Map<String, Service> serviceMap = new HashMap<String, Service>();

    static {
        init();
    }

    static void init() {
        InputStream input = Config.class.getClassLoader().getResourceAsStream("config.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>)yaml.load(input);

        LOG.debug(data.get("global"));
        LOG.debug(data.get("services"));

        parseGlobal((Map<String, Object>)data.get("global"));
        parseServices((Map<String, Map<String, Object>>)data.get("services"));
    }

    static void parseGlobal(Map<String, Object> global) {
        type = (String)global.get("type");
        flushPeriod = (Integer)global.get("flush_period");
    }

    static void parseServices(Map<String, Map<String, Object>> services) {
        for (String name : services.keySet()) {
            // TODO replace bns with host list

            ArrayList<String> hostList = (ArrayList<String>)services.get(name).get("host");


            int jmxPort = (Integer)(services.get(name).get("jmx_port"));
            int threadNum = (Integer)services.get(name).get("thread_num");

            LOG.debug("Name: " + name + ". Host: " + hostList + ". JMX_PORT: " + jmxPort + ". Thread_NUM: " + threadNum);
            Service service = new Service(name, hostList, jmxPort, threadNum);
            //    构造items
            Map<String, Map<String, String>> items = new HashMap<String, Map<String, String>>();
            Map<String, Map<String, String>> tmp = (Map<String, Map<String, String>>)services.get(name).get("items");

            service.setItems(tmp);
            LOG.debug(service);

            serviceMap.put(name, service);
        }
    }

    public static void main(String[] args) {
    }


	public static final HashSet<String> attributeTypes = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("int");
			add("long");
			add("double");
			add("float");
			add("java.lang.Integer");
			add("java.lang.Long");
			add("java.lang.Double");
			add("java.lang.Float");
		}
	};
	
	public static HashSet<String> getAttributetypes() {
		return attributeTypes;
	}


}
