package com.foo.monitor.jmx;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

import com.foo.monitor.jmx.monitor.AbstractMonitor;
import com.foo.monitor.jmx.monitor.MonitorFactory;
import com.foo.monitor.jmx.monitor.OpenFalconData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;


/**
 * Created by foo on 14-5-21.
 */


public class ServicePuller {
    private final static Logger LOG = LogManager.getLogger();

    private Service service;
    private List<String> hostList;
    private BlockingQueue<String> todoList = new ArrayBlockingQueue<String>(10240);
    //    key:      host
    //    value:    mBeanServerConnection
    Map<String, MBeanServerConnection> mBeanServerConnectionMap = new ConcurrentHashMap<String, MBeanServerConnection>();
    public static ArrayList<JMXConnector> openJMXConnectors = new ArrayList<JMXConnector>();

    private AbstractMonitor monitor = MonitorFactory.createMonitor(Config.type);

    public ServicePuller(Service service) {
        this.service = service;
    }

    public List<String> getHostList() {
        return hostList;
    }

    public void setHostList(List<String> hostList) {
        this.hostList = hostList;
    }

    public MBeanServerConnection getMBeanServerConnection(String jmxHost, int jmxPort) {
        LOG.debug("Get MBSC for " + jmxHost + ";" + jmxPort);
        try {
            if (mBeanServerConnectionMap.get(jmxHost) == null) {
                synchronized (mBeanServerConnectionMap) {
                    if (mBeanServerConnectionMap.get(jmxHost) == null) {
                        LOG.debug("Generating MBSC for " + jmxHost + " ... ");
                        JMXServiceURL url = new JMXServiceURL(getJMXUrl(jmxHost, jmxPort));

                        JMXConnector jmxConnector = JMXConnectorFactory.connect(url);
                        openJMXConnectors.add(jmxConnector);

                        MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
                        mBeanServerConnectionMap.put(jmxHost, mbeanServerConnection);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        LOG.debug("Now MBSCMap keys: " + mBeanServerConnectionMap.keySet());
        return mBeanServerConnectionMap.get(jmxHost);
    }


    public MBeanServerConnection getMBeanServerConnection(String jmxHost) {
        return getMBeanServerConnection(jmxHost, service.getJmxPort());
    }

    public static String getJMXUrl(String jmxHost, int jmxPort) {
        return "service:jmx:rmi:///jndi/rmi://" + jmxHost + ":" + jmxPort+ "/jmxrmi";
    }


    class HostUpdate extends TimerTask {
        @Override
        public void run() {
            try {
                //    TODO open config.yml get 'host' field of this service
                InputStream input = Config.class.getClassLoader().getResourceAsStream("config.yml");
                Yaml yaml = new Yaml();
                Map<String, Object> data = (Map<String, Object>)yaml.load(input);
                Map<String, Map<String, Object>> services = (HashMap<String, Map<String, Object>>)data.get("services");
                ArrayList<String> newHostList = (ArrayList<String>)services.get(service.getName()).get("host");

                hostList = newHostList;
                todoList.addAll(newHostList);
            } catch (Exception e) {
                // eat it
            }
        }
    }


    //    从machineList获取需要监测的host
    //    从mbscMap获取该host上的mbsc
    //    根据service配置的items，通过mbsc，获取数据，发送给monitor
    class Collecter implements Runnable {

        private void monitorSingleHost(String host) throws Exception {
            HostPuller hostPuller = new HostPuller(host);
            hostPuller.doMonitor();
        }

        public void run() {
            try {
                monitor = MonitorFactory.createMonitor(Config.type);

                while (true) {
                    String host = todoList.take();
                    monitorSingleHost(host);

                    LOG.debug("Collecting host " + host);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                LOG.error(e.toString());
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.toString());
            }
        }
    }


    class HostPuller {
        private String host;
        MBeanServerConnection mbsc;

        public HostPuller(String host) {
            this.host = host;
            this.mbsc = getMBeanServerConnection(host);
        }

        public void monitorService(String type) {
            LOG.debug("Collecting metrics. Host: " + host + ". Type: " + type + ".");
            Map<String, String> mBeanOfMetric = service.getItems().get(type);
            LOG.debug("Mbean of host: [" + host + "] [" + mBeanOfMetric + "]");
            if (mBeanOfMetric == null) {
                return;
            }

            for (String metric : mBeanOfMetric.keySet()) {
                LOG.debug("Collecting " + metric + " for " + host);
                try {
                    ObjectName mbean = new ObjectName(mBeanOfMetric.get(metric));
                    MBeanAttributeInfo[] attributes = mbsc.getMBeanInfo(mbean).getAttributes();
                    LOG.debug("We got " + attributes.length + " attributes");
                    for (MBeanAttributeInfo attr : attributes) {
                        Object value = mbsc.getAttribute(mbean, attr.getName());
                        LOG.debug("[" + host + "]Attribute [Type: " + attr.getType() + "][Name: " + attr.getName() + "][" + value + "]");
                        LOG.debug(attr.toString());

                        if (Config.getAttributetypes().contains(attr.getType())) {
                            String tags = String.format("host=%s,service=%s",host, service.getName());
                            OpenFalconData openFalconData = new OpenFalconData(
                                host,
                                metric + "_" + attr.getName(),
                                Double.parseDouble(value.toString()),
                                tags,
                                System.currentTimeMillis()/1000,
                                type.toUpperCase(),
                                Config.flushPeriod
                            );
                            monitor.monitor(openFalconData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    LOG.error(e.getStackTrace().toString());
                    continue;
                }

            }
        }

        public void doMonitor() {
            for (String type : service.getItems().keySet()) {
                monitorService(type);
            }
        }

    }


    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void start() {
        LOG.debug("HostUpdater Starting...");
        Timer timer = new Timer();
        timer.schedule(new HostUpdate(), 0, 30000);
        LOG.debug("HostUpdater started. ");

        //    collecter线程池，take machineList中未检测的机器，做JMX检测
        LOG.debug("Collecter Starting ...");
        ExecutorService executorService = Executors.newFixedThreadPool(service.getThreadNum());
        for (int i = 0; i < service.getThreadNum(); i++) {
            executorService.execute(new Collecter());
        }
        LOG.debug("Collecter started.");
    }
}

