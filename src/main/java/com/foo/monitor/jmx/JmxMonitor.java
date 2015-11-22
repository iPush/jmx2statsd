package com.foo.monitor.jmx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JmxMonitor {

    private static final Logger LOG = LogManager.getLogger();

    public static Config config = new Config();
    public static BlockingQueue<Map<String, Object>> metrics = new ArrayBlockingQueue<Map<String, Object>>(10240);

    public static void main(String[] args){
        try {
            Puller puller = new Puller();
            puller.start();

        } catch (Exception e) {
            //eat it
        }
    }

}
