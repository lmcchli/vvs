/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.sms;

import java.util.concurrent.ConcurrentHashMap;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;

/**
 * SMSPhoneOnListener listens to phoneOn send callbacks. It will only recieve callbacks on
 * the status of the sending of the request. When the actual result comes in after the user has turned the phone on,
 * that callback is sent to EventRouter.
 *
 * This class stores only a SMSAddress that is used in calls to EventRouter.
 */
public class SMSPhoneOnListener extends AbstractSMSResultHandler {

    private ConcurrentHashMap<Integer, SMSListenerEvent> events;

    private ManagementCounter successCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.SUCCESS);
    private ManagementCounter failCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.FAIL);

    public SMSPhoneOnListener() {
        events = new ConcurrentHashMap<Integer, SMSListenerEvent>();
    }

    public void add(int id, SMSAddress address, String mailAddress ) {
        SMSListenerEvent event = new SMSListenerEvent(address, mailAddress);
        events.put(new Integer(id), event);
    }

    public void ok(int id) {
        successCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this,event.getAddress().getNumber(), PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY, "PhoneOn sent out successfully");
            EventRouter.get().phoneOn(phoneOnEvent);
        }
    }

    public void retry(int id, String errorText) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this,event.getAddress().getNumber(), PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY,
                    "Could not send phone on request to SMSC temporarily: " + errorText );
            EventRouter.get().phoneOn(phoneOnEvent);
        }
    }

    public void failed(int id, String errorText) {
        failCounter.incr();
        SMSListenerEvent event =  events.remove(new Integer(id));
        if (event != null) {
    		PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, event.getAddress().getNumber(), PhoneOnEvent.PHONEON_CLIENT_FAILED,
    				"Could not send phone on request to SMSC permanently: " + errorText);
            EventRouter.get().phoneOn(phoneOnEvent);
        }
    }

    @Override
    protected SMSResultAggregator getEvent(int id) {
        return events.get(new Integer(id));
    }

    private class SMSListenerEvent extends SMSResultAggregator {
        private SMSAddress address;
        private String mailAddress;

        public SMSListenerEvent(SMSAddress address, String mailAddress) {
            this.address = address;
            this.mailAddress = mailAddress;
        }

        public SMSAddress getAddress() {
            return address;
        }

        public String getMailAddress() {
            return mailAddress;
        }
    }

}

