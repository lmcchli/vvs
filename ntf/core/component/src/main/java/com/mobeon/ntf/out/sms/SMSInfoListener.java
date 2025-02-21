/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sms;

import java.util.HashMap;

import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.util.Logger;

/**
 * SMSInfoListener listens to callbacks from FormattedSms calls in SMSClient.
 * This class is used for slamdownlist.
 *
 * Stores a slamdownlist and an InfoResultHandler. The InfoResultHandler comes from slamdown
 * and is not a SMSInfoListener object! 
 */
public class SMSInfoListener extends AbstractSMSInfoResultHandler {
    private final static Logger log = Logger.getLogger(SMSInfoListener.class); 
    
    private HashMap<Integer, SMSListenerEvent> events;
   
    private ManagementCounter successCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.SUCCESS);
    private ManagementCounter failCounter = ManagementInfo.get().getCounter(
            "ShortMessage", ManagementCounter.CounterType.FAIL);
    
    public SMSInfoListener() {
        events = new HashMap<Integer, SMSListenerEvent>();
    }
    
    public void add(int id, SlamdownList list, InfoResultHandler handler) {
        SMSListenerEvent event = new SMSListenerEvent(list, handler);
        events.put(new Integer(id), event);
    }
    
    public void failed(int id, String errorText) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));
        if( event != null ) {
            event.getHandler().noneOk(event.getList());
        }
    }
    
    public void ok(int id) {
        events.remove(new Integer(id));
        log.logMessage("Ok in SMSInfoListener, this shouldn't happen", Logger.L_VERBOSE);
    }
    
    public void retry(int id, String errorText) {
        SMSListenerEvent event = events.remove(new Integer(id));
        if (event != null) {
            event.getHandler().retry(event.getList());
        }
    }
    
    public void allOk(int id, int okCount) {
        successCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));
        if( event != null ) {
            event.getHandler().allOk(event.getList(), okCount);
        }
    }
    
    public void partlyFailed(int id, boolean result[], int okCount) {
        failCounter.incr();
        SMSListenerEvent event = events.remove(new Integer(id));
        if( event != null ) {
            event.getHandler().result(event.getList(), result, okCount);
        }
    }


    @Override
    protected SMSInfoResultAggregator getEvent(int id) {
        return events.get(new Integer(id));
    }

    
    private class SMSListenerEvent extends SMSInfoResultAggregator {
        private SlamdownList list;
        private InfoResultHandler handler;

        public SMSListenerEvent(SlamdownList list, InfoResultHandler handler) {
            this.list = list;
            this.handler = handler;
        }
        
        public SlamdownList getList() {
            return list;
        }
        
        public InfoResultHandler getHandler() {
            return handler;
        }              

    }


}

