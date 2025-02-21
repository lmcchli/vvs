package com.mobeon.masp.logging.demo;

import com.mobeon.masp.logging.ILoggerFactory;


/**
 * @author David Looberger
 */
public class Main {
    public static void main(String[] args) {
        // Get config file from cmd line
        String config = args[0];
        // Read logging cfg file. Watch it for changes one per minute
        ILoggerFactory.configureAndWatch(config);
        ILoggerFactory.getILogger(Main.class).debug("Starting");

        LogTestMain ltm = new LogTestMain();
        ltm.run();
    }
}
