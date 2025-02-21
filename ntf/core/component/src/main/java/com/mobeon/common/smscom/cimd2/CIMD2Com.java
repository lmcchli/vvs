/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.smscom.Logger;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSComConnectionException;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.util.NtfUtil;

import java.io.IOException;


/**
 * CIMD2Com is an implementation of SMSCom for the CIMD2 protocol.
 * <P>
 * <TABLE BGCOLOR="#FFF0E0" CELLPADDING=10 WIDTH=50% BORDER=1><TR><TD>
 * This class shall not be used directly. You use it indirectly
 * through SMSCom. This means that all
 * documentation of this class and other classes in the cimd2 package is for
 * implementors, you do not need it to use the API.
 * </TD></TR></TABLE>
 * <P>
 * Most messages are implemented to handle only one direction, either to or from the
 * SMS-C. The following messages are implemented:
 * <TABLE BORDER=1>
 * <TR><TH>Message</TH><TH>to SMS-C</TH><TH>from SMS-C</TH></TR>
 * <TR><TD>Login</TD><TD>X</TD><TD>&nbsp;</TD></TR>
 * <TR><TD>Login Response</TD><TD>&nbsp;</TD><TD>X</TD></TR>
 * <TR><TD>Submit Message</TD><TD>X</TD><TD>&nbsp;</TD></TR>
 * <TR><TD>Submit Message Response</TD><TD>&nbsp;</TD><TD>X</TD></TR>
 * <TR><TD>Logout</TD><TD>X</TD><TD>&nbsp;</TD></TR>
 * <TR><TD>Logout Response</TD><TD>X</TD><TD>X</TD></TR>
 * <TR><TD>Alive</TD><TD>X</TD><TD>X</TD></TR>
 * <TR><TD>Alive Response</TD><TD>X</TD><TD>X</TD></TR>
 * <TR><TD>DeliverMessage</TD><TD>&nbsp;</TD><TD>X</TD></TR>
 * <TR><TD>DeliverRespMessage</TD><TD>X</TD><TD>&nbsp;</TD></TR>
 * <TR><TD>DeliverStatusReportMessage</TD><TD>&nbsp;</TD><TD>X</TD></TR>
 * <TR><TD>DeliverStatusReportRespMessage</TD><TD>X</TD><TD>&nbsp;</TD></TR>
 * <TR><TD>GeneralErrorRespMessage</TD><TD>&nbsp;</TD><TD>X</TD></TR>
 * <TR><TD>NackMessage</TD><TD>X</TD><TD>X</TD></TR>
 * </TABLE>
 * To send a message to the SMS-C, you create an instance of the wanted message class
 * and call getBuffer with message-specific parameters. This gives you a byte array
 * that can be sent to the SMS-C. The getBuffer method writes parameters into
 * the buffer through the DataOutput interface.
 * <P>
 * To handle a message from the SMS-C, you use the read() method which reads the message
 * length and then the entire message, and parses the header. You then parse the
 * body with the parseBody() method and access parameters with message-specific get
 * methods.
 */
