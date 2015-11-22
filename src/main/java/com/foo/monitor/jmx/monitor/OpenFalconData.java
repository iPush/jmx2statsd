package com.foo.monitor.jmx.monitor;

/**
 * Created by Foo on 11/22/15.
 */
public class OpenFalconData {


    /*
    "endpoint": "test-endpoint",
        "metric": "test-metric2",
        "timestamp": ts,
        "step": 60,
        "value": 2,
        "counterType": "GAUGE",
        "tags": "idc=lg,loc=beijing",
     */

    private String endpoint;
    private String metric;
    private double value;
    private String tags;
    private long timestamp;
    private String counterType;
    private long step;

    public OpenFalconData(String endpoint, String metric, double value, String tags, long timestamp, String counterType, long step) {
        this.endpoint = endpoint;
        this.metric = metric;
        this.value = value;
        this.tags = tags;
        this.timestamp = timestamp;
        this.counterType = counterType;
        this.step = step;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCounterType() {
        return counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }
}
