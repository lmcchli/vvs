/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.time.NtfTime;
import java.util.*;

/**
 * The Delayer class delays objects of the class DelayItem. You enter the object
 * at the input of the Delayer, and after a while it pops out in the other
 * end. The Delayer is designed for an accuracy in the order of seconds. The
 * current time is not continuous. Instead the Delayer has a "pulse" that
 * advances the time in larger steps.
 * It is primarily designed to be used in the implementation of the DelayLine
 * class (@see DelayLine).
 */
public class Delayer extends Thread {

    private static final int MIN_DELAY_TIME = 1; //seconds

    protected int delayTime;
    protected DelayEventListener listener;
    protected LinkedList<DelayItem> items;
    protected Logger log;
    protected boolean keepRunning = true;

    /**
     * Constructor.
     *@param grp - the thread group the delayer thread belongs to.
     *@param name - the name of the delayer thread.
     *@param delayTime - how long this delay is.
     *@param listener - the class that is notified when a delay expires.
     */
    public Delayer(ThreadGroup grp, String name, int delayTime, DelayEventListener listener) {
        super(grp, name);
        setDelayTime(delayTime);
        this.listener = listener;
        items = new LinkedList<DelayItem>();
        start();
    }

    /**
     * setDelayTime updates the delay time set in the constructor.
     * @param newDelayTime is the delay in seconds.
     */
    public void setDelayTime(int newDelayTime) {
        delayTime = newDelayTime;
        if (delayTime < MIN_DELAY_TIME) {
            delayTime = MIN_DELAY_TIME;
        }
    }

    /**
     * @return current delay time
     */
    public int getDelayTime() {
        return delayTime;
    }

    /**
     * Adds an item to the Delayer. The item will pop out after the delay time
     * has expired.
     * @param item the DelayItem that shall be delayed
     */
    public void add(DelayItem item) {
        if (item.getOutTime() <= NtfTime.now) {
            item.setOutTime(NtfTime.now + delayTime);
        }
        synchronized (items) {
            items.add(item);
            items.notifyAll();
        }
    }

    /**
     *Stops the thread. The thread still waits for any item to expire before it dies.
     */
    public void stopDelayer() {
        keepRunning = false;
    }
    
    /**
     * Endless loop waiting for the first item in the queue to expire, so the
     * listener can be notified
     */
    public void run() {
        DelayItem item;
        while (keepRunning) {
            try {
                item = null;
                synchronized (items) {
                    //Wait for something in the queue
                    while (items.size() == 0 && keepRunning) {
                        try {
                            items.wait(1000);
                        } catch (java.lang.InterruptedException ie) { ; }
                    }
                    //Extract the next item to expire
                    try {
                        item = items.removeFirst();
                    } catch (NoSuchElementException e) {
                        item = null;
                    }
                }

                if (item != null) {
                    //Wait for the delay completion of the next item
                    while (!item.isCancelled() && item.getOutTime() > NtfTime.now) {
                        try {
                            sleep((item.getOutTime() - NtfTime.now) * 1000);
                        } catch (InterruptedException e) { ; }
                    }

                    if (!item.isCancelled()) {
                        listener.delayCompleted(item);
                    }
                }
            } catch (Exception e) {
                log.logMessage("Delayer got unexpected exception: "
                               + NtfUtil.stackTrace(e), Logger.L_ERROR);
            }

        }
    }
}
