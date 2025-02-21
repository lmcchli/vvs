/**
 * Copyright (c) Abcxyz 2013
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.ss7.Ss7PhoneOnHandler;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.mobeon.ntf.util.threads.NtfThread;

/**
 * Send phone on requests, storing pair of number+email in map.
 */
class SlamdownPhoneOnSenderWorker extends NtfThread {

    private ManagedArrayBlockingQueue<Object> queue;
   
    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownPhoneOnSenderWorker.class);
    Object perf; // track how long it takes to send a phone on..

    static private volatile int reportCount; //used to report queue size periodically.

    /**
     * Constructs a PhoneOnSender class with synchronized set for pending phone on requests.
     */
    public SlamdownPhoneOnSenderWorker(ManagedArrayBlockingQueue<Object> queue, String threadName)
    {
        super(threadName);
        this.queue = queue;
    }
    
   
    public boolean ntfRun() {
        SlamdownList list = null;

        // Get an event from the working queue
        Object obj = queue.take();
        if (reportCount++ >= 100) {
            reportCount = 0;
            log.info("queueSize = " + queue.size());
        }
        if (obj == null) return false;
        if (!(obj instanceof SlamdownList)) {
            log.error("slamdownPhoneOnSenderWorker: Invalid object received: " + obj.getClass().getName());
            return false;
        }

        list = (SlamdownList) obj;
        synchronized (list) {
            try {

                try {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        perf = CommonOamManager.profilerAgent.enterCheckpoint("SlamDownPhoneOnSenderWorker.requestPhoneOn");
                    }
                    if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_SMS_TYPE_0)) {
                        SMSAddress to = new SMSAddress(Config.getTypeOfNumber(), Config.getNumberingPlanIndicator(), list.getNotificationNumber());

                        /**
                         * The validity period for SMS-Type-0 requests is not the same as the validity period for SMS-Info request.
                         * SMS-Type-0 has a configured validity at NTF level, SMS-Info's validity can be on a per user/class of service basis.
                         */
                        int validity = Config.getValidity_smsType0();

                        if (log.isDebugEnabled()) {
                            log.debug("Sending PhoneOn request to notification number " + list.getNumber());
                        }

                        SMSOut.get().handlePhoneOnRequest(to,
                                validity,
                                list.getOrigDestinationNumber(), 
                                list.getPreferredLanguage(),
                                list.getCosName());
                    } else if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC)){
                        Ss7PhoneOnHandler.getInstance().requestPhoneOn(list.getNotificationNumber());
                    } else { //should never happen as checked before queued but configuration could have changed...
                        if (log.isDebugEnabled()) {                       
                            log.debug("SlamdownPhoneOnSender.sendPhoneOnRequest: phoneOnMethod was not sms type 0 nor alert sc, assuming phone on");
                        }
                        EventRouter.get().phoneOn(new PhoneOnEvent(this, list.getNotificationNumber(), PhoneOnEvent.PHONEON_OK, "Phone on was disabled"));
                    }
                } catch (Exception e) {
                    log.error("OutdialNotificationOut got unexpected Exception: ",e);
                } catch (Throwable t) {
                    log.error("OutdialNotificationOut got unexpected Throwable: ",t);
                }
            }finally {
                if (perf != null) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }
        }
        return false;
    }
    
    /**
     * This method shall complete the work currently going on in the thread. It
     * shall return false until the work is done and then return true. The run
     * method of NtfThread calls it over and over again until it returns true.
     * When implementing this function, try to use the loop in the run-method of
     * NtfThread instead of making a loop in ntfRun, so the administrative state
     * is checked frequently.
     *@return false until the thread is done with shutdown activities, then
     * true.
     */
    public boolean shutdown() {
        SlamdownList list = null;
        Object obj = queue.take();
        if (obj == null) return true;
        if (!(obj instanceof SlamdownList)) {
            log.error("slamdownPhoneOnSenderWorker: Invalid object received: " + obj.getClass().getName());
            return false;
        }
        list = (SlamdownList) obj;

        EventRouter.get().phoneOn(new PhoneOnEvent(this, list.getNotificationNumber(), PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY, "Could not send request, shutting down"));       
        return false;

    }
}

