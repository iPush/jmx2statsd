package com.foo.monitor.jmx.monitor;

/**
 * Created by yixin on 11/20/15.
 */



public class MonitorFactory {

    public enum MonitorType {
        OPENFALCON,
        NOVALUE;

        public static MonitorType toMonitor(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
                return NOVALUE;
            }
        }
    }


    public static AbstractMonitor createMonitor(String type) {
        AbstractMonitor monitor = null;

        switch (MonitorType.toMonitor(type.trim().toUpperCase())) {
            case OPENFALCON:
                monitor = new OpenFalcon();
                break;

            default:
                monitor = new OpenFalcon();
                break;

        }

        return monitor;
    }
}
