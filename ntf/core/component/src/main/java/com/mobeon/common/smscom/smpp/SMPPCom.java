/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.io.IOException;
import java.util.HashMap;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierSmppPduType;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.SMSComConnectionException;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.smscom.smpp.SMPP_PDU.MSAvailabilityStatusEventTypes;
import com.mobeon.common.smscom.smpp.SMPP_PDU.MessageStateActionTypes;
import com.mobeon.common.smscom.smpp.SMPP_PDU.MessageStateTypes;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.util.NtfUtil;

/**
 * SMPPCom is an implementation of SMSCom for the SMPP protocol.
 * <P>
 * Most PDUs are implemented to handle only one direction, either to or from the SMS-C. The following PDUs are implemented:
 * <TABLE BORDER=1>
 * <TR>
 * <TH>PDU</TH>
 * <TH>to SMS-C</TH>
 * <TH>from SMS-C</TH>
 * </TR>
 * <TR>
 * <TD>Bind Transceiver</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Bind Transceiver Response</TD>
 * <TD>&nbsp;</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Bind Transmitter</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Bind Transmitter Response</TD>
 * <TD>&nbsp;</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Submit SM</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Submit SM Response</TD>
 * <TD>&nbsp;</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Data SM</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Data SM Response</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Deliver SM</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Deliver SM Response</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Unbind</TD>
 * <TD>X</TD>
 * <TD>&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD>Unbind Response</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Enquire Link</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Enquire Link Response</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * <TR>
 * <TD>Generic Nack</TD>
 * <TD>X</TD>
 * <TD>X</TD>
 * </TR>
 * </TABLE>
 * To send a PDU to the SMS-C, you create an instance of the wanted PDU class and call getBuffer with PDU-specific parameters. This
 * gives you a byte array that can be sent to the SMS-C. The getBuffer method writes parameters into the buffer through the
 * DataOutput interface.
 * <P>
 * To handle a PDU from the SMS-C, you use the read() method which reads the PDU length and then the entire PDU, and parses the
 * header. You then parse the body with the parseBody() method and access parameters with PDU-specific get methods.
 */
public abstract class SMPPCom extends SMSCom implements SmppConstants {

    protected final static String POLLING_SERVICE_TYPE = "Polling";
    // private final static int MS_AVAILABILITY_STATUS_AVAILABLE = 0;

    private BindPDU bindReqToSmsc; // May be either transmitter or transceiver
    private BindRespPDU bindRespFromSmsc;// May be either transmitter or transceiver
    protected DataSmPDU dataSmReqToSmsc;
    protected DataSmRespPDU dataSmRespFromSmsc;
    protected SubmitSmPDU submitReq;
    protected SubmitSmRespPDU submitResp;
    protected CancelSmPDU cancelReq;
    protected SMPP_PDU cancelResp;
    protected EnquireLinkPDU enquireReqToSmsc;
    protected EnquireLinkRespPDU enquireRespFromSmsc;
    protected UnbindPDU unbindReqToSmsc;
    private GenericNackPDU nackToSmsc;

    // For requests initiated by SMSC
    protected DataSmPDU dataSmReqFromSmsc;
    protected DataSmRespPDU dataSmRespToSmsc;
    private DeliverSmPDU deliverReq;
    private DeliverSmRespPDU deliverResp;
    private EnquireLinkRespPDU enquireRespToSmsc;
    private UnbindRespPDU unbindRespToSmsc;

    protected SMPP_PDU response = null;

    private boolean[] errorCodesIgnored;
    private int sequenceNumber = 1;
    protected Object sequenceLock = new Object(); // used to make sure that newly generated sequence numbers are not unique.

    // For alert(MS available) Notification sent by SMSc
    private AlertNotificationSmPDU alertNotificationSm;

    // used to make sure that a new re-bind is not made in a short period
    // Otherwise NTF can go crazy
    static final int MIN_TIME_BEFORE_NEXT_REBIND = 5 * 1000; // 5 seconds in millis.
    long earliestNextReBindTime = 0;

    private int version = 0x34;

    public enum DeliveryState {
        DELIVERED, EXPIRED, DELETED, UNDELIVERABLE, ACCEPTED, UNKNOWN, REJECTED
    }

    private HashMap<String, DeliveryState> stateMap = new HashMap<String, DeliveryState>();

    /**
     * Generates a unique sequence number for each SMPP PDU, starting from 1.
     * 
     * @return the next sequence number.
     */
    public int nextSequenceNumber() {
        synchronized (sequenceLock) {
            return sequenceNumber++;
        }
    }

