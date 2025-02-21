package com.mobeon.ntf.out.sms;

import java.util.concurrent.atomic.AtomicInteger;

import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.ntf.util.Logger;

/**
 * AbstractSMSResultHandler implements the common SMS result aggregation functionality
 * (currently used only when the SMPP client is in asynchronous mode).
 * 
 * When adding a call to any of these common SMS result aggregation methods,
 * thread-safety should be verified again.
 */
public abstract class AbstractSMSResultHandler implements SMSResultHandler {

    public void waitForPhoneOn(int id) {
        Logger.getLogger().logMessage("AbstractSMSResultHandler discarding waitForPhoneOn for request " + id, Logger.L_DEBUG);
        return;
    }

    /**********************************************************
     * Methods to aggregate results for one SMSListenerEvent     
     **********************************************************/

    protected abstract SMSResultAggregator getEvent(int id);
    
    public void incrementNumberSmsResultPending(int id) {
        SMSResultAggregator event = getEvent(id);
        if( event != null ) {
            event.incrementNumberSmsResultPending();
            Logger.getLogger().logMessage("SMSResultAggregator.incrementNumberSmsResultPending: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
        }
    }
    
    public void setSendingCompleted(int id) {
        SMSResultAggregator event = getEvent(id);
        if( event != null ) {
            event.setSendingCompleted();
            Logger.getLogger().logMessage("SMSResultAggregator.setSendingCompleted: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
        }
    }

    public void oneOk(int id) {
        SMSResultAggregator event = getEvent(id);
        if( event != null ) {
            event.handleOneOkResult();
            Logger.getLogger().logMessage("SMSResultAggregator.oneOk: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
            sendStatusIfAllResultsReceived(id);
        }
    }
    
    public void oneFailed(int id) {
        SMSResultAggregator event = getEvent(id);
        if( event != null ) {
            event.handleOneFailedResult();
            Logger.getLogger().logMessage("SMSResultAggregator.oneFailed: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
            sendStatusIfAllResultsReceived(id);
        }
    }

    public void oneRetry(int id) {
        SMSResultAggregator event = getEvent(id);
        if( event != null ) {
            event.handleOneRetryResult();
            Logger.getLogger().logMessage("SMSResultAggregator.oneRetry: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
            sendStatusIfAllResultsReceived(id);
        }
    }

    public void oneWaitPhoneOn(int id) {
        SMSResultAggregator event = getEvent(id);
        if (event != null) {
            if (event.getNumFailedResults() != 0) {
                Logger.getLogger().logMessage("SMSResultAggregator.oneWaitPhoneOn: Updated event for request " + id + ": " 
                        + event.aggregationVariablestoString(), Logger.L_DEBUG);
                failed(id, "AbstractSMSResultHandler.sendStatusIfAllResultsReceived: failed to send");
            } else {
                // If their are OK and/or RETRY results as well as a waitPhoneOn result, waitPhoneOn has precedence
                waitForPhoneOn(id);
            }
        }
    }

    public void sendStatusIfAllResultsReceived(int id) {
        SMSResultAggregator event = getEvent(id);
        if(event != null) {
            if( event.isSendingCompleted() && event.getNumSmsResultPending() == 0) {                
                if(event.getNumFailedResults() != 0) {
                    Logger.getLogger().logMessage("SMSResultAggregator.sendStatusIfAllResultsReceived: Sending failed status for request " + id, Logger.L_DEBUG);
                    failed(id, "AbstractSMSResultHandler.sendStatusIfAllResultsReceived: failed to send");
                }
                else if (event.getNumRetryResults() != 0) {
                    Logger.getLogger().logMessage("SMSResultAggregator.sendStatusIfAllResultsReceived: Sending retry status for request " + id, Logger.L_DEBUG);
                    retry(id, "AbstractSMSResultHandler.sendStatusIfAllResultsReceived: failed to send");
                }
                else {
                    Logger.getLogger().logMessage("SMSResultAggregator.sendStatusIfAllResultsReceived: Sending ok status for request " + id, Logger.L_DEBUG);
                    ok(id);
                }
            }
        }
    }
    
    /**
     * Just implement the function defined in interface SMSResultHandler. Do nothing here.
     */
    public void expired(int id) {
        return;
    }

    /**
     * SMSResultAggregator stores all the information necessary for the aggregation of all pending SMS results  for a SMSListenerEvent.
     * (Currently, these methods are used only when SMPP Client is in asynchronous mode.)
     */
    protected class SMSResultAggregator {

        private boolean isSendingCompleted = false;
        private AtomicInteger numSmsResultPending = new AtomicInteger(0);
        
        //Currently no thread-safety issues for numOkResults since only one thread sets this variable.
        private int numOkResults = 0; 
        private AtomicInteger numRetryResults = new AtomicInteger(0);
        private AtomicInteger numFailedResults = new AtomicInteger(0);
        
        public void incrementNumberSmsResultPending() {
            numSmsResultPending.incrementAndGet();
        }
                
        public void setSendingCompleted(){
            isSendingCompleted = true;
        }
        
        public void handleOneOkResult() {
            numOkResults++;
            numSmsResultPending.decrementAndGet();
        }

        public void handleOneRetryResult() {
            numRetryResults.incrementAndGet();
            numSmsResultPending.decrementAndGet();
        }
        
        public void handleOneFailedResult() {
            numFailedResults.incrementAndGet();
            numSmsResultPending.decrementAndGet();
        }

        
        public boolean isSendingCompleted() {
            return isSendingCompleted;
        }
        
        public int getNumSmsResultPending() {
            return numSmsResultPending.get();
        }

        public int getNumOkResults() {
            return numOkResults;
        }
        
        public int getNumRetryResults() {
            return numRetryResults.get();
        }
        
        public int getNumFailedResults() {
            return numFailedResults.get();
        }
        
        public String aggregationVariablestoString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("isSendingCompleted: ").append(isSendingCompleted);
            buffer.append(", numSmsResultPending: ").append(numSmsResultPending);
            buffer.append(", numOkResults: ").append(numOkResults);
            buffer.append(", numRetryResults: ").append(numRetryResults);
            buffer.append(", numFailedResults: ").append(numFailedResults);
            
            return buffer.toString();
        }
    }
}
