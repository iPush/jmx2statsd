package com.foo.monitor.jmx;

/**
 * Created by foo on 14-5-21.
 */
public class Puller {
    public Config config;

    public void start() {
        for (String serviceName : config.serviceMap.keySet()) {
            ServicePuller servicePuller = new ServicePuller(config.serviceMap.get(serviceName));
            servicePuller.start();
        }
    }
}
