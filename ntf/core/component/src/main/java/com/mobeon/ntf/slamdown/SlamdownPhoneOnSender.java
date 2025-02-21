/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SlamdownPhoneOnSender {
    
    private static LogAgent log =  NtfCmnLogger.getLogAgent(SlamdownPhoneOnSender.class);
    private ManagedArrayBlockingQueue<Object> phoneOnRequestQueue;
    private SlamdownPhoneOnSenderWorker[] phoneOnSenderWorkers;
    private static final int PHONE_ON_REQUEST_QUEUE_SIZE = 100*Config.getSlamdownWorkers(); //100 per worker..
    private static SlamdownPhoneOnReceiver phoneOnReceiver;

    /**
     * Constructor
     */
    public SlamdownPhoneOnSender(ManagedArrayBlockingQueue<Object> queue) {
        
        if (log.isDebugEnabled())
            log.debug("SlamdownPhoneOnSender Init Start");
        

        phoneOnRequestQueue = new ManagedArrayBlockingQueue<Object>(PHONE_ON_REQUEST_QUEUE_SIZE);

        int NO_WORKERS = (int) (Config.getSlamdownWorkers()*1.5); //create with a ratio of 1.5 to 1...

        if (NO_WORKERS > 0) {
            createWorkers(NO_WORKERS);
        }
        
        if (log.isDebugEnabled())
            log.debug("SlamdownPhoneOnSender Init End");

        // PhoneOn receiver
        phoneOnReceiver = new SlamdownPhoneOnReceiver(queue);

        // Register to the EventRouter in order to get notified for PhoneOn response from SMSc
        EventRouter.get().register(phoneOnReceiver);
    }

   
    private void createWorkers(int numberOfWorkers) {
        phoneOnSenderWorkers = new SlamdownPhoneOnSenderWorker[numberOfWorkers];

        for (int i = 0; i<numberOfWorkers; i++) {
            phoneOnSenderWorkers[i] = new SlamdownPhoneOnSenderWorker(phoneOnRequestQueue, "SlamdownPhoneOnSenderWorker-" + i);
            phoneOnSenderWorkers[i].setDaemon(true);

        }
    }
    
    public void start() {
        if (log.isDebugEnabled())
            log.debug("PhoneOnSender Start worker Threads");
        for (int i = 0; i<phoneOnSenderWorkers.length; i++) {

            phoneOnSenderWorkers[i].start();
        }
    }
            
    public synchronized void sendPhoneOnRequest(SlamdownList list) {
        
        if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_SMS_TYPE_0) || Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC))
        {
            boolean storedInQueue = phoneOnRequestQueue.offer(list);
            if (!storedInQueue) {
                log.warn("SlamdownPhoneOnSender" + " queue full, will retry: " + list.getNotificationNumber());
                //can't do this, slamdown ignores failed temporary anyway...  Just will retry on unit level..
                //EventRouter.get().phoneOn(new PhoneOnEvent(this, list.getNotificationNumber(), PhoneOnEvent.PHONEON_FAILED_TEMPORARY, "queue full"));
            } else
            {
                if (log.isDebugEnabled()) {
                    log.debug("Phone on queued for " + list.getNotificationNumber());
                }
            }
            
        } else
        {    
            log.debug("SlamdownPhoneOnSender.sendPhoneOnRequest: phoneOnMethod was not sms type 0 nor alert sc");
            EventRouter.get().phoneOn(new PhoneOnEvent(this, list.getNotificationNumber(), PhoneOnEvent.PHONEON_OK, "Phone on was disabled"));
        }
    }

    private class SlamdownPhoneOnReceiver implements com.mobeon.ntf.event.PhoneOnEventListener {

        private LogAgent log;
        private ManagedArrayBlockingQueue<Object> phoneOnQueue;
        private volatile int reportCount = 0; //used to report queue size every so many events..

        SlamdownPhoneOnReceiver(ManagedArrayBlockingQueue<Object> queue) {
            log = NtfCmnLogger.getLogAgent(SlamdownPhoneOnReceiver.class);
            this.phoneOnQueue = queue;
        }

        /**
         * Implement PhoneOnEventListener interface
         * This now actually just stores on the event queue and the workers handle it from there.
         * Instead of having a bottleneck of one thread processing all incoming phone on events.
         */
        public void phoneOn(PhoneOnEvent phoneOnEvent) {
            if (reportCount++ >= 100) {
                reportCount = 0;
                log.info("phone on queue size = " + phoneOnQueue.size());
                }
            
            //wait a max of 500 ms if queue is full..
            //We are most probably in a bad state if the worker event queue is full but give it a chance to recover.
            //as this thread is small and fast as opposed to the workers, they may be doing IO, sending a list for example.
            //If we drop a phone on, the default behaviour is to retry the request in 24hrs, so this can be problematic.
            try {
                phoneOnQueue.offer(phoneOnEvent, 500, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                log.warn("Event " + phoneOnEvent + " is DROPPED since queue is full: " + phoneOnEvent.getAddress());
            }                
        }    
    }
}
