/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.ntf.out.sms;

import com.mobeon.common.sms.SMSInfoResultHandler;
import com.mobeon.ntf.util.Logger;

/**
 * AbstractSMSInfoResultHandler implements the common SMS result aggregation functionality for slamdown
 * (currently used only when the SMPP client is in asynchronous mode).
 * 
 * When adding a call to any of these common SMS result aggregation methods,
 * thread-safety should be verified again.
 */
public abstract class AbstractSMSInfoResultHandler extends AbstractSMSResultHandler implements SMSInfoResultHandler {

    /**********************************************************
     * Methods to aggregate results for one SMSListenerEvent     
     **********************************************************/

    protected abstract SMSInfoResultAggregator getEvent(int id);
    
    public void setNumberSmsToSend(int id, int numberOfResults) {
        SMSInfoResultAggregator event = getEvent(new Integer(id));
        if( event != null ) {
            event.setNumberSmsToSend(numberOfResults);
            Logger.getLogger().logMessage("AbstractSMSInfoResultHandler.setNumberSmsToSend: Updated event for request " + id + ": " 
                    + event.aggregationVariablestoString(), Logger.L_DEBUG);
        }
    }
    
    public void sendStatusIfAllResultsReceived(int id) {
        SMSInfoResultAggregator event = getEvent(new Integer(id));
        if(event != null) {
            if( event.isSendingCompleted() && event.getNumSmsResultPending() == 0) {
                int numOkResults = event.getNumOkResults();
                if(numOkResults == event.getNumberSmsToSend()) {
                    allOk(id, numOkResults);
                }
                else if (numOkResults == 0) {
                    if(event.getNumFailedResults() == event.getNumberSmsToSend()) {
                        failed(id, "AbstractSMSInfoResultHandler.sendStatusIfAllResultsReceived: all failed");
                    } else {
                        retry(id, "AbstractSMSInfoResultHandler.sendStatusIfAllResultsReceived: some to be retried");
                    }
                }
                else {
                    partlyFailed(id, null, numOkResults);
                }
            }
        }
    }
    
    
    protected class SMSInfoResultAggregator extends SMSResultAggregator {

        //No thread-safety issues for numSmsToSend since only one thread sets this variable once before any sending starts.
        private int numSmsToSend = 0;  
        

        public void setNumberSmsToSend(int numResults) {
            numSmsToSend = numResults;
        }

        public int getNumberSmsToSend() {
            return numSmsToSend;
        }

        public String aggregationVariablestoString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("numSmsToSend: ").append(numSmsToSend).append(" ");
            buffer.append(super.aggregationVariablestoString());
            return buffer.toString();
        }
    }
}
