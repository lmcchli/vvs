package com.mobeon.masp.monitor;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/*
* Copyright (c) $today.year Mobeon AB. All Rights Reserved.
*/

public class MonitorLoader {


    private static ApplicationContext ctx = null;
    private static String config = "/opt/moip/config/mas/MonitorConfig.xml";
    private static String logConf = "/opt/moip/config/mas/logmanager.xml";

    public static void main(String[] args) {
        new MonitorLoader();
    }

    public MonitorLoader(){
        ILoggerFactory.configureAndWatch(logConf);
        ILogger log = ILoggerFactory.getILogger(MonitorLoader.class);
        try {
            ctx = new FileSystemXmlApplicationContext(config);
            MonitorImpl mon = (MonitorImpl)ctx.getBean("Monitor");
        } catch (Exception e) {
            log.error("Failed to create MonitorImpl, bailing out", e);
            System.exit(0);
        }
    }
}
