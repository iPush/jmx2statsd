package com.baidu.aqueducts.dashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by foo on 14-5-21.
 */
public class Pusher implements Runnable {
    private static final Logger LOG = LogManager.getLogger();

    private StatsdClient statsd;

    public Pusher(){
        try {
            statsd = new StatsdClient(Config.statsdHost, Config.statsdPort);
        } catch (IOException e) {
            LOG.error("Com tin wong!");
        }
    }

    @Override
    public void run() {
        try {
            Map<String, Object> metric = JmxToStatsD.metrics.take();
            //    TODO

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
