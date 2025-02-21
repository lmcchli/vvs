/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms;


import java.util.concurrent.atomic.AtomicBoolean;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.sms.request.CancelRequest;
import com.mobeon.common.sms.request.FormattedSMSRequest;
import com.mobeon.common.sms.request.MultiRequest;
import com.mobeon.common.sms.request.Request;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.SMSComConfigException;
import com.mobeon.common.smscom.SMSComConnectionException;
import com.mobeon.common.smscom.SMSComDataException;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSComLoadException;
import com.mobeon.common.smscom.SMSComPhoneOffException;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.smscom.charset.ConvertedInfo;
import com.mobeon.common.smscom.smpp.SMPPCom;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.util.threads.NtfThread;


public class SMSConnection extends NtfThread
    implements com.mobeon.common.smscom.ConnectionStateListener {

    public static final int SEND_OK = 0;
    public static final int SEND_FAILED = 1;
    public static final int SEND_FAILED_RETRY = 2;
    public static final int SEND_RESULT_PENDING = 3; //Used when in asynchronous mode
    public static final int SEND_WAIT_PHONE_ON = 4;

    public static int id = 0;

    private SMSUnit unit;
    private SMSCom smsCom;
    private Logger log;
    private SMSConfig config;

    private boolean breakUp = false;
    private boolean isSender = true;
    private boolean isReceiver = true;
    private boolean isIdleAndDisconnecting = false;
    
    private long lastCatch = 0L;    
    private long lastSentTimestamp = 0L;
    private boolean isSmsResultPending = false;
    
    private static enum DelegatedActions {
        NONE("none"),
        BIND("bind"),
        UNBIND("unbind");
        
        String actionName = null;
        private DelegatedActions(String actionName) {
            this.actionName = actionName;
        }
        
        private String getActionName() {
            return actionName;
        }
        
    }    
    private DelegatedActions actionDelegated = DelegatedActions.NONE;
    private AtomicBoolean isActionDelegated = new AtomicBoolean(false);
    
    /** Indicates if this SMSConnection MUST unbind from the SMSC; used during SMS unit shutdown. */
    private boolean isMandatoryUnbindSignaled = false;
    
    /** Creates a new instance of SMSConnection 
     * @param unit - the SMS unit to which this connection belongs
     * @param isSender - true if this connection will send messages (other than responses) to the SMSC
     * @param isReceiver - true if this connection will receive messages (other than responses) from the SMSC
     */
    public SMSConnection(SMSUnit unit, boolean isSender, boolean isReceiver) {
        super("SMSConn-" + unit.getInstanceName() + "-" + nextId());
        this.unit = unit;
        log = unit.getLog();
        config = unit.getConfig();
        this.isSender = isSender;
        this.isReceiver = isReceiver;
        getSMSCom();
    }
    
    protected boolean isSender() {
        return isSender;
    }
    
    public synchronized boolean isConnected()
    {
    	if (smsCom == null)
			return false;
    	switch (smsCom.getState()) {
    	case SMSCom.SMSCOM_SENDING:
    	case SMSCom.SMSCOM_BINDING:
    	case SMSCom.SMSCOM_WAITING:
    		return true;
    	default:
    		return false;
    	}
    }

    private static synchronized int nextId() {
        ++id;
        if (id > 99) {
            id = 0;
        }
        return id;
    }
    
    protected void handleIdleDisconnected() {
        if(isIdleAndDisconnecting) {
            if(isSender()) {
                unit.idleConnectionDisconnected();
            } else {
                unit.idleReceiverOnlyConnectionDisconnected();
            }
            isIdleAndDisconnecting = false;
        }
    }
    
    /**
     * Signal to the SMSConnection thread that it should bind to the SMSC.
     * If a delegated action (bind or unbind) is already in progress, the new delegation is ignored.
     * <p>
     * Callers should be careful to not call the SMSConnection thread to do binding if it just did so not long ago; 
     * otherwise, SMSConnection thread will do nothing else but continuously try to re-bind.
     */
    public void signalDelegatedBind() {
        if(isActionDelegated.compareAndSet(false, true)){
            actionDelegated = DelegatedActions.BIND;
            this.interrupt();
            log.logString("SMSConnection.signalDelegatedBind: Bind signaled for " + getName(), Logger.LOG_DEBUG);
        } else {
            log.logString("SMSConnection.signalDelegatedBind: Bind not signaled for " + getName() + " because a delegated action already executing: " + 
                    actionDelegated.getActionName(), Logger.LOG_DEBUG);
        }
    }
    
    /**
     * Signal to the SMSConnection thread that it should unbind from the SMSC.
     * If a delegated action (bind or unbind) is already in progress, the new delegation is ignored.
     */
    public void signalDelegatedUnbind() {
        if(isActionDelegated.compareAndSet(false, true)){
            actionDelegated = DelegatedActions.UNBIND;
            this.interrupt();
            log.logString("SMSConnection.signalDelegatedUnbind: Unbind signaled for " + getName(), Logger.LOG_DEBUG);
        } else {
            log.logString("SMSConnection.signalDelegatedUnbind: Unbind not signaled for " + getName() + " because a delegated action already executing: " + 
                    actionDelegated.getActionName(), Logger.LOG_DEBUG);
        }
    }

    /**
     * Perform the bind or unbind action that was delegated by another thread.
     */
    protected void doDelegatedAction() {
        switch(actionDelegated) {
            case BIND:
                try {
                    log.logString("SMSConnection.doDelegatedAction: Starting bind for " + getName(), Logger.LOG_DEBUG);
                    smsCom.bind();
                } catch (SMSComException e) {
                    log.logString("SMSConnection.doDelegatedAction:" + getName() + " unable to connect to smsc: " + e.getMessage(), Logger.LOG_ERROR);
                    //SMSCom.connectandBind() did disconnect and breakUp would be already set to true.
                }
                break;
            case UNBIND:
                log.logString("SMSConnection.doDelegatedAction: Starting unbind for " + getName(), Logger.LOG_DEBUG);
                smsCom.close();
                break;
            default:
                log.logString("SMSConnection.doDelegatedAction: Unknown delegated action for " + getName(), Logger.LOG_DEBUG);
        }
        actionDelegated = DelegatedActions.NONE;
        SMSConnection.interrupted();
        isActionDelegated.set(false);
    }

    /**
     * Signal to the SMSConnection thread that it MUST unbind from the SMSC.
     * <p>
     * This method should be called only during SMS unit shutdown.
     */
    public void signalMandatoryUnbind() {
        isMandatoryUnbindSignaled = true;
        log.logString("SMSConnection.signalDelegatedMandatoryUnbind: Unbind signaled for " + getName(), Logger.LOG_DEBUG);
    }
    
    /* run on thread startup just once */
    public void ntfThreadInit() {
        if (ManagementInfo.get().isAdministrativeStateLocked())
        {
            log.logString("Unable to connect to smsc when ntf locked." , Logger.LOG_VERBOSE);
            breakUp = true;
            unit.connectionFailed();
        }
        if (ManagementInfo.get().isAdministrativeStateExit())
        {
            log.logString("Unable to connect to smsc, NTF is exiting..." , Logger.LOG_VERBOSE);
            breakUp = true;
            unit.connectionFailed();
        }
        try {
            smsCom.connectandBind();
        } catch (SMSComException e) {
            log.logString("Unable to connect to smsc: " + e.getMessage() , Logger.LOG_ERROR);
            breakUp = true;
            //report back that the connection failed to come up
            unit.connectionFailed();
        }
        //Wake up when locked to send poll requests, to stop connections timing out.
        //check every 5 seconds if poll is required.
        setLockAction(true,5000); 
        
        if (log != null && log.ifLog(Logger.LOG_VERBOSE)) {
            log.logString("Thread starting: " + getName() , Logger.LOG_VERBOSE);
        }
    }
    
   /* send poll requests while locked to keep threads alive */
   protected void ntfLockAction() {
       sendPoll();
   }
    
    /**
     *The run method waits for request on the queue from SMSUnit.
     *will return when no request is returned from the wait or the connection is down.
     */
    public boolean ntfRun() {
        
  	
        if (breakUp) {
            smsCom.close();
            return true;
        }
    	
        Request request = null;
        try {
            //Mandatory unbind always takes precedence over all other actions.
            if(isMandatoryUnbindSignaled) {
                smsCom.close();
                return true;
            }
            if(isActionDelegated.get()) {
                doDelegatedAction();
                return false;
            }

            if(isSender()) {
                if(!smsCom.isReadyForNextRequest()) {
                    //Since the connection is not in a state to send a request, this thread should sleep.
                    //When this thread wakes up:
                    //- If the connection is disconnected, the breakup variable will be false; thread exits this while-loop.
                    //- If the connection is ready, next call to isReadyForNextRequest() will return true; thread continues to process requests.
                    sleep(1000);
                    return false;
                }
                request = unit.waitForRequest(3);
            } else {
                //Receiver-only connection: sleep for a while, wake up to check poll and management state.
                sleep(1000);
            }

            if (request == null) {
                if(ManagementInfo.get().isAdministrativeStateLocked()) {
                    //NTF has been locked.
                    return false; //go back to main thread to lock.
                }
                if(unit.isBiengShutDownViaClient() || ManagementInfo.get().isAdministrativeStateShutdown()) {
                  //Request is null because an ungraceful shutdown is being performed (queued requests are not processed);
                    //or a graceful shutdown is being performed (queued requests are processed) but the queue is empty.
                    if(!smsCom.isWaitingForResponse()) {
                        log.logString("SMSConnection has no pending responses from SMSc, closing the connection because sms unit is shutting down.", Logger.LOG_DEBUG);
                        smsCom.close();
                    } else {
                        log.logString("SMSConnection is waiting for pending responses from SMSc before closing the connection because sms unit is shutting down.", Logger.LOG_DEBUG);
                        sendPoll();
                        sleep(1000);
                    }
                } else if(unit.isDiscardingQueuedRequests()) {
                    //Request is null because the queue is being discarded for another reason other than shutdown.
                    //For example, when the SMS unit is made temporarily unavailable for new requests and the currently queued requests
                    //are in the process of being removed.
                    //Poll requests must continue but since we are currently not waiting the poll interval when unit.waitForRequest() is called,
                    //check when the next poll is due.
                    sendPoll();
                    sleep(1000);
                } else {
                    //Request is null because the poll interval time has elapsed and there was no traffic to send.
                    //So, check if this connection can close because it is idle.  Otherwise, send a poll request.
                    if (!smsCom.isWaitingForResponse() && 
                            ( (isSender() && unit.canIdleConnectionDisconnect()) || (!isSender() && unit.canIdleReceiverOnlyConnectionDisconnect()) )) {
                        log.logString("SMSConnection timed out waiting for requests and there is no pending response from SMSc", Logger.LOG_DEBUG);
                        log.logString("SMSConnection closing idle connection", Logger.LOG_VERBOSE);
                        isIdleAndDisconnecting = true;
                        smsCom.close();
                    } else {
                        sendPoll();
                    }
                }
            } else {
                if( request instanceof CancelRequest ) {
                    CancelRequest cancelRequest = (CancelRequest) request;
                    smsCom.sendCancel(cancelRequest.getToAddress(), cancelRequest.getFromAddress(), cancelRequest.getServiceType());
                } else {
                    if (request instanceof FormattedSMSRequest) {
                        sendFormattedMessages((FormattedSMSRequest) request);

                    } else if( request instanceof MultiRequest ) {
                        sendMultiRequest((MultiRequest) request);

                    } else {
                        SMSMessage msg = request.getSMSMessage(unit);
                        int result = sendMessage(request, msg);
                        if (result == SEND_OK) {
                            request.getResultHandler().ok(request.getId());
                        }
                    }    		        
                }
                lastSentTimestamp = System.currentTimeMillis();
            }

        } catch (SMSComException e) {

            if ( request !=null && unit !=null)
                unit.sendFailed(request);

            if (log != null && log.ifLog(Logger.LOG_ERROR)) {
                log.logString("Communication exception, will retry: " + SMSCom.stackTrace(e),
                        Logger.LOG_ERROR);
                long now = System.currentTimeMillis();
                if (now - lastCatch < 500) {
                    try { sleep(500); } catch (InterruptedException ie) { return true; }
                }
                lastCatch = now;
            }

        }  catch (InterruptedException i) {
                breakUp = true; //exit this connection thread.
                log.logString("Connection is going down" + SMSCom.stackTrace(i),
                        Logger.LOG_VERBOSE);
                if (request !=null && unit !=null)
                    unit.sendFailed(request); //retry the message
                boolean res = ntfRun(); //drain queue..
                if (res == true) {
                    smsCom.close();                     
                }
                
                return res; //exit now, forced exit...

        } catch (Exception e1) {
            if (request !=null && unit !=null)
                unit.sendFailed(request); //retry the message
            if (log != null && log.ifLog(Logger.LOG_ERROR)) {
                log.logString("Unexpected: " + SMSCom.stackTrace(e1),
                        Logger.LOG_ERROR);
                long now = System.currentTimeMillis();
                if (now - lastCatch < 500) {
                    try { sleep(500); } catch (InterruptedException ie) { return true; }
                }
                lastCatch = now;
            }
        }
        return false;
    }
    
    public boolean shutdown() {
        if (isInterrupted()) {
                smsCom.close();
                return true;
            } //exit immediately if interrupted i.e shutdown now...
        if (unit.getDelayedSize() == 0)
        {
                if (unit.isIdle(3000))
                {
                    smsCom.close();
                    return true;
                }
                else {
                    if (unit.waitNotEmpty(3000)) {
                        boolean res = ntfRun(); //drain queue..
                        if (res == true) {
                            smsCom.close();                     
                        }
                        return res;
                    } else
                    {
                        smsCom.close();
                        return true;
                    }
                }
        } else {
            boolean res = ntfRun(); //drain queue..
            if (res == true) {
                smsCom.close();                     
            }
            return res;
        }
    }

    /**
     * Sends a poll request if it is time to send one.
     */
    private void sendPoll() {
        if(System.currentTimeMillis() >= lastSentTimestamp + Config.getSmscPollInterval() * 1000) {
            log.logString(this.getName() + " sending poll request to " + unit.getHost(), Logger.LOG_DEBUG);
            if (!smsCom.poll()) {
                log.logString("poll Failed to: " + unit.getHost(), Logger.LOG_ERROR);
            } else {
                lastSentTimestamp = System.currentTimeMillis();
            }
        }
    }
    
    private void sendMultiRequest(MultiRequest request) {
        int result = SEND_OK;
        while( request.getCount() > 0 ) {
            Request sendRequest = request.getNextRequest();
            result = sendMessage(sendRequest, sendRequest.getSMSMessage(unit));
            if( result == SEND_OK ) {
                sendRequest.getResultHandler().ok(sendRequest.getId());
            } else if( result == SEND_FAILED_RETRY ) {
                break;
            }            
            request.requestDone();
        }
    }

    /**
     *Sends several formatted SMSMessages.
     *@param request -the request holding SMSMessages and more.
     *
     */
    private void sendFormattedMessages(FormattedSMSRequest request) {
    	Object perf = null;
    	try {
	        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("SMSConnection.sendFormattedMessages");
	        }
	        ConvertedInfo info = request.getConvertedInfo(unit);
	        SMSMessage[] msgs = info.getMessages();
	        int[] crossRefs = info.getCrossReference();
	        boolean[] ok = new boolean[msgs.length];
	        SMSInfoResultHandler resultHandler = (SMSInfoResultHandler) request.getResultHandler();
	        resultHandler.setNumberSmsToSend(request.getId(), msgs.length);
	        
	        isSmsResultPending = false;
	        int okCount = 0;
	        int failedCount = 0;
	        int crossRefsItr = 0;
	        int crossRefsLen = crossRefs.length;
            int result = SEND_OK;
	        for (int i = 0; i < msgs.length; i++) {
	            SMSMessage msg = msgs[i];

	            msg.nextFragment();

	            boolean found = false;
	            while((crossRefsItr < crossRefsLen) && !found ){
	                if ( i == crossRefs[crossRefsItr])
	                    found = true;
	                crossRefsItr++;
	            }
	            if(found){
                    log.logString("Found in crossRefs " + (crossRefsItr-1) + ", preparing to send fragment sequence " + i ,Logger.LOG_DEBUG);
	                result = sendFragment(request.getSourceAddress(crossRefsItr-1),request.getToAddress(), msg, request.getId(), resultHandler, false);
	            } else{
                    log.logString("Not Found in crossRefs, ERROR occurs when trying to send fragment sequence " + i ,Logger.LOG_DEBUG);
                    result = sendFragment(request.getFromAddress(),request.getToAddress(), msg, request.getId(), resultHandler, false);
	            }

	            if (result == SEND_OK) {
	                ok[i] = true;
	                ++okCount;
	            }
	            else if (result == SEND_RESULT_PENDING) {
	                //No result yet for SMSResultHandler (asynchronous mode).
	                //Increment the number of SMS result pending so we know later when all results have been received.
	                resultHandler.incrementNumberSmsResultPending(request.getId());
	                isSmsResultPending = true;
	            } else if (result == SEND_WAIT_PHONE_ON) {
	                break;

	            } else {
	                ok[i] = false; //This will be used below if isSmsResultPending remains false.
	                
	                if(result == SEND_FAILED) {
	                    failedCount++;
	                }
	                
	                //For formatted messages, the final status is determined by the number of OK send fragment results.
	                //(This is different from the send non-formatted message case.)  
	                //Therefore, for the case that isSmsResultPending becomes true, 
	                //there is no need to record failed/retry fragment results in SMSResultHandler.
	            }

	            if (result == SEND_FAILED_RETRY) {
                // can't send any more on this connection.
	                // return status of call so far.
	                break;
	            }
	        }
	        
	        if(!isSmsResultPending) {
	            //Results are handled here in:
	            //- all cases for synchronous mode 
	            //- cases where there is no pending results for asynchronous mode (i.e. no send succeeded)
	            if (okCount == msgs.length) {
	                resultHandler.allOk(request.getId(), okCount);

	            } else if (okCount == 0) {
                    if (result == SEND_WAIT_PHONE_ON) {
                        resultHandler.waitForPhoneOn(request.getId());
                    } else {
                        if(failedCount == msgs.length) {
                            resultHandler.failed(request.getId(), "SMSConnection.sendFormattedMessages: all failed");
                        } else {
                            resultHandler.retry(request.getId(), "SMSConnection.sendFormattedMessages: some to be retried");
                        }
                    }

	            } else {
	                String[] lines = request.getLines();
	                boolean results[] = new boolean[lines.length];

	                for (int i = 0; i < lines.length; i++) {
	                    results[i] = ok[crossRefs[i]];
	                }
	                resultHandler.partlyFailed(request.getId(), results, okCount);
	            }
	        }
	        else {
	            //When the results for asynchronous mode are not being handled in this class, 
	            //we would not know if the results are being received before all SMS messages are sent out. 
	            //We need to signal to the result-handling thread when this thread has finish sending out the SMS messages.
	            //The result-handling thread should not send the final status until all sending has been completed/stopped.
	            resultHandler.setSendingCompleted(request.getId());
	            
	            //It is possible that the result-handling thread received and finished processing the last pending result
	            //before this thread set the sendingCompleted flag to true.
	            //So, try to send status if all the results have been received.
	            resultHandler.sendStatusIfAllResultsReceived(request.getId());
	        }
    	} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }


    private int sendMessage(Request request, SMSMessage msg) {
    	Object perf = null;
    	try {
	        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.Trace.SMPP.SMSConn.SendMessage");
	        }
	        int result = SEND_OK;
	        isSmsResultPending = false;
	        SMSResultHandler resultHandler = request.getResultHandler();
	        while (msg.hasMoreFragments()) {
	            msg.nextFragment();
	            result = sendFragment(request.getFromAddress(), request.getToAddress(), msg, request.getId(), resultHandler, (!isSmsResultPending));
	            if(result == SEND_FAILED) {
	                if(isSmsResultPending) {
	                    //Since the final status will be determined later when all send fragment results are received, record this send fragment failure.
	                    resultHandler.incrementNumberSmsResultPending(request.getId());
	                    resultHandler.oneFailed(request.getId());
	                }
	                break;
	            }
	            else if(result == SEND_FAILED_RETRY) {
                    if(isSmsResultPending) {
                        //Since the final status will be determined later when all send fragment results are received, record this send fragment retry.
                        resultHandler.incrementNumberSmsResultPending(request.getId());
                        resultHandler.oneRetry(request.getId());
                    }
	                break;
	            }
	            else if(result == SEND_RESULT_PENDING) {
                    //No result yet for SMSResultHandler (asynchronous mode).
	                //Increment the number of SMS result pending so we know later when all results have been received.
	                resultHandler.incrementNumberSmsResultPending(request.getId());
                    isSmsResultPending = true;
	            }
	        }
	        
	        if(isSmsResultPending) {
                //When the results for asynchronous mode are not being handled in this class, 
                //we would not know if the results are being received before all SMS messages are sent out. 
                //We need to signal to the result-handling thread when this thread has finish sending out the SMS messages.
                //The result-handling thread should not send the final status until all sending has been completed/stopped.
	            resultHandler.setSendingCompleted(request.getId());

                //It is possible that the result-handling thread received and finished processing the last pending result
                //before this thread set the sendingCompleted flag to true.
                //So, try to send status if all the results have been received.
	            resultHandler.sendStatusIfAllResultsReceived(request.getId());
	        }

	        return result;
    	} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }
    
    /**
     *sends a fragment to the smscom.
     *@param from - The source-address.
     *@param to - Where to send the message.
     *@param msg - The message to send.
     *@param id - the id for the call.
     *@param rh - The resulthandler to send callbacks to.
     *@return true if successful, false if any error.
     */
    private int sendFragment(SMSAddress from, SMSAddress to, SMSMessage msg, int id, SMSResultHandler rh, boolean isHandlingResult) {
        try {
            log.logString(
                    "Sending fragment=" + msg.getFragmentNumber() + " to=" + to
                    + " from=" + from + " validity="
                    + msg.getExpiryTimeRelative(),
                    Logger.LOG_DEBUG);            
            
            return smsCom.sendMessage(to, from, msg, id, rh);

        } catch (SMSComConnectionException e) {
            log.logString("Message not sent because " + e, Logger.LOG_ERROR);
            if (isHandlingResult) {
                rh.retry(id, e.getMessage());
            }
            return SEND_FAILED_RETRY;

        } catch (SMSComConfigException e) {
            log.logString("Config of SMS-C communication needs correction. " + e,
            		Logger.LOG_ERROR);
            if (isHandlingResult) {
                rh.failed(id, e.getMessage());
            }
            return SEND_FAILED;

        } catch (SMSComDataException e) {
            // This message is bad and can not be retried.
            log.logString("discarding bad message. " + e,
            		Logger.LOG_ERROR);
            if (isHandlingResult) {
                rh.failed(id, e.getMessage());
            }
            return SEND_FAILED;

        } catch (SMSComLoadException e) {
            // Overload in the SMS-C.
            log.logString("SMS-C overloaded. " + e, Logger.LOG_VERBOSE);
            if (isHandlingResult) {
                rh.retry(id, e.getMessage());
            }
            return SEND_FAILED_RETRY;

        } catch (SMSComPhoneOffException e) {
            // Phone off
            log.logString("Phone off. " + e, Logger.LOG_VERBOSE);
            if (isHandlingResult) {
                rh.waitForPhoneOn(id);
            }
            return SEND_WAIT_PHONE_ON;

        } catch (SMSComException e) {
            log.logString("unknown problem " + e, Logger.LOG_ERROR);
            if (isHandlingResult) {
                rh.retry(id, e.getMessage());
            }
            return SEND_FAILED_RETRY;
        }

    }

    /**
     * Sets the smscom object with the correct parameters.
     */
    private void getSMSCom() {
        try {
            log.logString(
                    "Getting SMSCom for " + unit.getProtocol() + " to "
                    + unit.getHost(),
                    Logger.LOG_VERBOSE);
            
            boolean asynchronousMode = Config.isSmsClientAsynchronous();
            smsCom = SMSCom.get(unit.getProtocol(), asynchronousMode, this);
            log.logString("Got smscom instance: " + smsCom.getClass().getName(), Logger.LOG_DEBUG);

            smsCom.setSend(isSender);
            smsCom.setReceive(isReceiver);
            if (isReceiver) {
                smsCom.setReceiptReceiver(config.getPhoneOnEventListener());
            }

            smsCom.setLogger(log);

            smsCom.setHostName(unit.getHost());
            smsCom.setPortNumber(unit.getPort());
            smsCom.setUserName(unit.getUserName());
            smsCom.setPassword(unit.getPassword());
            smsCom.setSystemType(unit.getSystemType());
            smsCom.setTimeout(config.getSmscTimeout() * 1000);
            smsCom.setRequestSenderThread(this);
            if (unit.getProtocol().equalsIgnoreCase("smpp")) {
                ((SMPPCom) smsCom).setErrorCodesIgnored(
                        config.getSmppErrorCodesIgnored());
                ((SMPPCom) smsCom).setVersion(config.getSmppVersion());
            }
            if (config.getSmscErrorAction().equalsIgnoreCase("IGNORE")) {
                smsCom.setErrCodeAction(SMSCom.IGNORE_ERRORS);
            } else if (config.getSmscErrorAction().equalsIgnoreCase("LOG")) {
                smsCom.setErrCodeAction(SMSCom.LOG_ERRORS);
            } // else keep the default; handle errors
            log.logString("Got SMSCom " + smsCom, Logger.LOG_VERBOSE);
        } catch (Exception e) {
            log.logString("Exception getting smscom " + e.toString(),
            		Logger.LOG_ERROR);
        }
    }

     public synchronized void connectionUp(String name) {
        unit.connectionUp(this);
    }

    public void connectionDown(String name) {
        try {
            unit.connectionDown(this, true);
            handleIdleDisconnected();
        } catch(Exception e) {
            log.logString("Unexpected exception " + SMSCom.stackTrace(e), Logger.LOG_ERROR);
        }
        breakUp = true;
        this.interrupt();
    }

    public synchronized void connectionReset(String name) {
        try {
            unit.connectionDown(this, false);
        } catch(Exception e) {
            log.logString("Unexpected exception " + SMSCom.stackTrace(e), Logger.LOG_ERROR);
        }
        breakUp = true;
        this.interrupt();
    }

    public void connectionTemporaryUnavailableForNewRequests() {
        unit.setTemporaryUnavailableForNewRequests();
    }
    
	public synchronized boolean isDisconnecting() {
		if (smsCom == null)
			return true;;
		switch (smsCom.getState()) {
    	case SMSCom.SMSCOM_DISCONNECTING:
    	case SMSCom.SMSCOM_CLOSED:
    	case SMSCom.SMSCOM_UNBINDING:
    		return true;
    	default:
    		return false;
    	}
	}

	public synchronized boolean connect() {

	    if (smsCom == null)
	    {
	        getSMSCom();
	    }

	    if (smsCom != null)
	    {
	        this.start();
	        return true;
	    }
	    else
	    {
	        return false;
	    }
	}
	
}
