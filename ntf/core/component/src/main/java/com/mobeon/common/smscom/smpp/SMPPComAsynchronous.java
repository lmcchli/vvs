package com.mobeon.common.smscom.smpp;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.sms.SMSConnection;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComConfigException;
import com.mobeon.common.smscom.SMSComConnectionException;
import com.mobeon.common.smscom.SMSComDataException;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSComLoadException;
import com.mobeon.common.smscom.SMSComPhoneOffException;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.Config;


/**
 * SMPPComAsynchronous is an implementation of SMSCom for the SMPP protocol.
 *
 * <P>
 * <TABLE BGCOLOR="#FFF0E0" CELLPADDING=10 WIDTH=50% BORDER=1><TR><TD>
 * This class shall not be used directly. You use it indirectly
 * through SMSCom. This means that all
 * documentation of this class and other classes in the smpp package is for
 * implementors, you do not need it to use the API.
 * </TD></TR></TABLE>
 * <P>
 * 
 * The requests to the SMSC are asynchronous in that this class does not wait for the
 * response from the SMSC for the current request before sending another request.
 * <P>
 * Note that the bind and unbind requests are kept synchronous since no other request
 * should be sent until it is known that there was successful binding to the SMSC and
 * no other request should be sent if we are unbinding from the the SMSC.
 * <p>
 * In some cases, the SmscReader thread will call methods which will try to bind/unbind from the SMSC.
 * Since the bind/unbind is done synchronously and the SmscReader thread cannot wait for the 
 * bind/unbind response (it has to listen for responses), it needs to delegate the bind/unbind to another thread.
 * <p>
 * If there is more than one thread sending synchronously, when a response comes in for a synchronous request,
 * all threads waiting would be notified which will lead to failures for all but one thread.  If we change to notify only
 * one waiting thread, we might not wake up the right thread for the received response.  Since there are cases that the 
 * request sender thread (SMSConnection) sends out unbind requests, it should be the only thread that sends synchronously.
 * Therefore, all bind/unbind will be performed by the request sender thread.
 */
public class SMPPComAsynchronous extends SMPPCom {

    private static final String PROFILER_CHECKPOINT_NAME_MAX_PENDING_REQ_NOT_SENT = "NTF.Trace.SMPP.As.MaxPendingReqNotSent";
    private static final String PROFILER_CHECKPOINT_NAME_RESP_OK = "NTF.Trace.SMPP.As.Resp.1.Ok.";
    private static final String PROFILER_CHECKPOINT_NAME_RESP_RETRY = "NTF.Trace.SMPP.As.Resp.2.Retry.";
    private static final String PROFILER_CHECKPOINT_NAME_RESP_WPOn= "NTF.Trace.SMPP.As.Resp.3.WPOn.";
    private static final String PROFILER_CHECKPOINT_NAME_RESP_FAIL = "NTF.Trace.SMPP.As.Resp.4.Fail.";
    private static final String PROFILER_CHECKPOINT_NAME_RESP_TIMEOUT = "NTF.Trace.SMPP.As.Resp.5.Timeout";
    
    private static final int MAXIMUM_NUM_RESPONSE_HANDLER_MAP_ENTRIES = 10000;

