/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Send phone on requests, storing pair of number+email in map.
 */
class PhoneOnSender {

    private PhoneOnSenderWorker[] phoneOnSenderWorkers;
    private ManagedArrayBlockingQueue<Object> phoneOnRequestQueue;
    private ManagedArrayBlockingQueue<Object> outdialWorkerQueue;
    private static LogAgent log = NtfCmnLogger.getLogAgent(OutdialNotificationOut.class);
    private static int EVENT_QUEUE_SIZE;

    protected class Request {
        public UserInfo _user;
        public String _number;
		public String _userEmail;
        Request(UserInfo user, OdlEvent info) {
            _user = user;
            _number = info.getTelNumber();
            _userEmail = info.getRecipentId();
        }
    }

    /**
     * Constructs a PhoneOnSender class with synchronised set for pending phone on requests.
     */
    public PhoneOnSender() {
        
        if (log.isDebugEnabled())
            log.debug("PhoneOnSender Init Start");
        
        int NO_WORKERS = (int) (Config.getOutdialWorkers()*1.5); //create with a ratio of 1.5 to 1...
        EVENT_QUEUE_SIZE=NO_WORKERS*100;
        phoneOnRequestQueue = new ManagedArrayBlockingQueue<Object>(EVENT_QUEUE_SIZE);
       
        if (NO_WORKERS > 0) {
            createWorkers(NO_WORKERS);
        }
        
        if (log.isDebugEnabled())
            log.debug("PhoneOnSender Init End");
    }

    private void createWorkers(int numberOfWorkers) {
        phoneOnSenderWorkers = new PhoneOnSenderWorker[numberOfWorkers];

        for (int i = 0; i<numberOfWorkers; i++) {
            phoneOnSenderWorkers[i] = new PhoneOnSenderWorker(phoneOnRequestQueue, "ODLPhoneOnSenderWorker-" + i);
            phoneOnSenderWorkers[i].setDaemon(true);
            
        }
    }
    
    public void setQueue(ManagedArrayBlockingQueue<Object> outdialWorkerQueue) {
        if (log.isDebugEnabled())
            log.debug("PhoneOnSender setQueue");
        this.outdialWorkerQueue = outdialWorkerQueue;
    }

    public void start() {
        if (log.isDebugEnabled())
            log.debug("PhoneOnSender Start worker Threads");
        for (int i = 0; i<phoneOnSenderWorkers.length; i++) {
            phoneOnSenderWorkers[i].start();
        }
    }
    
    public boolean request(UserInfo user, OdlEvent info) {

        //Give a short time for sender queue to catch up.
        boolean storedInQueue = false;

        storedInQueue = phoneOnRequestQueue.offer(new Request(user, info),500,TimeUnit.MILLISECONDS);

        if (!storedInQueue) {     
            log.warn("PhoneOnSender queue full, will assume phone on and continue..");
            info.setOdlTrigger(OdlInfo.EVENT_OUTDIAL_PHONEON);
            info.setOdlCode(OdlInfo.EVENT_CODE_NETWORK_CONGESTION);
            info.setFromNotify(true);
            info.notifyObservers();
            
            //Give a short time for worker queue to empty, to catch up it's work.
            if(!outdialWorkerQueue.offer(info,500,TimeUnit.MILLISECONDS))
               log.warn("outdialWorkerQueue queue full, when trying to queue phone on.");
        }
        
        return storedInQueue;
    }
}
