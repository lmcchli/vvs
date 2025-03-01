/*
 * Created on Feb 19, 2005
 * 
 * Copyright 2005 CafeSip.org 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package org.cafesip.sipunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.ListIterator;

import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This class is used for handling one leg of a call. That is, it represents an
 * outgoing call leg or an incoming call leg. In a telephone call, there are two
 * call legs. The outgoing call leg is the connection from the phone making the
 * call to the telephone network. The incoming call leg is a connection from the
 * telephone network to the phone being called. For a SIP call, the outbound leg
 * is the User Agent originating the call and the inbound leg is the User Agent
 * receiving the call. For each call leg, a separate SipCall object must be
 * used.
 * <p>
 * A SipCall object is created by calling SipPhone.createSipCall() or
 * SipPhone.makeCall().
 * <p>
 * Many of the methods in this class return an object or true return value if
 * successful. In case of an error or caller-specified timeout, a null object or
 * a false is returned. The getErrorMessage(), getReturnCode() and
 * getException() methods may be used for further diagnostics. The
 * getReturnCode() method returns either the SIP response code received from the
 * network (defined in SipResponse) or a SipUnit internal status/return code
 * (defined in SipSession). SipUnit internal codes are in a specially designated
 * range (SipSession.SIPUNIT_INTERNAL_RETURNCODE_MIN and upward). The
 * information provided by the getException() method is only meaningful when the
 * getReturnCode() method returns internal SipUnit return code
 * EXCEPTION_ENCOUNTERED. The getErrorMessage() method returns a descriptive
 * string indicating the cause of the problem. If an exception was involved,
 * this string will contain the name of the Exception class and the exception
 * message. This class has a method, format(), which can be called to obtain a
 * human-readable string containing all of this error information.
 * 
 * @author aab
 * 
 */
public class SipCall implements SipActionObject, MessageListener
{
    private SipPhone parent;

    private int returnCode = -1;

    private String errorMessage = "";

    private Throwable exception;

    private Address myAddress;

    private String myTag;

    private CSeqHeader cseq;

    private CallIdHeader callId;

    private SipTransaction transaction;

    private List receivedResponses; // list of SipResponse

    private List receivedRequests; // list of SipRequest

    private Dialog dialog;

    private boolean callAnswered;

    /**
     * A constructor for this class.
     * 
     * 
     */
    protected SipCall(SipPhone phone, Address myAddress)
    {
        this.parent = phone;
        this.myAddress = myAddress;

        receivedResponses = Collections.synchronizedList(new ArrayList());
        receivedRequests = Collections.synchronizedList(new ArrayList());
    }

    /**
     * This method releases all resources associated with this SipCall. This
     * SipCall object must not be used again after calling the dispose() method.
     * BYE is sent to the far end if the call dialog is in the confirmed state.
     */
    public void dispose()
    {
        parent.unlistenRequestMessage(); // TODO (buddy) - remove or refactor
        // SipSession -
        // make it per-callId. Update Subscription to do the same.

        if (dialog != null)
        {
            if (dialog.getState() != null)
            {
                if (dialog.getState().getValue() == DialogState._CONFIRMED)
                {
                    try
                    {
                        Request bye = dialog.createRequest(Request.BYE);
                        parent.addAuthorizations(callId.getCallId(), bye);
                        transaction = parent.sendRequestWithTransaction(bye,
                                false, dialog);
                    }
                    catch (Exception e)
                    {
                        SipStack.trace("Disposing call, couldn't send BYE: "
                                + e.getMessage());
                    }
                }
            }

            dialog.delete();
        }

        if (callId != null)
        {
            parent.clearAuthorizations(callId.getCallId());
        }

        parent.dropCall(this);
    }

    /**
     * Start listening for an ACK request. This is a non-blocking call. Starting
     * from the time this method is called, any received request(s) for this UA
     * are collected. After calling this method, you can call waitForAck() to
     * process the first ACK received since calling this method.
     * 
     * NOTE: it's not necessary to call this method if a previous listenForXyz()
     * method has been called and request listening has NOT been turned off
     * since (ie, method stopListeningForRequests() hasn't been called).
     * 
     * @return true unless an error is encountered, in which case false is
     *         returned.
     */
    public boolean listenForAck()
    {
        return parent.listenRequestMessage();
    }

    /**
     * Start listening for a BYE request. This is a non-blocking call. Starting
     * from the time this method is called, any received request(s) for this UA
     * are collected. After calling this method, call waitForDisconnect() to
     * process the first BYE received since calling this method.
     * 
     * @return true unless an error is encountered, in which case false is
     *         returned.
     */
    public boolean listenForDisconnect()
    {
        return parent.listenRequestMessage();
    }

    /**
     * Start listening for an INVITE request. This is a non-blocking call.
     * Starting from the time this method is called, any received request(s) for
     * this UA are collected. After calling this method, call
     * waitForIncomingCall() to process the first INVITE received since calling
     * this method.
     * 
     * @return true unless an error is encountered, in which case false is
     *         returned.
     */
    public boolean listenForIncomingCall()
    {
        return parent.listenRequestMessage();
    }

    /**
     * Start listening for a RE-INVITE request. This is a non-blocking call.
     * Starting from the time this method is called, any received request(s) for
     * this UA are collected. After calling this method, call waitForReinvite()
     * to process the first INVITE received since calling this method.
     * 
     * @return true unless an error is encountered, in which case false is
     *         returned.
     */
    public boolean listenForReinvite()
    {
        return parent.listenRequestMessage();
    }

    /**
     * Stop listening for requests on this user agent. Call this method after
     * calling any of the listenForXxx()/waitForXxx() methods, when no longer
     * looking for an incoming request. IT IS RECOMMENDED THAT THE CALLING
     * PROGRAM STOP LISTENING FOR REQUESTS WHILE NONE ARE EXPECTED. Otherwise
     * alot of overhead is used up and wasted.
     * 
     * If there are any pending requests (received but not processed yet), those
     * are discarded.
     * 
     * @return true unless an error is encountered, in which case false is
     *         returned.
     */
    public boolean stopListeningForRequests()
    {
        return parent.unlistenRequestMessage();

        // TODO: probably need several versions of unlistenRequestMessage(), to
        // handle multiple SipCalls per SipPhone; ie, here, purge parent's
        // request
        // queue of requests only with this SipCall's callId. Bigger issue -
        // wait blocking.
    }

    /**
     * The waitForDisconnect() method waits for a BYE request addressed to this
     * user agent to be received from the network. Call this method after
     * calling the listenForDisconnect() method.
     * <p>
     * This method blocks until one of the following occurs: 1) A BYE request
     * message has been received, addressed to this user agent. In this case, a
     * value of true is returned. The getLastReceivedRequest() method can be
     * called to get information about the received BYE request. Use the method
     * respondToDisconnect() for responding to the received BYE request. 2) The
     * wait timeout period specified by the parameter to this method expires.
     * false is returned in this case. 3) An error occurs. false is returned in
     * this case.
     * <p>
     * Any non-BYE requests received for this user agent are discarded while
     * waiting for a BYE message.
     * <p>
     * Regardless of the outcome, incoming requests associated with this User
     * Agent will continue to be queued up until the stopListeningForRequests()
     * method is called. IT IS RECOMMENDED THAT THE CALLING PROGRAM STOP
     * LISTENING FOR REQUESTS WHILE NONE ARE EXPECTED. Otherwise alot of
     * overhead is used up and wasted. Once a listenForXXX() method has been
     * called (pre-requisite to calling this method) and until the
     * stopListeningForRequests() method is called, the calling program can
     * continue to retrieve specific subsequently received requests by calling
     * one of the waitForXxx() methods.
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return false in the case of wait timeout or error; call getReturnCode()
     *         and/or getErrorMessage() and, if applicable, getException() for
     *         further diagnostics. A true value is returned if a BYE message
     *         was received. Call the getLastReceivedRequest() method to get
     *         information about the BYE, and call the respondToDisconnect()
     *         method to send a response to the BYE.
     */
    public boolean waitForDisconnect(long timeout)
    {
        initErrorInfo();

        RequestEvent event = parent.waitRequest(timeout);

        if (event == null)
        {
            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;
        }

        Request request = event.getRequest();
        receivedRequests.add(new SipRequest(request));

        while (request.getMethod().equals(Request.BYE) == false)
        {
            event = parent.waitRequest(timeout); // TODO, adjust

            if (event == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                setException(parent.getException());

                return false;
            }

            request = event.getRequest();
            receivedRequests.add(new SipRequest(request));
            continue;
        }

        ServerTransaction tr = event.getServerTransaction();

        if (tr == null)
        {
            try
            {
                tr = parent.getParent().getSipProvider()
                        .getNewServerTransaction(request);
            }
            catch (Exception ex)
            {
                setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
                setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                        + ex.getMessage());
                setException(ex);

                return false;
            }
        }

