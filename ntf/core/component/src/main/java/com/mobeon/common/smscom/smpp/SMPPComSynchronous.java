package com.mobeon.common.smscom.smpp;

import java.io.IOException;

import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComConnectionException;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSComReBindException;
import com.mobeon.common.smscom.SMSMessage;


/**
 * SMPPComSynchronous is an implementation of SMSCom for the SMPP protocol.
 * 
 * <P>
 * <TABLE BGCOLOR="#FFF0E0" CELLPADDING=10 WIDTH=50% BORDER=1><TR><TD>
 * This class shall not be used directly. You use it indirectly
 * through SMSCom. This means that all
 * documentation of this class and other classes in the smpp package is for
 * implementors, you do not need it to use the API.
 * </TD></TR></TABLE>
 * <P>
 * The requests to the SMSC are synchronous in that this class waits for the
 * response from the SMSC for the current request before sending another request.
 */
public class SMPPComSynchronous extends SMPPCom {


    /**
     * Submits a message to the SMSC for sending to a receiver.
     *@param adr - the address of the receiver.
     *@param org - the address of the sender.
     *@param msg - the message to send.
     *@param rh - the result handler; not used here since this class is for synchronous mode. 
     *@throws SMSComException if the SMSC rejects a request or the communication fails.
     */
    protected void protocolSend(SMSAddress adr, SMSAddress org, SMSMessage msg, int smsRequestId, SMSResultHandler rh) throws SMSComException {
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous sending message to " + adr + " from " + org + " message: "+msg, Logger.LOG_DEBUG);
            }

