/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.ntf.out.sms;

import java.util.concurrent.ConcurrentHashMap;

import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

/**
 * SMSListener listens to callbacks from SMS and MWI sendings. 
 *
 * SMSListener forwards the call to a NotificationGroup. User and notiftype
 * are used in the call to NotificationGroup.
 */
public class SMSListener extends AbstractSMSResultHandler {
    private final static Logger log = Logger.getLogger(SMSListener.class); 
    private ConcurrentHashMap<Integer, SMSListenerEvent> events;
   
    private ManagementCounter successCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.SUCCESS);
    private ManagementCounter failCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.FAIL);
    
    public SMSListener() {
        events = new ConcurrentHashMap<Integer, SMSListenerEvent>();
    }
    
    public void add(int id, UserInfo user, FeedbackHandler handler, int notifType) {
        SMSListenerEvent event = new SMSListenerEvent(notifType, user, handler);

        events.put(new Integer(id), event);
    }

    public void ok(int id) {
        successCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) { 
                event.getHandler().ok(event.getUser(), event.getNotifType());
            }
        } else {
            Logger.getLogger().logMessage("SMS-Callback to non-existing event " + id, Logger.L_VERBOSE);
        }
    }

    public void retry(int id, String errorText) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) {
                
                event.getHandler().retry(event.getUser(), event.getNotifType(), errorText);
            }
        } else {
            log.logMessage("SMS-Callback to non-existing event " + id, Logger.L_VERBOSE);
        }
    }

    public void failed(int id, String errorText) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) {
                event.getHandler().failed(event.getUser(), event.getNotifType(), errorText);
            }
        } else {
            log.logMessage("SMS-Callback to non-existing event " + id, Logger.L_VERBOSE);
        }
    }

    @Override
    public void expired(int id) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) {
                event.getHandler().expired(event.getUser(), event.getNotifType());
            }
        } else {
            log.logMessage("SMS-Callback to non-existing event " + id, Logger.L_VERBOSE);
        }
    }
    
    @Override
    protected SMSResultAggregator getEvent(int id) {
        return events.get(new Integer(id));
    }
    
    
    private class SMSListenerEvent extends SMSResultAggregator {
        private int notifType;
        private UserInfo user;
        private FeedbackHandler handler;
        
        public SMSListenerEvent(int notifType, UserInfo user, FeedbackHandler handler) {
            this.notifType = notifType;
            this.user = user;
            this.handler = handler;
        }
        
        public int getNotifType() {
            return notifType;
        }
        
        public UserInfo getUser() {
            return user;
        }
        
        public FeedbackHandler getHandler() {
            return handler;
        }
    }
}

