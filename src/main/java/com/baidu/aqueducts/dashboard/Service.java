package com.baidu.aqueducts.dashboard;

import java.util.Map;

/**
 * Created by foo on 14-5-21.
 */
public class Service {
    private String  name;
    private String  bns;
    private int     jmxPort;
    private int     threadNum;
    //    <type: <key: MBeanName>>
    private Map<String, Map<String, String>> items;

    public Service(String name, String bns, int jmxPort, int threadNum) {
        this.name = name;
        this.bns = bns;
        this.jmxPort = jmxPort;
        this.threadNum = threadNum;
    }

    public Service(String name, String bns, int jmxPort, int threadNum, Map<String, Map<String, String>> items) {
        this.name = name;
        this.bns = bns;
        this.jmxPort = jmxPort;
        this.threadNum = threadNum;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBns() {
        return bns;
    }

    public void setBns(String bns) {
        this.bns = bns;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public Map<String, Map<String, String>> getItems() {
        return items;
    }

    public void setItems(Map<String, Map<String, String>> items) {
        this.items = items;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", bns='" + bns + '\'' +
                ", jmxPort=" + jmxPort +
                ", threadNum=" + threadNum +
                ", items=" + items +
                '}';
    }

}
