/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.sipunit;

import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipTransaction;
import org.cafesip.sipunit.SipTestCase;

import javax.sip.address.AddressFactory;
import javax.sip.address.Address;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.AcceptHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.message.Message;
import javax.sip.TimeoutEvent;
import javax.sip.ResponseEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.Dialog;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.ParseException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gov.nist.javax.sip.header.AcceptEncoding;
import gov.nist.javax.sip.header.Accept;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.videofastupdate.MediaControlImpl;
import com.mobeon.masp.callmanager.sip.SipConstants;

/**
 * This class simulates a SIP phone using SipUnit.
 */
public class PhoneSimulator extends SipTestCase {

    private ILogger log = ILoggerFactory.getILogger(getClass());

    // Test related
    private int timeoutInMilliSeconds;

    // Stack related
    private SipStack sipStack;

    // Phone related
    private String phoneHost;
    private Integer phonePort;
    private final Integer phoneRTPPort = 1111;
    private String localContact;
    private String remoteContact;
    private String localSipUri;
    private String remoteSipUri;
    private SipPhone sipPhone;
    private AddressFactory addressFactory;
    private HeaderFactory headerFactory;
    private MessageFactory messageFactory;
    private boolean responsesEnabled = true;

    // Call related
    private AtomicReference<SipTransaction> initialTransaction =
            new AtomicReference<SipTransaction>();
    private AtomicReference<Dialog> initialDialog =
            new AtomicReference<Dialog>();
    private AtomicReference<RequestEvent> initialRequestEvent =
            new AtomicReference<RequestEvent>();
    private AtomicReference<ArrayList> initialViaHeaders =
            new AtomicReference<ArrayList>();
    private AtomicReference<ArrayList> reInviteViaHeaders =
            new AtomicReference<ArrayList>();

    // Dialog related
    // TODO: Drop 6! Document how local and remote tags and addresses are set
    private AtomicInteger cSeq = new AtomicInteger(0);
    private AtomicInteger inviteCSeq = new AtomicInteger(1);
    private AtomicReference<String> localTag = new AtomicReference<String>();
    private AtomicReference<String> remoteTag = new AtomicReference<String>();
    private AtomicReference<Address> localAddress = new AtomicReference<Address>();
    private AtomicReference<Address> remoteAddress = new AtomicReference<Address>();
    private AtomicReference<Address> remoteRecipient = new AtomicReference<Address>();
    private AtomicReference<CallIdHeader> callId = new AtomicReference<CallIdHeader>();

    public static final Boolean WITHIN_DIALOG = true;
    public static final Boolean OUT_OF_DIALOG = false;
    public static final Boolean WITH_BODY = true;
    public static final Boolean NO_BODY = false;


    public PhoneSimulator(SipStack sipStack,
                          String localUserName,
                          RemotePartyAddress localAddress,
                          String remoteUserName,
                          RemotePartyAddress remoteAddress,
                          int timeoutInMilliSeconds) throws UnknownHostException {
        this.sipStack = sipStack;
        this.phoneHost = InetAddress.getByName(localAddress.getHost()).getHostAddress();
        this.phonePort = localAddress.getPort();
        this.remoteSipUri = "sip:" + remoteUserName + "@" + remoteAddress.getHost();
        this.remoteContact = remoteSipUri + ":" + remoteAddress.getPort();
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
        this.localSipUri = "sip:" + localUserName + "@" + phoneHost;
        this.localContact = localSipUri + ":" + phonePort;
    }

    public void resetPhone() throws Exception {
        initialTransaction.set(null);
        initialDialog.set(null);
        initialRequestEvent.set(null);
        initialViaHeaders.set(null);
        reInviteViaHeaders.set(null);
        sipPhone.dispose();
        create();

        cSeq.set(0);
        inviteCSeq.set(1);
        localTag.set(null);
        remoteTag.set(null);
        localAddress.set(null);
        remoteAddress.set(null);
        remoteRecipient.set(null);
        callId.set(null);
    }

    public Integer getRTPPort() {
        return phoneRTPPort;
    }

    public String getHost() {
        return phoneHost;
    }

    public int getPhonePort() {
        return phonePort;
    }

