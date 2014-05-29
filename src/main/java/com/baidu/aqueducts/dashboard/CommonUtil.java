package com.baidu.aqueducts.dashboard;

/**
 * Created by foo on 14-5-23.
 */
public class CommonUtil {
    public static String formatHostName(String hostName) {
        System.out.println(hostName);
        return hostName.replace(".", "_");
    }

    public static void main(String[] args) {
        // for test
        String host = "tc-oped-test03.tc.baidu.com";
        System.out.println(CommonUtil.formatHostName(host));

    }
}
