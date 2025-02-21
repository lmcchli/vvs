/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging.demo;

import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.masp.logging.demo.sub2.LogTestSub2;

/**
 * Simple demo class used to illustrate how the ILogger interface can be used
 * Utilizes two sub classes, in order to demonstrate the configuration posibilities where different namespaces
 *
 * @author David Looberger
 */
public class LogTestMain implements Runnable {
    private static ILogger logger = ILoggerFactory.getILogger(LogTestMain.class);

    public void logDebug() {
        logger.debug("Debug");
    }

    public void logInfo() {
        if (logger.isInfoEnabled()) logger.info("Info");
    }

    public void logWarn() {
        logger.warn("Warn");
    }

    public void logError() {
        logger.error("Error");
    }

    public void run() {
        // Set the session id for this thread, and its children, to "Session_<threadid>"
        logger.registerSessionInfo("session", "Session_" + Thread.currentThread().getName());
        // Create sub thread, and see that the session is passed on
        // Thread t = new Thread(new LogTestSub()) ;
        // t.start();
        // Generate some log traces
        this.logDebug();
        this.logInfo();
        this.logWarn();
        this.logError();
        // Create a new object in a different package and see that the session
        // is passed on. Execut in the same thread as this.
        LogTestSub2 t2 = new LogTestSub2();
        t2.run();
    }

    public static void main(String[] args) {
        // Get config file from cmd line
        String config = args[0];
        // Read logging cfg file. Watch it for changes one per minute
        ILoggerFactory.configureAndWatch(config);

        // Set an initial session name, which will be overwritten further on, simply
        // to see how the session id is propagated.
        // logger.registerSessionInfo("session",Thread.currentThread().getName() + "_ DALO");
        // Start 5 separate threads, each running a LogTestMain object.
        LogTestMain ltm = new LogTestMain();
        ltm.run();

        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 3; i++) {
                Thread t = new Thread(new LogTestMain(), "" + i);
                t.start();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