            SendShortMessagePDU sendShortMsgReq = getShortMessagePdu(msg);
            writeBuffer(sendShortMsgReq.getBuffer(adr, org, msg), sendShortMsgReq);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous submit " + sendShortMsgReq.getSequenceNumber(), Logger.LOG_DEBUG);
            }

            SMPP_PDU shortMessageResponsePdu = getShortMessageResponsePdu(sendShortMsgReq.getCommandId());
            waitForResponse(shortMessageResponsePdu, shortMessageResponsePdu.getCommandId(),
                            sendShortMsgReq.getSequenceNumber(), getShortMessagePduOperationTimeout(sendShortMsgReq.getCommandId()));
            setState(SMSCOM_WAITING);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous submit response " + shortMessageResponsePdu.getSequenceNumber()
                              + " status 0x" + Integer.toHexString(shortMessageResponsePdu.getCommandStatus()),
                              Logger.LOG_DEBUG);
            }
            if (shortMessageResponsePdu.getCommandStatus() != SMPPSTATUS_ROK
                && errCodeAction != IGNORE_ERRORS && !isErrorCodeIgnored(shortMessageResponsePdu)) {
                handleProtocolErrorsForSendShortMessage(shortMessageResponsePdu, adr.toString(), org.toString(), msg, sendShortMsgReq.toString());
            }
        } catch (IOException e) {
            throw new SMSComConnectionException("SMPPComSynchronous failed to send message. " + e.getMessage()+" source: "+org+" dest: "+adr+" message: "+msg);
        }
    }

    /**
     * Cancels messages in the SMSC by the given from address, to address and optional serviceType.
     *@param to - the address of the receiver.
     *@param from - the address of the sender.
     *@param serviceType - the service type to cancel (from replace table).
     *@throws SMSComException if the SMSC rejects a request or the communication fails.
     */
    protected void protocolCancel(SMSAddress to,
                                SMSAddress from,
                                String serviceType) throws SMSComException {
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous canceling messages to " + to
                              + " from " + from + "with serviceType " + serviceType, Logger.LOG_DEBUG);
            }

            writeBuffer(cancelReq.getBuffer(to, from, serviceType), cancelReq);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous submit " + cancelReq.getSequenceNumber(),
                              Logger.LOG_DEBUG);
            }
            waitForResponse(cancelResp, SMPPCMD_CANCEL_SM_RESP,
                            cancelReq.getSequenceNumber());
            setState(SMSCOM_WAITING);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous cancel response " + cancelResp.getSequenceNumber()
                              + " status 0x" + Integer.toHexString(cancelResp.getCommandStatus()),
                              Logger.LOG_DEBUG);
            }
            if (cancelResp.getCommandStatus() != SMPPSTATUS_ROK
                && errCodeAction != IGNORE_ERRORS && !isErrorCodeIgnored(cancelResp)) {
                log.logString("Cancel request to " + to + " failed with error code " +
                        Integer.toHexString(cancelResp.getCommandStatus()), Logger.LOG_VERBOSE);
                handleProtocolErrors(cancelResp, serviceType, cancelReq.toString());
            }
        } catch (IOException e) {
            throw new SMSComConnectionException("SMPPComSynchronous failed to send message. " + e.getMessage());
        }
    }


    protected boolean protocolPoll(boolean retryIfRebind) {
        try {
            writeBuffer(enquireReqToSmsc.getBuffer(), enquireReqToSmsc);
            waitForResponse(enquireRespFromSmsc,
                            SMPPCMD_ENQUIRE_LINK_RESP,
                            enquireReqToSmsc.getSequenceNumber());
            setState(SMSCOM_WAITING);

            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPComSynchronous submit response (poll request) " + enquireRespFromSmsc.getSequenceNumber()
                              + " status 0x" + Integer.toHexString(enquireRespFromSmsc.getCommandStatus()),
                              Logger.LOG_DEBUG);
            }
            if (enquireRespFromSmsc.getCommandStatus() != SMPPSTATUS_ROK
                && errCodeAction != IGNORE_ERRORS && !isErrorCodeIgnored(enquireRespFromSmsc)) {
                handleProtocolErrors(enquireRespFromSmsc, POLLING_SERVICE_TYPE, enquireReqToSmsc.toString());
                return true;
            }

            return enquireRespFromSmsc.getCommandStatus() == SMPPSTATUS_ROK;

        } catch (IOException e) {
            close();
        } catch (SMSComReBindException e){
            //the connection has rebound retry once
            if (retryIfRebind)
            {
                log.logString("Poll failed(unBound error), retrying once.",
                        Logger.LOG_ERROR);
                return(protocolPoll(false));
            }
            else
            {
                 log.logString("Poll Failed on connection " + this + " 2nd bind Error on poll retry!",
                         Logger.LOG_ERROR);
            }
        } catch (SMSComException e) {
            // This message is bad and can not be retried.
            if (retryIfRebind){
                log.logString("Poll Failed on connection " + this + ", due to exception "  + e,
                        Logger.LOG_ERROR);
            }
            else
            {
                log.logString("2nd poll attempt on connection " + this + " failed, due to exception "  + e +
                        " probably due to forced disconnect on second failure.",
                        Logger.LOG_ERROR);
            }
            return false;
        }

        return (false);
    }   

    
    /**
     * handlePdu handles PDUs received from the SMSC.
     *@param pdu the received PDU. The header is already parsed, but the
     * body is not.
     */
    protected void handleReceivedPdu(SMPP_PDU pdu) {
        switch (pdu.getCommandId()) {
        case SMPPCMD_BIND_RECEIVER_RESP:
        case SMPPCMD_BIND_TRANSMITTER_RESP:
        case SMPPCMD_QUERY_SM_RESP:
        case SMPPCMD_SUBMIT_SM_RESP:
        case SMPPCMD_UNBIND_RESP:
        case SMPPCMD_REPLACE_SM_RESP:
        case SMPPCMD_CANCEL_SM_RESP:
        case SMPPCMD_BIND_TRANSCEIVER_RESP:
        case SMPPCMD_ENQUIRE_LINK_RESP:
        case SMPPCMD_SUBMIT_MULTI_RESP:
        case SMPPCMD_DATA_SM_RESP:
            handleReceivedResponse(pdu);
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
        case SMPPCMD_DATA_SM:
            handleReceivedUnusedPdu(pdu);
            //Break left out intentionally because a nack pdu must still be sent.
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
     * handleResponse handles PDUs that are responses.
     *@param pdu - the received response PDU.
     */
    private void handleReceivedResponse(SMPP_PDU pdu) {
        switch (pdu.getCommandId()) {
        case SMPPCMD_BIND_RECEIVER_RESP:
        case SMPPCMD_BIND_TRANSMITTER_RESP:
        case SMPPCMD_SUBMIT_SM_RESP:
        case SMPPCMD_BIND_TRANSCEIVER_RESP:
        case SMPPCMD_ENQUIRE_LINK_RESP:
        case SMPPCMD_DATA_SM_RESP:
        case SMPPCMD_CANCEL_SM_RESP:
        case SMPPCMD_UNBIND_RESP:
            handleSynchronousReceivedResponse(pdu);
            break;

        case SMPPCMD_QUERY_SM_RESP:
        case SMPPCMD_DELIVER_SM_RESP:
        case SMPPCMD_REPLACE_SM_RESP:
        case SMPPCMD_SUBMIT_MULTI_RESP:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Unexpected response: " + pdu, Logger.LOG_DEBUG);
            }
        }
    }


    /**
     * handleUnusedPdu is called if we receive a PDU that we do not use. Respond
     * if necessary, but ignore it otherwise.
     *@param pdu - the offending PDU.
     */
    protected void handleReceivedUnusedPdu(SMPP_PDU pdu) {
        switch(pdu.getCommandId()) {
        case SMPPCMD_OUTBIND:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Received Outbind: " + pdu, Logger.LOG_DEBUG);
            }
            break;
        case SMPPCMD_ALERT_NOTIFICATION:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Received Alert Notification: " + pdu, Logger.LOG_DEBUG);
            }
            break;
        case SMPPCMD_DATA_SM:
            dataSmReqFromSmsc.parseBody(pdu);
            dataSmReqFromSmsc.incrementCounterReceived();
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Received Data SM: " + dataSmReqFromSmsc, Logger.LOG_DEBUG);
            }
            dataSmRespToSmsc.setSequenceNumber(pdu.getSequenceNumber());
            dataSmRespToSmsc.setCommandStatus(SMPPSTATUS_ROK);
            try {
                writeBuffer(dataSmRespToSmsc.getBuffer(), dataSmRespToSmsc);
            } catch (IOException e) {
                if (willLog(Logger.LOG_DEBUG)) {
                    log.logString("Failed to write data sm response: " + e, Logger.LOG_ERROR);
                }
            }
            break;
        }
    }
}
