package com.baidu.aqueducts.dashboard;

import com.baidu.noah.naming.BNSClient;
import com.baidu.noah.naming.BNSException;
import com.baidu.noah.naming.BNSInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;
import java.util.concurrent.*;


/**
 * Created by foo on 14-5-21.
 */


public class ServicePuller {
    private final static Logger LOG = LogManager.getLogger();

    private Service service;
    private BNSClient bnsClient = new BNSClient();
    private BlockingQueue<String> machineList = new ArrayBlockingQueue<String>(10240);
    //    key:      host
    //    value:    mBeanServerConnection
    Map<String, MBeanServerConnection> mBeanServerConnectionMap = new ConcurrentHashMap<String, MBeanServerConnection>();
    public static ArrayList<JMXConnector> openJMXConnectors = new ArrayList<JMXConnector>();

    public ServicePuller(Service service) {
        this.service = service;
    }

    public MBeanServerConnection getMBeanServerConnection(String jmxHost, int jmxPort) throws Exception{
        LOG.debug("Get MBSC for " + jmxHost + ";" + jmxPort);

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
        LOG.debug("Now MBSCMap keys: " + mBeanServerConnectionMap.keySet());
        return mBeanServerConnectionMap.get(jmxHost);
    }


    public MBeanServerConnection getMBeanServerConnection(String jmxHost) throws Exception {
        return getMBeanServerConnection(jmxHost, service.getJmxPort());
    }

    public static String getJMXUrl(String jmxHost, int jmxPort) {
        return "service:jmx:rmi:///jndi/rmi://" + jmxHost + ":" + jmxPort+ "/jmxrmi";
    }


    //    定时运行，调用BNS获取服务机器列表，放入machineList
    //    相当于产生任务列表
    class BnsUpdate extends TimerTask {
        @Override
        public void run() {
            try{
                List<BNSInstance> instanceList = bnsClient.getInstanceByService(service.getBns(), 3000);
                for(BNSInstance instance : instanceList){
                    String host = instance.getHostName();
                    LOG.debug("BNS: " + service.getBns() + ". Host: " + host);
                    machineList.put(host);
                }
                LOG.debug("After BNS update, machineList: " + machineList);
            }catch(BNSException e){
                System.out.println("Encounter an error while using BNS:" +e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //    从machineList获取需要监测的host
    //    从mbscMap获取该host上的mbsc
    //    根据service配置的items，通过mbsc，获取数据，发送给statsd
    class Collecter implements Runnable {
        private StatsdClient client;
        @Override
        public void run() {
            try {
                client = new StatsdClient(Config.statsdHost, Config.statsdPort);

                while (true) {
                    String host = machineList.take();
                    LOG.debug("Collecting host " + host);
                    MBeanServerConnection mbsc = getMBeanServerConnection(host);

                    for (String type : service.getItems().keySet()) {
                        LOG.debug("Collecting metrics. Host: " + host + ". Type: " + type + ".");
                        Map<String, String> mBeanOfMetric = service.getItems().get(type);
                        LOG.debug("Mbean of host: [" + host + "] [" + mBeanOfMetric + "]");
                        if (mBeanOfMetric == null) {
                            continue;
                        }

                        for (String metric : mBeanOfMetric.keySet()) {
                            LOG.debug("Collecting " + metric + " for " + host);
                            ObjectName mbean = new ObjectName(mBeanOfMetric.get(metric));
                            try {
                                MBeanAttributeInfo[] attributes = mbsc.getMBeanInfo(mbean).getAttributes();
                                for (MBeanAttributeInfo attr : attributes) {
                                    Object value = mbsc.getAttribute(mbean, attr.getName());
                                    //TODO    处理attribute，并发送到statsd
                                    LOG.debug("[" + host + "]Attribute [Type: " + attr.getType() + "][Name: " + attr.getName() + "][" + value + "]");
                                    String statsName = service.getName() + "." + metric
                                            + "." + CommonUtil.formatHostName(host) + "." + attr.getName();
                                    if (Config.getAttributetypes().contains(attr.getType())) {
                                        if (type.equalsIgnoreCase("timing")) {
                                            client.timing(statsName, Integer.parseInt(value.toString()) * 1000);
                                        } else if (type.equalsIgnoreCase("gauges")) {
                                            client.gauge(statsName, Double.parseDouble(value.toString()));
                                        }
                                        LOG.debug("Sending to StatsdD. [" + statsName + "][" + Double.parseDouble(value.toString()) + "]");
                                    }
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage());
                                continue;
                            }

                        }
                    }
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


    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void start() {
        //    bns获取机器列表，填充machineList
        LOG.debug("BNSUpdater Starting...");
        Timer bnsTimer = new Timer();
        bnsTimer.schedule(new BnsUpdate(), 0, 3000);
        LOG.debug("BNSUpdater started. ");

        //    collecter线程池，take machineList中未检测的机器，做JMX检测
        LOG.debug("Collecter Starting ...");
        ExecutorService executorService = Executors.newFixedThreadPool(service.getThreadNum());
        for (int i = 0; i < service.getThreadNum(); i++) {
            executorService.execute(new Collecter());
        }
        LOG.debug("Collecter started.");
    }
}