public class CIMD2Com extends SMSCom
    implements Cimd2Constants {

    private LoginMessage loginReq;
    private LoginRespMessage loginResp;
    private DeliverMessage deliverReq;
    private DeliverRespMessage deliverResp;
    private DeliverStatusReportMessage reportReq;
    private DeliverStatusReportRespMessage reportResp;
    private SubmitMessage submitReq;
    private SubmitRespMessage submitResp;
    private LogoutMessage logoutReq;
    private LogoutRespMessage logoutResp;
    private AliveMessage aliveReq;
    private AliveRespMessage aliveResp;
    private NackMessage nackResp;
    private CancelMessage cancelReq;
    private CancelRespMessage cancelResp;

    private CIMD2Message response = null;

    private int packetNumber = 1;

    /**
     * Generates a unique sequence number for each CIMD2 message, starting from 1.
     * @return the next sequence number.
     */
    public synchronized int nextPacketNumber() {
        int i = packetNumber;
        packetNumber += 2;
        if (packetNumber > 255) { packetNumber = 1; }
        return i;
    }

    public synchronized void setPacketNumber(int i) {
        packetNumber = i;
    }

    /**
     * Constructor.
     */
    public CIMD2Com() {
        super();
        protocol = "CIMD2";
        loginReq = new LoginMessage(this);
        loginResp = new LoginRespMessage(this);
        deliverReq = new DeliverMessage(this);
        deliverResp = new DeliverRespMessage(this);
        reportReq = new DeliverStatusReportMessage(this);
        reportResp = new DeliverStatusReportRespMessage(this);
        submitReq = new SubmitMessage(this);
        submitResp = new SubmitRespMessage(this);
        logoutReq = new LogoutMessage(this);
        logoutResp = new LogoutRespMessage(this);
        aliveReq = new AliveMessage(this);
        aliveResp = new AliveRespMessage(this);
        new GeneralErrorRespMessage(this);
        nackResp = new NackMessage(this);
        cancelReq = new CancelMessage(this);
        cancelResp = new CancelRespMessage(this);
    }

    /**
     * Submits a message to the SMSC for sending to a receiver.
     *@param adr - the address of the receiver.
     *@param org - the address of the sender.
     *@param msg - the message to send.
     *@throws SMSComException
     */
    public void protocolSend(SMSAddress adr, SMSAddress org, SMSMessage msg, int smsRequestId, SMSResultHandler rh) throws SMSComException {
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com sending message to " + adr
                              + " from " + org, Logger.LOG_DEBUG);
            }

            writeBuffer(submitReq.getBuffer(adr, org, msg));
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com submit " + submitReq.getPacketNumber(), Logger.LOG_DEBUG);
            }
            waitForResponse(submitResp, CIMD2_SUBMIT_MESSAGE_RESP,
                            submitReq.getPacketNumber());
            setState(SMSCOM_WAITING);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com submit response " + submitResp.getPacketNumber()
                              + " status " + submitResp.getErrorCode(),
                              Logger.LOG_DEBUG);
            }
            if (submitResp.getErrorCode() != CIMD2_NO_ERROR
                && errCodeAction != IGNORE_ERRORS) {
                handleErrors(adr, org, msg);
            }
        } catch (IOException e) {
            throw new SMSComConnectionException("CIMD2Com failed to send message. " + e.getMessage());
        }
    }

    public void protocolCancel(SMSAddress to, SMSAddress from, String serviceType ) throws SMSComException {
        try {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com sending cancel message to " + to, Logger.LOG_DEBUG);
            }

            writeBuffer(cancelReq.getBuffer(to));
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com submit " + cancelReq.getPacketNumber(), Logger.LOG_DEBUG);
            }
            waitForResponse(cancelResp, CIMD2_CANCEL_MESSAGE_RESP,
                            cancelReq.getPacketNumber());
            setState(SMSCOM_WAITING);
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com cancel response " + cancelResp.getPacketNumber()
                              + " status " + cancelResp.getErrorCode(),
                              Logger.LOG_DEBUG);
            }
            if (cancelResp.getErrorCode() != CIMD2_NO_ERROR
                && errCodeAction != IGNORE_ERRORS) {
                log.logString("CIMD2Com cancel message failed with error code " + cancelResp.getErrorCode(),
                        Logger.LOG_ERROR);
            }
        } catch (IOException e) {
            throw new SMSComConnectionException("CIMD2Com failed to send cancel message. " + e.getMessage());
        }
    }

    /**
     * Sends an alive message and reads the response. All exceptions are
     * swallowed and just result in a false return value.
     * @return true iff it is possible to communicate with the SMS-C.
     */
    public boolean protocolPoll() {
        try {
            writeBuffer(aliveReq.getBuffer());
            waitForResponse(aliveResp,
                            CIMD2_ALIVE_RESP,
                            aliveReq.getPacketNumber());
            setState(SMSCOM_WAITING);
            return aliveResp.getErrorCode() == CIMD2_NO_ERROR;

        } catch (IOException e) {
            close();
        } catch (SMSComException e) {
            close();
            }

        return (false);
    }

    /**
     * Handles errors, either with an exception, by logging or by ignoring.
     *@throws SMSComException if there is an error and exceptions are enabled.
     */
    private void handleErrors(SMSAddress adr, SMSAddress org, SMSMessage msg) throws SMSComException {
        if (errCodeAction == LOG_ERRORS && !willLog(Logger.LOG_ERROR)) {
            return;
        }

        switch (submitResp.getErrorCode()) {
        case CIMD2_UNEXPECTED_OPERATION:
            dataError("SMS-C says unexpected operation: " + submitResp.getErrorText());
            break;
        case CIMD2_SYNTAX:
            dataError("SMS-C says syntax error: " + submitResp.getErrorText());
            break;
        case CIMD2_UNSUPPORTED_PARAMETER:
            dataError("SMS-C says unsupported parameter: " + submitResp.getErrorText());
            break;
        case CIMD2_CONNECTION_TO_MC_LOST:
            disconnect();
            dataError("SMS-C says connection lost: " + submitResp.getErrorText());
            break;
        case CIMD2_NO_RESPONSE_FROM_MC:
            dataError("SMS-C says no response: " + submitResp.getErrorText());
            break;
        case CIMD2_GENERAL_SYSTEM:
            dataError("SMS-C says general system failure: " + submitResp.getErrorText());
            break;
        case CIMD2_PARAMETER_FORMATTING:
            dataError("SMS-C says parameter formatting error " + submitResp.getErrorText());
            break;
        case CIMD2_REQUESTED_OPERATION_FAILED:
            dataError("SMS-C says operation failed " + submitResp.getErrorText());
            break;
        case CIMD2_DESTINATION_ADDRESS_ERR:
            dataError("SMS-C says invalid destination address " + adr + ": " + submitResp.getErrorText());
            break;
        case CIMD2_ORIGINATOR_ADDRESS_ERR:
            dataError("SMS-C says incorrect originator address usage " + org + ": " + submitResp.getErrorText());
            break;
        case CIMD2_NUMBER_OF_DESTINATIONS:
            dataError("SMS-C says invalid # of destinations: " + submitResp.getErrorText());
            break;
        case CIMD2_USER_DATA_SYNTAX:
            dataError("SMS-C says bad user data syntax: " + submitResp.getErrorText());
            break;
        case CIMD2_DCS_USAGE:
            dataError("SMS-C says bad data coding scheme: " + submitResp.getErrorText());
            break;
        case CIMD2_VALIDITY_PERIOD:
            if (msg.getExpiryTimeAbsolute() != null) {
                dataError("SMS-C says bad expiry time "
                     + msg.getExpiryTimeAbsolute()
                     + ": " + submitResp.getErrorText());
            } else {
                dataError("SMS-C says bad expiry time "
                     + msg.getExpiryTimeRelative()
                     + ": " + submitResp.getErrorText());
            }

        default:
            generalError("SMS to " + adr + " rejected by SMS-C, CIMD2 code ["
                         + submitResp.getErrorCode()
                         + "], " + submitResp.getErrorText());
        }
    }

    /**
     * handleMessage handles messages received from the SMSC.
     *@param msg the received message  The header is already parsed, but the
     * body is not.
     */
    private void handleMessage(CIMD2Message msg) {
        switch (msg.getOperationCode()) {
        case CIMD2_LOGIN_RESP:
        case CIMD2_LOGOUT_RESP:
        case CIMD2_SUBMIT_MESSAGE_RESP:
        case CIMD2_ENQUIRE_MESSAGE_STATUS_RESP:
        case CIMD2_DELIVERY_REQUEST_RESP:
        case CIMD2_CANCEL_MESSAGE_RESP:
        case CIMD2_SET_RESP:
        case CIMD2_GET_RESP:
        case CIMD2_ALIVE_RESP:
        case CIMD2_GENERAL_ERROR_RESP:
            handleResponse(msg);
            break;

        case CIMD2_DELIVER_MESSAGE:
            handleReceivedMessage(msg);
            break;

        case CIMD2_DELIVER_STATUS_REPORT:
            handleReceivedReport(msg);
            break;

        case CIMD2_ALIVE:
            handleAlive(msg);
            break;

        case CIMD2_LOGOUT:
            handleLogout(msg);
            break;

        case CIMD2_NACK:
            handleNack(msg);
            break;

        case CIMD2_LOGIN:
        case CIMD2_SUBMIT_MESSAGE:
        case CIMD2_ENQUIRE_MESSAGE_STATUS:
        case CIMD2_DELIVERY_REQUEST:
        case CIMD2_CANCEL_MESSAGE:
        case CIMD2_SET:
        case CIMD2_GET:
        case CIMD2_DELIVER_MESSAGE_RESP:
        case CIMD2_DELIVER_STATUS_REPORT_RESP:
        default:
            handleBadMessage(msg);
            break;
        }
    }

    /**
     * handleResponse handles messages that are responses.
     *@param msg - the received response message.
     */
    private void handleResponse(CIMD2Message msg) {
        switch (msg.getOperationCode()) {
        case CIMD2_LOGIN_RESP:
        case CIMD2_LOGOUT_RESP:
        case CIMD2_SUBMIT_MESSAGE_RESP:
        case CIMD2_ALIVE_RESP:
        case CIMD2_GENERAL_ERROR_RESP:
        case CIMD2_CANCEL_MESSAGE_RESP:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Handling response of expected type", Logger.LOG_DEBUG);
            }
            synchronized (responseLock) {
                while (response != null) {
                    //Wait until previous response is consumed
                    try {
                        responseLock.wait(timeout);
                    } catch (InterruptedException e) {
                        //Nobody seems to want the response, release it.
                        if (willLog(Logger.LOG_VERBOSE)) {
                            log.logString("Discarding unwanted response message " + response.getOperationCode()
                                          + "/" + response.getPacketNumber(), Logger.LOG_VERBOSE);
                        }
                        response = null;
                    }
                }
                response = msg;
                responseLock.notifyAll();
            }
            break;

        case CIMD2_ENQUIRE_MESSAGE_STATUS_RESP:
        case CIMD2_DELIVERY_REQUEST_RESP:
        case CIMD2_SET_RESP:
        case CIMD2_GET_RESP:
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Unexpected response: " + msg, Logger.LOG_DEBUG);
            }
        }
    }

    /**
     * releaseResponse releases response when the last response has been handled.
     */
    private void releaseResponse() {
        synchronized (responseLock) {
            response = null;
            responseLock.notifyAll();
        }
    }

    /**
     * handleReceivedMessage handles deliver SM messages.
     *@param msg - the deliver message.
     */
    private void handleReceivedMessage(CIMD2Message msg) {
        deliverReq.parseBody(msg);
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("CIMDCOM handleReceivedMessage: Unexpected delivery: " + deliverReq, Logger.LOG_DEBUG);
        }
        deliverResp.setPacketNumber(msg.getPacketNumber());
        deliverResp.setErrorCode(CIMD2_NO_ERROR);
        deliverResp.setErrorText("");
        try {
            writeBuffer(deliverResp.getBuffer());
        } catch (IOException e) {
            if (willLog(Logger.LOG_ERROR)) {
                log.logString("CIMDCOM handleReceivedMessage: Failed to write delivery response: " + NtfUtil.stackTrace(e), Logger.LOG_ERROR);
            }
        }
    }

    /**
     * Handles a received report message.
     *@param msg - the received report message.
     */
    private void handleReceivedReport(CIMD2Message msg) {
        reportReq.parseBody(msg);
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Deliver status report: " + reportReq, Logger.LOG_DEBUG);
        }
        reportResp.setPacketNumber(msg.getPacketNumber());
        reportResp.setErrorCode(CIMD2_NO_ERROR);
        reportResp.setErrorText("");
        try {
            writeBuffer(reportResp.getBuffer());
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write deliver status report response: " + e, Logger.LOG_ERROR);
            }
        }
        getReceiptReceiver().phoneOn(new PhoneOnEvent(this,
                                                      reportReq.getDestinationAddress().getNumber(),
                                                      PhoneOnEvent.PHONEON_OK,
                                                      ""));
    }

    /**
     * Handles alive requests from the SMSC.
     *@param msg - the alive message.
     */
    private void handleAlive(CIMD2Message msg) {
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Handling Alive request", Logger.LOG_DEBUG);
        }
        aliveResp.setPacketNumber(msg.getPacketNumber());
        aliveResp.setErrorCode(CIMD2_NO_ERROR);
        aliveResp.setErrorText("");
        try {
            writeBuffer(aliveResp.getBuffer());
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write alive response: " + e, Logger.LOG_ERROR);
            }
        }
    }

    /**
     * Handles logout requests from the SMSC by writing a response and closing
     * the connection.
     *@param msg - the logout message.
     */
    private void handleLogout(CIMD2Message msg) {
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Handling Logout request", Logger.LOG_DEBUG);
        }
        logoutResp.setPacketNumber(msg.getPacketNumber());
        logoutResp.setErrorCode(CIMD2_NO_ERROR);
        logoutResp.setErrorText("");
        try {
            writeBuffer(logoutResp.getBuffer());
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write logout response: " + e, Logger.LOG_ERROR);
            }
        }
        setState(SMSCOM_DISCONNECTING);
        close();
    }

    /**
     * handleNack is called when a generic NACK is received. This means that
     * synchronization of sequence numbers with the SMSC is lost. Best to close
     * the connection.
     *@param msg - the generic NACK PDU.
     */
    private void handleNack(CIMD2Message msg) {
        //Something is wrong with the packet numbers, restart
        reset();
    }

    /**
     * Handles messages that the SMSC should not send at all by responding with
     * a NACK.
     *@param msg - the offending message.
     */
    private void handleBadMessage(CIMD2Message msg) {
        if (willLog(Logger.LOG_ERROR)) {
            log.logString("Received illegal message " + msg, Logger.LOG_ERROR);
        }
        nackResp.setPacketNumber(msg.getPacketNumber());
        try {
            writeBuffer(nackResp.getBuffer());
        } catch (IOException e) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Failed to write response: " + e, Logger.LOG_DEBUG);
            }
        }
    }

    /**
     * logIn performs a CIMD2 login operation. If bad configuration makes log in
     * impossible, it throws an SMSComConfigException.
     *@return true if log in succeeded, false otherwise.
     */
    protected boolean protocolLogin() {
        if (willLog(Logger.LOG_VERBOSE)) {
            log.logString("CIMD2Com logging in", Logger.LOG_VERBOSE);
        }
        try {
            writeBuffer(loginReq.getBuffer());
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com login " + loginReq.getPacketNumber(), Logger.LOG_DEBUG);
            }
            waitForResponse(loginResp,
                            CIMD2_LOGIN_RESP,
                            loginReq.getPacketNumber());
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("CIMD2Com login response " + loginResp.getPacketNumber()
                              + " status " + loginResp.getErrorCode(),
                              Logger.LOG_DEBUG);
            }

            switch (loginResp.getErrorCode()) {
            case CIMD2_NO_ERROR:
                if (willLog(Logger.LOG_VERBOSE)) {
                    log.logString("CIMD2Com logged in to " + hostname
                                  + " as " + user,
                                  Logger.LOG_VERBOSE);
                }
                setState(SMSCOM_SENDING);
                return true;
            case CIMD2_INVALID_LOGIN:
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("CIMD2Com invalid login to " + hostname, Logger.LOG_ERROR);
                }
            case CIMD2_TOO_MANY_LOGINS:
            case CIMD2_LOGIN_REFUSED:
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("CIMD2Com can not login to " + hostname + " now", Logger.LOG_ERROR);
                }
            default:
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("CIMD2Com login failed, CIMD2 code [" + loginResp.getErrorCode() + "], " + loginResp.getErrorText(), Logger.LOG_ERROR);
                }
            }
        } catch (IOException e) {
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("CIMD2Com failed " + e, Logger.LOG_VERBOSE);
            }
        } catch (SMSComException e) {
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("CIMD2Com failed " + e, Logger.LOG_VERBOSE);
            }
        }
        return false;
    }

    /**
     * Logs out from the SMSC.
     */
    public void protocolLogout() {
        byte[] buf = logoutReq.getBuffer();
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("CIMD2Com logout " + logoutReq.getPacketNumber(), Logger.LOG_DEBUG);
        }

        try {
            writeBuffer(buf);
            Thread.sleep(1 * 1000); //Allow time for the response, but ignore it
        } catch (IOException e) {
            ;
        } catch (InterruptedException e) {
            ;
        }
        setState(SMSCOM_DISCONNECTING);
    }

    /**
     * Releases the response, so it is not reserved even after
     * the connection is closed.
     */
    protected void protocolCleanup() {
        releaseResponse();
    }

    /**
     * Reads a message from the SMSC and handles it.
     */
    protected void protocolReadAndHandleMessage() throws IOException {
        CIMD2Message msg = new CIMD2Message(this);
        if (msg.read(fromSmsc)) {
            if (willLog(Logger.LOG_DEBUG)) {
                log.logString("Read message " + msg, Logger.LOG_DEBUG);
            }
            handleMessage((CIMD2Message) msg);
        }
    }

    /**
     * waitForResponse reads an expected response message requests from the SMS-C. If a
     * message other than the expected is received, the connection is logged out and
     * closed and an SMSCom exception is thrown.
     *@param message - the expected message to read.
     *@param expectedOperationCode - the command id of the expected message type.
     *@param expectedPacketNo - the expected packet number of the message.
     *@throws SMSComException
     *@throws IOException
     */
    private void waitForResponse(CIMD2Message message,
                                 int expectedOperationCode,
                                 int expectedPacketNo)
        throws SMSComException, IOException {

        synchronized (responseLock) {
            if (response == null) {
                try { responseLock.wait(timeout); } catch (InterruptedException e) { ; }
            }
            if (state == SMSCOM_EXITING) {
                return;
            }
            if (response == null) {
                if (willLog(Logger.LOG_ERROR)) {
                    log.logString("CIMD2Com timeout waiting for response.", Logger.LOG_ERROR);
                }
                reset();
                throw new SMSComConnectionException("CIMD2Com timeout waiting for response");
            }

            if (response.getOperationCode() == expectedOperationCode
                && response.getPacketNumber() == expectedPacketNo) {
                message.parseBody(response);
                releaseResponse();
            } else {
                //Unexpected message
                String msg ="Expected command id/packet number "
                    + expectedOperationCode + "/" + expectedPacketNo
                    + " but received " + response.getOperationCode()
                    + "/" + response.getPacketNumber();
                    reset();
                throw new SMSComConnectionException(msg);
            }
        }
    }
}
