/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.util;

import org.apache.log4j.Logger;

import java.util.TimerTask;
import java.util.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-01
 * Time: 13:09:11
 * To change this template use File | Settings | File Templates.
 */
public class InterruptTimerTask extends TimerTask {
    public static Logger logger = Logger.getLogger("com.mobeon");
    private Thread thread = null;

    public InterruptTimerTask(Thread thread) {
        this.thread = thread;
    }

    public void run() {
        logger.debug("Timer expired!");
        thread.interrupt();
    }


    public static void main(String argv[]) {
        Timer t = new Timer();
        logger.debug("Staring timer...");
        t.schedule(new InterruptTimerTask(Thread.currentThread()),2000);
        try {
            logger.debug("Sleeping...");
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            logger.debug("Received InterruptedException");
        }
        logger.debug("Done!");
    }
}