        dialog = tr.getDialog();

        transaction = new SipTransaction();
        transaction.setServerTransaction(tr);

        return true;
    }

    /**
     * The waitForIncomingCall() method waits for an INVITE request addressed to
     * this user agent to be received from the network. Call this method after
     * calling the listenForIncomingCall() method.
     * <p>
     * This method blocks until one of the following occurs: 1) An INVITE
     * message has been received, addressed to this user agent. In this case, a
     * value of true is returned. The getLastReceivedRequest() method can be
     * called to get information about the received INVITE request. Use the
     * method sendIncomingCallResponse() for responding to the received INVITE.
     * 2) The wait timeout period specified by the parameter to this method
     * expires. False is returned in this case. 3) An error occurs. False is
     * returned in this case.
     * <p>
     * Any non-INVITE requests received for this user agent are discarded while
     * waiting for an INVITE message.
     * <p>
     * Regardless of the outcome, incoming requests associated with this User
     * Agent will continue to be queued up until the stopListeningForRequests()
     * method is called. IT IS RECOMMENDED THAT THE CALLING PROGRAM STOP
     * LISTENING FOR REQUESTS WHILE NONE ARE EXPECTED. Otherwise alot of
     * overhead is used up and wasted. Once a listenForXXX() method has been
     * called (pre-requisite to calling this method) and until the
     * stopListeningForRequests() method is called, the calling program can
     * continue to retrieve specific subsequently received requests by calling
     * one of the waitForXxx() methods.
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return false in the case of wait timeout or error; call getReturnCode()
     *         and/or getErrorMessage() and, if applicable, getException() for
     *         further diagnostics. A true value is returned if an INVITE
     *         message was received. Call the getLastReceivedRequest() method to
     *         get information about the INVITE, and call the
     *         sendIncomingCallResponse() method to send a response to the
     *         INVITE.
     */
    public boolean waitForIncomingCall(long timeout)
    {
        initErrorInfo();

        receivedRequests.clear();
        receivedResponses.clear();
        transaction = null;
        dialog = null;
        myTag = null;
        callAnswered = false;

        RequestEvent event = parent.waitRequest(timeout);

        if (event == null)
        {
            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;
        }

        Request request = event.getRequest();
        receivedRequests.add(new SipRequest(request));

        while (request.getMethod().equals(Request.INVITE) == false)
        {
            event = parent.waitRequest(timeout); // TODO, adjust timeout

            if (event == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                setException(parent.getException());

                return false;
            }

            request = event.getRequest();
            receivedRequests.add(new SipRequest(request));
            continue;
        }

        SipStack.dumpMessage("INVITE after received by stack", request);

        ServerTransaction tr = event.getServerTransaction();
        if (tr == null)
        {
            try
            {
                tr = parent.getParent().getSipProvider()
                        .getNewServerTransaction(request);
            }
            catch (Exception ex)
            {
                setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
                setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                        + ex.getMessage());
                setException(ex);

                return false;
            }
        }

        transaction = new SipTransaction();
        transaction.setServerTransaction(tr);

        callId = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        parent.enableAuthorization(callId.getCallId());

        cseq = (CSeqHeader) request.getHeader(CSeqHeader.NAME);

        return true;
    }

    /**
     * The waitForAck() method waits for an ACK request addressed to this user
     * agent to be received from the network. Prior to calling this method, any
     * of the listenForXyz() methods must have already been called to turn on
     * request listening. You can call this method to wait for any kind of ACK
     * (invite, re-invite...).
     * <p>
     * This method blocks until one of the following occurs: 1) An ACK request
     * message has been received, addressed to this user agent. In this case, a
     * value of true is returned. The getLastReceivedRequest() method can be
     * called to get information about the received ACK request. 2) The wait
     * timeout period specified by the parameter to this method expires. false
     * is returned in this case. 3) An error occurs. false is returned in this
     * case.
     * <p>
     * Any non-ACK requests received for this user agent are discarded while
     * waiting for an ACK message.
     * <p>
     * Regardless of the outcome, incoming requests associated with this User
     * Agent will continue to be queued up until the stopListeningForRequests()
     * method is called. IT IS RECOMMENDED THAT THE CALLING PROGRAM STOP
     * LISTENING FOR REQUESTS WHILE NONE ARE EXPECTED. Otherwise alot of
     * overhead is used up and wasted. Once a listenForXXX() method has been
     * called (pre-requisite to calling this or other 'wait' methods) and until
     * the stopListeningForRequests() method is called, the calling program can
     * continue to retrieve specific subsequently received requests by calling
     * one of the waitForXxx() methods.
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return false in the case of wait timeout or error; call getReturnCode()
     *         and/or getErrorMessage() and, if applicable, getException() for
     *         further diagnostics. A true value is returned if an ACK message
     *         was received. Call the getLastReceivedRequest() method to get
     *         information about the ACK.
     */
    public boolean waitForAck(long timeout)
    {
        initErrorInfo();

        RequestEvent event = parent.waitRequest(timeout);

        if (event == null)
        {
            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;
        }

        Request request = event.getRequest();
        receivedRequests.add(new SipRequest(request));

        while (request.getMethod().equals(Request.ACK) == false)
        {
            event = parent.waitRequest(timeout); // TODO, adjust

            if (event == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                setException(parent.getException());

                return false;
            }

            request = event.getRequest();
            receivedRequests.add(new SipRequest(request));
            continue;
        }

        ServerTransaction tr = event.getServerTransaction();

        if (tr == null)
        {
            try
            {
                tr = parent.getParent().getSipProvider()
                        .getNewServerTransaction(request);
            }
            catch (Exception ex)
            {
                setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
                setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                        + ex.getMessage());
                setException(ex);

                return false;
            }
        }

        return true;
    }

    /**
     * This method sends a basic response to a previously received INVITE
     * request. The response is constructed based on the parameters passed in.
     * Call this method after waitForIncomingCall() returns true. Call this
     * method multiple times to send multiple responses to the received INVITE.
     * The INVITE being responded to must be the last received request on this
     * SipCall.
     * 
     * @param statusCode
     *            The status code of the response to send (may use SipResponse
     *            constants).
     * @param reasonPhrase
     *            If not null, the reason phrase to send.
     * @param expires
     *            If not -1, an expiration time is added to the response. This
     *            parameter indicates the duration the message is valid, in
     *            seconds.
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean sendIncomingCallResponse(int statusCode,
            String reasonPhrase, int expires)
    {
        return sendIncomingCallResponse(statusCode, reasonPhrase, expires,
                null, null, null);
    }

    /**
     * This method is the same as the basic sendIncomingCallResponse() method
     * except that it allows the caller to specify a message body and/or
     * additional JAIN-SIP API message headers to add to or replace in the
     * outbound message. Use of this method requires knowledge of the JAIN-SIP
     * API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean sendIncomingCallResponse(int statusCode,
            String reasonPhrase, int expires, ArrayList additionalHeaders,
            ArrayList replaceHeaders, String body)
    {
        initErrorInfo();
        if (myTag == null)
        {
            myTag = parent.generateNewTag();
        }

        if (parent.sendReply(transaction, statusCode, reasonPhrase, myTag,
                (Address) parent.getContactInfo().getContactHeader()
                        .getAddress().clone(), expires, additionalHeaders,
                replaceHeaders, body) != null)
        {
            dialog = transaction.getServerTransaction().getDialog();
            if (statusCode == SipResponse.OK)
            {
                callAnswered = true;
            }

            return true;
        }

        setReturnCode(parent.getReturnCode());
        setException(parent.getException());
        setErrorMessage("sendIncomingCallResponse() - "
                + parent.getErrorMessage());

        return false;
    }

    /**
     * This method is the same as the basic sendIncomingCallResponse() method
     * except that it allows the caller to specify a message body and/or
     * additional message headers to add to or replace in the outbound message
     * without requiring knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean sendIncomingCallResponse(int statusCode,
            String reasonPhrase, int expires, String body, String contentType,
            String contentSubType, ArrayList additionalHeaders,
            ArrayList replaceHeaders)
    {
        try
        {
            return sendIncomingCallResponse(statusCode, reasonPhrase, expires,
                    parent.toHeader(additionalHeaders, contentType,
                            contentSubType), parent.toHeader(replaceHeaders),
                    body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }
    }

    /**
     * The waitForReinvite() method waits for a RE-INVITE request addressed to
     * this user agent to be received from the network. Call this method after
     * calling the listenForReinvite() method.
     * <p>
     * This method blocks until one of the following occurs: 1) An INVITE
     * message has been received on the current dialog. In this case, a non-null
     * SipTransaction object is returned. The getLastReceivedRequest() method
     * can be called to get information about the received RE-INVITE request.
     * The SipTransaction object is required for responding to the received
     * RE-INVITE. Use the method respondToReinvite() for sending a response to
     * the received RE-INVITE. 2) The wait timeout period specified by the
     * parameter to this method expires. Null is returned in this case. 3) An
     * error occurs. Null is returned in this case.
     * <p>
     * Any non-INVITE requests received for this user agent are discarded while
     * waiting for the RE-INVITE message.
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return null in the case of wait timeout or error; call getReturnCode()
     *         and/or getErrorMessage() and, if applicable, getException() for
     *         further diagnostics. A SipTransaction object is returned if a
     *         RE-INVITE message was received. Call the getLastReceivedRequest()
     *         method to get information about the RE-INVITE, and call the
     *         respondToReinvite() method to send a response to the RE-INVITE,
     *         passing it the SipTransaction object returned here.
     */
    public SipTransaction waitForReinvite(long timeout)
    {
        initErrorInfo();

        if (dialog == null)
        {
            setReturnCode(SipSession.INVALID_OPERATION);
            setErrorMessage((String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - dialog hasn't been created, can't wait for RE-INVITE");
            return null;
        }

        RequestEvent event = parent.waitRequest(timeout);

        if (event == null)
        {
            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return null;
        }

        Request request = event.getRequest();
        receivedRequests.add(new SipRequest(request));

        while (request.getMethod().equals(Request.INVITE) == false)
        {
            event = parent.waitRequest(timeout); // TODO, adjust timeout

            if (event == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                setException(parent.getException());

                return null;
            }

            request = event.getRequest();
            receivedRequests.add(new SipRequest(request));
            continue;
        }

        SipStack.dumpMessage("INVITE after received by stack", request);

        ServerTransaction tr = event.getServerTransaction();
        if (tr == null)
        {
            try
            {
                tr = parent.getParent().getSipProvider()
                        .getNewServerTransaction(request);
            }
            catch (Exception ex)
            {
                setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
                setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                        + ex.getMessage());
                setException(ex);

                return null;
            }
        }

        SipTransaction siptrans = new SipTransaction();
        siptrans.setServerTransaction(tr);

        return siptrans;
    }

    /**
     * This method sends a basic response to a previously received RE-INVITE
     * request. The response is constructed based on the parameters passed in.
     * Call this method after waitForReinvite() returns non-null. Call this
     * method multiple times to send multiple responses to the received
     * RE-INVITE.
     * 
     * @param siptrans
     *            This is the object that was returned by method
     *            waitForReinvite(). It identifies a specific RE-INVITE
     *            transaction.
     * @param statusCode
     *            The status code of the response to send (may use SipResponse
     *            constants).
     * @param reasonPhrase
     *            If not null, the reason phrase to send.
     * @param expires
     *            If not -1, an expiration time is added to the response. This
     *            parameter indicates the duration the message is valid, in
     *            seconds.
     * @param newContact
     *            An URI string (ex: sip:bob@192.0.2.4:5093) for updating the
     *            remote target URI kept by the far end (target refresh), or
     *            null to not change that information.
     * @param displayName
     *            Display name to set in the contact header sent to the far end
     *            if newContact is not null.
     * @param body
     *            A String to be used as the body of the message, for changing
     *            the media session. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean respondToReinvite(SipTransaction siptrans, int statusCode,
            String reasonPhrase, int expires, String newContact,
            String displayName, String body, String contentType,
            String contentSubType)
    {
        return respondToReinvite(siptrans, statusCode, reasonPhrase, expires,
                newContact, displayName, body, contentType, contentSubType,
                null, null);
    }

    /**
     * This method is the same as the basic respondToReinvite() plus it
     * additionally allows the caller to specify additional message headers to
     * add to or replace in the response message without requiring knowledge of
     * the JAIN-SIP API.
     * 
     * Additional parameters handled by this method include:
     * 
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * 
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean respondToReinvite(SipTransaction siptrans, int statusCode,
            String reasonPhrase, int expires, String newContact,
            String displayName, String body, String contentType,
            String contentSubType, ArrayList additionalHeaders,
            ArrayList replaceHeaders)
    {
        try
        {
            return respondToReinvite(siptrans, statusCode, reasonPhrase,
                    expires, newContact, displayName, parent.toHeader(
                            additionalHeaders, contentType, contentSubType),
                    parent.toHeader(replaceHeaders), body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }

    }

    /**
     * This method is equivalent to the basic respondToReinvite() method except
     * that it allows the caller to specify additional JAIN-SIP API message
     * headers to add to or replace in the outbound message. Use of this method
     * requires knowledge of the JAIN-SIP API.
     * 
     * NOTE: The additionalHeaders parameter passed to this method must contain
     * a ContentTypeHeader in order for a body to be included in the message.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * 
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * 
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean respondToReinvite(SipTransaction siptrans, int statusCode,
            String reasonPhrase, int expires, String newContact,
            String displayName, ArrayList additionalHeaders,
            ArrayList replaceHeaders, String body)
    {
        initErrorInfo();

        try
        {
            ContactHeader contact_hdr = null;
            if (newContact == null)
            {
                contact_hdr = (ContactHeader) parent.getContactInfo()
                        .getContactHeader().clone();
            }
            else
            {
                contact_hdr = parent.updateContactInfo(newContact, displayName);
            }

            if (additionalHeaders == null)
            {
                additionalHeaders = new ArrayList();
            }
            additionalHeaders.add(contact_hdr);

            if (parent.sendReply(siptrans, statusCode, reasonPhrase, null,
                    null, expires, additionalHeaders, replaceHeaders, body) == null)
            {
                setException(parent.getException());
                setErrorMessage(parent.getErrorMessage());
                setReturnCode(parent.getReturnCode());

                return false;
            }

            return true;
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }

    }

    /**
     * This method sends a basic response to a previously received BYE request.
     * The response is constructed using a status code of OK. Call this method
     * after waitForDisconnect() returns true. Call this method multiple times
     * to send multiple OK responses to the received BYE. The BYE being
     * responded to must be the last received request on this SipCall.
     * 
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean respondToDisconnect()
    {
        return respondToDisconnect(SipResponse.OK, "OK");
    }

    /**
     * This method is the same as the basic respondToDisconnect() method except
     * that it uses the given parameters for the status code and reason in the
     * response message sent out.
     * 
     * @param statusCode
     *            The integer status code to use (ie, SipResponse.OK).
     * @param reasonPhrase
     *            The String reason phrase to use.
     * @return true if the response was successfully sent, false otherwise.
     */
    public boolean respondToDisconnect(int statusCode, String reasonPhrase)
    {
        initErrorInfo();
        if (parent.sendReply(transaction, statusCode, reasonPhrase, myTag,
                null, -1) != null)
        {
            return true;
        }

        setReturnCode(parent.getReturnCode());
        setException(parent.getException());
        setErrorMessage("respondToDisconnect() - " + parent.getErrorMessage());

        return false;
    }

    /**
     * This method is the same as the basic respondToDisconnect() method except
     * that it uses the given parameters for the status code and reason in the
     * response message sent out and allows the caller to specify a message body
     * and/or additional JAIN-SIP API message headers to add to or replace in
     * the outbound message. Use of this method requires knowledge of the
     * JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param statusCode
     *            The integer status code to use (ie, SipResponse.OK).
     * @param reasonPhrase
     *            The String reason phrase to use.
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean respondToDisconnect(int statusCode, String reasonPhrase,
            ArrayList additionalHeaders, ArrayList replaceHeaders, String body)
    {
        initErrorInfo();
        if (parent.sendReply(transaction, statusCode, reasonPhrase, myTag,
                null, -1, additionalHeaders, replaceHeaders, body) != null)
        {
            return true;
        }

        setReturnCode(parent.getReturnCode());
        setException(parent.getException());
        setErrorMessage("respondToDisconnect(JSIP headers/body) - "
                + parent.getErrorMessage());

        return false;
    }

    /**
     * This method is the same as the basic respondToDisconnect() method except
     * that it uses the given parameters for the status code and reason in the
     * response message sent out and allows the caller to specify a message body
     * and/or additional message headers to add to or replace in the outbound
     * message without requiring knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param statusCode
     *            The integer status code to use (ie, SipResponse.OK).
     * @param reasonPhrase
     *            The String reason phrase to use.
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean respondToDisconnect(int statusCode, String reasonPhrase,
            String body, String contentType, String contentSubType,
            ArrayList additionalHeaders, ArrayList replaceHeaders)
    {
        initErrorInfo();
        if (parent.sendReply(transaction, statusCode, reasonPhrase, myTag,
                null, -1, body, contentType, contentSubType, additionalHeaders,
                replaceHeaders) != null)
        {
            return true;
        }

        setReturnCode(parent.getReturnCode());
        setException(parent.getException());
        setErrorMessage("respondToDisconnect(String headers/body) - "
                + parent.getErrorMessage());

        return false;
    }

    /**
     * This basic method is used to initiate an outgoing call. That is, it
     * applies to the scenario where a UAC is originating a call to the network.
     * There are two ways to make an outgoing call: 1) Use
     * SipPhone.createSipCall() and then call this method, when you need to see
     * intermediate/provisional responses received - you will have to handle
     * each yourself. Or, 2) use one of the SipPhone.makeCall() methods when you
     * want to establish an outgoing call without worrying about the call
     * establishment details (TRYING, authentication challenge, etc.).
     * Regardless, all received responses are collected, so you will be able to
     * see intermediate/provisional responses received (albeit after the fact,
     * with makeCall()).
     * <p>
     * This method returns when the request message has been sent out. Your
     * calling program must subsequently call the waitOutgoingCallResponse()
     * method (one or more times) to get the result(s), and optionally at some
     * point, waitForAnswer() if you're no longer interested in processing
     * intermediate responses.
     * 
     * @param fromUri
     *            An URI string (ex: sip:bob@192.0.2.4), or null to use the
     *            default 'from' address (me) specified when the SipPhone object
     *            was created (SipStack.createSipPhone()).
     * @param toUri
     *            The URI (sip:bob@nist.gov) to which the call should be
     *            directed
     * @param viaNonProxyRoute
     *            Indicates whether to route the INVITE via Proxy or some other
     *            route. If null, route the call to the Proxy that was specified
     *            when the SipPhone object was created
     *            (SipStack.createSipPhone()). Else route it to the given node,
     *            which is specified as "hostaddress:port;parms/transport" i.e.
     *            129.1.22.333:5060;lr/UDP.
     * @return true if the message was successfully sent, false otherwise.
     */
    public boolean initiateOutgoingCall(String fromUri, String toUri,
            String viaNonProxyRoute)
    {
        return initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute, null);
    }

    /**
     * This method is the same as the basic initiateOutgoingCall() method except
     * that it allows the caller to specify a message body and/or additional
     * JAIN-SIP API message headers to add to or replace in the outbound
     * message. Use of this method requires knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean initiateOutgoingCall(String fromUri, String toUri,
            String viaNonProxyRoute, ArrayList additionalHeaders,
            ArrayList replaceHeaders, String body)
    {
        return initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute, null,
                additionalHeaders, replaceHeaders, body);
    }

    /**
     * This method is the same as the basic initiateOugoingCall() method except
     * that it allows the caller to specify a message body and/or additional
     * message headers to add to or replace in the outbound message without
     * requiring knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean initiateOutgoingCall(String fromUri, String toUri,
            String viaNonProxyRoute, String body, String contentType,
            String contentSubType, ArrayList additionalHeaders,
            ArrayList replaceHeaders)
    {
        try
        {
            return initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute, null,
                    parent.toHeader(additionalHeaders, contentType,
                            contentSubType), parent.toHeader(replaceHeaders),
                    body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }
    }

    protected boolean initiateOutgoingCall(String fromUri, String toUri,
            String viaNonProxyRoute, MessageListener respListener)
    {
        return initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute,
                respListener, null, null, null);
    }

    protected boolean initiateOutgoingCall(String fromUri, String toUri,
            String viaNonProxyRoute, MessageListener respListener,
            ArrayList additionalHeaders, ArrayList replaceHeaders, String body)
    {
        initErrorInfo();

        transaction = null;
        dialog = null;
        receivedResponses.clear();
        receivedRequests.clear();
        callAnswered = false;

        toUri = toUri.trim();
        if (fromUri == null)
        {
            fromUri = myAddress.getURI().toString();
        }

        try
        {
            AddressFactory addr_factory = parent.getAddressFactory();
            HeaderFactory hdr_factory = parent.getHeaderFactory();

            URI request_uri = addr_factory.createURI(toUri);
            if (request_uri.isSipURI() == false)
            {
                setReturnCode(SipSession.UNSUPPORTED_URI_SCHEME);
                setErrorMessage("URI " + toUri + " is not a Sip URI");
                return false;
            }

            // create a new Call-ID
            callId = parent.getNewCallIdHeader();
            parent.enableAuthorization(callId.getCallId());

            String method = Request.INVITE;

            cseq = hdr_factory.createCSeqHeader(cseq == null ? 1 : (cseq
                    .getSeqNumber() + 1), method);

            /*
             * Requests within a dialog MUST contain strictly monotonically
             * increasing and contiguous CSeq sequence numbers
             * (increasing-by-one) in each direction (excepting ACK and CANCEL
             * of course, whose numbers equal the requests being acknowledged or
             * cancelled). Therefore, if the local sequence number is not empty,
             * the value of the local sequence number MUST be incremented by
             * one, and this value MUST be placed into the CSeq header field. If
             * the local sequence number is empty, an initial value MUST be
             * chosen using the guidelines of Section 8.1.1.5. The method field
             * in the CSeq header field value MUST match the method of the
             * request.
             */

            Address to_address = addr_factory.createAddress(request_uri);
            ToHeader to_header = hdr_factory.createToHeader(to_address, null);

            myTag = parent.generateNewTag();
            FromHeader from_header = hdr_factory.createFromHeader(myAddress,
                    myTag);

            MaxForwardsHeader max_forwards = hdr_factory
                    .createMaxForwardsHeader(SipPhone.MAX_FORWARDS_DEFAULT);

            ArrayList via_headers = parent.getViaHeaders();

            Request msg = parent.getMessageFactory().createRequest(request_uri,
                    method, callId, cseq, from_header, to_header, via_headers,
                    max_forwards);

            msg.addHeader((ContactHeader) parent.getContactInfo()
                    .getContactHeader().clone());

            // create and add the RouteHeader if needed
            boolean viaProxy = true;
            if (viaNonProxyRoute != null)
            {
                viaProxy = false;

                int xport_offset = viaNonProxyRoute.indexOf('/');
                SipURI route_uri = addr_factory.createSipURI(null,
                        viaNonProxyRoute.substring(0, xport_offset));
                route_uri.setTransportParam(viaNonProxyRoute
                        .substring(xport_offset + 1));
                route_uri.setSecure(((SipURI) request_uri).isSecure());
                route_uri.setLrParam();

                Address route_address = addr_factory.createAddress(route_uri);
                msg.addHeader(hdr_factory.createRouteHeader(route_address));
            }

            // send the message
            synchronized (this) // needed for asynchronous response -
            // processEvent()
            {
                transaction = parent.sendRequestWithTransaction(msg, viaProxy,
                        null, respListener, additionalHeaders, replaceHeaders,
                        body);
            }

            if (transaction != null)
            {
                SipStack.dumpMessage("INVITE after sending out through stack",
                        transaction.getClientTransaction().getRequest());
                return true;
            }

            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;
        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            return false;
        }
    }

    /**
     * This method is the same as the basic initiateOutgoingCall() method,
     * without the 'from URI' parameter which will be defaulted to the 'from'
     * address (me) specified when the SipPhone object was created
     * (SipStack.createSipPhone()).
     * 
     * @param toUri
     *            The URI (sip:bob@nist.gov) to which the call should be
     *            directed
     * @param viaNonProxyRoute
     *            Indicates whether to route the INVITE via Proxy or some other
     *            route. If null, route the call to the Proxy that was specified
     *            when the SipPhone object was created
     *            (SipStack.createSipPhone()). Else route it to the given node,
     *            which is specified as "hostaddress:port;parms/transport" i.e.
     *            129.1.22.333:5060;lr/UDP.
     * @return true if the message was successfully sent, false otherwise.
     */
    public boolean initiateOutgoingCall(String toUri, String viaNonProxyRoute)
    {
        return initiateOutgoingCall(null, toUri, viaNonProxyRoute);
    }

    /**
     * This method is used to re-initiate an outgoing call. That is, it applies
     * to the scenario where a UAC is re-originating a call to the network
     * because a previous origination attempt failed. An example of when this
     * would be used is resending an INVITE after receiving an authentication
     * challenge.
     * <p>
     * Knowledge of the JAIN-SIP API is required to use this method.
     * <p>
     * This method returns when the request message has been sent out. The
     * calling program must subsequently call the waitOutgoingCallResponse()
     * method (one or more times) to get the result(s), and optionally at some
     * point, waitForAnswer() if you're no longer interested in processing
     * intermediate responses.
     * 
     * @param msg
     *            The Request message to resend. The CSEQ number will be
     *            incremented by this method, otherwise the request is sent as
     *            is.
     * 
     * @return true if the message was successfully sent, false otherwise.
     */
    protected boolean reInitiateOutgoingCall(Request msg)
    {
        return reInitiateOutgoingCall(msg, null);
    }

    protected boolean reInitiateOutgoingCall(Request msg,
            MessageListener respListener)
    {
        initErrorInfo();

        // clean up last transaction and dialog
        if (transaction != null)
        {
            parent.clearTransaction(transaction);
            transaction = null;
        }

        try
        {
            // bump up the sequence number
            cseq.setSeqNumber(cseq.getSeqNumber() + 1);
            msg.setHeader(cseq);

            // send the message
            synchronized (this) // needed for asynchronous response -
            // processEvent()
            {
                transaction = parent.sendRequestWithTransaction(msg, false,
                        null, respListener);
            }

            if (transaction != null)
            {
                return true;
            }

            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;
        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            return false;
        }
    }

    /**
     * This method sends a basic RE-INVITE on the current dialog.
     * <p>
     * This method returns when the request message has been sent out. The
     * calling program must subsequently call the waitReinviteResponse() method
     * (one or more times) to get the response(s) and perhaps the
     * sendReinviteOkAck() method to send an ACK. On the receive side, pertinent
     * methods include waitForReinvite(), respondToReinvite(), and waitForAck().
     * 
     * @param newContact
     *            An URI string (ex: sip:bob@192.0.2.4:5093) for updating the
     *            remote target URI kept by the far end (target refresh), or
     *            null to not change that information.
     * @param displayName
     *            Display name to set in the contact header sent to the far end
     *            if newContact is not null.
     * @param body
     *            A String to be used as the body of the message, for changing
     *            the media session. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @return A SipTransaction object if the message was successfully sent,
     *         null otherwise. You don't need to anything with this returned
     *         object other than to pass it to methods that you call
     *         subsequently for this operation, namely waitReinviteResponse()
     *         and sendReinviteOkAck().
     */
    public SipTransaction sendReinvite(String newContact, String displayName,
            String body, String contentType, String contentSubType)
    {
        return sendReinvite(newContact, displayName, body, contentType,
                contentSubType, null, null);
    }

    /**
     * This method is equivalent to the basic sendReinvite() method except that
     * it allows the caller to specify additional JAIN-SIP API message headers
     * to add to or replace in the outbound message. Use of this method requires
     * knowledge of the JAIN-SIP API.
     * 
     * NOTE: The additionalHeaders parameter passed to this method must contain
     * a ContentTypeHeader in order for a body to be included in the message.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * 
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * 
     * @return A SipTransaction object if the message was successfully sent,
     *         null otherwise. You don't need to anything with this returned
     *         object other than to pass it to methods that you call
     *         subsequently for this operation, namely waitReinviteResponse()
     *         and sendReinviteOkAck().
     */
    public SipTransaction sendReinvite(String newContact, String displayName,
            ArrayList additionalHeaders, ArrayList replaceHeaders, String body)
    {
        initErrorInfo();

        if (dialog == null)
        {
            setReturnCode(SipSession.INVALID_OPERATION);
            setErrorMessage((String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - dialog hasn't been established, can't send RE-INVITE");
            return null;
        }

        try
        {
            Request req = dialog.createRequest(Request.INVITE);

            parent.addAuthorizations(callId.getCallId(), req);
            MaxForwardsHeader mf = parent.getHeaderFactory()
                    .createMaxForwardsHeader(10);
            req.setHeader(mf);

            if (newContact != null)
            {
                req
                        .setHeader(parent.updateContactInfo(newContact,
                                displayName));
            }
            else
            {
                req.setHeader((ContactHeader) parent.getContactInfo()
                        .getContactHeader().clone());
            }

            SipStack.dumpMessage("We have created this RE-INVITE", req);

            SipTransaction siptrans;
            synchronized (this) // needed for asynchronous response -
            // processEvent(), although we're not using that here now.
            // Change there would be needed because that uses attribute
            // 'transaction'
            {
                siptrans = parent.sendRequestWithTransaction(req, false,
                        dialog, additionalHeaders, replaceHeaders, body);
            }

            if (siptrans != null)
            {
                return siptrans;
            }

            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());
            return null;

        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            return null;
        }

    }

    /**
     * This method is the same as the basic sendReinvite() method except that it
     * allows the caller to specify additional message headers to add to or
     * replace in the outbound message without requiring knowledge of the
     * JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     * @return A SipTransaction object if the message was successfully sent,
     *         null otherwise. You don't need to anything with this returned
     *         object other than to pass it to methods that you call
     *         subsequently for this operation, namely waitReinviteResponse()
     *         and sendReinviteOkAck().
     */
    public SipTransaction sendReinvite(String newContact, String displayName,
            String body, String contentType, String contentSubType,
            ArrayList additionalHeaders, ArrayList replaceHeaders)
    {
        try
        {
            return sendReinvite(newContact, displayName, parent.toHeader(
                    additionalHeaders, contentType, contentSubType), parent
                    .toHeader(replaceHeaders), body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return null;
        }

    }

    /**
     * This method sends a basic ACK in response to an OK received in response
     * to a sent INVITE.
     * 
     * @return true if the message was successfully sent, false otherwise.
     */
    public boolean sendInviteOkAck()
    {
        return sendInviteOkAck(null, null, null);
    }

    /**
     * This method is the same as the basic sendInviteOkAck() method except that
     * it allows the caller to specify a message body and/or additional JAIN-SIP
     * API message headers to add to or replace in the outbound message. Use of
     * this method requires knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean sendInviteOkAck(ArrayList additionalHeaders,
            ArrayList replaceHeaders, String body)
    {
        /*
         * RULES for Subsequent Request within a dialog: To = dialog state
         * remote URI To tag = dialog ID remote tag (if null, omit tag param)
         * From = dialog state local URI From tag = dialog ID local tag (if
         * null, omit tag param) Call-ID = dialog Call-ID CSEQ sequence# =
         * increase by 1 for this direction (except ACK, CANCEL) CSEQ method =
         * Request method ACK: Request URI for ACK = Request URI for INVITE,
         * else Request URI = To URI ACK: Via branch for ACK = Via branch for
         * INVITE? ACK: Add route header with far-end contact URI ACK: Add
         * remaining route headers received in response, in reverse order and
         * set Request URI & route headers according to loose/strict routing -
         * Dialog.sendAck() does this for us? ACK: must have same credentials as
         * the succeeding invite.
         * 
         */

        initErrorInfo();

        if (dialog == null)
        {
            returnCode = SipSession.INVALID_OPERATION;
            errorMessage = (String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - dialog hasn't been established";

            return false;
        }

        try
        {
            Thread.sleep(100); // TODO - investigate more. why needed? if so,
            // use OK timestamp here

            Request ack = dialog.createRequest(Request.ACK);
            parent.addAuthorizations(callId.getCallId(), ack);
            parent.putElements(ack, additionalHeaders, replaceHeaders, body);

            dialog.sendAck(ack);

            return true;
        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());

            return false;
        }
    }

    /**
     * This method is the same as the basic sendInviteOkAck() method except that
     * it allows the caller to specify a message body and/or additional message
     * headers to add to or replace in the outbound message without requiring
     * knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean sendInviteOkAck(String body, String contentType,
            String contentSubType, ArrayList additionalHeaders,
            ArrayList replaceHeaders)
    {
        try
        {
            return sendInviteOkAck(parent.toHeader(additionalHeaders,
                    contentType, contentSubType), parent
                    .toHeader(replaceHeaders), body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }
    }

    /**
     * This basic method sends an ACK in response to an OK received in response
     * to a sent RE-INVITE.
     * 
     * @param siptrans
     *            This is the object that was returned by method sendReinvite().
     *            It identifies a specific RE-INVITE transaction.
     * @return true if the message was successfully sent, false otherwise.
     */
    public boolean sendReinviteOkAck(SipTransaction siptrans)
    {
        return sendReinviteOkAck(siptrans, null, null, null);
    }

    /**
     * This method is the same as the basic sendReinviteOkAck() method except
     * that it allows the caller to specify a message body and/or additional
     * JAIN-SIP API message headers to add to or replace in the outbound
     * message. Use of this method requires knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean sendReinviteOkAck(SipTransaction siptrans,
            ArrayList additionalHeaders, ArrayList replaceHeaders, String body)
    {
        initErrorInfo();

        if ((siptrans == null) || (siptrans.getRequest() == null)
                || (dialog == null))
        {
            returnCode = SipSession.INVALID_OPERATION;
            errorMessage = (String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - RE-INVITE transaction info missing";
            return false;
        }

        try
        {
            Thread.sleep(10); // TODO - needed here? see sendInviteOkAck().

            Request ack = dialog.createRequest(Request.ACK);

            parent.addAuthorizations(callId.getCallId(), ack);
            parent.putElements(ack, additionalHeaders, replaceHeaders, body);

            dialog.sendAck(ack);

            return true;
        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());

            return false;
        }
    }

    /**
     * This method is the same as the basic sendReinviteOkAck() method except
     * that it allows the caller to specify a message body and/or additional
     * message headers to add to or replace in the outbound message without
     * requiring knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean sendReinviteOkAck(SipTransaction siptrans, String body,
            String contentType, String contentSubType,
            ArrayList additionalHeaders, ArrayList replaceHeaders)
    {
        try
        {
            return sendReinviteOkAck(siptrans, parent.toHeader(
                    additionalHeaders, contentType, contentSubType), parent
                    .toHeader(replaceHeaders), body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }
    }

    /**
     * This method is the same as the other waitOutgoingCallResponse() method
     * with an indefinite (no) wait timeout.
     * 
     * @return true if a response was received - in that case, call
     *         getReturnCode() to get the status code that was contained in the
     *         received response, or call getLastReceivedResponse() to see the
     *         response details. Returns false if timeout or error.
     */
    public boolean waitOutgoingCallResponse()
    {
        return waitOutgoingCallResponse(0);
    }

    /**
     * The waitOutgoingCallResponse() method waits for a response to be received
     * from the network for a sent INVITE. Call this method after calling
     * initiateOutgoingCall() (or SipPhone.makeCall() if you know the
     * transaction is still up).
     * <p>
     * This method blocks until one of the following occurs: 1) A response
     * message has been received. In this case, a value of true is returned.
     * Call the getLastReceivedResponse() method to get the response details.
     * Use the method sendInviteOkAck() for responding to an OK. 2) A timeout
     * occurs. A false value is returned in this case. 3) An error occurs. False
     * is returned in this case.
     * <p>
     * Regardless of the outcome, getReturnCode() can be called after this
     * method returns to get the status code: IE, the SIP response code received
     * from the network (defined in SipResponse, along with the corresponding
     * textual equivalent) or a SipUnit internal status/return code (defined in
     * SipSession, along with the corresponding textual equivalent). SipUnit
     * internal codes are in a specially designated range
     * (SipSession.SIPUNIT_INTERNAL_RETURNCODE_MIN and upward).
     * <p>
     * This method can be called repeatedly to get each subsequently received
     * response.
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return true if a response was received - in that case, call
     *         getReturnCode() to get the status code that was contained in the
     *         received response, and/or call getLastReceivedResponse() to see
     *         the response details. Returns false if timeout or error.
     */
    public boolean waitOutgoingCallResponse(long timeout)
    {
        initErrorInfo();

        if (transaction == null)
        {
            returnCode = SipSession.INVALID_OPERATION;
            errorMessage = (String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - no outgoing transactional call in progress";
            return false;
        }

        EventObject response_event = parent.waitResponse(transaction, timeout);

        if (response_event == null)
        {
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());
            setReturnCode(parent.getReturnCode());
            return false;
        }

        if (response_event instanceof TimeoutEvent == true)
        {
            setReturnCode(SipPhone.TIMEOUT_OCCURRED);
            setErrorMessage("A Timeout Event was received");
            return false;
        }

        Response resp = ((ResponseEvent) response_event).getResponse();
        receivedResponses.add(new SipResponse(resp));
        SipStack.trace("Outgoing call response received: " + resp.toString());

        setReturnCode(resp.getStatusCode());
        if (returnCode == SipResponse.OK)
        {
            callAnswered = true;
        }

        dialog = transaction.getClientTransaction().getDialog();

        /*
         * Note, on future requests: add RouteHeaders per dialog.getRouteSet()
         * (RFC: The calling user agent client copies the RecordRouteHeaders
         * into RouteHeaders of subsequent Requests within the same call leg,
         * reversing the order, so that the first entry is closest to the user
         * agent client. If the Response contained a ContactHeader field, the
         * calling user agent adds its content as the last RouteHeader. Unless
         * this would cause a loop, a client must send subsequent Requests for
         * this call leg to the Address URI in the first RouteHeader and remove
         * that entry.)
         */

        return true;
    }

    /**
     * The waitReinviteResponse() method waits for a response to be received
     * from the network for a sent RE-INVITE. Call this method after calling
     * sendReinvite().
     * <p>
     * This method blocks until one of the following occurs: 1) A response
     * message has been received. In this case, a value of true is returned.
     * Call the getLastReceivedResponse() method to get the response details.
     * Use the method sendReinviteOkAck() for responding to an OK. 2) A timeout
     * occurs. A false value is returned in this case. 3) An error occurs. False
     * is returned in this case.
     * <p>
     * Regardless of the outcome, getReturnCode() can be called after this
     * method returns to get the status code: IE, the SIP response code received
     * from the network (defined in SipResponse, along with the corresponding
     * textual equivalent) or a SipUnit internal status/error code (defined in
     * SipSession, along with the corresponding textual equivalent). SipUnit
     * internal codes are in a specially designated range
     * (SipSession.SIPUNIT_INTERNAL_RETURNCODE_MIN and upward).
     * <p>
     * This method can be called repeatedly to get each subsequently received
     * response for this particular RE-INVITE transaction.
     * 
     * @param siptrans
     *            This is the object that was returned by method sendReinvite().
     *            It identifies a specific RE-INVITE transaction.
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return true if a response was received - in that case, call
     *         getReturnCode() to get the status code that was contained in the
     *         received response, and/or call getLastReceivedResponse() to see
     *         the response details. Returns false if timeout or error.
     */
    public boolean waitReinviteResponse(SipTransaction siptrans, long timeout)
    {
        initErrorInfo();

        if (siptrans == null)
        {
            returnCode = SipSession.INVALID_OPERATION;
            errorMessage = (String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - no RE-INVITE transaction object given";
            return false;
        }

        EventObject response_event = parent.waitResponse(siptrans, timeout);

        if (response_event == null)
        {
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());
            setReturnCode(parent.getReturnCode());
            return false;
        }

        if (response_event instanceof TimeoutEvent == true)
        {
            setReturnCode(SipPhone.TIMEOUT_OCCURRED);
            setErrorMessage("A Timeout Event was received");
            return false;
        }

        Response resp = ((ResponseEvent) response_event).getResponse();
        receivedResponses.add(new SipResponse(resp));
        SipStack.trace("RE-INVITE response received: " + resp.toString());

        setReturnCode(resp.getStatusCode());

        return true;
    }

    /**
     * This method sends a basic BYE message. It incorporates required
     * authorization headers that have previously been accumulated during the
     * call. This SipCall object will handle an authentication request received
     * in response to the BYE.
     * 
     * @return true if the message was successfully sent, false otherwise.
     */
    public boolean disconnect()
    {
        return disconnect(null, null, null);
    }

    /**
     * This method is the same as the basic disconnect() method except that it
     * allows the caller to specify a message body and/or additional JAIN-SIP
     * API message headers to add to or replace in the outbound message. Use of
     * this method requires knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param additionalHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message. These headers are added
     *            to the message after a correct message has been constructed.
     *            Note that if you try to add a header that there is only
     *            supposed to be one of in a message, and it's already there and
     *            only one single value is allowed for that header, then this
     *            header addition attempt will be ignored. Use the
     *            'replaceHeaders' parameter instead if you want to replace the
     *            existing header with your own. Use null for no additional
     *            message headers.
     * @param replaceHeaders
     *            ArrayList of javax.sip.header.Header, each element a SIP
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. These
     *            headers are applied to the message after a correct message has
     *            been constructed. Use null for no replacement of message
     *            headers.
     * @param body
     *            A String to be used as the body of the message. The
     *            additionalHeaders parameter must contain a ContentTypeHeader
     *            for this body to be included in the message. Use null for no
     *            body bytes.
     */
    public boolean disconnect(ArrayList additionalHeaders,
            ArrayList replaceHeaders, String body)
    {
        initErrorInfo();

        if (dialog == null)
        {
            returnCode = SipSession.INVALID_OPERATION;
            errorMessage = (String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " - dialog hasn't been established";

            return false;
        }

        try
        {
            Request bye = dialog.createRequest(Request.BYE);
            parent.addAuthorizations(callId.getCallId(), bye);

            synchronized (this) // needed for asynchronous response -
            // processEvent()
            {
                transaction = parent.sendRequestWithTransaction(bye, false,
                        dialog, this, additionalHeaders, replaceHeaders, body);
            }

            if (transaction != null)
            {
                return true;
            }

            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            setException(parent.getException());

            return false;

        }
        catch (Exception ex)
        {
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());

            return false;
        }
    }

    /**
     * This method is the same as the basic disconnect() method except that it
     * allows the caller to specify a message body and/or additional message
     * headers to add to or replace in the outbound message without requiring
     * knowledge of the JAIN-SIP API.
     * 
     * The extra parameters supported by this method are:
     * 
     * @param body
     *            A String to be used as the body of the message. Parameters
     *            contentType, contentSubType must both be non-null to get the
     *            body included in the message. Use null for no body bytes.
     * @param contentType
     *            The body content type (ie, 'application' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param contentSubType
     *            The body content sub-type (ie, 'sdp' part of
     *            'application/sdp'), required if there is to be any content
     *            (even if body bytes length 0). Use null for no message
     *            content.
     * @param additionalHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message. Examples: "Priority:
     *            Urgent", "Max-Forwards: 10". These headers are added to the
     *            message after a correct message has been constructed. Note
     *            that if you try to add a header that there is only supposed to
     *            be one of in a message, and it's already there and only one
     *            single value is allowed for that header, then this header
     *            addition attempt will be ignored. Use the 'replaceHeaders'
     *            parameter instead if you want to replace the existing header
     *            with your own. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no additional message headers.
     * @param replaceHeaders
     *            ArrayList of String, each element representing a SIP message
     *            header to add to the outbound message, replacing existing
     *            header(s) of that type if present in the message. Examples:
     *            "Priority: Urgent", "Max-Forwards: 10". These headers are
     *            applied to the message after a correct message has been
     *            constructed. Unpredictable results may occur if your headers
     *            are not syntactically correct or contain nonsensical values
     *            (the message may not pass through the local SIP stack). Use
     *            null for no replacement of message headers.
     * 
     */
    public boolean disconnect(String body, String contentType,
            String contentSubType, ArrayList additionalHeaders,
            ArrayList replaceHeaders)
    {
        try
        {
            return disconnect(parent.toHeader(additionalHeaders, contentType,
                    contentSubType), parent.toHeader(replaceHeaders), body);
        }
        catch (Exception ex)
        {
            setException(ex);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            return false;
        }
    }

    /**
     * This method returns the SipPhone object associated with this call.
     * 
     * @return This call's SipPhone parent.
     */
    public SipPhone getParent()
    {
        return parent;
    }

    /**
     * This method returns the AddressFactory associated with this object. It
     * may be needed by the caller if providing additional or replacement JAIN
     * SIP headers for outbound messages.
     * 
     * @return This SipCall's javax.sip.address.AddressFactory
     * 
     */
    public AddressFactory getAddressFactory()
    {
        return parent.getAddressFactory();
    }

    /**
     * This method returns the HeaderFactory associated with this object. It
     * will be needed by the caller if providing additional or replacement JAIN
     * SIP headers for outbound messages.
     * 
     * @return This SipCall's javax.sip.header.HeaderFactory
     * 
     */
    public HeaderFactory getHeaderFactory()
    {
        return parent.getHeaderFactory();
    }

    /**
     * @param parent
     *            The parent to set.
     */
    protected void setParent(SipPhone parent)
    {
        this.parent = parent;
    }

    /*
     * @see org.cafesip.sipunit.SipActionObject#getErrorMessage()
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /*
     * @see org.cafesip.sipunit.SipActionObject#getException()
     */
    public Throwable getException()
    {
        return exception;
    }

    /*
     * @see org.cafesip.sipunit.SipActionObject#getReturnCode()
     */
    public int getReturnCode()
    {
        return returnCode;
    }

    /*
     * @see org.cafesip.sipunit.SipActionObject#format()
     */
    public String format()
    {
        if (SipSession.isInternal(returnCode) == true)
        {
            return SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + (errorMessage.length() > 0 ? (": " + errorMessage) : "");
        }
        else
        {
            return "Status code received from network = "
                    + returnCode
                    + ", "
                    + SipResponse.statusCodeDescription.get(new Integer(
                            returnCode))
                    + (errorMessage.length() > 0 ? (": " + errorMessage) : "");
        }
    }

    /**
     * @param errorMessage
     *            The errorMessage to set.
     */
    private void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    /**
     * @param exception
     *            The exception to set.
     */
    private void setException(Throwable exception)
    {
        this.exception = exception;
    }

    private void initErrorInfo()
    {
        setErrorMessage("");
        setException(null);
        setReturnCode(SipSession.NONE_YET);
    }

    /**
     * @param returnCode
     *            The returnCode to set.
     */
    private void setReturnCode(int returnCode)
    {
        this.returnCode = returnCode;
    }

    /**
     * This method returns the last response received on this call.
     * 
     * @return A SipResponse object representing the last response message
     *         received on this call, or null if none has been received.
     * 
     * @see org.cafesip.sipunit.MessageListener#getLastReceivedResponse()
     */
    public SipResponse getLastReceivedResponse()
    {
        synchronized (receivedResponses)
        {
            if (receivedResponses.isEmpty())
            {
                return null;
            }

            return (SipResponse) receivedResponses
                    .get(receivedResponses.size() - 1);
        }
    }

    /**
     * This method returns the last request received on this call.
     * 
     * @return A SipRequest object representing the last request message
     *         received on this call, or null if none has been received.
     * 
     * @see org.cafesip.sipunit.MessageListener#getLastReceivedRequest()
     */
    public SipRequest getLastReceivedRequest()
    {
        synchronized (receivedRequests)
        {
            if (receivedRequests.isEmpty())
            {
                return null;
            }

            return (SipRequest) receivedRequests
                    .get(receivedRequests.size() - 1);
        }
    }

    /**
     * This method returns all the responses received on this call, including
     * any that required re-initiation of the call (ie, authentication
     * challenge).
     * 
     * @return ArrayList of zero or more SipResponse objects.
     * 
     * @see org.cafesip.sipunit.MessageListener#getAllReceivedResponses()
     */
    public ArrayList getAllReceivedResponses()
    {
        return new ArrayList(receivedResponses);
    }

    /**
     * This method returns all the requests received on this call.
     * 
     * @return ArrayList of zero or more SipRequest objects.
     * 
     * @see org.cafesip.sipunit.MessageListener#getAllReceivedRequests()
     */
    public ArrayList getAllReceivedRequests()
    {
        return new ArrayList(receivedRequests);
    }

    /**
     * This method returns the last transaction-initiating request that was sent
     * out with the exception of a re-invite. It's use requires knowledge of the
     * JAIN-SIP API.
     * 
     * @return A javax.sip.message.Request object representing the last stateful
     *         request sent on this call.
     */
    protected Request getSentRequest()
    {
        return transaction == null ? null : transaction.getClientTransaction()
                .getRequest();
    }

    protected SipTransaction getTransaction()
    {
        return transaction;
    }

    /*
     * @see org.cafesip.sipunit.MessageListener#processEvent(java.util.EventObject)
     */
    public void processEvent(EventObject event) // for asynchronous
    // (nonblocking) response
    // handling
    {
        synchronized (this)
        {
        } // wail til message sending has finished (transaction attribute set)

        if (event instanceof ResponseEvent)
        {
            processResponse((ResponseEvent) event);
        }
        else if (event instanceof TimeoutEvent)
        {
            processTimeout((TimeoutEvent) event);
        }

        // note, requests don't come through here, they are handled via the
        // listen/waitFor methods.
    }

    private void processTimeout(TimeoutEvent timeout)
    {
        // this method is called if there was no response to the request we sent

        setReturnCode(SipSession.TIMEOUT_OCCURRED);
        setErrorMessage((String) SipSession.statusCodeDescription
                .get(new Integer(returnCode))
                + " Received response timeout for: "
                + timeout.getClientTransaction().getRequest().toString());

        if (transaction == null)
        {
            System.err
                    .println("SipCall.processTimeout() - Unexpected null transaction, received timeout event for: "
                            + timeout.getClientTransaction().getRequest()
                                    .toString());
            return;
        }

        transaction = null;
        SipStack.trace("SipCall: " + getErrorMessage());
    }

    private void processResponse(ResponseEvent responseEvent)
    {
        Response resp = responseEvent.getResponse();
        receivedResponses.add(new SipResponse(resp));
        SipStack.trace("Asynchronous response received: " + resp.toString());

        if (transaction == null)
        {
            setReturnCode(SipSession.INTERNAL_ERROR);
            setErrorMessage((String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode))
                    + " Unexpected null transaction, received response: "
                    + resp.toString()
                    + "\n"
                    + " for sent request: "
                    + responseEvent.getClientTransaction().getRequest()
                            .toString());

            System.err.println("SipCall.processResponse() - "
                    + getErrorMessage());
            return;
        }

        setReturnCode(resp.getStatusCode());

        String req_type = transaction.getClientTransaction().getRequest()
                .getMethod();
        if (req_type.equals(Request.INVITE))
        {
            processInviteResponse(resp);
        }
        else
        {
            processNonInviteResponse(resp);
        }

        return;
    }

    private void processNonInviteResponse(Response resp)
    {
        if (returnCode / 100 == 1)
        {
            return; // provisional response, keep waiting
        }

        Request req = getSentRequest();

        if ((returnCode == Response.UNAUTHORIZED)
                || (returnCode == Response.PROXY_AUTHENTICATION_REQUIRED))
        {
            authorizeResend(resp, req);
        }
        else if (returnCode != SipResponse.OK)
        {
            setErrorMessage((String) SipSession.statusCodeDescription
                    .get(new Integer(returnCode)));

            SipStack
                    .trace("SipCall.processNonInviteResponse() - received response: "
                            + resp.toString()
                            + "\n"
                            + " for sent request: "
                            + req.toString());
        }
    }

    private void processInviteResponse(Response resp)
    {
        dialog = transaction.getClientTransaction().getDialog();

        if (returnCode / 100 == 1)
        {
            return; // provisional response, keep waiting
        }

        if (returnCode == SipResponse.OK)
        {
            callAnswered = true;
        }
        else if ((returnCode == Response.UNAUTHORIZED)
                || (returnCode == Response.PROXY_AUTHENTICATION_REQUIRED))
        {
            Request msg = getSentRequest();

            // modify the request to include user authorization info

            msg = parent.processAuthChallenge(resp, msg);
            if (msg == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                SipStack.trace(getErrorMessage());

                return;
            }

            if (reInitiateOutgoingCall(msg, this) == false)
            {
                // error info already set
                SipStack.trace(getErrorMessage());
            }
        }

    }

    private void authorizeResend(Response resp, Request sentmsg)
    {
        // modify the request to include user authorization info and resend

        Request msg = parent.processAuthChallenge(resp, sentmsg);
        if (msg == null)
        {
            setReturnCode(parent.getReturnCode());
            setErrorMessage(parent.getErrorMessage());
            SipStack.trace(getErrorMessage());

            return;
        }

        try
        {
            // send the message
            synchronized (this) // needed for asynchronous response -
            // processEvent()
            {
                transaction = parent.sendRequestWithTransaction(msg, false,
                        dialog, this);
            }

            if (transaction == null)
            {
                setReturnCode(parent.getReturnCode());
                setErrorMessage(parent.getErrorMessage());
                setException(parent.getException());
            }
        }
        catch (Exception ex)
        {
            setException(ex);
            setReturnCode(SipSession.EXCEPTION_ENCOUNTERED);
            setErrorMessage("Exception: " + ex.getClass().getName() + ": "
                    + ex.getMessage());
            transaction = null;
        }

        if (transaction == null)
        {
            SipStack.trace(getErrorMessage());
        }

        return;
    }

    /**
     * Indicates if the current call (incoming or outgoing) has been answered.
     * 
     * @return true if answered, false if not.
     */
    public boolean isCallAnswered()
    {
        return callAnswered;
    }

    /**
     * Indicates if the current outgoing call has encountered a response timeout
     * or any kind of error. Only applicable for an outgoing call leg.
     * 
     * @return true if a response timeout or error has occurred, false
     *         otherwise. For a true return value, call getReturnCode() and/or
     *         getErrorMessage() for details.
     */
    public boolean callTimeoutOrError()
    {
        return (getErrorMessage().length() > 0);
    }

    /**
     * The waitForAnswer() method waits for answer(OK) to be received from the
     * network. While waiting, it collects all intermediate responses (which can
     * be viewed later by calling getAllReceivedResponses()) and handles any
     * received authentication challenges.
     * <p>
     * Call this method after calling SipPhone.makeCall() or
     * SipCall.initiateOutgoingCall().
     * <p>
     * If OK has already been received for this call, this method returns
     * immediately. Otherwise, this method blocks until one of the following
     * occurs: 1) An OK response message is received. In this case, a value of
     * true is returned. Call the getLastReceivedResponse() method to get the
     * response details. Use the method sendInviteOkAck() for responding to the
     * OK. 2) A timeout occurs. A false value is returned in this case. 3) An
     * error occurs. False is returned in this case. 4) A final response other
     * than OK is received. False is returned in this case. See details below
     * for what to do when false is returned.
     * <p>
     * Regardless of the outcome, getReturnCode() can be called after this
     * method returns to get the status code: IE, the SIP response code received
     * from the network (defined in SipResponse, along with the corresponding
     * textual equivalent) or a SipUnit internal status/return code (defined in
     * SipSession, along with the corresponding textual equivalent). SipUnit
     * internal codes are in a specially designated range
     * (SipSession.SIPUNIT_INTERNAL_RETURNCODE_MIN and upward).
     * 
     * @param timeout
     *            The maximum amount of time to wait, in milliseconds. Use a
     *            value of 0 to wait indefinitely.
     * @return true if an OK response was received - call
     *         getLastReceivedResponse() to see the OK response details and
     *         sendInviteOkAck() for responding to the OK. Returns false
     *         otherwise (timeout, error, or non-OK final response) - call
     *         getReturnCode() to find out the result: if it is less than
     *         SipSession.SIPUNIT_INTERNAL_RETURNCODE_MIN, that means a non-OK
     *         final response was received and getReturnCode() returns the value
     *         of the SIP response code received from the network (defined in
     *         SipResponse) - call getErrorMessage() to get the textual
     *         equivalent and getLastReceivedResponse() to see the response
     *         details. Otherwise, a timeout or error occurred and
     *         getReturnCode() returns the SipUnit internal status/return code
     *         (defined in SipSession) - you can call getErrorMessage() to see
     *         the textual error message.
     */
    public boolean waitForAnswer(long timeout)
    {
        if (callAnswered == true)
        {
            return true;
        }

        if (waitOutgoingCallResponse(timeout) == false)
        {
            return false;
        }

        while (returnCode != SipResponse.OK)
        {
            if (returnCode / 100 == 1)
            {
                if (waitOutgoingCallResponse(timeout) == false)
                {
                    return false;
                }

                continue;
            }
            else if ((returnCode == Response.UNAUTHORIZED)
                    || (returnCode == Response.PROXY_AUTHENTICATION_REQUIRED))
            {
                Request msg = getSentRequest();

                // modify the request to include user authorization info

                msg = parent.processAuthChallenge(
                        (Response) getLastReceivedResponse().getMessage(), msg);
                if (msg == null)
                {
                    return false;
                }

                if (reInitiateOutgoingCall(msg) == false)
                {
                    return false;
                }

                if (waitOutgoingCallResponse(timeout) == false)
                {
                    return false;
                }

                continue;
            }
            else
            {
                setErrorMessage("Call was not answered, got this instead: "
                        + returnCode);
                return false;
            }
        }

        return true;
    }

    /**
     * This method returns the last received response with status code matching
     * the given parameter.
     * 
     * @param statusCode
     *            Indicates the type of response to return.
     * @return SipResponse object or null, if not found.
     */
    public SipResponse findMostRecentResponse(int statusCode)
    {
        ArrayList responses = getAllReceivedResponses();

        ListIterator i = responses.listIterator(responses.size());
        while (i.hasPrevious())
        {
            SipResponse resp = (SipResponse) i.previous();
            if (resp.getStatusCode() == statusCode)
            {
                return resp;
            }
        }

        return null;
    }
}