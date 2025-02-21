/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.component.environment.sipunit;

import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipTransaction;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponse;

import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Properties;
import java.util.EventObject;
import java.text.ParseException;

/**
 * @author Mats Hägg
 */
public class NotifyReceiverSimulator extends SipTestCase {

    private ILogger log = ILoggerFactory.getILogger(getClass());

    // Test related
    // Toggle this to enable/disable logging of the simulated phones SIP stack
    private static final boolean doLogging = false;
    private int timeoutInMilliSeconds;

    // Stack related
    private static final String STACK_NAME = "SipUnitStack";
    private static SipStack sipStack;
    private SipPhone sipPhone;
    private AddressFactory addressFactory;
    private HeaderFactory headerFactory;
    private MessageFactory messageFactory;
    private AtomicReference<String> localTag = new AtomicReference<String>();


    // Notify reciever related
    private String simUri = null;
    private String simHost = null;
    private int simPort;

    /**
     * Configure the simulator for receiveing notify requests.
     * @param simUri - URI for receiving simulator. Should match the to address in notify request.
     * @param simHost
     * @param simPort
     */
    public NotifyReceiverSimulator(String simUri,
                             String simHost,
                             int simPort,
                             int timeoutInMilliSeconds) {

        this.simHost = simHost;
        this.simPort = simPort;
        this.simUri = simUri;
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
    }

    /**
     * Create the actual simulator
     * @throws Exception
     */
    public void create() throws Exception {
        Properties properties = setupSipStackConfiguration();
        if (log.isDebugEnabled())
            log.debug("Creating SipStack with properties: \r\n" + properties);

        NotifyReceiverSimulator.sipStack = new SipStack("udp", simPort, properties);

        // Create SIP phone
        if (log.isDebugEnabled())
            log.debug("Creating sipPhone with URI: " + simUri);

        sipPhone = NotifyReceiverSimulator.sipStack.createSipPhone(simUri);
        sipPhone.listenRequestMessage();
        localTag.set(sipPhone.generateNewTag());
        addressFactory = sipPhone.getParent().getAddressFactory();
        headerFactory = sipPhone.getParent().getHeaderFactory();
        messageFactory = sipPhone.getParent().getMessageFactory();
    }

    public void delete() {
        // Delete SIP phone
        sipPhone.dispose();
        NotifyReceiverSimulator.sipStack.dispose();
    }

    public RequestEvent assertNotifyReceived() {

        RequestEvent requestEvent;
        Request request;
        requestEvent = waitForRequest();
        request = requestEvent.getRequest();

        // Verify that the request method is as expected
        assertEquals("Request method not as expected.",
                Request.NOTIFY, request.getMethod());

        // Verify that the Request-URI is as expected
        assertEquals("Request-URI not as expected.",
                simUri,requestEvent.getRequest().getRequestURI().toString());

        return requestEvent;
    }


    public Response assertResponseReceived(
            SipTransaction transaction, int responseType,
            String requestMethod) {
        Response response = waitForResponse(transaction);

        int responseClass = responseType / 100;
        if ((responseType == Response.RINGING) ||
                (responseClass == 3) || (responseClass == 4) ||
                (responseClass == 5) || (responseClass == 6)){
            // If waiting for Ringing or a failure response (3xx, 4xx, 5xx or
            // 6xx), ignore additional Trying responses if received
            while (response.getStatusCode() == Response.TRYING) {
                response = waitForResponse(transaction);
            }
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


    public SipTransaction sendOutOfDialogRequest(Request request) {
        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(request, false, null);
        assertNotNull(sipPhone.getErrorMessage(), transaction);
        if (log.isDebugEnabled())
            log.debug("SIP " + request.getMethod() + " sent.");
        return transaction;
    }

    public SipResponse createOkResponse(RequestEvent requestEvent)
            throws ParseException
    {
        Response response = messageFactory.createResponse(
                Response.OK, requestEvent.getRequest());
        SipResponse sipResponse =
                new SipResponse(response, requestEvent.getServerTransaction(),
                        (SipProvider)requestEvent.getSource());
        sipResponse.addToTag(localTag.get());
        response.addHeader(requestEvent.getRequest().getHeader(ContactHeader.NAME));

        return sipResponse;
    }


    public SipResponse createResponse(RequestEvent requestEvent,
                                      int statusCode,
                                      String reasonPhrase,
                                      Integer retryAfter)

            throws ParseException, InvalidArgumentException {

        Response response = messageFactory.createResponse(
                statusCode, requestEvent.getRequest());
        SipResponse sipResponse =
                new SipResponse(response, requestEvent.getServerTransaction(),
                        (SipProvider)requestEvent.getSource());
        sipResponse.addToTag(localTag.get());

        if (reasonPhrase != null)
            response.setReasonPhrase(reasonPhrase);

        if (retryAfter != null) {
            RetryAfterHeader header = headerFactory.createRetryAfterHeader(retryAfter);
            response.addHeader(header);
        }
        return sipResponse;
    }


    public void sendResponse(RequestEvent requestEvent, int responseCode)
            throws ParseException
    {
        Response response = messageFactory.createResponse(
                responseCode, requestEvent.getRequest());
        SipResponse sipResponse =
                new SipResponse(response, requestEvent.getServerTransaction(),
                        (SipProvider)requestEvent.getSource());
        sipResponse.addToTag(localTag.get());
        sendResponse(requestEvent, response);
    }

    public SipTransaction sendResponse(
            RequestEvent requestEvent, Response response) throws ParseException
    {
        SipTransaction transaction = sipPhone.sendReply(requestEvent, response);
        assertNotNull(sipPhone.format(), transaction);
        return transaction;
    }


    //============================ Private methods ============================

    private RequestEvent waitForRequest() {
        // Wait for request
        EventObject requestEvent = sipPhone.waitRequest(timeoutInMilliSeconds);
        assertNotNull(sipPhone.format(), requestEvent);
        assertFalse("Operation timeout", requestEvent instanceof TimeoutEvent);

        // Got request, returning it
        return (RequestEvent)requestEvent;
    }

    private Response waitForResponse(SipTransaction transaction) {
        // Wait for response
        EventObject responseEvent =
                sipPhone.waitResponse(transaction, timeoutInMilliSeconds);
        assertNotNull(sipPhone.format(), responseEvent);
        assertFalse("Operation timeout", responseEvent instanceof TimeoutEvent);
        // Got response, returning it
        return ((ResponseEvent) responseEvent).getResponse();
    }

    private Properties setupSipStackConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("javax.sip.IP_ADDRESS", simHost );
        properties.setProperty("javax.sip.STACK_NAME", NotifyReceiverSimulator.STACK_NAME);
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");

        // We must tell the stack to accept unsolicited NOTIFY's
        properties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");

        if (NotifyReceiverSimulator.doLogging) {
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                    "true");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "notifyRecieverSim_sipstacklog.txt");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "notifyRecieverSim_sipstackdebug.txt");
            properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG",
                    "notifyRecieverSim_sipstackbadmessages.txt");
        }

        return properties;
    }

    // The below is only included to make IntelliJ happy. Not used at all.

    public NotifyReceiverSimulator() {
    }

    public void testDoNothing() throws Exception {
    }
}