    /**
     * numWaitingResponseHandlers is used to ensure that the waitingResponseHandlers map size never exceeds the MAXIMUM_NUM_RESPONSE_HANDLER_MAP_ENTRIES.
     */
    private AtomicInteger numWaitingResponseHandlers = new AtomicInteger(0);
    private ConcurrentHashMap<Integer, AsynchrononusPduResponseHandler> waitingResponseHandlers = new ConcurrentHashMap<Integer, AsynchrononusPduResponseHandler>();
        
    
    @Override
    protected void setState(int st) {
        //If the connection is disconnecting (already unbinded and is closing the socket),
        //the only state it can transition to is exiting.
        //This will prevent the send request thread from changing the state (to waiting for example) 
        //after the SmscReader thread received a unbind request from the SMSC and is disconnecting.
        if(state == SMSCOM_DISCONNECTING && st != SMSCOM_EXITING) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Cannot perform the connection state transition: " + _stateStrings[state] + "->" + _stateStrings[st], Logger.LOG_DEBUG);
            }
            return;
        }
        super.setState(st);
    }
    
    @Override
    protected int getSuccessfulSendCode() {
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Returning SEND_RESULT_PENDING successful send code", Logger.LOG_DEBUG);
        }
        return SMSConnection.SEND_RESULT_PENDING;
    }
    
    @Override
    public boolean isWaitingForResponse() {
        boolean isWaiting = false;
        if(numWaitingResponseHandlers.get() != 0) {
            //We care only about responses for the sending of short messages.
            //Waiting for a poll response does not count as waiting for a response.
            //
            //To avoid concurrency issues, take a snapshot of the pending response sequence numbers
            //and use that to iterate through the pending responses.
            Integer[] sequenceNumbersArray = new Integer[numWaitingResponseHandlers.get()];
            waitingResponseHandlers.keySet().toArray(sequenceNumbersArray);
            for(int i=0; i < sequenceNumbersArray.length; i++) {
                if(sequenceNumbersArray[i] != null) {
                    AsynchrononusPduResponseHandler handler = waitingResponseHandlers.get(sequenceNumbersArray[i]);
                    if(handler != null) {             
                        if(!POLLING_SERVICE_TYPE.equals(handler.getServiceType())) {
                            isWaiting = true;
                            break;
                        }
                    }
                }
            }      
        }
        return isWaiting;
    }
    
    @Override
    protected void protocolCleanup() {
        super.protocolCleanup();

        //Send retry result to the result handlers of the pending responses.
        //To avoid concurrency issues, take a snapshot of the pending response sequence numbers
        //and use that to iterate through the pending responses.
        Integer[] sequenceNumbersArray = new Integer[numWaitingResponseHandlers.get()];
        waitingResponseHandlers.keySet().toArray(sequenceNumbersArray);
        for(int i=0; i < sequenceNumbersArray.length; i++) {
            if(sequenceNumbersArray[i] != null) {
                AsynchrononusPduResponseHandler pduRespHandler = removeWaitingResponseHandler(sequenceNumbersArray[i]);
                if(pduRespHandler != null) {
                    sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                }
            }
        }        
    }
    
    @Override
    protected void protocolSend(SMSAddress to, SMSAddress from, SMSMessage msg, int smsRequestId, SMSResultHandler rh) throws SMSComException {
        SendShortMessagePDU sendShortMsgReq = null;
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolSend: Sending message to " + to + " from " + from + " message: "+msg, Logger.LOG_DEBUG);
            }

            //Prepare request PDU first since sequence number is needed to add to response handler map.
            sendShortMsgReq = getShortMessagePdu(msg);
            byte[] sendShortMsgReqBuffer = sendShortMsgReq.getBuffer(to, from, msg);
            
            //Initialise response handler and add it to the map.            
            AsynchrononusPduResponseHandler asyncRespHandler = new AsynchrononusPduResponseHandler(sendShortMsgReq.getExpectedPduResponseCommandId(), smsRequestId,
                    rh, from.toString(), to.toString(), msg, sendShortMsgReq.toString());            
            addWaitingResponseHandler(sendShortMsgReq.getSequenceNumber(), asyncRespHandler);

            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolSend: Added to waitingResponseHandlers map: sequenceNumber: " + sendShortMsgReq.getSequenceNumber() + 
                        " responseHandler: " + asyncRespHandler.toString(), Logger.LOG_DEBUG);
            }
            
            //Send request to SMSC.
            writeBuffer(sendShortMsgReqBuffer, sendShortMsgReq);

            startResponseTimeoutTimer(sendShortMsgReq.getSequenceNumber(), getShortMessagePduOperationTimeout(sendShortMsgReq.getCommandId()));

            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolSend: Sent send short message request " + sendShortMsgReq.getSequenceNumber(), Logger.LOG_DEBUG);
            }
            setState(SMSCOM_WAITING);
        } catch (IOException e) {
            removeWaitingResponseHandler(sendShortMsgReq.getSequenceNumber());
            throw new SMSComConnectionException("Failed to send short message with " +" source: " + from + " dest: " + to + " message: "+ msg + " error: " + e.getMessage());            
        }          
    }

    @Override
    protected void protocolCancel(SMSAddress to, SMSAddress from, String serviceType) throws SMSComException {
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolCancel: Canceling messages to " + to + " from " + from + " with serviceType " + serviceType, Logger.LOG_DEBUG);
            }

            //Prepare request PDU first since sequence number is needed to add to response handler map.
            byte[] cancelReqBuffer = cancelReq.getBuffer(to, from, serviceType);        
            AsynchrononusPduResponseHandler asyncRespHandler = new AsynchrononusPduResponseHandler(SMPPCMD_CANCEL_SM_RESP, serviceType, cancelReq.toString());
            addWaitingResponseHandler(cancelReq.getSequenceNumber(), asyncRespHandler);

            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolCancel: Added to waitingResponseHandlers map: sequenceNumber: " + cancelReq.getSequenceNumber() + 
                        " responseHandler: " + asyncRespHandler.toString(), Logger.LOG_DEBUG);
            }
            
            //Send request to SMSC.
            writeBuffer(cancelReqBuffer, cancelReq);
            
            startResponseTimeoutTimer(cancelReq.getSequenceNumber(), Config.getSmscTimeout() * 1000);
            
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolCancel: Sent cancel request " + cancelReq.getSequenceNumber(), Logger.LOG_DEBUG);
            }
            setState(SMSCOM_WAITING);
        } catch (IOException e) {
            removeWaitingResponseHandler(cancelReq.getSequenceNumber());
            throw new SMSComConnectionException("SMPPComAsynchronous.protocolCancel: Failed to send cancel with " + " source: " + from + " dest: " + to + " service type: " + serviceType + " error: " + e.getMessage());
        }
    }

    @Override
    protected boolean protocolPoll(boolean retryIfRebind) {
        boolean isPollSent = false;
        try {
            //Prepare request PDU first since sequence number is needed to add to response handler map.
            byte[] enquireReqToSmscBuffer = enquireReqToSmsc.getBuffer();       
            AsynchrononusPduResponseHandler asyncRespHandler = new AsynchrononusPduResponseHandler(SMPPCMD_ENQUIRE_LINK_RESP, POLLING_SERVICE_TYPE, enquireReqToSmsc.toString());
            addWaitingResponseHandler(enquireReqToSmsc.getSequenceNumber(), asyncRespHandler);

            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.protocolPoll: Added to waitingResponseHandlers map: sequenceNumber: " + enquireReqToSmsc.getSequenceNumber() + 
                        " responseHandler: " + asyncRespHandler.toString(), Logger.LOG_DEBUG);
            }

            //Send request to SMSC.
            writeBuffer(enquireReqToSmscBuffer, enquireReqToSmsc);

            startResponseTimeoutTimer(enquireReqToSmsc.getSequenceNumber(), Config.getSmscTimeout() * 1000);            
            setState(SMSCOM_WAITING);
            isPollSent = true;     

        } catch (SMSComLoadException e) {
            log.logString("SMPPComAsynchronous.protocolPoll: Failed to send poll: " + e.getMessage(), Logger.LOG_ERROR);
        } catch (IOException e) {
            removeWaitingResponseHandler(enquireReqToSmsc.getSequenceNumber());
            log.logString("SMPPComAsynchronous.protocolPoll: Failed to send poll: " + e.getMessage(), Logger.LOG_ERROR);
            close();
        }
        return isPollSent;
    }

    private void addWaitingResponseHandler(int sequenceNumber, AsynchrononusPduResponseHandler asyncRespHandler) throws SMSComLoadException {
        if(numWaitingResponseHandlers.get() < MAXIMUM_NUM_RESPONSE_HANDLER_MAP_ENTRIES) {
            numWaitingResponseHandlers.incrementAndGet();
            waitingResponseHandlers.put(sequenceNumber, asyncRespHandler);
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                asyncRespHandler.enterResponseDelayProfilingCheckPoint();
            }
        }
        else {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_MAX_PENDING_REQ_NOT_SENT);
            }
            log.logString("SMPPComAsynchronous.addWaitingResponseHandler: Allowed maximum number of waiting response handlers is reached.  Unable to add: " + asyncRespHandler, Logger.LOG_DEBUG);
            throw new SMSComLoadException("Unable to add asynchronous response handler because too many waiting response handlers");
        }   
    }
    
    private AsynchrononusPduResponseHandler removeWaitingResponseHandler(int sequenceNumber) {        
        AsynchrononusPduResponseHandler responseHandler = waitingResponseHandlers.remove(sequenceNumber);
        if(responseHandler != null) {
            numWaitingResponseHandlers.decrementAndGet();
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                responseHandler.exitResponseDelayProfilingCheckPoint();
            }
        }
        return responseHandler;
    }
    
    private void startResponseTimeoutTimer(int pduSequenceNumber, int timeout) {
        Timer timer = new Timer(true);
        AsynchronousResponseTimeoutTask responseTimeoutTask = new AsynchronousResponseTimeoutTask(pduSequenceNumber);
        timer.schedule(responseTimeoutTask, timeout);
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("SMPPComAsynchronous.startResponseTimeoutTimer: " + timeout + " millisecond timer started for sequence number " + pduSequenceNumber, Logger.LOG_DEBUG);
        }
    }        
    
    
    @Override
    protected void handleReceivedPdu(SMPP_PDU pdu) {
        switch (pdu.getCommandId()) {
        case SMPPCMD_BIND_TRANSMITTER_RESP:
        case SMPPCMD_BIND_TRANSCEIVER_RESP:
        case SMPPCMD_BIND_RECEIVER_RESP:
        case SMPPCMD_UNBIND_RESP:
            handleSynchronousReceivedResponse(pdu);
            break;
            
        case SMPPCMD_SUBMIT_SM_RESP:
        case SMPPCMD_DATA_SM_RESP:
        case SMPPCMD_CANCEL_SM_RESP:
        case SMPPCMD_ENQUIRE_LINK_RESP:
            handleAsynchronousReceivedResponse(pdu);
            break;

        case SMPPCMD_QUERY_SM_RESP:
        case SMPPCMD_REPLACE_SM_RESP:
        case SMPPCMD_SUBMIT_MULTI_RESP:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.handleReceivedPdu: Unexpected response: " + pdu, Logger.LOG_DEBUG);
            }
            break;
            
        case SMPPCMD_DELIVER_SM:
            handleDeliverSmReq(pdu);
            break;

        case SMPPCMD_ENQUIRE_LINK:
            handleEnquireLinkReq(pdu);
            break;

        case SMPPCMD_UNBIND:
            handleUnbindReq(pdu);
            break;

        case SMPPCMD_GENERIC_NACK:
            handleNack(pdu);
            break;

        case SMPPCMD_ALERT_NOTIFICATION:
            handleAlertNotificationMessage(pdu);
            break;
            
        case SMPPCMD_OUTBIND:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.handleReceivedPdu: Received Outbind: " + pdu, Logger.LOG_DEBUG);
            }
        
        case SMPPCMD_DATA_SM:
            dataSmReqFromSmsc.parseBody(pdu);
            dataSmReqFromSmsc.incrementCounterReceived();
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.handleReceivedPdu: Received Data SM: " + dataSmReqFromSmsc, Logger.LOG_DEBUG);
            }
            dataSmRespToSmsc.setSequenceNumber(pdu.getSequenceNumber());
            dataSmRespToSmsc.setCommandStatus(SMPPSTATUS_ROK);
            try {
                writeBuffer(dataSmRespToSmsc.getBuffer(), dataSmRespToSmsc);
            } catch (IOException e) {
                if (willLog(Logger.LOG_DEBUG)) {
                    log.logString("SMPPComAsynchronous.handleReceivedPdu: Failed to write data sm response: " + e, Logger.LOG_ERROR);
                }
            }

        case SMPPCMD_DELIVER_SM_RESP:
        case SMPPCMD_BIND_TRANSCEIVER:
        case SMPPCMD_BIND_RECEIVER:
        case SMPPCMD_BIND_TRANSMITTER:
        case SMPPCMD_QUERY_SM:
        case SMPPCMD_SUBMIT_SM:
        case SMPPCMD_REPLACE_SM:
        case SMPPCMD_CANCEL_SM:
        case SMPPCMD_SUBMIT_MULTI:
        default:
            handleBadPdu(pdu);
        }
    }

    /**
     * handleNack is called when a generic NACK is received. This means that synchronisation of sequence numbers with the SMSC is lost.
     * Best to close the connection.
     * <p>
     * For asynchronous mode, this method is called by the SmscReader thread.  Delegate unbind to the request sender thread.
     * @param pdu - the generic NACK PDU.
     */
    protected void handleNack(SMPP_PDU pdu) {
        //Something is wrong with the sequence numbers, restart
        log.logString("SMPPComAsynchronous.rebindOrDisconnect: Received NACK from SMSC, will unbind.",Logger.LOG_ERROR);
        getRequestSenderThread().signalDelegatedUnbind();
    }

    /** 
     * Function to re-bind if the bind is lost to the smsc.
     * This can only be done once every MIN_TIME_BEFORE_NEXT_REBIND to prevent NTF from getting into a loop of continually re-binding.
     * Unlikely, but this seems to be what is happening with some smsc.
     * <p>
     * For asynchronous mode, this method is called by the SmscReader thread.  Delegate bind/unbind to the request sender thread.
     */
    protected void rebindOrDisconnect() {

        if (earliestNextReBindTime > System.currentTimeMillis()) {
            log.logString("SMPPComAsynchronous.rebindOrDisconnect: Lost Bind to SMSC, unable to re-bind - too soon since last time, disconecting from SMS-C",Logger.LOG_ERROR);
            getRequestSenderThread().signalDelegatedUnbind();
        }
        else {
            log.logString("SMPPComAsynchronous.rebindOrDisconnect: Lost Bind to SMSC, will re-bind.",Logger.LOG_ERROR);
            earliestNextReBindTime = System.currentTimeMillis() + MIN_TIME_BEFORE_NEXT_REBIND + Config.getSmscTimeout() * 1000;
            getRequestSenderThread().signalDelegatedBind();
        }
    }

    /**
     * Returns the response PDU object that will be used to parse the body of a received response.
     * @param respPduCommandId the command id of the received response
     * @return SMPP_PDU response object
     */
    protected SMPP_PDU getTargetResponsePdu(int respPduCommandId) {
        SMPP_PDU targetResponsePdu = null;
        switch(respPduCommandId) {
            case SMPPCMD_DATA_SM_RESP:
                targetResponsePdu = dataSmRespFromSmsc;
                break;
            case SMPPCMD_SUBMIT_SM_RESP:
                targetResponsePdu = submitResp;
                break;
            case SMPPCMD_CANCEL_SM_RESP:
                targetResponsePdu = cancelResp;
                break;
            case SMPPCMD_ENQUIRE_LINK_RESP:
                targetResponsePdu = enquireRespFromSmsc;
                break;
        }
        return targetResponsePdu;
    }
    
    
    private void handleAsynchronousReceivedResponse(SMPP_PDU pduResp) {
        int pduSequenceNumber = pduResp.getSequenceNumber();

        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("SMPPComAsynchronous.handleAsynchronousReceivedResponse: response " + pduSequenceNumber + ", command id " + pduResp.getCommandId() 
                    + ": status 0x" + Integer.toHexString(pduResp.getCommandStatus()), Logger.LOG_DEBUG);
        }
        
        AsynchrononusPduResponseHandler pduRespHandler = removeWaitingResponseHandler(pduSequenceNumber);
        if(pduRespHandler != null) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.handleAsynchronousReceivedResponse: Response handler retrieved for received response " + pduResp.getCommandId()
                        + "/" + pduResp.getSequenceNumber() + ": " + pduRespHandler.toString(), Logger.LOG_DEBUG);
            }
            
            if(pduResp.getCommandId() == pduRespHandler.getExpectedRespPduCommandId()) {
                SMPP_PDU targetRespPdu = getTargetResponsePdu(pduRespHandler.getExpectedRespPduCommandId());
                targetRespPdu.parseBody(pduResp);
                targetRespPdu.incrementCounterReceived();
                targetRespPdu.decrementNbPendingReqs();
                
                switch (targetRespPdu.getCommandId()) {
                    case SMPPCMD_SUBMIT_SM_RESP:
                    case SMPPCMD_DATA_SM_RESP:
                        handleSendShortMessageResponse(pduRespHandler, targetRespPdu);
                        break;
                    case SMPPCMD_ENQUIRE_LINK_RESP:
                        handleResponse(pduRespHandler, targetRespPdu);
                        break;
                    case SMPPCMD_CANCEL_SM_RESP:
                        handleResponse(pduRespHandler, targetRespPdu);
                        break;
                }      
            }
            else {
                String errorText = "Expected pduResp command id/sequence number "
                        + pduRespHandler.getExpectedRespPduCommandId() + "/" + pduSequenceNumber
                        + " but received " + pduResp.getCommandId()
                        + "/" + pduResp.getSequenceNumber();
                log.logString("SMPPComAsynchronous.handleAsynchronousReceivedResponse: : " + errorText, Logger.LOG_ERROR);
                sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                getRequestSenderThread().signalDelegatedUnbind();
            }
            
        }
        else {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.handleAsynchronousReceivedResponse: No response handler waiting for received response: " + pduResp.getCommandId()
                        + "/" + pduResp.getSequenceNumber(), Logger.LOG_DEBUG);
            }
        }
    }
    
    
    private void handleResponse(AsynchrononusPduResponseHandler pduRespHandler, SMPP_PDU respPdu) {
        if (respPdu.getCommandStatus() != SMPPSTATUS_ROK && errCodeAction != IGNORE_ERRORS && !isErrorCodeIgnored(respPdu)) {
            try {
                handleProtocolErrors(respPdu, pduRespHandler.getServiceType(), pduRespHandler.getPduRequestInfo());
            } catch (SMSComException e) {
                log.logString("SMPPComAsynchronous.handlePollResponse: Failed on connection " + this + ", due to exception "  + e, Logger.LOG_ERROR);
            }
        }        
    }
    
    private void handleSendShortMessageResponse(AsynchrononusPduResponseHandler pduRespHandler, SMPP_PDU respPdu) {
        
        if (respPdu.getCommandStatus() == SMPPSTATUS_ROK || errCodeAction == IGNORE_ERRORS || isErrorCodeIgnored(respPdu)) {
            sendSMSResultHandlerOk(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_OK + Integer.toHexString(respPdu.getCommandStatus()));
            }
        }
        else {
            try {
                handleProtocolErrorsForSendShortMessage(respPdu, pduRespHandler.getDestinationAddress(), pduRespHandler.getSourceAddress(), 
                        pduRespHandler.getSMSMessage(), pduRespHandler.getPduRequestInfo());
                sendSMSResultHandlerOk(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_OK + Integer.toHexString(respPdu.getCommandStatus()));
                }
                
            } catch (SMSComConnectionException e) {
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: Message not sent because " + e, Logger.LOG_ERROR);
                sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_RETRY + Integer.toHexString(respPdu.getCommandStatus()));
                }

            } catch (SMSComConfigException e) {
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: Config of SMS-C communication needs correction. " + e, Logger.LOG_ERROR);
                sendSMSResultHandlerFailed(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_FAIL + Integer.toHexString(respPdu.getCommandStatus()));
                }

            } catch (SMSComDataException e) {
                // This message is bad and can not be retried.
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: Discarding bad message. " + e, Logger.LOG_ERROR);
                sendSMSResultHandlerFailed(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_FAIL + Integer.toHexString(respPdu.getCommandStatus()));
                }

            } catch (SMSComLoadException e) {
                // Overload in the SMS-C.
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: SMS-C overloaded. " + e, Logger.LOG_VERBOSE);
                sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_RETRY + Integer.toHexString(respPdu.getCommandStatus()));
                }
                
            } catch (SMSComPhoneOffException e) {
                // Phone is off!
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: phone off. " + e, Logger.LOG_VERBOSE);
                sendSMSResultHandlerWaitPhoneOn(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_WPOn + Integer.toHexString(respPdu.getCommandStatus()));
                }

            } catch (SMSComException e) {
                log.logString("SMPPComAsynchronous.handleSendShortMessageResponse: Unknown problem " + e, Logger.LOG_ERROR);
                sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {                
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_RETRY + Integer.toHexString(respPdu.getCommandStatus()));
                }
            }
        }
    }

    private void sendSMSResultHandlerOk(SMSResultHandler smsResultHandler, int smsRequestId) {
        if(smsResultHandler != null) {
            smsResultHandler.oneOk(smsRequestId);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.sendSMSResultHandlerOk: Sent oneOk for request id " + smsRequestId, Logger.LOG_DEBUG);
            }
        }
    }
    
    private void sendSMSResultHandlerRetry(SMSResultHandler smsResultHandler, int smsRequestId) {
        if(smsResultHandler != null) {
            smsResultHandler.oneRetry(smsRequestId);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.sendSMSResultHandlerRetry: Sent oneRetry for request id " + smsRequestId, Logger.LOG_DEBUG);
            }
        }
    }
    
    private void sendSMSResultHandlerWaitPhoneOn(SMSResultHandler smsResultHandler, int smsRequestId) {
        if(smsResultHandler != null) {
             smsResultHandler.waitForPhoneOn(smsRequestId);
             if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.sendSMSResultHandlerWaitPhoneOn: Sent waitForPhoneOn for request id " + smsRequestId, Logger.LOG_DEBUG);
             }
        }
    }
    
    private void sendSMSResultHandlerFailed(SMSResultHandler smsResultHandler, int smsRequestId) {
        if(smsResultHandler != null) {
            smsResultHandler.oneFailed(smsRequestId);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComAsynchronous.sendSMSResultHandlerFailed: Sent oneFailed for request id " + smsRequestId, Logger.LOG_DEBUG);
            }
        }
    }
    
    private class AsynchrononusPduResponseHandler {
        
        private static final String PROFILER_CHECKPOINT_NAME_RESP_DELAY_DATA_SM = "NTF.Trace.SMPP.ResponseDelay.DataSm";
        private static final String PROFILER_CHECKPOINT_NAME_RESP_DELAY_SUBMIT_SM = "NTF.Trace.SMPP.ResponseDelay.SubSm";
        private static final String PROFILER_CHECKPOINT_NAME_RESP_DELAY_POLL = "NTF.Trace.SMPP.ResponseDelay.Poll";
        private static final String PROFILER_CHECKPOINT_NAME_RESP_DELAY_CANCEL = "NTF.Trace.SMPP.ResponseDelay.Canc";
        private Object responseDelayProfilingObject = null;
        
        int expectedRespPduCommandId;
        
        //Used for send short message responses
        int smsRequestId = -1;
        SMSResultHandler smsResultHandler = null;
                
        //Variables that can be used for error handling messages
        String sourceAddress = null;
        String destinationAddress = null;
        SMSMessage message = null;
        String serviceType = null;
        String requestPduInfo = null;
        
        AsynchrononusPduResponseHandler(int expectedRespPduCommandId, int smsRequestId, SMSResultHandler smsResultHandler, 
                String sourceAddress, String destinationAddress, SMSMessage message, String requestPduInfo) {
            
            this.expectedRespPduCommandId = expectedRespPduCommandId;
            this.smsResultHandler = smsResultHandler;
            this.smsRequestId = smsRequestId;
            this.sourceAddress = sourceAddress;
            this.destinationAddress = destinationAddress;
            this.message = message;
            this.requestPduInfo = requestPduInfo;
        }
        
        AsynchrononusPduResponseHandler(int expectedRespPduCommandId, String serviceType, String requestPduInfo) {
            this.expectedRespPduCommandId = expectedRespPduCommandId;   
            this.requestPduInfo = requestPduInfo;        
            this.serviceType = serviceType; 
        }
                
        public int getExpectedRespPduCommandId() {
            return expectedRespPduCommandId;
        }
        
        public int getSmsRequestId() {
            return smsRequestId;
        }
        
        public SMSResultHandler getResultHandler() {
            return smsResultHandler;
        }
        
        public String getSourceAddress() {
            return sourceAddress;
        }
        
        public String getDestinationAddress() {
            return destinationAddress;
        }
        
        public SMSMessage getSMSMessage() {
            return message;
        }

        public String getServiceType() {
            return serviceType;
        }        

        public String getPduRequestInfo() {
            return requestPduInfo;
        }
        
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("smsRequestId: ").append(smsRequestId);
            buffer.append(", expectedRespPduCommandId: ").append(expectedRespPduCommandId);
            buffer.append(", smsResultHandler: ").append(smsResultHandler);
            buffer.append(", sourceAddress: ").append(sourceAddress);
            buffer.append(", destinationAddress: ").append(destinationAddress);
            buffer.append(", message: ").append(message);
            buffer.append(", serviceType: ").append(serviceType);
            buffer.append(", requestPduInfo: ").append(requestPduInfo);
            return buffer.toString();
        }
        
        public void enterResponseDelayProfilingCheckPoint() {
            String checkPoint = null;
            switch(getExpectedRespPduCommandId()) {
                case SMPPCMD_DATA_SM_RESP:
                    checkPoint = PROFILER_CHECKPOINT_NAME_RESP_DELAY_DATA_SM;
                    break;
                case SMPPCMD_SUBMIT_SM_RESP:
                    checkPoint = PROFILER_CHECKPOINT_NAME_RESP_DELAY_SUBMIT_SM;
                    break;
                case SMPPCMD_ENQUIRE_LINK_RESP:
                    checkPoint = PROFILER_CHECKPOINT_NAME_RESP_DELAY_POLL;
                    break;
                case SMPPCMD_CANCEL_SM_RESP:
                    checkPoint = PROFILER_CHECKPOINT_NAME_RESP_DELAY_CANCEL;
                    break;
            }
            if(checkPoint != null) {
                responseDelayProfilingObject = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
            }
        }
        
        public void exitResponseDelayProfilingCheckPoint() {
            if(responseDelayProfilingObject != null) {
                CommonOamManager.profilerAgent.exitCheckpoint(responseDelayProfilingObject);
            }
        }
    }
    
    
    private class AsynchronousResponseTimeoutTask extends TimerTask {
        int pduSequenceNumber;
        
        public AsynchronousResponseTimeoutTask(int pduSequenceNum) {
            pduSequenceNumber = pduSequenceNum;
        }

        @Override
        public void run() {          
            AsynchrononusPduResponseHandler pduRespHandler = removeWaitingResponseHandler(pduSequenceNumber);            
            if(pduRespHandler != null) {
                if (willLog(Logger.LOG_DEBUG)) {
                    log.logString("AsynchronousResponseTimeoutTask.run: Response handler retrieved for sequence number " + pduSequenceNumber + ": " 
                            + pduRespHandler.toString(), Logger.LOG_DEBUG);
                }
                sendSMSResultHandlerRetry(pduRespHandler.getResultHandler(), pduRespHandler.getSmsRequestId());
                if(POLLING_SERVICE_TYPE.equals(pduRespHandler.getServiceType())) {
                    getRequestSenderThread().signalDelegatedUnbind();
                }
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_RESP_TIMEOUT);
                }
            }
            else {
                if (willLog(Logger.LOG_DEBUG)) {
                    log.logString("AsynchronousResponseTimeoutTask.run: No response handler waiting for sequence number " + pduSequenceNumber, Logger.LOG_DEBUG);
                }
            }
        }        
    }
    
    
}
