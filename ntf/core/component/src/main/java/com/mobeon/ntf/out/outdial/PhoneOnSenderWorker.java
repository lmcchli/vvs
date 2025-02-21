/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
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
class PhoneOnSenderWorker extends NtfThread {

    private ManagedArrayBlockingQueue<Object> queue;
   
    private static LogAgent log = NtfCmnLogger.getLogAgent(PhoneOnSenderWorker.class);

    /**
     * Constructs a PhoneOnSender class with synchronised set for pending phone on requests.
     */
    public PhoneOnSenderWorker(ManagedArrayBlockingQueue<Object> queue, String threadName)
    {
        super(threadName);
        this.queue = queue;
    }

    public boolean ntfRun() {
        PhoneOnSender.Request request = null;

        // Get an event from the working queue
        Object obj = queue.take();
        if (obj == null) return false;
        if (!(obj instanceof PhoneOnSender.Request)) {
            log.error("PhoneOnSenderWorker: Invalid object received: " + obj.getClass().getName());
            return false;
        }

        request = (PhoneOnSender.Request) obj;
        synchronized (request) {
            try {
                if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC)){
                    Ss7PhoneOnHandler.getInstance().requestPhoneOn(request._number);
                } else {
                    // Default case: Config.PHONE_ON_SMS_TYPE_0
                    SMSAddress to =  new SMSAddress(request._user.getTypeOfNumber(), request._user.getNumberingPlan(), request._number);
                    SMSOut.get().handlePhoneOnRequest(
                            to,
                            request._user.getNotifExpTime(),
                            request._user.getMail(),
                            request._user.getPreferredLanguage(),
                            request._user.getCosName());
                }
            } catch (Exception e) {
                log.error("OutdialNotificationOut got unexpected Exception: ",e);
            } catch (Throwable t) {
                log.error("OutdialNotificationOut got unexpected Throwable: ",t);
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
        PhoneOnSender.Request request = null;

        // Get an event from the working queue
        Object obj = queue.take();
        if (obj == null) return true;
        if (!(obj instanceof PhoneOnSender.Request)) {
            log.error("PhoneOnSenderWorker: Invalid object received: " + obj.getClass().getName());
            return false;
        }
        request = (PhoneOnSender.Request) obj;

        PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this,request._user.getNotifNumber(), PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY, "Could not send request, shutting down");
        EventRouter.get().phoneOn(phoneOnEvent);
        return false;

    }
}

