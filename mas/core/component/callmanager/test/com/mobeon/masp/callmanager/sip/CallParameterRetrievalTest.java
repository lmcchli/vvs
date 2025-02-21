/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import junit.framework.TestCase;
import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.configuration.ConfigConstants;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.callhandling.CallParameters;
import com.mobeon.common.logging.ILoggerFactory;
import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.parser.URLParser;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.header.ims.HistoryInfoHeader;

import javax.sip.message.Request;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.ServerTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;

import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.RedirectingParty.RedirectingReason;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import org.jmock.Mock;

import java.text.ParseException;

/**
 * CallParameterRetrieval Tester.
 *
 * @author Malin Flodin
 */
public class CallParameterRetrievalTest extends TestCase
{
    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private static CallParameterRetrieval parameterRetrieval;

    // The StringMsgParser comes from the NIST SIP implementation and is not
    // part of the JAIN SIP interface. It is used for testing purposes only.
    private static final StringMsgParser stringMsgParser = new StringMsgParser();

    // Listed to shrink the test code.
    private static final PresentationIndicator UNKNOWN =
            PresentationIndicator.UNKNOWN;
    private static final PresentationIndicator ALLOWED =
            PresentationIndicator.ALLOWED;
    private static final PresentationIndicator RESTRICTED =
            PresentationIndicator.RESTRICTED;
    private static final RedirectingReason REASON_UNKNOWN =
            RedirectingReason.UNKNOWN;
    private static final String CAUSE_UNKNOWN = "unknown";
    Mock serverTransactionMock;
    Mock dialogMock;

    private static final String SIP_REQUEST_LINE =
            "INVITE sip:masUser@127.0.0.1:5060;transport=udp SIP/2.0\r\n";

    // User agents
    private static final String USER_AGENT_CISCO = "Cisco-SIPGateway/IOS-12.x";
    private static final String USER_AGENT_RADVISION = "RADVision ViaIP GW vers. 2.5";
    private static final String USER_AGENT_EYEBEAM = "eyeBeam release 3010n stamp 19039";
    private static final String USER_AGENT_EXPRESS_TALK = "Express Talk v1.03";
    private static final String USER_AGENT_MIRIAL = "Dylogic Mirial 4.3.6";
    private static final String USER_AGENT_UNKNOWN = "Zisco-SIPGateway/IOS-12.x";


    // Contents of SIP headers
    private static final String SIP = "sip:";
    private static final String TEL = "tel:";
    private static final String USER = "user";
    private static final String USER2 = "user2";
    private static final String NUMBER = "1234";
    private static final String NUMBER_0000 = "0000";
    private static final String NON_DIGIT_NUMBER = "1x34";
    private static final String TOO_LONG_NUMBER = "1234567890123456";
    private static final String HOST = "@host.com";
    private static final String HOST2 = "@host2.com";
    private static final String USER_PHONE = ";user=phone";
    private static final String TRANSPORT_UDP = ";transport=udp";
    private static final String PORT = ":5060";

    // URIs
    private static final String URI_UNKNOWN         = "fax:user@host.com";
    private static final String URI_NORMAL_SIP      =
            SIP + USER + HOST + PORT + TRANSPORT_UDP;
    private static final String URI_NORMAL_SIP2      =
            SIP + USER2 + HOST2 + PORT + TRANSPORT_UDP;
    private static final String URI_USER_PHONE      =
            SIP + NUMBER + HOST + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_NUMBER_WITHOUT_PHONE =
            SIP + NUMBER + HOST + PORT + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_0000 =
            SIP + NUMBER_0000 + HOST + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_WITH_PLUS =
            SIP + "+" + NUMBER + HOST + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_NOT_DIGIT =
            SIP + NON_DIGIT_NUMBER + HOST  + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_NOT_DIGIT_WITH_PLUS =
            SIP + "+" + NON_DIGIT_NUMBER + HOST  + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_TOO_SHORT =
            SIP + "+" + HOST  + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_TOO_LONG =
            SIP + "+" + TOO_LONG_NUMBER + HOST  + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_PHONE_WITHOUT_HOST =
            SIP + NUMBER + PORT + USER_PHONE + TRANSPORT_UDP;
    private static final String URI_USER_NUMBER = SIP + NUMBER + HOST + PORT + TRANSPORT_UDP;
    private static final String URI_NORMAL_TEL = TEL + NUMBER;
    private static final String URI_NORMAL_TEL_WITH_PLUS = TEL + "+" + NUMBER;
    private static final String URI_TEL_TOO_LONG = TEL + TOO_LONG_NUMBER;

    private CallManagerConfiguration config;

    public void setUp() throws Exception {
        super.setUp();
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Setup configuration
        ConfigurationManagerImpl configMgr = new ConfigurationManagerImpl();
        configMgr.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);
        ConfigurationReader.getInstance().setInitialConfiguration(
                configMgr.getConfiguration());
        ConfigurationReader.getInstance().update();
        config = ConfigurationReader.getInstance().getConfig();

        parameterRetrieval = CallParameterRetrieval.getInstance();
        serverTransactionMock = new Mock(ServerTransaction.class);
        dialogMock = new Mock(Dialog.class);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that the returned call parameters are null when the SIP request
     * is null.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenRequestIsNull() throws Exception {
        assertNull(parameterRetrieval.retrieveCallParameters(null, config).
                getCalledParty());
        assertNull(parameterRetrieval.retrieveCallParameters(null, config).
                getCallingParty());
        assertNull(parameterRetrieval.retrieveCallParameters(null, config).
                getRedirectingParty());
    }

