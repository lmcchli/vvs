/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.releasecausemapping.Q850CauseLocationPair;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;

import gov.nist.javax.sip.parser.StringMsgParser;

import javax.sip.message.Request;
import javax.sip.header.AllowHeader;
import javax.sip.SipFactory;
import java.util.ListIterator;
import java.util.HashSet;

/**
 * SipMessage Tester.
 *
 * @author Malin Flodin
 */
public class SipMessageTest extends TestCase
{
    private static final String SIP_REQUEST_LINE =
            "INVITE sip:masUser@127.0.0.1:5060;transport=udp SIP/2.0\r\n";

    private StringMsgParser msgParser = new StringMsgParser();
    private String defaultMsg = SIP_REQUEST_LINE;

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);


        // Create a CMUtils instance with a header factory
        CMUtils cmUtils = CMUtils.getInstance();
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        cmUtils.setSipHeaderFactory(new SipHeaderFactory(
                sipFactory.createAddressFactory(), sipFactory.createHeaderFactory()));


    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAcceptHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddAcceptEncodingHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddAcceptLanguageHeader() throws Exception {
        //TODO: Test goes here...
    }


    /**
     * Verifies that when an Allow header is added to a SIP message, the
     * following methods are included:
     * INVITE, ACK, BYE, CANCEL, OPTIONS, INFO, PRACK
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testAddAllowHeader() throws Exception {
        String sipMsg = defaultMsg;
        SipMessageImpl sipMessage =
                new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));

        // Add allow headers to message
        sipMessage.addAllowHeader();

        // Retrieve all Allow headers from the message and verify that they
        // are set correctly
        ListIterator headers = sipMessage.message.getHeaders(AllowHeader.NAME);
        HashSet<String> methodSet = new HashSet<String>();
        while (headers.hasNext()) {
            methodSet.add(((AllowHeader)headers.next()).getMethod());
        }

        // Verify that there are 7 Allow header entries.
        assertEquals(7, methodSet.size());

        // Verify that INVITE, ACK, BYE, CANCEL, OPTIONS, INFO and PRACK all
        // are included
        assertTrue(methodSet.contains(Request.INVITE));
        assertTrue(methodSet.contains(Request.ACK));
        assertTrue(methodSet.contains(Request.BYE));
        assertTrue(methodSet.contains(Request.CANCEL));
        assertTrue(methodSet.contains(Request.OPTIONS));
        assertTrue(methodSet.contains(Request.INFO));
        assertTrue(methodSet.contains(Request.PRACK));
    }

    public void testAddContactHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddContactHeaderExpiration() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddDiversionHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddExpiresHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddBody() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddOperationalStatusHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddPrivacyHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddRemotePartyIdHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddSupportedHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddToTag() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddUnsupportedHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddUserAgentHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testAddWarningHeader() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsMethodKnownButUnsupported() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsMethodSupported() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Verifies that provisional responses are required to use if the SIP
     * message contains a Require header field with the extension "100rel".
     * @throws Exception Exception is thrown if the test case fails.
     */
    public void testIsReliableProvisionalResponsesRequired() throws Exception {
        String sipMsg;
        SipMessage sipMessage;

        // Verify that false is returned if no Require header field exists
        sipMsg = defaultMsg;
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesRequired());

        // Verify that false is returned if Require header fields exists but
        // do not contain "100rel"
        sipMsg = defaultMsg + "Require: abcd\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesRequired());

        sipMsg = defaultMsg + "Require: abcd, efgh\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesRequired());

        sipMsg = defaultMsg + "Require: abcd\r\n" + "Require:timers\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesRequired());

        // Verify that true is returned if a Require header field contains
        // "100rel"
        sipMsg = defaultMsg + "Require: 100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesRequired());

        sipMsg = defaultMsg + "Require: abcd, 100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesRequired());

        sipMsg = defaultMsg + "Require: abcd\r\n" + "Require:100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesRequired());
    }

    /**
     * Verifies that provisional responses are supported to use if the SIP
     * message contains a Supported header field with the extension "100rel".
     * @throws Exception Exception is thrown if the test case fails.
     */
    public void testIsReliableProvisionalResponsesSupported() throws Exception {
        String sipMsg;
        SipMessage sipMessage;

        // Verify that false is returned if no Supported header field exists
        sipMsg = defaultMsg;
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesSupported());

        // Verify that false is returned if Supported header fields exists but
        // do not contain "100rel"
        sipMsg = defaultMsg + "Supported: abcd\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesSupported());

        sipMsg = defaultMsg + "Supported: abcd, efgh\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesSupported());

        sipMsg = defaultMsg + "Supported: abcd\r\n" + "Supported:timers\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertFalse(sipMessage.isReliableProvisionalResponsesSupported());

        // Verify that true is returned if a Supported header field contains
        // "100rel"
        sipMsg = defaultMsg + "Supported: 100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesSupported());

        sipMsg = defaultMsg + "Supported: abcd, 100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesSupported());

        sipMsg = defaultMsg + "Supported: abcd\r\n" + "Supported:100rel\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertTrue(sipMessage.isReliableProvisionalResponsesSupported());
    }

    public void testIsRequestUriValid() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsSipVersionSupported() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsToHeaderValid() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsUriSchemeSupported() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Verifies that the call type can be retrieved from the Call-Info header
     * field. Verifies that CallType is UNKNOWN if there is no header or if it
     * does not contain voice or video. Otherwise VOICE or VIDEO is returned.
     * @throws Exception of test case fails.
     */
    public void testGetCallInfoType() throws Exception {
        String sipMsg = defaultMsg;
        SipMessage sipMessage;

        // Verify that if no Call-Info header field is present, call type is unknown.
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(CallType.UNKNOWN, sipMessage.getCallInfoType());

        // Verify that if Call-Info header is empty, call type is unknown.
        sipMsg = defaultMsg +
                "Call-Info: ;purpose=info\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(CallType.UNKNOWN, sipMessage.getCallInfoType());

        // Verify that if Call-Info header contains voice, call type is voice.
        sipMsg = defaultMsg +
                "Call-Info: <Media:Voice>;purpose=info\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(CallType.VOICE, sipMessage.getCallInfoType());

        // Verify that if Call-Info header contains video, call type is video.
        sipMsg = defaultMsg +
                "Call-Info: <Media:Video>;purpose=info\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(CallType.VIDEO, sipMessage.getCallInfoType());

        // Verify that if Call-Info header contains neither voice nor video,
        // call type is unknown.
        sipMsg = defaultMsg +
                "Call-Info: <Media:XXX>;purpose=info\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(CallType.UNKNOWN, sipMessage.getCallInfoType());
    }

    public void testGetContent() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetContent1() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Verifies that the contact expires time can be retrieved from the Contact
     * and Expires header.
     * Verifies that -1 is returned if there is no Contact or Expires header,
     * that the expires time is selected from the Contact header if set there,
     * otherwise it is selected from the Expires header.
     * @throws Exception if test case fails.
     */
    public void testGetContactExpireTime() throws Exception {
        String sipMsg = defaultMsg;
        SipMessage sipMessage;

        // Verify that if no Contact or Expires headers are present, -1 is returned.
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(-1, sipMessage.getContactExpireTime());

        // Verify that if no expiration is set in the Contact header, the one
        // in the Expires header is returned instead.
        sipMsg = defaultMsg +
                "Contact: <sip:person@host.com>\r\n" + "Expires: 5\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(5, sipMessage.getContactExpireTime());

        // Verify that if expiration is set in the Contact header, it is
        // returned.
        sipMsg = defaultMsg +
                "Contact: <sip:person@host.com>;expires=5\r\n" + "Expires: 7\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(5, sipMessage.getContactExpireTime());
    }

    /**
     * Verifies that the expires time can be retrieved from the Contact header.
     * Verifies that -1 is returned if there is no Contact header or if the
     * expires parameter has not been set.
     * @throws Exception if test case fails.
     */
    public void testGetExpiresTimeFromContactHeader() throws Exception {
        String sipMsg = defaultMsg;
        SipMessage sipMessage;

        // Verify that if no Contact header is present, -1 is returned.
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(-1, sipMessage.getExpireTimeFromContactHeader());

        // Verify that if no expiration is set in the Contact header, -1 is
        // returned.
        sipMsg = defaultMsg +
                "Contact: <sip:person@host.com>\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(-1, sipMessage.getExpireTimeFromContactHeader());

        // Verify that if expiration is set to zero in the Contact header,
        // zero is returned.
        sipMsg = defaultMsg +
                "Contact: <sip:person@host.com>;expires=0\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(0, sipMessage.getExpireTimeFromContactHeader());

        // Verify that if expiration is set in the Contact header, it is
        // returned.
        sipMsg = defaultMsg +
                "Contact: <sip:person@host.com>;expires=5\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(5, sipMessage.getExpireTimeFromContactHeader());
    }

    /**
     * Verifies that the expires time can be retrieved from the Expires header.
     * Verifies that -1 is returned if there is no Expires header.
     * @throws Exception if test case fails.
     */
    public void testGetExpireTimeFromExpiresHeader() throws Exception {
        String sipMsg = defaultMsg;
        SipMessage sipMessage;

        // Verify that if no Expires header is present, -1 is returned.
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(-1, sipMessage.getExpireTimeFromExpiresHeader());

        // Verify that if no expiration is set in the Expires header, -1 is
        // returned.
        sipMsg = defaultMsg +
                "Expires:   \r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(-1, sipMessage.getExpireTimeFromExpiresHeader());

        // Verify that if expiration is set to zero in the Expires header,
        // zero is returned.
        sipMsg = defaultMsg +
                "Expires: 0\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(0, sipMessage.getExpireTimeFromExpiresHeader());

        // Verify that if expiration is set in the Expires header, it is
        // returned.
        sipMsg = defaultMsg +
                "Expires: 5\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertEquals(5, sipMessage.getExpireTimeFromExpiresHeader());
    }

    /**
     * Verifies that the Q.850 cause/location can be retrieved from the
     * Reason header.
     * Verifies that location is null if not set in header.
     * Verifies that null is returned if there is no Reason header, if the
     * header could not be parsed, if the protocol is not Q.850 or if cause
     * is not set.
     * Verifies that null is returned if the Q.850 cause or location is
     * out-of-range.
     *
     * @throws Exception if test case fails.
     */
    public void testGetQ850CauseLocation() throws Exception {
        String sipMsg = defaultMsg;
        SipMessage sipMessage;
        Q850CauseLocationPair pair;

        // Verify that if no Reason header is present, null is returned.
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertNull(sipMessage.getQ850CauseLocation());

        // Verify that if protocol is NOT Q.850 in the Reason header, null is
        // returned.
        sipMsg = defaultMsg + "Reason: SIP;cause=16\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertNull(sipMessage.getQ850CauseLocation());

        // Verify that if no cause is set in the Reason header, null is returned.
        sipMsg = defaultMsg + "Reason: Q.850\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertNull(sipMessage.getQ850CauseLocation());

        // Verify that if no location is set in the Reason header, location is
        // set to null in the returned pair
        sipMsg = defaultMsg + "Reason: Q.850;cause=16\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        pair = sipMessage.getQ850CauseLocation();
        assertNotNull(pair);
        assertEquals(16, pair.getCause());
        assertNull(pair.getLocation());

        // Verify that if both cause and location is set in the Reason header,
        // both cause and location are set in the returned pair
        sipMsg = defaultMsg + "Reason: Q.850;cause=127;eri-location=14\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        pair = sipMessage.getQ850CauseLocation();
        assertNotNull(pair);
        assertEquals(127, pair.getCause());
        assertEquals(new Integer(14), pair.getLocation());

        // Verify that if cause is out-of-range, null is returned.
        sipMsg = defaultMsg + "Reason: Q.850;cause=128;eri-location=14\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertNull(sipMessage.getQ850CauseLocation());

        // Verify that if location is out-of-range, null is returned.
        sipMsg = defaultMsg + "Reason: Q.850;cause=127;eri-location=16\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        assertNull(sipMessage.getQ850CauseLocation());

        // Verify that if location is not a number, location is set to null.
        sipMsg = defaultMsg + "Reason: Q.850;cause=127;eri-location=x\r\n";
        sipMessage = new SipRequest((Request)msgParser.parseSIPMessage(sipMsg));
        pair = sipMessage.getQ850CauseLocation();
        assertNotNull(pair);
        assertEquals(127, pair.getCause());
        assertNull(pair.getLocation());
    }


    public void testGetMethod() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetUnsupportedButRequiredExtensions() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetVersion() throws Exception {
        //TODO: Test goes here...
    }

}