    /**
     * Constructor.
     */
    public SMPPCom() {
        super();
        protocol = "SMPP";
        errorCodesIgnored = new boolean[256];

        bindReqToSmsc = new BindTransmitterPDU(this); // May be changed later to another bind request
        bindRespFromSmsc = new BindTransmitterRespPDU(this); // May be changed later to another bind request
        dataSmReqToSmsc = new DataSmPDU(this);
        dataSmRespFromSmsc = new DataSmRespPDU(this);
        submitReq = new SubmitSmPDU(this);
        submitResp = new SubmitSmRespPDU(this);
        cancelReq = new CancelSmPDU(this);
        cancelResp = new CancelSmRespPDU(this);
        enquireReqToSmsc = new EnquireLinkPDU(this);
        enquireRespFromSmsc = new EnquireLinkRespPDU(this);
        unbindReqToSmsc = new UnbindPDU(this);
        nackToSmsc = new GenericNackPDU(this);

        // For requests initiated by SMSC
        deliverReq = new DeliverSmPDU(this);
        deliverResp = new DeliverSmRespPDU(this);
        enquireRespToSmsc = new EnquireLinkRespPDU(this);
        unbindRespToSmsc = new UnbindRespPDU(this);
        dataSmReqFromSmsc = new DataSmPDU(this);
        dataSmRespToSmsc = new DataSmRespPDU(this);

        // For alert(MS available) Notification sent by SMSc
        alertNotificationSm = new AlertNotificationSmPDU(this);

        stateMap.put("DELIVRD", DeliveryState.DELIVERED);
        stateMap.put("EXPIRED", DeliveryState.EXPIRED);
        stateMap.put("DELETED", DeliveryState.DELETED);
        stateMap.put("UNDELIV", DeliveryState.UNDELIVERABLE);
        stateMap.put("ACCEPTD", DeliveryState.ACCEPTED);
        stateMap.put("UNKNOWN", DeliveryState.UNKNOWN);
        stateMap.put("REJECTD", DeliveryState.REJECTED);
    }