    public void create() throws Exception {
        // Create SIP phone
        sipPhone = sipStack.createSipPhone(localSipUri);
        sipPhone.listenRequestMessage();

        if (log.isDebugEnabled())
            log.debug("SipPhone created with uri: " + localSipUri);

        addressFactory = sipPhone.getParent().getAddressFactory();
        headerFactory = sipPhone.getParent().getHeaderFactory();
        messageFactory = sipPhone.getParent().getMessageFactory();
    }

    public void delete() {
        // Delete SIP phones
        sipPhone.dispose();
        remoteTag = null;
    }

    public void disableResponses() {
        responsesEnabled = false;
    }

    //================== Getters ====================
    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public String getLocalContact() {
        return localContact;
    }

    //================== Call Related methods ====================

    public void addCallInfoType(
            Message message, CallProperties.CallType callType)
            throws ParseException {
        message.addHeader(
                headerFactory.createCallInfoHeader(
                        addressFactory.createURI("Media:" + callType)));
    }

    public void acceptCall() throws ParseException {
        Response response = createOkResponse(initialRequestEvent.get());
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public void acceptCall(String body) throws ParseException {
        Response response = createOkResponse(initialRequestEvent.get(), body);
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public void indicateEarlyMedia(String body) throws ParseException {
        Response response = createSessionProgressResponse(initialRequestEvent.get(), body);
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public void sendReliableRinging() throws Exception {
        Dialog dialog = initialDialog.get();
        assertNotNull(dialog);
        Response response =
                dialog.createReliableProvisionalResponse(Response.RINGING);

        ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
        if (localTag.get() != null)
            to.setTag(localTag.get());

        Address contactAddress = addressFactory.createAddress(localContact);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(contactAddress);
        response.addHeader(contactHeader);

        dialog.sendReliableProvisionalResponse(response);
    }

    public void sendAck(Request ack) {
        sipPhone.sendUnidirectionalRequest(ack, false);
        assertLastOperationSuccess(sipPhone.format(), sipPhone);
    }

    public void acknowledge(
            Boolean withinDialog, Boolean withBody, boolean reInvite)
            throws ParseException, InvalidArgumentException, IOException {
        Request ack = createRequest(
                Request.ACK, withinDialog, withBody, null,
                false, reInvite);
        sipPhone.sendUnidirectionalRequest(ack, false);
        assertLastOperationSuccess(sipPhone.format(), sipPhone);
    }

    public void sendPrack(Response ringingRequest)
            throws ParseException, InvalidArgumentException, IOException {
        
        Request prack = createRequest(Request.PRACK, PhoneSimulator.WITHIN_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);        

        // Set the CallId
        CallIdHeader ringingCallId = (CallIdHeader)ringingRequest.getHeader(CallID.NAME);
        prack.setHeader(ringingCallId);

        FromHeader ringingFrom = (FromHeader)ringingRequest.getHeader(From.NAME);
        prack.setHeader(ringingFrom);

        ToHeader ringingTo = (ToHeader)ringingRequest.getHeader(To.NAME);
        prack.setHeader(ringingTo);

        ViaHeader ringingVia = (ViaHeader)ringingRequest.getHeader(Via.NAME);
        prack.setHeader(ringingVia);

        sipPhone.sendUnidirectionalRequest(prack, false);

        assertLastOperationSuccess(sipPhone.format(), sipPhone);
    }
    
    public void acknowledge(
            Boolean withinDialog, byte[] sdpBody, boolean reInvite)
            throws ParseException, InvalidArgumentException, IOException {
        Request ack = createRequest(
                Request.ACK, withinDialog, PhoneSimulator.NO_BODY, null,
                false, reInvite);
        addBody(ack, "application", "sdp",
                null, false, sdpBody);
        log.info("Sending ack: \n" + ack.toString());
        sipPhone.sendUnidirectionalRequest(ack, false);
        assertLastOperationSuccess(sipPhone.format(), sipPhone);
    }

    public void acknowledgeReliableResponse(
            Response response, Boolean withBody,
            boolean illegalBody, boolean waitForResponse)
            throws SipException, ParseException {

        Dialog dialog = initialDialog.get();
        Request prack = dialog.createPrack(response);
        if (withBody) {
            if (illegalBody)
                addBody(prack, "application", "sdp",
                        null, false, generateDifferentSDPContent());
            else
                addBody(prack, "application", "sdp",
                        null, false, generateSDPContent());
        }

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(prack, false, dialog);
        assertNotNull(sipPhone.getErrorMessage(), transaction);
        if (waitForResponse)
            assertResponseReceived(transaction, Response.OK, Request.PRACK);
    }

    public void addBody(
            Message message, String contentType, String subType,
            String charset, boolean optional, byte[] content)
            throws ParseException
    {
        ContentTypeHeader contentTypeHeader =
                headerFactory.createContentTypeHeader(contentType, subType);
        if (optional) {
            ContentDispositionHeader contentDispositionHeader =
                    headerFactory.createContentDispositionHeader("session");
            contentDispositionHeader.setParameter("handling", "optional");
            message.addHeader(contentDispositionHeader);
        }
        if (charset != null)
            contentTypeHeader.setParameter("charset", charset);

        // The content is created using a String instead of a byte[] due to an
        // error in the SIP stack.
        message.setContent(new String(content), contentTypeHeader);
    }

    public void cancel(Boolean withinDialog, Boolean callCanceled, Boolean cancelDoneOk)
            throws InvalidArgumentException, ParseException, IOException
    {
        Request cancel = createRequest(Request.CANCEL, withinDialog, NO_BODY, null, false, false);

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(cancel, false, null);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        if (callCanceled) {
            // Wait for Request Terminated response on INVITE
            assertResponseReceived(initialTransaction.get(),
                    Response.REQUEST_TERMINATED, Request.INVITE);
        }

        if (cancelDoneOk) {
            // Wait for OK response on CANCEL
            assertResponseReceived(transaction, Response.OK, Request.CANCEL);
        } else {
            // Wait for Call or Transaction Does Not Exist response on CANCEL
            assertResponseReceived(transaction,
                    Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.CANCEL);
        }
    }

    public Request createRequest(String method, Boolean withinDialog,Boolean withBody,
                                 String numberCompletion, Boolean withGtd, boolean isReInvite)
            throws ParseException, InvalidArgumentException, IOException {
        return createRequest(method, withinDialog, withBody, numberCompletion, withGtd, isReInvite, true);
    }

    public Request createRequest(String method, Boolean withinDialog,
                                 Boolean withBody, String numberCompletion,
                                 Boolean withGtd, boolean isReInvite, boolean createRouteHeader)
            throws ParseException, InvalidArgumentException, IOException {
        Request request;

        if ((method.equals(Request.INVITE) && (!withinDialog))) {
            this.cSeq.incrementAndGet();
        }

        if (withinDialog) {
            request = messageFactory.createRequest(
                                   method + " " + remoteAddress.get().getURI() +
                                           ";transport=udp SIP/2.0 \r\n");
            addCallIdHeader(request, callId.get());
            addFromHeader(request, localAddress.get(), localTag.get());
            addToHeader(request, remoteRecipient.get(), remoteTag.get());

        } else {
             request = messageFactory.createRequest(
                                   method + " " + remoteContact +
                                           ";transport=udp SIP/2.0 \r\n");

            // Generate a new callId and store it for the dialog
            callId.set(sipPhone.getParent().getSipProvider().getNewCallId());
            addCallIdHeader(request, callId.get());
            // Generate a new from tag and store it for the dialog

            localAddress.set(addressFactory.createAddress(
                    addressFactory.createURI(localSipUri)));
            localTag.set(sipPhone.generateNewTag());

            addFromHeader(request, localAddress.get(), localTag.get());
            remoteAddress.set(addressFactory.createAddress(
                    addressFactory.createURI(remoteSipUri)));
            remoteRecipient.set(remoteAddress.get());
            addToHeader(request, remoteAddress.get(), null);

            if (method.equals(Request.INVITE)) {
                addDiversionHeader(request);
                addHistoryInfoHeader(request);
                inviteCSeq.set(this.cSeq.get());
            }

            if (createRouteHeader) {
                addRouteHeader(request);
            }
        }

        if (method.equals(Request.ACK)) {
            if (isReInvite)
                addCSeqHeader(request, this.cSeq.get(), method);
            else
                addCSeqHeader(request, inviteCSeq.get(), method);
        } else {
            addCSeqHeader(request, this.cSeq.get(), method);
        }
        addContactHeader(request);
        addMaxForwardsHeader(request);
        addViaHeaders(request, withinDialog, isReInvite);

        if (withBody) {
            if (method.equals(Request.INFO)) {
                addBody(request, "application", "media_control+xml",
                        null, false,
                        MediaControlImpl.getInstance().
                                createPictureFastUpdateRequest().getBytes());
            } else {
                if(withGtd)
                    addMultiPartBody(request, "application", "sdp",
                        null, false, generateSDPContent(), numberCompletion);
                else
                    addBody(request, "application", "sdp",
                            null, false, generateSDPContent());
            }
        }

        return request;
    }

    private void addHistoryInfoHeader(Request request) throws ParseException {
        request.addHeader(headerFactory.createHeader(
                 "History-Info", "aaa <sip:1111@" + phoneHost +
                 ";user=phone?Reason=SIP%3bcause%3d408&Privacy=none>;index=1"));
        request.addHeader(headerFactory.createHeader(
                "History-Info", "bbb <sip:2222@" + phoneHost +
                ";user=phone?Reason=SIP%3bcause3d302>;index=1.1"));
    }

    private void addMultiPartBody(Message message, String contentType, String subType,
                                  String charset, boolean optional, byte[] content, String numberCompletion)
            throws ParseException
    {
        ContentTypeHeader contentTypeHeader =
                headerFactory.createContentTypeHeader("multipart", "mixed");
        contentTypeHeader.setParameter("boundary", "uniqueBoundary");
        if (optional) {
            ContentDispositionHeader contentDispositionHeader =
                    headerFactory.createContentDispositionHeader("session");
            contentDispositionHeader.setParameter("handling", "optional");
            message.addHeader(contentDispositionHeader);
        }
        if (charset != null)
            contentTypeHeader.setParameter("charset", charset);

        // The content is created using a String instead of a byte[] due to an
        // error in the SIP stack.

        String multiPartBody = "--uniqueBoundary\r\n" +
                "Content-Type: application/sdp\r\n" +
                "\r\n" +
                new String(content) +
                "--uniqueBoundary\r\n"+
                "Content-Type: application/gtd\r\n" +
                "Content-Disposition: signal;handling=optional\r\n" +
                "\r\n" +
                "IAM\r\n" +
                "CGN,04,"+numberCompletion+",1,y,4,1133\r\n"+
                "GCI,aa1f2adec97611d9adcc0003ba909185\r\n" +
                "\r\n" +
                "--uniqueBoundary\r\n";

        message.setContent(multiPartBody, contentTypeHeader);
    }

    public void disconnect(boolean callCanceled)
            throws InvalidArgumentException, ParseException, IOException {
        SipTransaction transaction = sendBye(PhoneSimulator.WITHIN_DIALOG);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        if (callCanceled) {
            // Wait for Request Terminated response on INVITE
            assertResponseReceived(initialTransaction.get(),
                    Response.REQUEST_TERMINATED, Request.INVITE);
        }

        // Wait for OK response
        assertResponseReceived(transaction, Response.OK, Request.BYE);
    }

    public void ring() throws ParseException {
        // Send Ringing response
        Response response = createResponse(initialRequestEvent.get(), Response.RINGING);
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public Response createResponse(int responseType) throws ParseException {
        // Send response
        if (!responsesEnabled) {
            return null;
        }
        return createResponse(initialRequestEvent.get(), responseType);
    }

    public void sendResponse(Response response) throws ParseException {
        // Send response
        if (!responsesEnabled) {
            return;
        }
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public void sendResponse(int responseType) throws ParseException {
        // Send response
        if (!responsesEnabled) {
            return;
        }
        Response response = createResponse(initialRequestEvent.get(), responseType);
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public void trying() throws ParseException {
        // Send Trying response
        Response response = createResponse(initialRequestEvent.get(), Response.TRYING);
        if (initialTransaction.get() == null) {
            initialTransaction.set(sendResponse(initialRequestEvent.get(), response));
            initialDialog.set(initialTransaction.get().getDialog());
        } else {
            sendResponse(initialTransaction.get(), response);
        }
    }

    public SipTransaction sendBye(Boolean withinDialog)
            throws ParseException, InvalidArgumentException, IOException {
        cSeq.incrementAndGet();
        Request bye = createRequest(Request.BYE, withinDialog, NO_BODY, null, false, false);

        Dialog dialog = null;
        if (withinDialog)
            dialog = initialDialog.get();

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(bye, false, dialog);
        log.debug("BYE request sent.");
        return transaction;
    }

    public void sendInvite(Request invite) {
        initialTransaction.set(
                sipPhone.sendRequestWithTransaction(invite, false, null));
        initialDialog.set(initialTransaction.get().getDialog());
        log.debug("INVITE sent.");
    }

    public SipTransaction sendOutOfDialogRequest(Request request) {
        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(request, false, null);
        log.debug("SIP " + request.getMethod() + " sent.");
        return transaction;
    }

    public SipTransaction sendInfo(boolean withinDialog, boolean mediaControlBody)
            throws ParseException, InvalidArgumentException, IOException
    {
        cSeq.incrementAndGet();
        Request info = createRequest(Request.INFO, withinDialog, mediaControlBody, null, false, false);
        log.debug("Trying to send Info.");

        Dialog dialog = null;
        if (withinDialog)
            dialog = initialDialog.get();

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(info, false, dialog);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        return transaction;
    }

    public Response createOkResponse(RequestEvent requestEvent)
            throws ParseException
    {
        Response response = messageFactory.createResponse(
                Response.OK, requestEvent.getRequest());

        ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
        if (localTag.get() != null)
            to.setTag(localTag.get());

        Address contactAddress = addressFactory.createAddress(localContact);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(contactAddress);
        response.addHeader(contactHeader);
        return response;
    }

    public Response createOkResponse(RequestEvent requestEvent, String body)
            throws ParseException
    {
        Response response = createOkResponse(requestEvent);

        if (body == null) {
            addBody(response, "application", "sdp", null, false, generateSDPContent());
        } else {
            addBody(response, "application", "sdp", null, false, body.getBytes());
        }

        return response;
    }

    public Response createSessionProgressResponse(RequestEvent requestEvent)
            throws ParseException
    {
        Response response = messageFactory.createResponse(
                Response.SESSION_PROGRESS, requestEvent.getRequest());

        ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
        if (localTag.get() != null)
            to.setTag(localTag.get());

        Address contactAddress = addressFactory.createAddress(localContact);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(contactAddress);
        response.addHeader(contactHeader);
        return response;
    }

    public Response createSessionProgressResponse(
            RequestEvent requestEvent, String body)
            throws ParseException
    {
        Response response = createSessionProgressResponse(requestEvent);

        if (body == null) {
            addBody(response, "application", "sdp", null, false, generateSDPContent());
        } else {
            addBody(response, "application", "sdp", null, false, body.getBytes());
        }

        return response;
    }

    public SipTransaction sendOptions(boolean withinDialog)
            throws ParseException, InvalidArgumentException, IOException
    {
        cSeq.incrementAndGet();
        Request options = createRequest(Request.OPTIONS, withinDialog, NO_BODY, null, false, false);

        Dialog dialog = null;
        if (withinDialog)
            dialog = initialDialog.get();

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(options, false, dialog);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        return transaction;
    }

    public void sendRedirect(int responseType, String contact)
            throws ParseException {
        Address contactAddress = addressFactory.createAddress(contact);
        SipTransaction transaction;

        if (initialTransaction.get() == null) {
            transaction = sipPhone.sendReply(
                    initialRequestEvent.get(), responseType, null,
                    sipPhone.generateNewTag(), contactAddress, -1);
            initialTransaction.set(transaction);
            initialDialog.set(transaction.getDialog());
        } else {
            transaction = sipPhone.sendReply(
                    initialTransaction.get(), responseType, null,
                    initialDialog.get().getLocalTag(), contactAddress, -1);
        }

        assertNotNull(sipPhone.getErrorMessage(), transaction);
    }

    public SipTransaction sendRegister(boolean withinDialog)
            throws ParseException, InvalidArgumentException, IOException
    {
        cSeq.incrementAndGet();
        Request register = createRequest(Request.REGISTER, withinDialog, NO_BODY, null, false, false);
        log.debug("Trying to send Register.");

        Dialog dialog = null;
        if (withinDialog)
            dialog = initialDialog.get();

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(register, false, dialog);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        return transaction;
    }

    public SipTransaction sendReInvite(boolean withBody, boolean illegalBody)
            throws InvalidArgumentException, ParseException,
            SipException, IOException
    {
        cSeq.incrementAndGet();
        log.debug("CSeq before sending re-INVITE is " + cSeq.get());
        Request reInvite =
                createRequest(Request.INVITE, WITHIN_DIALOG, NO_BODY, null, false, false);

        if (withBody)
            if (illegalBody)
                addBody(reInvite, "application", "sdp",
                        null, false, generateDifferentSDPContent());
            else
                addBody(reInvite, "application", "sdp",
                        null, false, generateSDPContent());

        SipTransaction transaction = sipPhone.sendRequestWithTransaction(
                reInvite, false, initialDialog.get());
        assertNotNull(sipPhone.getErrorMessage(), transaction);
        
        return transaction;
    }

    public Response createResponse(
            RequestEvent requestEvent, int responseType) throws ParseException
    {
        Response response = messageFactory.createResponse(
                responseType, requestEvent.getRequest());

        // Add local tag if not set
        ToHeader toHeader = (ToHeader)response.getHeader(ToHeader.NAME);
        if (toHeader.getTag() == null) {
            toHeader.setTag(localTag.get());
        }

        Address contactAddress = addressFactory.createAddress(localContact);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(contactAddress);
        response.addHeader(contactHeader);

        return response;
    }

    public SipTransaction sendResponse(
            RequestEvent requestEvent, Response response) throws ParseException
    {
        if (!responsesEnabled) {
            return null;
        }
        return sipPhone.sendReply(requestEvent, response);
    }

    public void sendResponse(
            SipTransaction transaction, Response response) throws ParseException
    {
        if (!responsesEnabled) {
            return;
        }
        sipPhone.sendReply(transaction, response);
    }

    public SipTransaction sendUnknownMethod(boolean withinDialog)
            throws ParseException, InvalidArgumentException, IOException
    {
        cSeq.incrementAndGet();
        Request unknownMethod = createRequest("UNKNOWN", withinDialog, NO_BODY, null, false, false);


        Dialog dialog = null;
        if (withinDialog)
            dialog = initialDialog.get();

        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(unknownMethod, false, dialog);
        assertNotNull(sipPhone.getErrorMessage(), transaction);

        return transaction;
    }


    //================== Assertion methods ====================

    public void assertAcceptEncodingHeader(Response response,
                                           String encoding) {
        assertEquals("Expected Accept-Encoding header == identity.", encoding,
                ((AcceptEncoding)response.
                        getHeader(AcceptEncoding.NAME)).getEncoding());
    }

    public void assertAcceptHeader(Response response, String contentType,
                                   String subType) {
        ListIterator acceptHeaders = response.getHeaders(Accept.NAME);
        boolean found = false;
        while (acceptHeaders.hasNext()) {
            AcceptHeader accept = (Accept)acceptHeaders.next();
            if (accept.getContentType().equals(contentType) &&
                    accept.getContentSubType().equals(subType)) {
                found = true;
                break;
            }
        }

        assertTrue(found);
    }

    public void assertExperiencedOperationalStatus(
            Response response, String state) {
        assertEquals(
                "Expected Experienced-Operational-Status header with value == " +
                        state,
                state, ((ExtensionHeader)response.getHeader(
                SipConstants.HDR_EXPERIENCED_OPERATIONAL_STATUS)).getValue());
    }

    public Response assertResponseReceived(int responseType) {
        return assertResponseReceived(
                initialTransaction.get(), responseType, Request.INVITE);
    }

    public Response assertResponseReceived(
            SipTransaction transaction, int responseType,
            String requestMethod) {
        Response response = waitForResponse(transaction);

        int responseClass = responseType / 100;
        if ((responseType == Response.RINGING) ||
                (responseType == Response.SESSION_PROGRESS) ||
                (responseClass == 3) || (responseClass == 4) ||
                (responseClass == 5) || (responseClass == 6)){
            // If waiting for Ringing or a failure response (3xx, 4xx, 5xx or
            // 6xx), ignore additional Trying responses if received
            while (response.getStatusCode() == Response.TRYING) {
                response = waitForResponse(transaction);
            }
        }

        if ((responseType == Response.SERVER_TIMEOUT) ||
                (responseType == Response.OK) ||
                (responseType == Response.NOT_ACCEPTABLE_HERE)){
            // If waiting for a Server Timeout response, ignore additional
            // 180 or 183 responses if received
            while (response.getStatusCode() == Response.RINGING) {
                response = waitForResponse(transaction);
            }
            while (response.getStatusCode() == Response.SESSION_PROGRESS) {
                response = waitForResponse(transaction);
            }
        }

        if ((response.getStatusCode() != Response.TRYING) &&
                (requestMethod.equals(Request.INVITE))) {
            remoteTag.set(
                    ((ToHeader)response.getHeader(ToHeader.NAME)).getTag());
            remoteRecipient.set(
                    ((ToHeader)response.getHeader(ToHeader.NAME)).getAddress());
        }

        // Verify that the response is as expected
        assertEquals("Expected " + responseType + " response",
                responseType, response.getStatusCode());

        // Verify that the response is sent for the expected request method
        CSeqHeader cseqheader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
        assertEquals("Not expected method in " + responseType + " response",
                requestMethod, cseqheader.getMethod());

        return response;
    }

    public RequestEvent assertRequestReceived(String requestMethod,
                                              Boolean initialInvite,
                                              boolean redirected) {
        RequestEvent requestEvent = waitForRequest();
        Request request = requestEvent.getRequest();

        // Verify that the request method is as expected
        assertEquals("Request method not as expected.",
                requestMethod, request.getMethod());

        if (initialInvite) {
            log.debug("Retrieving initial INVITE");
            if (!redirected) {
                String receivedTag =
                        ((FromHeader)request.getHeader(FromHeader.NAME)).getTag();
                if (remoteTag.get() != null) {
                    while (receivedTag.equals(remoteTag.get())) {
                        log.debug("Remote tag was already set, this is a retransmit " +
                                "of previous INVITE message. It is ignored. " +
                                "Remote tag: " + remoteTag.get());
                        requestEvent = waitForRequest();
                        request = requestEvent.getRequest();
                        receivedTag =
                                ((FromHeader)request.getHeader(FromHeader.NAME)).getTag();
                    }
                    log.debug("New invite found, Remote tag: " + receivedTag);
                }
            }

            localTag.set(sipPhone.generateNewTag());
            localAddress.set(
                    ((ToHeader)request.getHeader(ToHeader.NAME)).getAddress());
            remoteTag.set(((FromHeader)request.getHeader(FromHeader.NAME)).getTag());
            remoteRecipient.set(
                    ((FromHeader)request.getHeader(FromHeader.NAME)).getAddress());
            remoteAddress.set(
                    ((ContactHeader)request.getHeader(ContactHeader.NAME)).getAddress());

            callId.set((CallIdHeader)request.getHeader(CallIdHeader.NAME));
            initialRequestEvent.set(requestEvent);
            initialTransaction.set(null);
            initialDialog.set(null);
        }

        return requestEvent;
    }

    public RequestEvent assertRequestReceivedAndIgnoreInviteResends(
            String requestMethod) {

        RequestEvent requestEvent = waitForRequest();
        Request request = requestEvent.getRequest();

        while (request.getMethod().equals(Request.INVITE)) {
            requestEvent = waitForRequest();
            request = requestEvent.getRequest();
        }

        // Verify that the request method is as expected
        assertEquals("Request method not as expected.",
                requestMethod, request.getMethod());

        return requestEvent;
    }

    //================== Private methods ====================

    private byte[] generateSDPContent() {
        String timestamp = "0";
        String sdpData =
                    "v=0\r\n"
                    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
                    + " IN IP4 " + phoneHost + "\r\n"
                    + "s=MAS prompt session\r\n"
                    + "c=IN IP4 " + phoneHost + "\r\n"
                    + "t=0 0\r\n"
                    + "m=audio " + phoneRTPPort + " RTP/AVP 0 101\r\n"
                    + "b=AS:64\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:101 telephone-event/8000\r\n"
                    + "a=fmtp:101 0-15\r\n"
                    + "a=ptime:40\r\n";
        return sdpData.getBytes();
    }

    private byte[] generateDifferentSDPContent() {
        String timestamp = "0";
        String sdpData =
                    "v=0\r\n"
                    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
                    + " IN IP4 " + phoneHost + "\r\n"
                    + "s=MAS prompt session\r\n"
                    + "c=IN IP4 " + phoneHost + "\r\n"
                    + "t=0 0\r\n"
                    + "m=audio " + phoneRTPPort + " RTP/AVP 8 101\r\n"
                    + "b=AS:64\r\n"
                    + "a=rtpmap:8 PCMA/8000\r\n"
                    + "a=rtpmap:101 telephone-event/8000\r\n"
                    + "a=fmtp:101 0-15\r\n"
                    + "a=ptime:40\r\n";
        return sdpData.getBytes();
    }

    private Response waitForResponse(SipTransaction transaction) {
        // Wait for response
        EventObject responseEvent =
                sipPhone.waitResponse(transaction, timeoutInMilliSeconds);

        // TODO: Phase 2! Find out why null is returned before timeout time.
        // Meanwhile an extra wait is performed to improve chances of succeeding
        if (responseEvent == null) {
            responseEvent =
                    sipPhone.waitResponse(transaction, timeoutInMilliSeconds);
        }

        assertNotNull(sipPhone.format(), responseEvent);
        assertFalse("Operation timeout", responseEvent instanceof TimeoutEvent);
        // Got response, returning it
        return ((ResponseEvent) responseEvent).getResponse();
    }

    private RequestEvent waitForRequest() {
        // Wait for request
        EventObject requestEvent = sipPhone.waitRequest(timeoutInMilliSeconds);
        assertNotNull(sipPhone.format(), requestEvent);
        assertFalse("Operation timeout", requestEvent instanceof TimeoutEvent);

        // Got request, returning it
        return (RequestEvent)requestEvent;
    }

    private void addCallIdHeader(Request request, CallIdHeader callId) {
        CallIdHeader callIdHeader = callId;
        if (callIdHeader == null) {
            callIdHeader = sipPhone.getParent().getSipProvider().getNewCallId();
        }
        request.addHeader(callIdHeader);
    }

    private void addContactHeader(Message message) throws ParseException {
        Address contactAddress =
                addressFactory.createAddress(this.localContact);
        message.addHeader(headerFactory.createContactHeader(contactAddress));
    }

    private void addCSeqHeader(Request request, long cSeq, String method)
        throws ParseException, InvalidArgumentException
    {
        request.addHeader(headerFactory.createCSeqHeader(cSeq, method));
    }

    private void addDiversionHeader(Request request) throws ParseException {
        request.addHeader(headerFactory.createHeader(
                "Diversion", "displayname <sip:1234@" + phoneHost +
                ";user=phone>;privacy=full;reason=user-busy"));
    }

    private void addFromHeader(Request request, Address address, String tag)
            throws ParseException {
        request.addHeader(headerFactory.createFromHeader(address, tag));
    }

    private void addMaxForwardsHeader(Message message)
            throws InvalidArgumentException {
        message.addHeader(headerFactory.createMaxForwardsHeader(70));
    }

    private void addToHeader(Request request, Address address, String tag)
            throws ParseException {
        request.addHeader(headerFactory.createToHeader(address, tag));
    }

    private void addRouteHeader(Request request) throws ParseException {
        Address routeAddress = addressFactory.createAddress(remoteContact);
        request.addHeader(headerFactory.createRouteHeader(routeAddress));
    }

    private void addViaHeaders(
            Request request, boolean withinDialog, boolean isReInvite)
            throws InvalidArgumentException, ParseException {
        ArrayList viaHeaders = sipPhone.getViaHeaders();


        if (request.getMethod().equals(Request.INVITE) && !withinDialog) {
            initialViaHeaders.set(viaHeaders);
        } else if (request.getMethod().equals(Request.INVITE) && isReInvite) {
            reInviteViaHeaders.set(viaHeaders);
        } else if (request.getMethod().equals(Request.ACK) ||
                request.getMethod().equals(Request.CANCEL)) {
            // Use the same via header as for INVITE
            if (isReInvite)
                viaHeaders = reInviteViaHeaders.get();
            else
                viaHeaders = initialViaHeaders.get();
            if (viaHeaders == null) {
                ViaHeader via_header = headerFactory.createViaHeader(
                        sipPhone.getStackAddress(), phonePort, "UDP", null);
                viaHeaders = new ArrayList(1);
                viaHeaders.add(via_header);
            }
        } else {
            ViaHeader via_header = headerFactory.createViaHeader(
                    sipPhone.getStackAddress(), phonePort, "UDP", null);
            viaHeaders = new ArrayList(1);
            viaHeaders.add(via_header);
        }
        request.addHeader((ViaHeader) viaHeaders.get(0));
    }

    public void addExpiresHeader(Message message, int expiresTime)
            throws InvalidArgumentException {
        message.addHeader(headerFactory.createExpiresHeader(expiresTime));

    }

    // This is only included to make IntelliJ happy. Not used at all.
    public PhoneSimulator() {
    }

    public void testDoNothing() throws Exception {
    }
}
