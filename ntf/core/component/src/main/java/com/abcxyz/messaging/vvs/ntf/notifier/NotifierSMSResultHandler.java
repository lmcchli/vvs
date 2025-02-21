/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.concurrent.ConcurrentHashMap;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierResultHandlerSms;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.out.sms.AbstractSMSResultHandler;


public class NotifierSMSResultHandler extends AbstractSMSResultHandler {

    private static final NotifierSMSResultHandler _inst = new NotifierSMSResultHandler();
    
    private LogAgent log = NtfCmnLogger.getLogAgent(NotifierSMSResultHandler.class);
    private ConcurrentHashMap<Integer, NotifierResultAggregator> resultAggregators = null;

    private NotifierSMSResultHandler() {
        resultAggregators = new ConcurrentHashMap<Integer, NotifierResultAggregator>();
    }
    
    public static NotifierSMSResultHandler get() {
        return _inst;
    }
    
    public void add(int id, ANotifierResultHandlerSms notificationResultHandler) {
        NotifierResultAggregator resultAggregator = new NotifierResultAggregator(notificationResultHandler);
        resultAggregators.put(new Integer(id), resultAggregator);
        if(log.isDebugEnabled()) {
            NotifierResultAggregator resultAggregatorInMap = resultAggregators.get(new Integer(id));
            log.debug("NotifierResultAggregator " + id + " added in Map: " + (resultAggregatorInMap != null ? resultAggregatorInMap.aggregationVariablestoString() : null));
        }
    }
    
    @Override
    public void ok(int id) {
        NotifierResultAggregator resultAggregator = resultAggregators.remove(new Integer(id));
        if(resultAggregator != null) {
            resultAggregator.getNotificationResultHandler().ok();
            if(log.isDebugEnabled()) {
                log.debug("Processed ok - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
            }
        } else {
            log.warn("NotifierResultAggregator " + id + " not found");
        }
    }

    @Override
    public void retry(int id, String errorText) {
        NotifierResultAggregator resultAggregator = resultAggregators.remove(new Integer(id));
        if(resultAggregator != null) {
            resultAggregator.getNotificationResultHandler().retry(errorText);
            if(log.isDebugEnabled()) {
                log.debug("Processed retry - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
            }
        } else {
            log.warn("NotifierResultAggregator " + id + " not found");
        }        
    }

    @Override
    public void failed(int id, String errorText) {
        NotifierResultAggregator resultAggregator = resultAggregators.remove(new Integer(id));
        if(resultAggregator != null) {
            resultAggregator.getNotificationResultHandler().failed(errorText);
            if(log.isDebugEnabled()) {
                log.debug("Processed failed - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
            }
        } else {
            log.warn("NotifierResultAggregator " + id + " not found");
        }        
    }

    @Override
    public void expired(int id) {
        NotifierResultAggregator resultAggregator = resultAggregators.remove(new Integer(id));
        if(resultAggregator != null) {
            resultAggregator.getNotificationResultHandler().expired();
            if(log.isDebugEnabled()) {
                log.debug("Processed expired - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
            }
        } else {
            log.warn("NotifierResultAggregator " + id + " not found");
        }       
    }

    @Override
    public void waitForPhoneOn(int id) {
        NotifierResultAggregator resultAggregator = resultAggregators.remove(new Integer(id));
        if(resultAggregator != null) {
            resultAggregator.getNotificationResultHandler().waitForPhoneOn();
            if(log.isDebugEnabled()) {
                log.debug("Processed waitForPhoneOn - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
            }
        } else {
            log.warn("NotifierResultAggregator " + id + " not found");
        }        
    }
    
    @Override
    protected NotifierResultAggregator getEvent(int id) {
        return resultAggregators.get(new Integer(id));
    }

    @Override
    public void sendStatusIfAllResultsReceived(int id) {
        NotifierResultAggregator resultAggregator = getEvent(id);
        if(resultAggregator != null) {
            if(resultAggregator.isSendingCompleted() && resultAggregator.getNumSmsResultPending() == 0) {
                //For SMPP asynchronous mode, it has happened that both the sending thread and receiving thread get to this code at the same time.
                //To ensure results are processed only once, only the thread that actually removes the result aggregator from the Map should process the results.
                resultAggregator = resultAggregators.remove(new Integer(id));
                if(resultAggregator != null) {
                    resultAggregator.getNotificationResultHandler().processResults(resultAggregator.getNumOkResults(), 
                                                                                   resultAggregator.getNumRetryResults(),
                                                                                   resultAggregator.getNumFailedResults());
                    if(log.isDebugEnabled()) {
                        log.debug("Processed sendStatus - NotifierResultAggregator " + id + " removed from Map: " + resultAggregator.aggregationVariablestoString());
                    }
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("No need to process sendStatus - remove returned null; NotifierResultAggregator " + id + " already removed and processed by another thread");
                    }
                }   
                
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Not ready to process sendStatus yet - NotifierResultAggregator " + id);
                }
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("No need to process sendStatus - getEvent returned null; NotifierResultAggregator " + id + " already removed and processed by another thread");
            }
        }
    }
    
    private class NotifierResultAggregator extends SMSResultAggregator {
        private ANotifierResultHandlerSms notificationResultHandler;

        public NotifierResultAggregator(ANotifierResultHandlerSms notificationResultHandler) {
            this.notificationResultHandler = notificationResultHandler;
        }
        
        public ANotifierResultHandlerSms getNotificationResultHandler() {
            return notificationResultHandler;
        }
    }
}