    /**
     * logIn performs an SMPP bind operation. If bad configuration makes log in impossible, it throws an SMSComConfigException.
     */
    protected boolean protocolLogin() {
        if (willLog(Logger.LOG_VERBOSE)) {
            log.logString("SMPPCom logging in", Logger.LOG_VERBOSE);
        }
        try {
            if (isSend() && !isReceive()) {
                bindReqToSmsc = new BindTransmitterPDU(this);
                bindRespFromSmsc = new BindTransmitterRespPDU(this);
            } else if(!isSend() && isReceive()) {
                bindReqToSmsc = new BindReceiverPDU(this);
                bindRespFromSmsc = new BindReceiverRespPDU(this);
            } else if(isSend() && isReceive()) {
                bindReqToSmsc = new BindTransceiverPDU(this);
                bindRespFromSmsc = new BindTransceiverRespPDU(this);
            } else {
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("SMPPCom login request not sent: SMPPCom set to not sending and not receiving from " + hostname, Logger.LOG_ERROR);
                }
                return false;
            }
            writeBuffer(bindReqToSmsc.getBuffer(), bindReqToSmsc);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPCom bind " + bindReqToSmsc.getSequenceNumber(), Logger.LOG_DEBUG);
            }
            waitForResponse(bindRespFromSmsc, bindRespFromSmsc.getCommandId(), bindReqToSmsc.getSequenceNumber());
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString(
                        "SMPPCom bind response " + bindRespFromSmsc.getSequenceNumber() + " status 0x"
                                + Integer.toHexString(bindRespFromSmsc.getCommandStatus()), Logger.LOG_DEBUG);
            }

            switch (bindRespFromSmsc.getCommandStatus()) {
                case SMPPSTATUS_ROK:
                case SMPPSTATUS_RALYBND:
                    if (willLog(Logger.LOG_VERBOSE)) {
                        log.logString("SMPPCom bound to " + bindRespFromSmsc.getSystemId() + " on " + hostname + " as " + user
                                + ", version 0x" + Integer.toHexString(bindRespFromSmsc.getInterfaceVersion()), Logger.LOG_VERBOSE);
                    }
                    setState(SMSCOM_SENDING);
                    return true;

                case SMPPSTATUS_RINVPASWD:
                    if (willLog(Logger.LOG_ERROR)) {
                        log.logString(
                                errorCodeMsg(bindRespFromSmsc.getCommandStatus(), "(invalid password) when binding to " + hostname),
                                Logger.LOG_ERROR);
                    }
                    break;
                case SMPPSTATUS_RINVSYSID:
                    if (willLog(Logger.LOG_ERROR)) {
                        log.logString(
                                errorCodeMsg(bindRespFromSmsc.getCommandStatus(), "(invalid user name) when binding to " + hostname),
                                Logger.LOG_ERROR);
                    }
                    break;
                default:
                    if (willLog(Logger.LOG_ERROR)) {
                        log.logString(errorCodeMsg(bindRespFromSmsc.getCommandStatus(), "when binding to " + hostname),
                                Logger.LOG_ERROR);
                    }
                    break;
            }
        } catch (IOException e) {
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("SMPPCom failed " + e, Logger.LOG_VERBOSE);
            }
        } catch (SMSComException e) {
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("SMPPCom failed " + e, Logger.LOG_VERBOSE);
            }
        }
        return false;
    }

    /**
     * Logs out from the SMS-C.
     */
    protected void protocolLogout() {
        byte[] buf = unbindReqToSmsc.getBuffer();
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("SMPPCom unbind " + unbindReqToSmsc.getSequenceNumber(), Logger.LOG_DEBUG);
        }

        try {
            writeBuffer(buf, unbindReqToSmsc);
            waitForResponse(unbindReqToSmsc, unbindReqToSmsc.getCommandId(), unbindReqToSmsc.getSequenceNumber(), 1000);
        } catch (IOException e) {
            disconnected();
            return;
        } catch (SMSComException e) {
            // error when logging out
            // ignore;
        }
        setState(SMSCOM_DISCONNECTING);
    }

    /**
     * Function to re-bind if the bind is lost to the smsc. This can only be done once every MIN_TIME_BEFORE_NEXT_REBIND to prevent
     * NTF from getting into a loop of continually re-binding. Unlikely, but this seems to be what is happening with some smsc.
     */
    protected void rebindOrDisconnect() {

        if (earliestNextReBindTime > System.currentTimeMillis()) {
            log.logString("Lost Bind to SMS-C!, unable to re-bind - to soon since last time, disconecting from SMS-C",
                    Logger.LOG_ERROR);
            close();
            return;
        } else {
            log.logString("Lost Bind to SMS-C!, re-binding....", Logger.LOG_ERROR);
            setState(SMSCOM_BINDING);
            earliestNextReBindTime = System.currentTimeMillis() + MIN_TIME_BEFORE_NEXT_REBIND;
            if (!protocolLogin()) {
                log.logString("Unable to re-bind, disconnecting from SMS-C.", Logger.LOG_ERROR);
                close();
            } else {
                setState(SMSCOM_SENDING);
            }
        }
    }

    /**
     * Sends an enquire link PDU and reads the response. All exceptions are swallowed and just result in a false return value.
     * 
     * @return true iff it is possible to communicate with the SMS-C.
     */
    protected boolean protocolPoll() {
        return (protocolPoll(true));
    }

    /**
     * Sends an enquire link PDU and reads the response. All exceptions are swallowed and just result in a false return value.
     * 
     * @param retryIfRebind
     *        if the poll failed because of a bind status error, retry poll if we are able to rebind sucessfully
     * @return true iff it is possible to communicate with the SMS-C.
     */
    protected abstract boolean protocolPoll(boolean retryIfRebind);

    /**
     * Handle the received PDU from SMSC.
     * 
     * @param pdu
     *        the received PDU from SMSC
     */
    protected abstract void handleReceivedPdu(SMPP_PDU pdu);

    /**
     * Returns the PDU operation object for sending short messages as per configuration.
     * 
     * @return SendShortMessagePDU object (submit_sm or data_sm)
     */
    protected SendShortMessagePDU getShortMessagePdu(SMSMessage msg) {

        String pduOperationString = System.getProperty("sendShortMessagePduOperation");

        // If a system property value is defined for "sendShortMessagePduOperation", this one as precedence.
        NotifierSmppPduType notifierSmppPduType; 
        if (pduOperationString != null && !pduOperationString.isEmpty()) {
            try {
            notifierSmppPduType = NotifierSmppPduType.valueOf(pduOperationString.toUpperCase());
            } catch(Exception e) {
                notifierSmppPduType = NotifierSmppPduType.SUBMIT_SM;
                log.logString("No map to notifierSmppPduType " + pduOperationString + ", " + notifierSmppPduType.toString() + " will be used)", Logger.LOG_DEBUG);
            }
        } else {
            notifierSmppPduType = msg.getNotifierSmppPduType();
        }

        SendShortMessagePDU sendPdu = null;
        if (NotifierSmppPduType.SUBMIT_SM.equals(notifierSmppPduType)) {
            sendPdu = submitReq;
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPCom.getShortMessagePDU: Returning submit_sm request object.", Logger.LOG_DEBUG);
            }
        } else if (NotifierSmppPduType.DATA_SM.equals(notifierSmppPduType)) {
            sendPdu = dataSmReqToSmsc;
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPCom.getShortMessagePDU: Returning data_sm request object.", Logger.LOG_DEBUG);
            }
        } else {
            sendPdu = submitReq;
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("SMPPCom.getShortMessagePDU: Unknown send short message PDU operation in config.  Returning default submit_sm request object.", Logger.LOG_DEBUG);
            }
        }
        return sendPdu;
    }

    /**
     * Returns the send short message response PDU operation object corresponding to the given request command id.
     * 
     * @param shortMessageReqCommandId
     *        the request command id
     * @return the corresponding response PDU object
     */
    protected SMPP_PDU getShortMessageResponsePdu(int shortMessageReqCommandId) {
        SMPP_PDU shortMessageRespPdu = null;
        switch (shortMessageReqCommandId) {
            case SMPPCMD_SUBMIT_SM:
                shortMessageRespPdu = submitResp;
                break;
            case SMPPCMD_DATA_SM:
                shortMessageRespPdu = dataSmRespFromSmsc;
                break;
        }
        return shortMessageRespPdu;
    }

    /**
     * Returns the timeout for the send short message PDU operation as per configuration.
     * 
     * @param shortMessagePduCommandId
     *        the request command id
     * @return the timeout in milliseconds
     */
    protected int getShortMessagePduOperationTimeout(int shortMessagePduCommandId) {
        int shortMessagePduTimeout = timeout;
        switch (shortMessagePduCommandId) {
            case SMPPCMD_SUBMIT_SM:
                shortMessagePduTimeout = Config.getSmscTimeoutSubmitSm() * 1000;
                break;
            case SMPPCMD_DATA_SM:
                shortMessagePduTimeout = Config.getSmscTimeoutDataSm() * 1000;
                break;
        }
        return shortMessagePduTimeout;
    }


    /**
     * Wrapper method to handle counter incrementation.
     * 
     * @param buf - the bytes to write.
     * @param pdu - the pdu that will have it's counter incremented, if no exception thrown.
     * @throws IOException if writing fails
     */
    protected void writeBuffer(byte[] buf, SMPP_PDU pdu) throws IOException {
        try {
            writeBuffer(buf);
            pdu.incrementCounterSent();
            pdu.incrementNbPendingReqs();
        } catch (IOException e) {
            throw e;
        }
    }
    
    /**
     * waitForResponse reads an expected response PDU request from the SMS-C. If a response other than the expected is received, the
     * connection is logged out and closed and an SMSCom exception is thrown.
     * 
     * @param pdu
     *        - the expected PDU to read.
     * @param expectedCommandId
     *        - the command id of the expected PDU type.
     * @param expectedSeqNo
     *        - the expected sequence number of the message.
     * @throws SMSComException
     *         if there is an error while waiting for the response
     */
    protected void waitForResponse(SMPP_PDU pdu, int expectedCommandId, int expectedSeqNo) throws SMSComException {
        waitForResponse(pdu, expectedCommandId, expectedSeqNo, timeout);
    }

    protected void waitForResponse(SMPP_PDU pdu, int expectedCommandId, int expectedSeqNo, int Timeout) throws SMSComException {

        synchronized (responseLock) {
            if (response == null) {
                try {
                    responseLock.wait(Timeout);
                } catch (InterruptedException e) {
                    ;
                }
            }
            if (state == SMSCOM_EXITING) {
                throw new SMSComConnectionException("SMPPCom exiting");
            }
            if (response == null) {
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("SMPPCom timeout waiting for response.", Logger.LOG_ERROR);
                }
                reset();
                throw new SMSComConnectionException("SMPPComSynchronous timeout waiting for response");
            }

            if (response.getCommandId() == expectedCommandId && response.getSequenceNumber() == expectedSeqNo) {
                pdu.parseBody(response);
                pdu.incrementCounterReceived();
                pdu.decrementNbPendingReqs();
                releaseResponse();
            } else {
                // Unrequested response
                String msg = "Expected response command id/sequence number " + expectedCommandId + "/" + expectedSeqNo
                        + " but received " + response.getCommandId() + "/" + response.getSequenceNumber();
                reset();
                throw new SMSComConnectionException(msg);
            }
        }
    }

    /**
     * releaseResponse releases response when the last response has been handled.
     */
    protected void releaseResponse() {
        synchronized (responseLock) {
            response = null;
            responseLock.notifyAll();
        }
    }

    /**
     * Releases the response, so it is not reserved even after the connection is closed.
     */
    protected void protocolCleanup() {
        releaseResponse();
    }

    /**
     * Reads a message from the SMSC and handles it.
     */
    protected void protocolReadAndHandleMessage() throws IOException {
        SMPP_PDU msg = new SMPP_PDU(this);
        if (msg.read(fromSmsc)) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Read message " + msg, Logger.LOG_DEBUG);
            }
            handleReceivedPdu(msg);
        }
    }

    protected void handleSynchronousReceivedResponse(SMPP_PDU pdu) {
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Handling response of expected type", Logger.LOG_DEBUG);
        }
        synchronized (responseLock) {
            while (response != null) {
                // Wait until previous response is consumed
                try {
                    responseLock.wait(timeout);
                } catch (InterruptedException e) {
                    // Nobody seems to want the response, release it.
                    if (willLog(Logger.LOG_VERBOSE)) {
                        log.logString(
                                "Discarding unwanted response PDU " + response.getCommandId() + "/" + response.getSequenceNumber(),
                                Logger.LOG_VERBOSE);
                    }
                    response = null;
                }
            }
            response = pdu;
            responseLock.notifyAll();
        }
    }

    /**
     * handleReceivedMessage handles deliver SM messages.
     * 
     * @param pdu
     *        - the deliver SM PDU.
     */
    protected void handleDeliverSmReq(SMPP_PDU pdu) {
        deliverReq.parseBody(pdu);
        deliverReq.incrementCounterReceived();
        
        if (getReceiptReceiver() != null) {          
            if (isDeliveredState(deliverReq, DeliveryState.DELIVERED)) {
                handleReceivedMessagePhoneOn();
            } else if (isDeliveredState(deliverReq, DeliveryState.EXPIRED)) {
                log.logString("Phone on expired for number: " + deliverReq.getSourceAddress() + ", Dropping it. ",
                        Logger.LOG_VERBOSE);
            } else {
                log.logString("Phone on received: " + deliverReq.getSourceAddress() + ", Dropping it. ", Logger.LOG_VERBOSE);
            }
        } else {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Unexpected delivery respone (phoneOn) for number: " + deliverReq.getSourceAddress() + " Resp: "
                        + deliverReq, Logger.LOG_DEBUG);
            }
        }
        deliverResp.setSequenceNumber(pdu.getSequenceNumber());
        deliverResp.setCommandStatus(SMPPSTATUS_ROK);
        try {
            writeBuffer(deliverResp.getBuffer(), deliverResp);
        } catch (IOException e) {
            if (willLog(Logger.LOG_ERROR)) {
                log.logString("Failed to write delivery respone (phoneOn): " + NtfUtil.stackTrace(e), Logger.LOG_ERROR);
            }
        }
    }

    /**
     * handleAlertNotificationMessage handles AlertNotification messages from SMSc
     * 
     * @param pdu
     *        - the AlertNotificationSm PDU
     */
    protected void handleAlertNotificationMessage(SMPP_PDU pdu) {
        alertNotificationSm.parseBody(pdu);
        alertNotificationSm.incrementCounterReceived();
        handleReceivedAlertNotificationPhoneOn();
    }

    /**
     * Validates the received MessageState value (if provided since optional - refer to SMPP 3.4, 5.3.2.35) from the DeliverySm
     * request and maps the action to take based on this.
     */
    private void handleReceivedMessagePhoneOn() {
        /**
         * Retrieving the action to take (from the configuration) based on the message state received in the DeliverySm request The
         * message state might be missing since this parameter is optional.
         */
        String configuredAction = Config.getSmsType0MessageState(deliverReq.getMessageState().getType());

        // Check if the PhoneOn request received must be propagated to the clients (Slamdown/Mcn, Outdial) or not.
        MessageStateActionTypes action = MessageStateActionTypes.mapToMessageStateActionTypes(configuredAction);

        log.logString("DeliverySm received (" + deliverReq.getMessageState().getName() + "), action " + action.getName(),
                Logger.LOG_DEBUG);

        if (action.getSendSmsType0ResponseToClient()) {
            PhoneOnEvent poe = new PhoneOnEvent(this, deliverReq.getSourceAddress().getNumber(), action.getPhoneOnEvent(),
                    action.getName());
            getReceiptReceiver().phoneOn(poe);
        }
    }

    /**
     * Handles enquire link requests from the SMSC.
     * 
     * @param pdu
     *        - the enquire link PDU.
     */
    protected void handleEnquireLinkReq(SMPP_PDU pdu) {
        
        EnquireLinkPDU enquireReqFromSmsc = new EnquireLinkPDU(this);
        enquireReqFromSmsc.parseBody(pdu);
        enquireReqFromSmsc.incrementCounterReceived();
        
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Handling Enquire_link", Logger.LOG_DEBUG);
        }
        enquireRespToSmsc.setSequenceNumber(pdu.getSequenceNumber());
        enquireRespToSmsc.setCommandStatus(SMPPSTATUS_ROK);
        try {
            writeBuffer(enquireRespToSmsc.getBuffer(), enquireRespToSmsc);
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write enquire link response: " + e, Logger.LOG_ERROR);
            }
        }
    }

    /**
     * Handles unbind requests from the SMSC by writing a response and closing the connection.
     * 
     * @param pdu
     *        - the unbind PDU.
     */
    protected void handleUnbindReq(SMPP_PDU pdu) {
        
        UnbindPDU unbindReqFromSmsc = new UnbindPDU(this);
        unbindReqFromSmsc.parseBody(pdu);
        unbindReqFromSmsc.incrementCounterReceived();
        
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Handling Unbind request from SMSC", Logger.LOG_DEBUG);
        }
        unbindRespToSmsc.setSequenceNumber(pdu.getSequenceNumber());
        unbindRespToSmsc.setCommandStatus(SMPPSTATUS_ROK);
        try {
            writeBuffer(unbindRespToSmsc.getBuffer(), unbindRespToSmsc);
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write unbind response: " + e, Logger.LOG_ERROR);
            }
        }
        setState(SMSCOM_DISCONNECTING);
        close();
    }

    /**
     * handleNack is called when a generic NACK is received. This means that synchronization of sequence numbers with the SMSC is
     * lost. Best to close the connection.
     * 
     * @param pdu
     *        - the generic NACK PDU.
     */
    protected void handleNack(SMPP_PDU pdu) {
        // Something is wrong with the sequence numbers, restart
        reset();
    }

    /**
     * handleBadPdu handles PDUs that the SMSC should not send at all by responding with a general NACK.
     * 
     * @param pdu
     *        - the offending PDU.
     */
    protected void handleBadPdu(SMPP_PDU pdu) {
        if (willLog(Logger.LOG_ERROR)) {
            log.logString("Received PDU the SMSC should never send: " + pdu, Logger.LOG_ERROR);
        }
        nackToSmsc.setCommandStatus(SMPPSTATUS_RINVCMDID);
        nackToSmsc.setSequenceNumber(pdu.getSequenceNumber());
        try {
            writeBuffer(nackToSmsc.getBuffer(), nackToSmsc);
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write data sm response: " + e, Logger.LOG_ERROR);
            }
        }
    }

    /**
     * Checks a delivered message to see if it is a delivery receipt.
     * 
     * @return true if the received message is a delivery receipt.
     */
    private boolean isDeliveryReceipt(DeliverSmPDU rec) {
        return ((rec.getEsmClass() & 0x3C) == 0x04);
    }

    private boolean isDeliveredState(DeliverSmPDU rec, DeliveryState state) {
        DeliveryState returnedState = null;
        MessageStateTypes messageState = rec.getMessageState();
        
        // message_state has precedence on delivery_text. Read TR: HT51489
        if (messageState != MessageStateTypes.MESSAGE_STATE_NOT_PROVIDED) {
           log.logString("SMPPCom delivery message state " + messageState, Logger.LOG_DEBUG);
           if (messageState ==  MessageStateTypes.MESSAGE_STATE_DELIVERED) {
               returnedState = DeliveryState.DELIVERED;
           }
        } else if (isDeliveryReceipt(rec)) {
            String dilveredMessage = rec.getShortMessage();

            log.logString("SMPPCom delivery text message " + dilveredMessage, Logger.LOG_DEBUG);

            // we only cares about stat
            int dIndex;
            dIndex = dilveredMessage.indexOf("stat:");          

            if (dIndex == -1)
                dIndex = dilveredMessage.indexOf("STAT:");

            if (dIndex != -1) {
                String sStates = dilveredMessage.substring(dIndex + 5);

                int endIndex = sStates.indexOf(' ');

                String sState = sStates.substring(0, endIndex);

                returnedState = stateMap.get(sState.toUpperCase());
                log.logString("SMPPCom delivery text message state " + sState, Logger.LOG_DEBUG);
            }
        } else {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Unexpected delivery respone (phoneOn) for number: " + deliverReq.getSourceAddress() + " Resp: "
                        + deliverReq, Logger.LOG_DEBUG);
            }
        }
        
        return (returnedState != null) ? returnedState == state : false;
    }

    protected void handleProtocolErrors(SMPP_PDU resp, String type, String requestPduInfo) throws SMSComException {
        if (errCodeAction == LOG_ERRORS && !willLog(Logger.LOG_ERROR)) {
            return;
        }

        log.logString("SMPPCom::handleProtocolErrors Response SMS ERROR CODE [0x" + Integer.toHexString(resp.getCommandStatus())
                + "], " + requestPduInfo, Logger.LOG_ERROR);
        
        boolean isDataSmResp = resp.commandId == SmppConstants.SMPPCMD_DATA_SM_RESP;

        int errorCode = resp.getCommandStatus();
        switch (errorCode) {
            case SMPPSTATUS_RINVMSGLEN:
            case SMPPSTATUS_RINVCMDLEN:
            case SMPPSTATUS_RINVCMDID:
                handleError(SMPPSTATUS_RINVMSGLEN, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(bad header) when sending " + type), isDataSmResp);
                break;

            case SMPPSTATUS_RINVBNDSTS:
                handleError(SMPPSTATUS_RINVBNDSTS, resp.getNetworkErrorCodes(), 
                        errorCodeMsg(errorCode,
                        "(unbound state)  when sending " + type), isDataSmResp);


                break;
            case SMPPSTATUS_RINVSRCADR:
                handleError(SMPPSTATUS_RINVSRCADR, resp.getNetworkErrorCodes(), 
                                        errorCodeMsg(errorCode,
                                       "(invalid source address)  when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVDSTADR:
                handleError(SMPPSTATUS_RINVDSTADR, resp.getNetworkErrorCodes(), 
                                        errorCodeMsg(errorCode,
                                       "(invalid destination address)  when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RMSGQFUL:
                handleError(SMPPSTATUS_RMSGQFUL, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(queue full) when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVSERTYP:
                handleError(SMPPSTATUS_RINVSERTYP, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid service type when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVDSTTON:
                handleError(SMPPSTATUS_RINVDSTTON, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid destination TON)  when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVDSTNPI:
                handleError(SMPPSTATUS_RINVDSTNPI, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid destination NPI) when sending " + type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVEXPIRY:
                    handleError(SMPPSTATUS_RINVEXPIRY, resp.getNetworkErrorCodes(), 
                                           errorCodeMsg(errorCode,
                                                   "(invalid expiry time) when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVOPTPARSTREAM:
                handleError(SMPPSTATUS_RINVOPTPARSTREAM, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid optional parameter) when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_ROPTPARNOTALLWD:
                handleError(SMPPSTATUS_ROPTPARNOTALLWD, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid optional parameter) when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_RMISSINGOPTPARAM:
                handleError(SMPPSTATUS_RMISSINGOPTPARAM, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid optional parameter) when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVOPTPARAMVAL:
                handleError(SMPPSTATUS_RINVOPTPARAMVAL, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid optional parameter) when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_RINVPARLEN:
                handleError(SMPPSTATUS_RINVPARLEN, resp.getNetworkErrorCodes(), 
                                       errorCodeMsg(errorCode,
                                       "(invalid parameter length)when sending "+ type), isDataSmResp);
                break;
            case SMPPSTATUS_RTHROTTLED:
                handleError(SMPPSTATUS_RTHROTTLED, resp.getNetworkErrorCodes(), 
                            errorCodeMsg(errorCode,
                            "(Exceeded message limit) when sending SMS when sending "+ type), isDataSmResp);;
                break;
            default:
                handleError(errorCode, resp.getNetworkErrorCodes(), 
                            errorCodeMsg(errorCode,
                                       "when sending " + type), isDataSmResp);;
            }
    }

    /**
     * Handles errors, either with an exception, by logging or by ignoring.
     * 
     * @throws SMSComException
     *         if there is an error and exceptions are enabled.
     */
    protected void handleProtocolErrorsForSendShortMessage(SMPP_PDU sendMsgResp, String adr, String org, SMSMessage msg,
            String requestPduInfo) throws SMSComException {
        if (errCodeAction == LOG_ERRORS && !willLog(Logger.LOG_ERROR)) {
            return;
        }
        
        boolean isDataSmResp = sendMsgResp.commandId == SmppConstants.SMPPCMD_DATA_SM_RESP;

        log.logString(
                "SMPPCom::handleProtocolErrorsForSendShortMessage Response SMS ERROR CODE [0x" + Integer.toHexString(sendMsgResp.getCommandStatus())
                        + "], " + requestPduInfo + ", Source: " + org + ", Destination: " + adr + ", Message: " + msg,
                Logger.LOG_ERROR);

        switch (sendMsgResp.getCommandStatus()) {
            case SMPPSTATUS_RINVMSGLEN:
            handleError(SMPPSTATUS_RINVMSGLEN, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(bad header) when sending SMS to " + adr + " from "+org+"("
                                   + requestPduInfo + ")"), isDataSmResp);
            break;
        case SMPPSTATUS_RINVCMDLEN:
            handleError(SMPPSTATUS_RINVCMDLEN, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(bad header) when sending SMS to " + adr + " from "+org+"("
                                   + requestPduInfo + ")"), isDataSmResp);
            break;

        case SMPPSTATUS_RINVCMDID:
            handleError(SMPPSTATUS_RINVCMDID, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(bad header) when sending SMS to " + adr + " from "+org+"("
                                   + requestPduInfo + ")"), isDataSmResp);
            break;
        case SMPPSTATUS_RINVBNDSTS:
            handleError(SMPPSTATUS_RINVBNDSTS, sendMsgResp.getNetworkErrorCodes(), 
                    errorCodeMsg(sendMsgResp.getCommandStatus(),
                    "(unbound state) when sending SMS to " + adr + " from "+org+"("
                                   + requestPduInfo + ")"), isDataSmResp);


            break;
        case SMPPSTATUS_RINVSRCADR:
            handleError(SMPPSTATUS_RINVSRCADR, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid source address) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVDSTADR:
            handleError(SMPPSTATUS_RINVDSTADR, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid destination address) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RMSGQFUL:
            handleError(SMPPSTATUS_RMSGQFUL, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(queue full) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVSERTYP:
            handleError(SMPPSTATUS_RINVSERTYP, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid service type  + " + msg.getServiceType()
                                   + ") when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVDSTTON:
            handleError(SMPPSTATUS_RINVDSTTON, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid destination TON) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVDSTNPI:
            handleError(SMPPSTATUS_RINVDSTNPI, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid destination NPI) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVEXPIRY:
            if (msg.getExpiryTimeAbsolute() != null) {
                handleError(SMPPSTATUS_RINVEXPIRY, sendMsgResp.getNetworkErrorCodes(), 
                                       errorCodeMsg(sendMsgResp.getCommandStatus(),
                                       "(invalid expiry time " + msg.getExpiryTimeAbsolute()
                                       + ") when sending SMS to " + adr + " from "+org), isDataSmResp);
            } else {
                handleError(SMPPSTATUS_RINVEXPIRY, sendMsgResp.getNetworkErrorCodes(), 
                                       errorCodeMsg(sendMsgResp.getCommandStatus(),
                                       "(invalid expiry time " + msg.getExpiryTimeRelative()
                                       + ") when sending SMS to " + adr + " from "+org), isDataSmResp);
            }
            break;
        case SMPPSTATUS_RINVOPTPARSTREAM:
            handleError(SMPPSTATUS_RINVOPTPARSTREAM, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid optional parameter) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_ROPTPARNOTALLWD:
            handleError(SMPPSTATUS_ROPTPARNOTALLWD, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid optional parameter) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RMISSINGOPTPARAM:
            handleError(SMPPSTATUS_RMISSINGOPTPARAM, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid optional parameter) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVOPTPARAMVAL:
            handleError(SMPPSTATUS_RINVOPTPARAMVAL, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid optional parameter) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        case SMPPSTATUS_RINVPARLEN:
            handleError(SMPPSTATUS_RINVPARLEN, sendMsgResp.getNetworkErrorCodes(), 
                                   errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "(invalid parameter length) when sending SMS to " + adr + " from "+org+" length: "+msg.getLength()), isDataSmResp);
            break;
        case SMPPSTATUS_RTHROTTLED:
            handleError(SMPPSTATUS_RTHROTTLED, sendMsgResp.getNetworkErrorCodes(), 
                        errorCodeMsg(sendMsgResp.getCommandStatus(),
                        "(Exceeded message limit) when sending SMS to " + adr + " from "+org), isDataSmResp);
            break;
        default:
            handleError(sendMsgResp.getCommandStatus(), sendMsgResp.getNetworkErrorCodes(), 
                        errorCodeMsg(sendMsgResp.getCommandStatus(),
                                   "when sending SMS to " + adr + " from "+org), isDataSmResp);
        }
    }

    /**
     * Checks with class SMSConfigWrapper what action a specific error code should have. Actions are retry or failed.
     * 
     * @param errorCode
     *        the SMPP error code in hex format such as 0x0.
     * @param errormsg
     *        the error message for the SMPP error code
     * @param isDataSmResp - Whether or not the error was returned by a data_sm_resp PDU.
     */
    private void handleError(int errorCode, String networkErrorCode, String errormsg, boolean isDataSmResp) throws SMSComException {
        Integer code = new Integer(errorCode);
        String errorCodeClientAction = SMSConfigWrapper.getErrorClientAction(code);
        if(errorCodeClientAction != null) {
            if(errorCodeClientAction.equalsIgnoreCase("temporaryUnavailable")) {
                log.logString("Handling SMPP error code [0x" + Integer.toHexString(errorCode) + "]: applying temporaryUnavailable client action", Logger.LOG_DEBUG);
                connectionHandler.connectionTemporaryUnavailableForNewRequests();
            } else {
                log.logString("Handling SMPP error code [0x" + Integer.toHexString(errorCode) + "]: ignoring unknown client action: " + errorCodeClientAction, Logger.LOG_DEBUG);
            }
        }

        // special case...
        if (errorCode == SMPPSTATUS_RINVBNDSTS) {
            rebindOrDisconnect();
            if (getState() == SMSCOM_SENDING) {
                rebindError(errormsg);
            } else {
                loadError(errormsg);
            }
        }
        
        String errorCodeAction =null;
        // Network error code
        if (isDataSmResp)
        {
            String networkErrorCodeLookup = Config.getLookupNetworkErrorCodeWhenCommandStatusIs();
            if (networkErrorCodeLookup != null && !networkErrorCodeLookup.equals("")) {
                int networkErrorCodeVerif;
                if (networkErrorCodeLookup.startsWith("0x")) networkErrorCodeVerif = Integer.parseInt(networkErrorCodeLookup.substring(2), 16);
                else networkErrorCodeVerif = Integer.parseInt(networkErrorCodeLookup, 16);
            log.logString("Error code to verify network (from config) (in int) = " + networkErrorCodeVerif, Logger.LOG_DEBUG);
            log.logString("Current error code (in int) = " + errorCode, Logger.LOG_DEBUG);
            log.logString("Current network error code (in int) = " + networkErrorCode, Logger.LOG_DEBUG);
                if (errorCode == networkErrorCodeVerif) {
                    if (networkErrorCode != null)
                    {
                        int reconstructedNetworkErrorCode = Integer.parseInt(networkErrorCode, 16);
                        errorCodeAction = SMSConfigWrapper.getNetworkErrorAction(reconstructedNetworkErrorCode);    
                        log.logString("Using network error code action, \"" + errorCodeAction + "\"", Logger.LOG_DEBUG);
                    }
                    else
                    {
                        errorCodeAction = SMSConfigWrapper.getDefaultNetworkErrorAction();
                        log.logString("Using default network error code action, \"" + errorCodeAction + "\"", Logger.LOG_DEBUG);
                    }
                }
            } 
        }
        
        // If network error code not used, get action according to command status
        if (errorCodeAction == null){
            errorCodeAction = SMSConfigWrapper.getErrorAction(code);
        }

        if (errorCodeAction.equalsIgnoreCase("ok")) {
            // The mechanism here is to throw an exception on an error action;
            // for example, a retry operation will throw a SMSComLoadException.
            // However, in the case where the error action is to consider it as
            // a valid operation, we simply return from the method without
            // throwing an exception, thus handling it like an "ok" scenario.
            log.logString("Handle SMPP error code [0x" + Integer.toHexString(errorCode) + "] with action ok", Logger.LOG_DEBUG);
        }
        else if (errorCodeAction.equalsIgnoreCase("disconnect")) {
             log.logString("Handle SMPP error code [0x" +
                     Integer.toHexString(errorCode) +
                     "] with action disconnect and retry", Logger.LOG_VERBOSE);
             close();
             loadError(errormsg);
        }
        else if(errorCodeAction.equalsIgnoreCase("retry")) {
            log.logString("Handle SMPP error code [0x" +
                          Integer.toHexString(errorCode) +
                          "] with action retry", Logger.LOG_VERBOSE);
            loadError(errormsg);
        }
        else if (errorCodeAction.equalsIgnoreCase("failed")){
            log.logString("Handle SMPP error code [0x" +
                          Integer.toHexString(errorCode) +
                          "] with action failed", Logger.LOG_VERBOSE);
            dataError(errormsg);
        }
        else if (errorCodeAction.equalsIgnoreCase("waitphoneon")){
            log.logString("Handle SMPP error code [0x" +
                          Integer.toHexString(errorCode) +
                          "] with action waitphoneon", Logger.LOG_VERBOSE);
            phoneOffError(errormsg);
        }
        else {
            log.logString("Handle SMPP error code [0x" +
                          Integer.toHexString(errorCode) +
                          "] with action failed", Logger.LOG_VERBOSE);
            dataError(errormsg);
        }        
    }

    protected String errorCodeMsg(int code, String msg) {
        return "ERROR CODE [0x"
            + Integer.toHexString(code) + "] FROM SMSC "
            + msg;
    }

    protected boolean isErrorCodeIgnored(SMPP_PDU response) {
        int errorCode = response.getCommandStatus();
        if( errorCode < 0 || errorCode >= errorCodesIgnored.length ){
            return false;
        }
        return errorCodesIgnored[errorCode];
    }

    public void setErrorCodesIgnored(int[] values) {
        for (int i = 0; i < errorCodesIgnored.length; i++) {
            errorCodesIgnored[i] = false;
        }
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                if (value < errorCodesIgnored.length && value >= 0) {
                    errorCodesIgnored[value] = true;
                }
            }
        }

    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }


    private void handleReceivedAlertNotificationPhoneOn() {
        PhoneOnEvent poe = null;
        if (alertNotificationSm.getMsAvailabilityStatus().getMsAvailabilityStatusValue() == MSAvailabilityStatusEventTypes.MS_STATUS_AVAILABLE
                .getMsAvailabilityStatusValue()) {
            poe = new PhoneOnEvent(this, alertNotificationSm.getSourceAddress().getNumber(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_AVAILABLE.getPhoneOnEventMapping(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_AVAILABLE.getStatus());
        } else if (alertNotificationSm.getMsAvailabilityStatus().getMsAvailabilityStatusValue() == MSAvailabilityStatusEventTypes.MS_STATUS_DENIED
                .getMsAvailabilityStatusValue()) {
            poe = new PhoneOnEvent(this, alertNotificationSm.getSourceAddress().getNumber(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_DENIED.getPhoneOnEventMapping(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_DENIED.getStatus());
        } else if (alertNotificationSm.getMsAvailabilityStatus().getMsAvailabilityStatusValue() == MSAvailabilityStatusEventTypes.MS_STATUS_UNAVAILABLE
                .getMsAvailabilityStatusValue()) {
            poe = new PhoneOnEvent(this, alertNotificationSm.getSourceAddress().getNumber(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_UNAVAILABLE.getPhoneOnEventMapping(),
                    MSAvailabilityStatusEventTypes.MS_STATUS_UNAVAILABLE.getStatus());
        }
        getReceiptReceiver().phoneOn(poe);
    }

}// class