    /**
     * Verify that the returned call parameters contain the URI if the
     * needed headers contain an unknown URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenHeadersContainUnknownUri()
            throws Exception
    {
        CallParameters callParameters = retrieveCallParameters(URI_UNKNOWN);
        assertCalledParty(URI_UNKNOWN, null, null, callParameters.getCalledParty());
        assertCallingParty(URI_UNKNOWN, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_UNKNOWN, null, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_UNKNOWN, false);
        assertCallingParty(URI_UNKNOWN, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI and the SIP user
     * if the headers contain a normal SIP URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenHeadersContainSipUri()
            throws Exception
    {
        CallParameters callParameters = retrieveCallParameters(URI_NORMAL_SIP);
        assertCalledParty(URI_NORMAL_SIP, USER, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_NORMAL_SIP, false);
        assertCallingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters does not contain a telephone
     * number if the received telephone number is 0000.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenNumberIs0000()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_0000);

        assertCalledParty(
                URI_USER_PHONE_0000, NUMBER_0000, null,
                callParameters.getCalledParty());
        assertCallingParty(
                URI_USER_PHONE_0000, NUMBER_0000, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(
                URI_USER_PHONE_0000, NUMBER_0000, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_USER_PHONE_0000, false);
        assertCallingParty(URI_USER_PHONE_0000, NUMBER_0000, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * and the telephone number if the headers contain a SIP URI including
     * the parameter user=phone and the user is a telephone number without a
     * ´+´ sign.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneWithoutPlus()
            throws Exception
    {
        CallParameters callParameters = retrieveCallParameters(URI_USER_PHONE);
        assertCalledParty(URI_USER_PHONE, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE, NUMBER, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_USER_PHONE, false);
        assertCallingParty(URI_USER_PHONE, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * and the telephone number if the headers contain a SIP URI including
     * the parameter user=phone and the user is a telephone number including
     * a ´+´ sign.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneWithPlus()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_WITH_PLUS);
        assertCalledParty(URI_USER_PHONE_WITH_PLUS, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_WITH_PLUS, NUMBER,
                NUMBER, UNKNOWN, callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_WITH_PLUS, NUMBER,
                NUMBER, UNKNOWN, REASON_UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_USER_PHONE_WITH_PLUS, false);
        assertCallingParty(URI_USER_PHONE_WITH_PLUS, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * but not the telephone number if the headers contain a SIP URI
     * including the parameter user=phone and the user is a non-digit string.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneButNotDigit()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_NOT_DIGIT);
        assertCalledParty(URI_USER_PHONE_NOT_DIGIT, NON_DIGIT_NUMBER,
                null, callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_NOT_DIGIT, NON_DIGIT_NUMBER,
                null, UNKNOWN, callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_NOT_DIGIT, NON_DIGIT_NUMBER,
                null, UNKNOWN, REASON_UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_USER_PHONE_NOT_DIGIT, false);
        assertCallingParty(URI_USER_PHONE_NOT_DIGIT, NON_DIGIT_NUMBER,
                null, UNKNOWN, callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contains the URI, the SIP user,
     * but not the telephone number if the headers contain a SIP URI
     * including the parameter user=phone and the user is a non-digit
     * string starting with a ´+´.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneWithPlusButNotDigit()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS);
        assertCalledParty(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS,
                "+" + NON_DIGIT_NUMBER, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS,
                "+" + NON_DIGIT_NUMBER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS,
                "+" + NON_DIGIT_NUMBER, null, UNKNOWN, REASON_UNKNOWN,
                CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters =
                retrieveCallParameters(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS, false);
        assertCallingParty(URI_USER_PHONE_NOT_DIGIT_WITH_PLUS,
                "+" + NON_DIGIT_NUMBER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contains the URI, the SIP user,
     * but not the telephone number if the headers contain a SIP URI
     * including the parameter user=phone and the user is a telephone
     * number that is too short (i.e. consisting only of a ´+´ sign).
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneButTooShort()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_TOO_SHORT);
        assertCalledParty(URI_USER_PHONE_TOO_SHORT, "+", null,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_TOO_SHORT, "+", null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_TOO_SHORT, "+", null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

        callParameters =
                retrieveCallParameters(URI_USER_PHONE_TOO_SHORT, false);
        assertCallingParty(URI_USER_PHONE_TOO_SHORT, "+", null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }


    /**
     * Verify that the returned Call Parameters contain the URI, the SIP user,
     * but not the telephone number if the headers contain a SIP URI
     * including the parameter user=phone and the user is a telephone
     * number longer than 15 digits.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneButTooLong()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_TOO_LONG);
        assertCalledParty(URI_USER_PHONE_TOO_LONG, "+" + TOO_LONG_NUMBER,
                null, callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_TOO_LONG, "+" + TOO_LONG_NUMBER,
                null, UNKNOWN, callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_TOO_LONG,
                "+" + TOO_LONG_NUMBER, null, UNKNOWN, REASON_UNKNOWN,
                CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters =
                retrieveCallParameters(URI_USER_PHONE_TOO_LONG, false);
        assertCallingParty(URI_USER_PHONE_TOO_LONG, "+" + TOO_LONG_NUMBER,
                null, UNKNOWN, callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned Call Parameters contain the URI, and the
     * telephone number but no SIP user if the headers contain a SIP URI
     * without a host and including the parameter user=phone.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserIsPhoneAndNoHost()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_USER_PHONE_WITHOUT_HOST);
        assertCalledParty(URI_USER_PHONE_WITHOUT_HOST, null, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_PHONE_WITHOUT_HOST, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_PHONE_WITHOUT_HOST, null, null,
                UNKNOWN, REASON_UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

        callParameters =
                retrieveCallParameters(URI_USER_PHONE_WITHOUT_HOST, false);
        assertCallingParty(URI_USER_PHONE_WITHOUT_HOST, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contains the URI, the SIP user,
     * and the telephone number if the headers contain a SIP URI with a
     * telephone number and the UserAgent contains cisco, radvision, eyebeam,
     * express talk or mirial.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserAgentIsKnown()
            throws Exception
    {
        CallParameters callParameters;

        callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_CISCO),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, NUMBER, ALLOWED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_EYEBEAM),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_EXPRESS_TALK),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_MIRIAL),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_RADVISION),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * but not the telephone number if the headers contain a SIP URI with a
     * telephone number and the UserAgent is unknown.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserAgentIsUnknown()
            throws Exception
    {
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, USER_AGENT_UNKNOWN),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * but not the telephone number if the headers contain a
     * SIP URI and the UserAgent is empty.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenUserAgentIsEmpty()
            throws Exception
    {
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createRequestEventFromAgent(URI_USER_NUMBER, ""),
                config);
        assertCalledParty(URI_USER_NUMBER, NUMBER, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_USER_NUMBER, NUMBER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_USER_NUMBER, NUMBER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * and the telephone number if the headers contain a TEL URL with a ´+´.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenTelUrlWithPlus()
            throws Exception
    {
        CallParameters callParameters =
                retrieveCallParameters(URI_NORMAL_TEL_WITH_PLUS);
        assertCalledParty(URI_NORMAL_TEL_WITH_PLUS, null, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_NORMAL_TEL_WITH_PLUS, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_NORMAL_TEL_WITH_PLUS, null, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_NORMAL_TEL_WITH_PLUS, false);
        assertCallingParty(URI_NORMAL_TEL_WITH_PLUS, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * and the telephone number if the headers contain a TEL URL without a ´+´.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCalledPartyWhenTelUrlWithoutPlus()
            throws Exception
    {
        CallParameters callParameters = retrieveCallParameters(URI_NORMAL_TEL);
        assertCalledParty(URI_NORMAL_TEL, null, NUMBER,
                callParameters.getCalledParty());
        assertCallingParty(URI_NORMAL_TEL, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_NORMAL_TEL, null, NUMBER, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_NORMAL_TEL, false);
        assertCallingParty(URI_NORMAL_TEL, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned call parameters contain the URI, the SIP user,
     * but not the telephone number if the headers contain a TEL URL that has
     * too many digits (> 15).
     * @throws Exception if test case fails.
     */
    public void testRetrieveCalledPartyWhenTelUrlTooLong()
            throws Exception
    {
        CallParameters callParameters = retrieveCallParameters(URI_TEL_TOO_LONG);
        assertCalledParty(URI_TEL_TOO_LONG, null, null,
                callParameters.getCalledParty());
        assertCallingParty(URI_TEL_TOO_LONG, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertRedirectingParty(URI_TEL_TOO_LONG, null, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        callParameters = retrieveCallParameters(URI_TEL_TOO_LONG, false);
        assertCallingParty(URI_TEL_TOO_LONG, null, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the CallingParty is retrieved as calling if the
     * Remote-Party-ID header lacks the Party parameter.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersWhenRemotePartyIdHeaderLacksParty()
            throws Exception
    {
        String toHeader = "Remote-Party-ID: <" + URI_NORMAL_TEL + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                        createEventFromRequest((Request) sipMessage),
                        config);
        assertCallingParty(URI_NORMAL_TEL, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned CallingParty is null if the
     * Remote-Party-ID could not be parsed.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenRemotePartyCouldNotBeParsed()
            throws Exception
    {
        String toHeader = "Remote-Party-ID: <<><<>>\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());
    }

    /**
     * Verify that the returned CallingParty is null if the Party parameter in
     * the Remote-Party-ID header is not set to "calling".
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenRemotePartyIdHeaderNotCalling()
            throws Exception
    {
        String uri = "sip:user@host.com";
        String toHeader = "Remote-Party-ID: <" + uri + ">;party=called\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());
    }

    public void testRetrievalCallingPartyWithMultipleRemotePartyIdHeaders()
            throws Exception {
        String uri = "sip:user@host.com";
        String uri2 = "sip:user2@anotherhost.com";
        String uri3 = "sip:willnotbeused@anotherhost.com";
        String toHeader = "Remote-Party-ID: <" + uri + ">;party=called\r\n";
        toHeader += "Remote-Party-ID: <" + uri2 + ">;party=calling\r\n";
        toHeader += "Remote-Party-ID: <" + uri3 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri2, "user2", null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned CallingParty is null if the
     * P-Asserted-Identity could not be parsed.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenAssertedIdCouldNotBeParsed()
            throws Exception
    {
        String toHeader = "P-Asserted-Identity: <<><<>>\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());
    }

    /**
     * Verify that the returned Calling Party contains the first
     * P-Asserted-Identity URI if no one was found containing a phone number.
     * @throws Exception if test case fails.
     */
    public void testRetrievalCallingPartyWithMultipleNonPhoneAssertedIdHeaders()
            throws Exception {
        String uri = "sip:user1@host1.com";
        String uri2 = "sip:user2@host2.com";
        String toHeader = "P-Asserted-Identity: <" + uri + ">\r\n";
        toHeader += "P-Asserted-Identity: <" + uri2 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri, "user1", null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned Calling Party contains the first
     * P-Asserted-Identity URI if it contains a telephone number.
     * @throws Exception if test case fails.
     */
    public void testRetrievalCallingPartyWhenFirstAssertedIdHeaderIsPhone()
            throws Exception {
        String uri = "sip:1234@host1.com;user=phone";
        String uri2 = "sip:user2@host2.com";
        String toHeader = "P-Asserted-Identity: <" + uri + ">\r\n";
        toHeader += "P-Asserted-Identity: <" + uri2 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri, "1234", "1234", UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned Calling Party contains the second
     * P-Asserted-Identity URI if that is the first URI containing a
     * telephone number.
     * @throws Exception if test case fails.
     */
    public void testRetrievalCallingPartyWhenSecondAssertedIdHeaderIsPhone()
            throws Exception {
        String uri = "sip:user1@host1.com";
        String uri2 = "tel:1234";
        String toHeader = "P-Asserted-Identity: <" + uri + ">\r\n";
        toHeader += "P-Asserted-Identity: <" + uri2 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri2, null, "1234", UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned Calling Party contains the correct
     * P-Asserted-Identity URI if a SIP URI indicates that it should
     * contain a telephone number (user=phone), but the actual value contains
     * non-digits as well.
     * @throws Exception if test case fails.
     */
    public void testRetrievalCallingPartyFromAssertedIdHeaderWhenPhoneParameterUsedIncorrectly()
            throws Exception {
        String uri = "sip:user1@host1.com;user=phone";
        String uri2 = "tel:1234";
        String toHeader = "P-Asserted-Identity: <" + uri + ">\r\n";
        toHeader += "P-Asserted-Identity: <" + uri2 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri2, null, "1234", UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned Calling Party contains the correct
     * P-Asserted-Identity URI if a TEL URI contains non-digits as well.
     * @throws Exception if test case fails.
     */
    public void testRetrievalCallingPartyFromAssertedIdHeaderWhenTelUriUsedIncorrectly()
            throws Exception {
        String uri = "sip:user1@host1.com";
        String uri2 = "tel:123q";
        String toHeader = "P-Asserted-Identity: <" + uri + ">\r\n";
        toHeader += "P-Asserted-Identity: <" + uri2 + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertCallingParty(uri, "user1", null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned CallingParty presentation is
     * restricted in the following situations: <br>
     * <ol>
     * <li>
     * the P-Asserted-Identity header is existing and the
     * Privacy header is set to a value other than "none",
     * or if there is no P-Asserted-Identity header:
     * </li>
     * <li>
     * the Remote-Party-ID header contains a privacy value other than "off",
     * or if there is no Remote-Party-ID header:
     * </li>
     * <li>
     * the Privacy header is set to a value other than "none".
     * </li>
     * </ol>
     *
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenPresentationRestricted()
            throws Exception
    {
        String assertedIdentityHeader;
        String privacyHeader;
        String remotePartyHeader;
        String fromHeader;
        SIPMessage sipMessage;
        CallParameters callParameters;

        // P-Asserted-Identity, privacy header
        assertedIdentityHeader =
                "P-Asserted-Identity: <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: header\r\n";
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=off\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader +
                        assertedIdentityHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // P-Asserted-Identity, privacy header;id
        assertedIdentityHeader =
                "P-Asserted-Identity: <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: header;id\r\n";
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=off\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader +
                        assertedIdentityHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage),
                config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy full
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=full\r\n";
        privacyHeader = "Privacy: none\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy name
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=name\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy uri
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=uri\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy ipaddr
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=ipaddr\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // From + Privacy header;id
        fromHeader = "From: displayname <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: header;id\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + fromHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // From + Privacy other
        fromHeader = "From: displayname <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: other\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + fromHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned CallingParty presentation is
     * allowed in the following situations: <br>
     * <ol>
     * <li>
     * the P-Asserted-Identity and Privacy headers exists and the
     * Privacy header is set to value "none",
     * or if there is no P-Asserted-Identity header:
     * </li>
     * <li>
     * the Remote-Party-ID header contains a privacy value "off",
     * or if there is no Remote-Party-ID header:
     * </li>
     * <li>
     * the Privacy header is set to value "none".
     * </li>
     * </ol>
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenPresentationAllowed()
            throws Exception
    {
        String assertedIdentityHeader;
        String privacyHeader;
        String remotePartyHeader;
        String fromHeader;
        SIPMessage sipMessage;
        CallParameters callParameters;

        // P-Asserted-Identity, privacy none
        assertedIdentityHeader =
                "P-Asserted-Identity: <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: none\r\n";
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=uri\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader +
                        assertedIdentityHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy off
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=off\r\n";
        privacyHeader = "Privacy: header;id\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + privacyHeader + remotePartyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // From + Privacy none
        fromHeader = "From: displayName <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: none\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + fromHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // From + Privacy history
        fromHeader = "From: displayName <" + URI_NORMAL_SIP + ">\r\n";
        privacyHeader = "Privacy: history\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + fromHeader + privacyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

    }

    /**
     * Verify that the returned CallingParty presentation is
     * unknown in the following situations: <br>
     * <ol>
     * <li>
     * the P-Asserted-Identity header exists but there is no Privacy header,
     * or if there is no P-Asserted-Identity header:
     * </li>
     * <li>
     * the Remote-Party-ID header contains no privacy parameter,
     * or if there is no Remote-Party-ID header:
     * </li>
     * <li>
     * there is no Privacy header.
     * </li>
     * </ol>
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingPartyWhenPresentationUnknown()
            throws Exception
    {
        String assertedIdentityHeader;
        String privacyHeader;
        String remotePartyHeader;
        String fromHeader;
        SIPMessage sipMessage;
        CallParameters callParameters;

        // P-Asserted-Identity, no privacy header
        assertedIdentityHeader =
                "P-Asserted-Identity: <" + URI_NORMAL_SIP + ">\r\n";
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling;privacy=uri\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + remotePartyHeader +
                        assertedIdentityHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Remote-Party-ID, privacy not included
        remotePartyHeader = "Remote-Party-ID: <" + URI_NORMAL_SIP +
                ">;party=calling\r\n";
        privacyHeader = "Privacy: none\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + privacyHeader + remotePartyHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // From but no privacy header
        fromHeader = "From: displayName <" + URI_NORMAL_SIP + ">\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + fromHeader);
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the returned RedirectingParty is null if the
     * Diversion header could not be parsed.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenDiversionCannotBeParsed()
            throws Exception
    {
        String toHeader = "Diversion: <<><<>>\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * restricted if the Diversion header's display name is anonymous and the
     * user agent indicates Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiRestrictedForCisco()
            throws Exception {
        String diversionHeader =
                "Diversion: anonymous <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_CISCO) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header's display name is "unknown" and the
     * user agent indicates Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiUnknownForCisco()
            throws Exception {
        String diversionHeader =
                "Diversion: unknown <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_CISCO) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * allowed if the Diversion header's display name is NOT "anonymous" or
     * "unknown" (if any) and the user agent indicates Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiAllowedForCisco()
            throws Exception {
        String diversionHeader =
                "Diversion: displayName <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_CISCO) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header's display name is anonymous and the
     * user agent indicates other GW than Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenDisplayNameRestrictedForOtherGW()
            throws Exception {
        String diversionHeader =
                "Diversion: anonymous <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_MIRIAL) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header's display name is "unknown" and the
     * user agent indicates other gateway than Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenDisplayNameUnknownForOtherGW()
            throws Exception {
        String diversionHeader =
                "Diversion: unknown <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_MIRIAL) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header's display name is NOT "anonymous" or
     * "unknown" (if any) and the user agent indicates other gateway than Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenDisplayNameAllowedForOtherGW()
            throws Exception {
        String diversionHeader =
                "Diversion: displayName <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_MIRIAL) +
                                diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * restricted if the Diversion header contains privacy=full and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiFull()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=full\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * restricted if the Diversion header contains privacy=uri and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiUri()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=uri\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * restricted if the Diversion header contains privacy=name and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiName()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=name\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * restricted if the Diversion header contains privacy=ipaddr and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiIpaddr()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=ipaddr\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * allowed if the Diversion header contains privacy=off and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiOff()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=off\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header contains privacy=on and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiOn()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=on\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header contains privacy=xxx and the
     * user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenPiXxx()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;privacy=xxx\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty presentation is
     * unknown if the Diversion header does not contain the privacy parameter
     * and the user agent is not Cisco.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWhenNoPrivacyParameter()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * unknown if the Diversion header contains "reason=unknown".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnknown()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unknown\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN, callParameters.getRedirectingParty());
    }


     /**
     * Verify that the returned RedirectingParty reason is
     * Unknown if the Diversion header contains "reason=unknown"
     * and the Generic Transparency Decsriptor (GTD) is present in the Invite message
     * with Redirection Information for Redirecting Party set to "u", i.e. "Unknown"
     * (see Q.763 for more information).
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnknownGTDuString()
            throws Exception {

        String total = createGtdBodyWithRedirectingReason("u");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unknown\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "unknown", callParameters.getRedirectingParty());
    }

     /**
     * Verify that the returned RedirectingParty reason is
     * Unknown if the Diversion header contains "reason=unknown"
     * and the Generic Transparency Decsriptor (GTD) is present in the Invite message
     * with Redirection Information for Redirecting Party set to "0", i.e. "Unknown"
     * (see Q.763 for more information).
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnknownGTD0String()
            throws Exception {

        String total = createGtdBodyWithRedirectingReason("0");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unknown\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "unknown", callParameters.getRedirectingParty());
    }

     /**
     * Verify that the returned RedirectingParty reason is
     * Unknown if the Diversion header contains "reason=unknown"
     * and the Generic Transparency Decsriptor (GTD) is present in the Invite message
     * with Redirection Information for Redirecting Party set to "", i.e. "Unknown"
     * (see Q.763 for more information).
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnknownGTDNoString()
            throws Exception {

        String total = createGtdBodyWithRedirectingReason("");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unknown\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "unknown", callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * User Busy if the Diversion header contains "reason=user-busy".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUserBusy()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=user-busy\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.USER_BUSY, "user-busy",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * User Busy if the Diversion header contains "reason=user-busy".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUserBusyGTD()
            throws Exception {
        String total = createGtdBodyWithRedirectingReason("6");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=user-busy\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE, "mobile subscriber not reachable",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * No Answer if the Diversion header contains "reason=no-answer".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonNoAnswer()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=no-answer\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.NO_REPLY, "no-answer",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * No Answer if the Diversion header contains "reason=no-answer".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonNoAnswerGTD()
            throws Exception {
        String total = createGtdBodyWithRedirectingReason("1");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=no-answer\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.NO_REPLY, "no-answer",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Unavailable if the Diversion header contains "reason=unavailable".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnavailable()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unavailable\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE,
                "unavailable", callParameters.getRedirectingParty());
    }

     /**
     * Verify that the returned RedirectingParty reason is
     * Unavailable if the Diversion header contains "reason=unavailable".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnavailableGTD()
            throws Exception {
        String total = createGtdBodyWithRedirectingReason("6");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unavailable\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE,
                "mobile subscriber not reachable", callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Unconditional if the Diversion header contains "reason=unconditional".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnconditional()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unconditional\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNCONDITIONAL, "unconditional",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Unconditional if the Diversion header contains "reason=unconditional".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonUnconditionalGTD()
            throws Exception {
        String total = createGtdBodyWithRedirectingReason("1");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unconditional\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNCONDITIONAL, "unconditional",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Time Of Day if the Diversion header contains "reason=time-of-day".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonTimeOfDay()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=time-of-day\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "time-of-day",
                callParameters.getRedirectingParty());
    }

     /**
     * Verify that the returned RedirectingParty reason is
     * Time Of Day if the Diversion header contains "reason=time-of-day"
     * even if GTD is specified to another value (User Busy).
     * @throws Exception if test case fails.
     */
   public void testRetrieveRedirectingPartyReasonTimeOfDayGTDBusyUser()
            throws Exception {
        String total = createGtdBodyWithRedirectingReason("1");
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=time-of-day\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader + total);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "time-of-day",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Do Not Disturb if the Diversion header contains "reason=do-not-disturb".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonDoNotDisturb()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=do-not-disturb\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "do-not-disturb",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Deflection if the Diversion header contains "reason=deflection".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonDeflection()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=deflection\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE, "deflection",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Follow Me if the Diversion header contains "reason=follow-me".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonFollowMe()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=follow-me\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "follow-me",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Out Of Service if the Diversion header contains "reason=out-of-service".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonOutOfService()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=out-of-service\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "out-of-service",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Away if the Diversion header contains "reason=away".
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonAway()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=away\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "away",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the returned RedirectingParty reason is
     * Unknown if the Diversion header contains "reason=zzz", i.e. it is unknown.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyReasonSomethingOther()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=zzz\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "zzz",
                callParameters.getRedirectingParty());
    }

    /**
    * Verify that the returned RedirectingParty reason is
    * Mobile Subscriber Not Reachable if the Diversion header contains "reason=unknown"
    * and the Generic Transparency Decsriptor (GTD) is present in the Invite message
    * with Redirection Information for Redirecting Party set to "6", i.e. "Mobile
    * Subscriber Not Reachable" (see Q.763 for more information).
    * @throws Exception if test case fails.
    */
   public void testRetrieveRedirectingPartyReasonMobileSubscriberNotReachable()
           throws Exception {

       String total = createGtdBodyWithRedirectingReason("6");
       String diversionHeader =
               "Diversion: displayname <" + URI_NORMAL_SIP + ">;reason=unknown\r\n";
       SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                       SIP_REQUEST_LINE + diversionHeader + total);

       CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
               createEventFromRequest((Request) sipMessage), config);
       assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
               RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE, "mobile subscriber not reachable", callParameters.getRedirectingParty());
   }


    /**
     * Verify that the returned RedirectingParty reason is
     * Unknown if the Diversion header does not contain the reason parameter.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyWithoutReason()
            throws Exception {
        String diversionHeader =
                "Diversion: displayname <" + URI_NORMAL_SIP + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that all user info parameters are removed before storing the
     * user part in the redirecting party.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingPartyContainingParameters()
            throws Exception
    {
        String toHeader = "Diversion: displayname <" + SIP + "user;param=1" + HOST + ">\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + toHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(SIP + "user;param=1" + HOST,
                USER, null, UNKNOWN,
                REASON_UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the phonenumber is extracted from the Diversion header when user=phone is set
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephone()
           throws Exception
    {
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertRedirectingParty(URI_USER_PHONE, NUMBER, NUMBER, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that a phonenumber is not extracted from the Diversion header
     * when the URI does not contain user=phone
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhone()
           throws Exception
    {
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);

        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the phonenumber is extracted from the diversion header
     * despite that user=phone is missing when it is configured to always threat
     * empty uri:s as phonenumbers.
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhoneConfiguredEmpty()
           throws Exception
    {
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, NUMBER, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

    }

    /**
     * Verify that the phonenumber is extracted from the diversion header
     * despite that user=phone is missing when it is configured to always threat
     * all uri:s as phonenumbers.
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhoneConfiguredAll()
           throws Exception
    {
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_ALL);
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + diversionHeader);

        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_ALL);
        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, NUMBER, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

    }

    /**
     * Verify that the phonenumber is extracted from the diversion header for an unknown UA
     * despite that user=phone is missing when it is configured to always threat
     * all uri:s as phonenumbers.
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhoneUAUnknownConfiguredAll()
           throws Exception
    {
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_ALL);
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_UNKNOWN) +
                                diversionHeader);


        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_ALL);
        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, NUMBER, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

    }

    /**
     * Verify that no phonenumber is extracted from the diversion header for an unknown UA
     * despite that user=phone is missing when it is configured to threat
     * empty uri:s as phonenumbers.
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhoneUAUnknownConfiguredEmpty()
           throws Exception
    {
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_UNKNOWN) +
                                diversionHeader);


        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, null, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

    }

    /**
     * Verify that the phonenumber is extracted from the diversion header for an unknown UA
     * despite that user=phone is missing when it is configured to threat
     * empty uri:s and all uri:s as phonenumbers.
     * @throws Exception
     */
    public void testRetrievedRedirectingPartyTelephoneWithoutPhoneUAUnknownConfiguredEmptyAndAll()
           throws Exception
    {
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        parameterRetrieval.addUserAgent(ConfigConstants.USER_AGENT_ALL);
        String diversionHeader =
                "Diversion: displayname <" + URI_USER_NUMBER_WITHOUT_PHONE + ">\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + createUserAgent(USER_AGENT_UNKNOWN) +
                                diversionHeader);


        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_EMPTY);
        parameterRetrieval.removeUserAgent(ConfigConstants.USER_AGENT_ALL);
        assertRedirectingParty(URI_USER_NUMBER_WITHOUT_PHONE, NUMBER, NUMBER, UNKNOWN,
                RedirectingReason.UNKNOWN, CAUSE_UNKNOWN,
                callParameters.getRedirectingParty());

    }

    /**
     * Verify that the calling parameters could be retrieved from the To-header
     * URI when test input is unsupported.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersFromToUriWhenTestInputNotSupported()
            throws Exception
    {
        String uri = URI_NORMAL_SIP + ";test=on;calling=1234;called=1234;redir=1234";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());
        assertCalledParty(
                "sip:masUser@127.0.0.1:5060;transport=udp",
                "masUser", null,
                callParameters.getCalledParty());
        assertNull(callParameters.getRedirectingParty());
    }

    /**
     * Verify that the calling parameters could be set in the To-header URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallingParametersFromToUri()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri;
        SIPMessage sipMessage;
        CallParameters callParameters;

        // Verify test not set
        uri = URI_NORMAL_SIP + ";calling=1234>";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());

        // Verify calling number
        uri = URI_NORMAL_SIP + ";test=on;calling=1234";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        // Verify calling number allowed
        uri = URI_NORMAL_SIP + ";test=on;calling_privacy=off";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, null, ALLOWED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);

        uri = URI_NORMAL_SIP + ";test=on;calling_privacy=on";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, null, RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }

    /**
     * Verify that the called parameters could be set in the To-header URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCalledParametersFromToUri()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ";test=on;called=1234";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCalledParty(uri, null, NUMBER, callParameters.getCalledParty());
    }

    /**
     * Verify that the redirecting parameters could be set in the To-header URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingParametersFromToUri()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ";test=on;redir=1234";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, NUMBER, UNKNOWN, REASON_UNKNOWN,
                CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_privacy=off";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, ALLOWED, REASON_UNKNOWN,
                CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_privacy=on";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, RESTRICTED, REASON_UNKNOWN,
                CAUSE_UNKNOWN, callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=time-of-day";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "time-of-day",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the redirecting cause could be set in the To-header URI and
     * parsed by CM even though the ´-´ char in e.g. user-busy is escaped to
     * %2D.
     * @throws Exception if test case fails.
     */
    public void testRetrieveRedirectingParametersFromToUriWithEscapedCharInCause()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ";test=on;redir_cause=do%2Dnot%2Ddisturb";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "do-not-disturb",
                callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=follow%2Dme";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "follow-me",
                callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=no%2Danswer";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.NO_REPLY, "no-answer",
                callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=out%2Dof%2Dservice";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "out-of-service",
                callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=time%2Dof%2Dday";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.UNKNOWN, "time-of-day",
                callParameters.getRedirectingParty());

        uri = URI_NORMAL_SIP + ";test=on;redir_cause=user%2Dbusy";
        sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(uri, null, null, UNKNOWN,
                RedirectingReason.USER_BUSY, "user-busy",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the call parameters could be set in the To-header URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersFromToUri()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ";test=on;" +
                "calling=1234;calling_privacy=on;called=2345;" +
                "redir=3456;redir_privacy=off;redir_cause=do-not-disturb";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, "1234", RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertCalledParty(uri, null, "2345",
                callParameters.getCalledParty());
        assertRedirectingParty(uri, null, "3456", ALLOWED,
                RedirectingReason.UNKNOWN, "do-not-disturb",
                callParameters.getRedirectingParty());
    }

    public void testRetrieveCallParametersFromToUriWithEscapeChars() throws Exception {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ";test=on;" +
                "calling=46%2A10%2A1234;calling_privacy=on;called=10%232345;" +
                "redir=%2B46%2A%2A3456;redir_privacy=off;redir_cause=do%2dnot%2ddisturb";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(SIP_REQUEST_LINE + createTo(uri));
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, "46*10*1234", RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertCalledParty(uri, null, "10#2345",
                callParameters.getCalledParty());
        assertRedirectingParty(uri, null, "+46**3456", ALLOWED,
                RedirectingReason.UNKNOWN, "do-not-disturb",
                callParameters.getRedirectingParty());
    }

    /**
     * Verify that the calling parameters could be retrieved from the Request
     * URI when test input is unsupported.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersFromRequestUriWhenTestInputNotSupported()
            throws Exception
    {
        String uri = URI_NORMAL_SIP +";test=on;calling=1234;called=1234;redir=1234";
        String requestUri = "INVITE " + uri + " SIP/2.0\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(requestUri);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getCallingParty());
        assertCalledParty(URI_NORMAL_SIP, USER, null, callParameters.getCalledParty());
        assertNull(callParameters.getRedirectingParty());
    }

    /**
     * Verify that the call parameters could be set in the Request-URI.
     * @throws Exception if test case fails.
     */
    public void testRetrieveCallParametersFromRequestUri()
            throws Exception
    {
        // Activate support test input
        config.setSupportTestInput(true);

        String uri = URI_NORMAL_SIP + ":5060;transport=udp" + ";test=on;" +
                "calling=1234;calling_privacy=on;called=2345;" +
                "redir=3456;redir_privacy=off;redir_cause=do-not-disturb";
        String requestUri = "INVITE " + uri + " SIP/2.0\r\n";
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(requestUri);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(uri, null, "1234", RESTRICTED,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
        assertCalledParty(uri, null, "2345",
                callParameters.getCalledParty());
        assertRedirectingParty(uri, null, "3456", ALLOWED,
                RedirectingReason.UNKNOWN, "do-not-disturb",
                callParameters.getRedirectingParty());
    }

    public void testNumberCompletionComplete() throws Exception {
        // Activate support test input
        config.setSupportTestInput(true);

        String total = createGtdBodyWithNumberCompletion("y");
        String uri = URI_NORMAL_SIP + ";test=on;calling=1234";
        String msg = SIP_REQUEST_LINE + createTo(uri) + total;
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(msg);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP,
                null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.COMPLETE);
    }

    public void testNumberCompletionIncomplete() throws Exception {
        // Activate support test input
        config.setSupportTestInput(true);

        String total = createGtdBodyWithNumberCompletion("n");
        String uri = URI_NORMAL_SIP + ";test=on;calling=1234";
        String msg = SIP_REQUEST_LINE + createTo(uri) + total;
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(msg);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP,
                null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.INCOMPLETE);
    }

    public void testNumberCompletionUnknown() throws Exception {
        // Activate support test input
        config.setSupportTestInput(true);

        String total = createGtdBodyWithNumberCompletion("u");
        String uri = URI_NORMAL_SIP + ";test=on;calling=1234";
        String msg = SIP_REQUEST_LINE + createTo(uri) + total;
        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(msg);
        CallParameters callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertCallingParty(URI_NORMAL_SIP,
                null, NUMBER, UNKNOWN,
                callParameters.getCallingParty(), NumberCompletion.UNKNOWN);
    }
    /**
     * Test retrieval of presentation indicator when using History-Info as
     * redirecting party.
     * @throws Exception
     */
    public void testGetPiForHistory() throws Exception {

        String historyHeader;
        Request request;
        PresentationIndicator pi;

        pi = parameterRetrieval.getPiForHistory(null, null);
        assertEquals(PresentationIndicator.UNKNOWN,pi);

        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), null);
        assertEquals(PresentationIndicator.ALLOWED,pi);

        pi = parameterRetrieval.getPiForHistory(null, request);
        assertEquals(PresentationIndicator.UNKNOWN,pi);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.ALLOWED,pi);



        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), null);
        assertEquals(PresentationIndicator.RESTRICTED,pi);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);



        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1\r\n" +
                "Privacy: history\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1\r\n" +
                "Privacy: history\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1\r\n" +
                "Privacy: none\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);

        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1\r\n" +
                "Privacy: none\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.ALLOWED,pi);

        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1\r\n" +
                "Privacy: id;header\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1\r\n" +
                "Privacy: qwerty;session;abc\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302>;index=1\r\n" +
                "Privacy: qwerty;session;abc\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);

        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302>;index=1\r\n" +
                "Privacy: qwerty;abc\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.ALLOWED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=apa%3bhistory>;index=1\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=apa%3Bhomer>;index=1\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.ALLOWED,pi);



        historyHeader = "History-Info: <" + URI_NORMAL_SIP +
                "?Reason=SIP%3Bcause%3D302&Privacy=apa%3Bhistory%3Bfoo>;index=1\r\n";

        request = (Request)stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        pi = parameterRetrieval.getPiForHistory((HistoryInfoHeader)
                request.getHeader(HistoryInfoHeader.NAME), request);
        assertEquals(PresentationIndicator.RESTRICTED,pi);


    }

    /**
     * Test retrieval of SIP cause value from a Reason header.
     * @throws Exception
     */
    public void testGetSipCauseFromReason() throws Exception {
        assertNull(parameterRetrieval.getSipCauseFromReason(null));
        assertNull(parameterRetrieval.getSipCauseFromReason(""));
        assertNull(parameterRetrieval.getSipCauseFromReason("abc;cause=101"));
        assertNull(parameterRetrieval.getSipCauseFromReason("cause=222"));
        assertEquals("302",parameterRetrieval.getSipCauseFromReason("SIP;cause=302"));
        assertEquals("302",parameterRetrieval.getSipCauseFromReason("SIP;cause=302;"));
        assertEquals("1",parameterRetrieval.getSipCauseFromReason("SIP;cause=1"));
        assertEquals("123456",parameterRetrieval.getSipCauseFromReason("SIP;cause=123456"));
        assertNull(parameterRetrieval.getSipCauseFromReason("SIP;cause=302abc"));
        assertNull(parameterRetrieval.getSipCauseFromReason("SIP;cause="));
        assertNull(parameterRetrieval.getSipCauseFromReason("SIP;cause=abc"));
        assertNull(parameterRetrieval.getSipCauseFromReason("SIP;abc=200"));
        assertEquals("408",parameterRetrieval.getSipCauseFromReason("SIP;cause=408;text=abc"));
        assertEquals("483",parameterRetrieval.getSipCauseFromReason("SIP;foo=bar;cause=483"));
        assertEquals("486",parameterRetrieval.getSipCauseFromReason("SIP;foo=bar;cause=486;text=abc"));
    }


    /**
     * Positive tests of retrieving RedirectingParty from History-Info header
     * @throws Exception
     */
    public void testRetrieveRedirectingPartyUsingHistoryInfo() throws Exception {

        String historyHeader;
        SIPMessage sipMessage;
        CallParameters callParameters;

        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP + "?Reason=SIP%3Bcause%3D302>;index=1;foo=bar," +
                        "<" + URI_NORMAL_SIP + "?Reason=SIP%3Bcause%3D408>;index=1.1\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, UNKNOWN,
                RedirectingReason.NO_REPLY, "408",
                callParameters.getRedirectingParty());




        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D480>;index=1.1\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE, "480",
                callParameters.getRedirectingParty());


        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302&Privacy=other>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D503>;index=1.1\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE, "503",
                callParameters.getRedirectingParty());


        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302&Privacy=other>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D404>;index=1.1\r\n" +
                        "Privacy: id;history\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.UNKNOWN, "404",
                callParameters.getRedirectingParty());



        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP + ">;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D486>;index=1.1\r\n" +
                        "Privacy: id;history;foobar\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.USER_BUSY, "486",
                callParameters.getRedirectingParty());



        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D444>;index=1.1\r\n" +
                        "Privacy: id\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.UNKNOWN, "444",
                callParameters.getRedirectingParty());



        historyHeader =
                "History-Info: " +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D101&Privacy=none>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D102&Privacy=none>;index=1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D103&Privacy=none>;index=1.1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D104&Privacy=none>;index=1.1.1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D105&Privacy=none>;index=1.1.1.1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D106&Privacy=none>;index=1.1.1.1.1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D107&Privacy=none>;index=1.1.1.1.1.1.1," +
                        "<" + URI_NORMAL_SIP + "?Reason=SIP%3Bcause%3D108&Privacy=history>;index=1.1.1.1.1.1.1.1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D109>;index=1.1.1.1.1.1.1.1.1\r\n";

        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.UNKNOWN, "109",
                callParameters.getRedirectingParty());


    }


    /**
     * Negative tests of retrieving RedirectingParty from History-Info header
     * @throws Exception
     */
    public void testRetrieveRedirectingPartyUsingHistoryInfo_neg() throws Exception {

        String historyHeader;
        SIPMessage sipMessage;
        CallParameters callParameters;

        // Bad Reason => Do not extract redirectingParty from History-Info
        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=Q.850%3Bcause%3D16>;index=1.1\r\n" +
                        "Privacy: header\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getRedirectingParty());


        // No Reason => Do not extract redirectingParty from History-Info
        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302>;index=1," +
                        "<" + URI_NORMAL_SIP2 + ">;index=1.1\r\n" +
                        "Privacy: header\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getRedirectingParty());


        // Only one History-Info header => Do not extract redirectingParty from History-Info
        historyHeader =
                "History-Info: <" + URI_NORMAL_SIP +
                        "?Reason=SIP%3Bcause%3D302>;index=1\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + historyHeader);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertNull(callParameters.getRedirectingParty());


    }

    /**
     * Test of preference order when retrieving RedirectingParty.
     * Diversion header should be used in preference of the History-Info header.
     * @throws Exception
     */
    public void testRetrieveRedirectingPartyPreference() throws Exception {

        String headers;
        SIPMessage sipMessage;
        CallParameters callParameters;

        headers =
                "History-Info: <" + URI_NORMAL_SIP2 +
                        "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D404>;index=1.1\r\n" +
                        "Diversion: <" + URI_NORMAL_SIP + ">;reason=user-busy;privacy=full\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + headers);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, RESTRICTED,
                RedirectingReason.USER_BUSY, "user-busy",
                callParameters.getRedirectingParty());



        headers =
                "History-Info: <" + URI_NORMAL_SIP2 +
                        "?Reason=SIP%3Bcause%3D302&Privacy=none>;index=1," +
                        "<" + URI_NORMAL_SIP2 + "?Reason=SIP%3Bcause%3D404>;index=1.1\r\n" +
                        "Diversion: <" + URI_NORMAL_SIP + ">;reason=follow-me;privacy=off\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                        SIP_REQUEST_LINE + headers);

        callParameters = parameterRetrieval.retrieveCallParameters(
                createEventFromRequest((Request) sipMessage), config);
        assertRedirectingParty(URI_NORMAL_SIP, USER, null, ALLOWED,
                RedirectingReason.UNKNOWN, "follow-me",
                callParameters.getRedirectingParty());


    }

    /* ===================== Private methods =========================== */

    private String createGtdBodyWithNumberCompletion(String numberCompletionValue) {
        String multiPartBody = "Content-Type: multipart/mixed;boundary=uniqueBoundary\r\n";

        String body = "--uniqueBoundary\r\n" +
                "Content-Type: application/sdp\r\n" +
                "\r\n" +
                "s=SIP Call\r\n"+
                "--uniqueBoundary\r\n"+
                "Content-Type: application/gtd\r\n" +
                "Content-Disposition: signal;handling=optional\r\n" +
                "\r\n" +
                "IAM\r\n" +
                "CGN,04," + numberCompletionValue + ",1,y,4,1133\r\n"+
                "GCI,aa1f2adec97611d9adcc0003ba909185\r\n" +
                "\r\n" +
                "--uniqueBoundary\r\n";
        return multiPartBody + "Content-Length: "+body.length()+"\r\n\r\n"+ body;
    }

    private String createGtdBodyWithRedirectingReason(String redirectingReasonValue) {
        String multiPartBody = "Content-Type: multipart/mixed;boundary=uniqueBoundary\r\n";

        String body = "--uniqueBoundary\r\n" +
                "Content-Type: application/sdp\r\n" +
                "\r\n" +
                "s=SIP Call\r\n"+
                "--uniqueBoundary\r\n"+
                "Content-Type: application/gtd\r\n" +
                "Content-Disposition: signal;handling=optional\r\n" +
                "\r\n" +
                "IAM\r\n" +
                "CGN,04,y,1,y,4,1133\r\n"+
                "GCI,aa1f2adec97611d9adcc0003ba909185\r\n" +
                "RNI,0,1,0," +  redirectingReasonValue +
                "\r\n" +
                "--uniqueBoundary\r\n";
        return multiPartBody + "Content-Length: "+body.length()+"\r\n\r\n"+ body;
    }


    private SipRequestEvent createEventFromRequest(Request request) {
        return new SipRequestEventImpl(new RequestEvent(this, (ServerTransaction) serverTransactionMock.proxy(),
                (Dialog) dialogMock.proxy(), request));
    }

    private void assertCallParty(String uri, String sipUser,
                                 String telephoneNumber,
                                 CallPartyDefinitions callParty)
            throws Exception
    {
        // Verify call party uri
        if (uri == null) {
            assertNull(callParty.getUri());
        } else {
            assertNotNull(callParty.getUri());
            URLParser parser = new URLParser(uri);
            URI uri1 = parser.parse();
            parser = new URLParser(callParty.getUri());
            URI uri2 = parser.parse();
            assertEquals(uri1, uri2);
        }

        // Verify call party sip user
        if (sipUser == null) {
            assertNull(callParty.getSipUser());
        } else {
            assertEquals(sipUser, callParty.getSipUser());
        }

        // Verify call party telephone number
        if (telephoneNumber == null) {
            assertNull(callParty.getTelephoneNumber());
        } else {
            assertEquals(telephoneNumber, callParty.getTelephoneNumber());
        }
    }

    private void assertCalledParty(String uri, String sipUser,
                                   String telephoneNumber,
                                   CalledParty calledParty)
            throws Exception {
        assertCallParty(uri, sipUser, telephoneNumber, calledParty);
    }

    private void assertCallingParty(String uri, String sipUser,
                                    String telephoneNumber,
                                    PresentationIndicator pi,
                                    CallingParty callingParty,
                                    NumberCompletion numberCompletion)
            throws Exception {
        assertCallParty(uri, sipUser, telephoneNumber, callingParty);
        assertEquals(pi, callingParty.getPresentationIndicator());
        assertEquals(numberCompletion, callingParty.getNumberCompletion());
    }

    private void assertRedirectingParty(String uri, String sipUser,
                                        String telephoneNumber,
                                        PresentationIndicator pi,
                                        RedirectingReason redirReason,
                                        String redirCause,
                                        RedirectingParty redirParty)
            throws Exception {
        assertCallParty(uri, sipUser, telephoneNumber, redirParty);
        assertEquals(pi, redirParty.getPresentationIndicator());

        // Verify redirecting reason
        if (redirReason == null) {
            assertNull(redirParty.getRedirectingReason());
        } else {
            assertEquals(redirReason, redirParty.getRedirectingReason());
        }

        // Verify redirect cause
        if (redirCause == null) {
            assertNull(redirParty.getRedirectingReasonText());
        } else {
            assertEquals(redirCause, redirParty.getRedirectingReasonText());
        }

    }

    private Request createRequest(String uri) throws Exception {
        Request request = null;

        if (uri != null) {
            String message = createRequestURI(uri) +
                    createTo(uri) + createRemoteParty(uri) +
                    createFrom(uri) + createDiversion(uri);
            request = (Request)stringMsgParser.parseSIPMessage(message);
        }
        return request;
    }

    private SipRequestEvent createRequestEventFromAgent(String uri, String userAgent)
            throws Exception {
        Request request;
        SipRequestEvent event = null;

        if (uri != null) {
            String message = createRequestURI(uri) +
                    createTo(uri) + createRemoteParty(uri) +
                    createFrom(uri) + createDiversion(uri);
            message += createUserAgent(userAgent);
            request = (Request)stringMsgParser.parseSIPMessage(message);
            event = new SipRequestEventImpl(new RequestEvent(this, (ServerTransaction) serverTransactionMock.proxy(),
                (Dialog) dialogMock.proxy(), request));
        }
        return event;
    }

    private Request createRequestWithNoRemoteParty(String uri) throws Exception {
        Request request = null;

        if (uri != null) {
            String message = createRequestURI(uri) +
                    createTo(uri) + createFrom(uri) + createDiversion(uri);
            request = (Request)stringMsgParser.parseSIPMessage(message);
        }
        return request;
    }

    private String createUserAgent(String userAgent) {
        return "User-Agent: " + userAgent + "\r\n";
    }

    private String createTo(String uri) {
        return "To: <" + uri + ">\r\n";
    }

    private String createRequestURI(String uri) throws Exception {
        // Add port and transport to the uri
        GenericURI genericURI = stringMsgParser.parseUrl(uri);
        String uriString = uri;
        if (genericURI instanceof SipURI) {
            SipURI sipUri = (SipURI)genericURI;
            sipUri.setPort(5060);
            sipUri.setParameter("transport", "udp");
            uriString = sipUri.toString();
        }
        return "INVITE " + uriString + " SIP/2.0\r\n";
    }

    private String createRemoteParty(String uri) {
        return "Remote-Party-ID: <" + uri + ">;party=calling\r\n";
    }

    private String createFrom(String uri) {
        return "From: <" + uri + ">\r\n";
    }

    private String createDiversion(String uri) {
        return "Diversion: <" + uri + ">\r\n";
    }

    private CallParameters retrieveCallParameters(String uri)
            throws Exception {
        return retrieveCallParameters(uri, true);
    }

    private CallParameters retrieveCallParameters(String uri, boolean remoteParty)
            throws Exception {
        if (remoteParty) {
            return parameterRetrieval.retrieveCallParameters(
                    createEventFromRequest(createRequest(uri)), config);
        } else {
            return parameterRetrieval.retrieveCallParameters(
                    createEventFromRequest(createRequestWithNoRemoteParty(uri)),
                    config);
        }
    }


    public void testGetPrivacy() throws Exception {
        String privacyHeader;
        SIPMessage sipMessage;


        privacyHeader = "Privacy: none\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.ALLOWED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: history\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.ALLOWED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy:\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.UNKNOWN,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.UNKNOWN,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: none;history\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: something\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: header;session\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: history;header\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: history\r\nPrivacy: header\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));


        privacyHeader = "Privacy: none\r\nPrivacy: none\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(
                SIP_REQUEST_LINE + privacyHeader);
        assertEquals(PresentationIndicator.RESTRICTED,
                parameterRetrieval.getPrivacy((Request)sipMessage));

    }

    /**
     * Verify that the returned call parameters contains a correct telephone
     * number if the received TEL URI contains a global number.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testRetrieveCallParametersWhenTelUriContainsGlobalNumber()
            throws Exception
    {
        CallParameters callParameters;
        String uri;

        // Verify for a tel uri with a correct global number containing a "+"
        uri = "tel:+12345";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "12345", callParameters.getCalledParty());

        // Verify for a tel uri with a correct global number containing only
        // digits
        uri = "tel:12345";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "12345", callParameters.getCalledParty());

        // Verify for a tel uri with a correct global number containing all
        // visual separators
        uri = "tel:1-2.3(4)5";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "12345", callParameters.getCalledParty());

        // Verify for a tel uri with an incorrect global number, i.e. a number
        // containing a + but to indicate global but also contains unsupported
        // characters such as A.
        // A parse exception is thrown by the stack when parsing the URI.
        uri = "tel:+1234A";
        try {
            retrieveCallParameters(uri);
            fail("Expected ParseException but none was thrown.");
        } catch (ParseException e) {
        }
    }

    /**
     * Verify that the returned call parameters contains a correct telephone
     * number if the received TEL URI contains a local number.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testRetrieveCallParametersWhenTelUriContainsLocalNumber()
            throws Exception
    {
        CallParameters callParameters;
        String uri;

        // Verify for a tel uri with a correct local number containing digits
        // only
        uri = "tel:12345";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "12345", callParameters.getCalledParty());

        // Verify for a tel uri with a correct local number containing "*"
        uri = "tel:*123*45*";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "*123*45*", callParameters.getCalledParty());

        // Verify for a tel uri with a correct local number containing "#"
        uri = "tel:#123#45#";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "#123#45#", callParameters.getCalledParty());

        // Verify for a tel uri with a correct local number containing
        // upper-case hexdigits
        uri = "tel:1A2B3C4D5E6F";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "1A2B3C4D5E6F", callParameters.getCalledParty());

        // Verify for a tel uri with a correct local number containing
        // lower-case hexdigits
        uri = "tel:1a2b3c4d5e6f";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "1a2b3c4d5e6f", callParameters.getCalledParty());

        // Verify for a tel uri with a correct local number containing all
        // visual separators
        uri = "tel:1-2.3(4)5";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, null, "12345", callParameters.getCalledParty());

        // Verify for a tel uri with an incorrect local number, i.e. a number
        // containing a letter other than A-F.
        // A parse exception is thrown by the stack when parsing the URI.
        uri = "tel:1234G";
        try {
            retrieveCallParameters(uri);
            fail("Expected ParseException but none was thrown.");
        } catch (ParseException e) {
        }
    }

    /**
     * Verify that the returned call parameters contains a correct telephone
     * number if the received SIP URI contains a global number.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testRetrieveCallParametersWhenSipUriContainsGlobalNumber()
            throws Exception
    {
        CallParameters callParameters;
        String uri;

        // Verify for a sip uri with a correct global number containing a "+"
        uri = "sip:+12345@host.com:5060;user=phone";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "12345", "12345",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct global number containing only
        // digits
        uri = "sip:12345@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "12345", "12345",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct global number containing all
        // visual separators
        uri = "sip:1-2.3(4)5@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "12345", "12345",
                callParameters.getCalledParty());

        // Verify for a sip uri with an incorrect global number, i.e. a number
        // containing a + but to indicate global but also contains unsupported
        // characters such as A.
        uri = "sip:+1234A@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "+1234A", null,
                callParameters.getCalledParty());
    }

    /**
     * Verify that the returned call parameters contains a correct telephone
     * number if the received SIP URI contains a local number.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testRetrieveCallParametersWhenSipUriContainsLocalNumber()
            throws Exception
    {
        CallParameters callParameters;
        String uri;

        // Verify for a sip uri with a correct local number containing digits
        // only
        uri = "sip:12345@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "12345", "12345",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct local number containing "*"
        uri = "sip:*123*45*@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "*123*45*", "*123*45*",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct local number containing "#"
        uri = "sip:#123#45#@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "#123#45#", "#123#45#",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct local number containing
        // upper-case hexdigits
        uri = "sip:1A2B3C4D5E6F@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "1A2B3C4D5E6F", "1A2B3C4D5E6F",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct local number containing
        // lower-case hexdigits
        uri = "sip:1a2b3c4d5e6f@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "1a2b3c4d5e6f", "1a2b3c4d5e6f",
                callParameters.getCalledParty());

        // Verify for a sip uri with a correct local number containing all
        // visual separators
        uri = "sip:1-2.3(4)5@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "12345", "12345",
                callParameters.getCalledParty());

        // Verify for a sip uri with an incorrect local number, i.e. a number
        // containing a letter other than A-F.
        uri = "sip:1234G@host.com:5060;user=phone;transport=udp";
        callParameters = retrieveCallParameters(uri);
        assertCalledParty(uri, "1234G", null,
                callParameters.getCalledParty());
    }

}
