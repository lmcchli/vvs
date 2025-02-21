/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.sipunit;

import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.header.ContactHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.address.Address;
import java.util.Properties;
import java.util.EventObject;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.text.ParseException;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.SipConstants;

/**
 * @author Malin Flodin
 */
public class SspSimulator extends SipTestCase {

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


    // SSP related
    private String sipUnitPhoneUri;
    private String sspUser = null;
    private String sspHost = null;
    private int sspPort;
    private String callManagerHost;


    public SspSimulator(String sspHost,
                        int sspPort,
                        String callManagerHost,
                        int timeoutInMilliSeconds) {

        this.callManagerHost = callManagerHost;
        String[] sspUserAndHostList = sspHost.split("@");

        if (sspUserAndHostList.length > 1) {
            this.sspUser = sspUserAndHostList[0];
            this.sspHost = sspUserAndHostList[1];
        } else if (sspUserAndHostList.length == 1){
            this.sspHost = sspUserAndHostList[0];
        }

        this.sspPort = sspPort;
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
        this.sipUnitPhoneUri = "sip:mas@" + callManagerHost ;
    }

    public void create() throws Exception {
        Properties properties = setupSipStackConfiguration();
        sipStack = new SipStack("udp", sspPort, properties);

        // Create SIP phone
        log.debug("Creating sipPhone with URI: " + sipUnitPhoneUri);
        sipPhone = sipStack.createSipPhone(sipUnitPhoneUri);
        sipPhone.listenRequestMessage();
        localTag.set(sipPhone.generateNewTag());
        addressFactory = sipPhone.getParent().getAddressFactory();
        headerFactory = sipPhone.getParent().getHeaderFactory();
        messageFactory = sipPhone.getParent().getMessageFactory();
    }

    public void delete() {
        // Delete SIP phone
        sipPhone.dispose();
        sipStack.dispose();
    }

    public RequestEvent assertRegisterReceived() {
        RequestEvent requestEvent = waitForRequest();
        Request request = requestEvent.getRequest();

        // Verify that the request method is as expected
        assertEquals("Request method not as expected.",
                Request.REGISTER, request.getMethod());

        // Verify that the Request-URI is as expected
        assertEquals("Request-URI not as expected.",
                getSspUri(),
                requestEvent.getRequest().getRequestURI().toString());

        return requestEvent;
    }

    public RequestEvent assertUnregisterReceived() {
        RequestEvent requestEvent = waitForRequest();
        Request request = requestEvent.getRequest();
        SipRequest sipRequest = new SipRequest(request);

        // Verify that the request method is as expected
        assertEquals("Request method not as expected.",
                Request.REGISTER, request.getMethod());

        // Verify that the Request-URI is as expected
        assertEquals("Request-URI not as expected.",
                getSspUri(),
                requestEvent.getRequest().getRequestURI().toString());

        // Verify that the Contact header expire time is zero
        assertEquals("Expires not as expected", 0, sipRequest.getContactExpireTime());

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

    public void assertExperiencedOperationalStatus(
            Response response, String state) {
        assertEquals(
                "Expected Experienced-Operational-Status header with value == " +
                        state,
                state, ((ExtensionHeader)response.getHeader(
                SipConstants.HDR_EXPERIENCED_OPERATIONAL_STATUS)).getValue());
    }

    public String getSspUri() {
        if (sspUser != null) {
            return "sip:" + sspUser + "@" + sspHost + ":" + sspPort;
        } else {
            return "sip:" + sspHost + ":" + sspPort;
        }
    }

    public String getSspId() {
        return sspHost + ":" + sspPort;
    }

    public Request createOptionsRequest()
            throws ParseException, InvalidArgumentException {
        Request request = messageFactory.createRequest(
                                   Request.OPTIONS + " " + callManagerHost +
                                           ":5060;transport=udp SIP/2.0 \r\n");

        // Generate a new callId and store it for the dialog
        CallIdHeader callIdHeader = sipPhone.getParent().getSipProvider().getNewCallId();
        request.addHeader(callIdHeader);

        Address localAddress = addressFactory.createAddress(
                addressFactory.createURI(sipUnitPhoneUri));
        request.addHeader(headerFactory.createFromHeader(
                localAddress, sipPhone.generateNewTag()));

        Address remoteAddress = addressFactory.createAddress(
                addressFactory.createURI(sipUnitPhoneUri));
        request.addHeader(headerFactory.createToHeader(remoteAddress, null));
        Address routeAddress = addressFactory.createAddress(
                "sip:" + callManagerHost + ":5060");
        request.addHeader(headerFactory.createRouteHeader(routeAddress));
        request.addHeader(headerFactory.createCSeqHeader(
                (long)1, Request.OPTIONS));
        ArrayList viaHeaders = sipPhone.getViaHeaders();
        request.addHeader((ViaHeader) viaHeaders.get(0));

        MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
        request.addHeader(maxForwards);

        return request;
    }

    public SipTransaction sendOutOfDialogRequest(Request request) {
        SipTransaction transaction =
                sipPhone.sendRequestWithTransaction(request, false, null);
        assertNotNull(sipPhone.getErrorMessage(), transaction);
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
        properties.setProperty("javax.sip.IP_ADDRESS", sspHost );
        properties.setProperty("javax.sip.STACK_NAME", STACK_NAME);
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");

        if (doLogging) {
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                    "true");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "simulatedSsp_sipstacklog.txt");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "simulatedSsp_sipstackdebug.txt");
            properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG",
                    "simulatedSsp_sipstackbadmessages.txt");
        }

        return properties;
    }

    // The below is only included to make IntelliJ happy. Not used at all.

    public SspSimulator() {
    }

    public void testDoNothing() throws Exception {
    }
}
