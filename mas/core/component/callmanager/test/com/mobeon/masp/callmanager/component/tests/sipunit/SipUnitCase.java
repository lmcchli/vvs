/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls.OutboundSipUnitCase;
import com.mobeon.masp.callmanager.statistics.StatisticsCollector;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.NotAllowedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;

import java.util.Set;
import java.util.Properties;
import java.net.InetAddress;

import org.jmock.MockObjectTestCase;
import org.cafesip.sipunit.SipStack;

/**
 * Base class used to setup necessary for tests using SipUnit.
 * It contains a SimulatedPhone and a SimulatedSystem used to simulate the two
 * external clients to the Call Manager.
 *
 * @author Malin Flodin
 */
public abstract class SipUnitCase extends MockObjectTestCase {

    // ===================== Constants =====================

    // Toggle this to enable/disable logging of the SIP UNIT stack
    private static final boolean doLogging = false;

    /** Timeout that shall be used when the test case is waiting for a specific
     * input. If the input is not received within this time, the test case
     * should fail. */
    protected static final int TIMEOUT_IN_MILLI_SECONDS = 10000;


    // Constants that indicates whether an event occured locally or remote

    /** When in test cases indicating that something occurred remote,
     * use this contant. It is used to make test cases more readable. */
    protected static final boolean FAR_END = false;

    /** When in test cases indicating that something occurred locally,
     * use this contant. It is used to make test cases more readable. */
    protected static final boolean NEAR_END = true;


    // Constants that indicates whether or not early media is used.

    /** When in test cases indiating that early media is/shall be used,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean EARLY_MEDIA = true;

    /** When in test cases indiating that early media is not/shall not be used,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean NO_EARLY_MEDIA = false;


    // Constants that indicates whether or not acknowledge a reliable response.

    /** When in test cases indiating that acknowledge of a reliable response
     * should be done, use this constant. It is used to make test cases more
     * readable. */
    protected static final boolean ACKNOWLEDGE = true;

    /** When in test cases indiating that no acknowledge of a reliable response
     * should be done, use this constant. It is used to make test cases more
     * readable. */
    protected static final boolean NO_ACKNOWLEDGE = false;


    // Constants that indicates whether or not ringing is expected.

    /** When in test cases indiating that a ringing response is expected,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean RINGING = true;

    /** When in test cases indiating that no ringing is expected,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean NO_RINGING = false;


    // Constants that indicates whether or not session progress is expected.

    /** When in test cases indiating that a session progress is expected,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean SESSION_PROGRESS = true;

    /** When in test cases indiating that no session progress is expected,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean NO_SESSION_PROGRESS = false;


    // Constants that indicates whether or not an operation succeeded.

    /** When in test cases indicating that an operation fails,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean FAIL = true;

    /** When in test cases indicating that an operation succeeds,
     * use this constant. It is used to make test cases more readable. */
    protected static final boolean SUCCEED = false;


    // ===================== Variables =====================

    /** A logger instance. */
    private final ILogger log = ILoggerFactory.getILogger(getClass());


    // Simulated system related

    /** An instance of the simualted system, i.e. execution engine and similar.*/
    protected SystemSimulator simulatedSystem;


    // CallManager related
    protected String callManagerConfigFile = CallManagerTestContants.CALLMANAGER_XML;

    /** An instance of callmanager, i.e. the system under test. */
    protected CallManagerToVerify callManager;

    /** The Call Manager user name in SIP URIs.*/
    private static final String callmanagerUser = "mas";


    // Simulated phones related

    /** The name of the SIP UNIT stack. Used at creation. */
    private static final String STACK_NAME = "SipUnitStack";

    /** The SIP UNIT stack used in the simulated phones.*/
    private static SipStack sipStack;

    /** The SIP UNIT stack used in the UAS.*/
    private static SipStack sipStackUas;

    /** A simulated phone.*/
    protected PhoneSimulator simulatedPhone;
    /** A second simulated phone that is used when testing redirection of
     * outbound calls.*/
    protected PhoneSimulator simulatedSecondPhone;

    /** A third simulated phone that is used when testing PROXY mode. */
    protected PhoneSimulator simulatedUasPhone;

    /** The host and port address to the simulated phones. */
    protected RemotePartyAddress phoneAddress;

    /** The user name in SIP URIs for the first simulated phone. */
    private static final String phoneUser = "sipPhone";

    /** The user name in SIP URIs for the seconds simulated phone. */
    private static final String secondPhoneUser = "sipSecondPhone";

    /** The user name in SIP URIs for the third simulated phone. */
    private static final String thirdUasUser = "sipUasPhone";

    // Input needed for some test cases
    /** A test setup of call properties that can be used when creating an
     * outbound call. */
    protected CallProperties callProperties;


    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Load classes so that first test case will not be slower and thus
        // cause SIP retransmissions
        try {
            ClassLoadingCase.getInstance().loadClasses();
        } catch (ClassNotFoundException e) {
            fail("Class not loaded: " + e.getMessage());
        }
    }

    public void setUp() throws Exception {
        System.gc();

        simulatedSystem = new SystemSimulator();

        // Create and initiate call manager
        RemotePartyAddress callmanagerAddress = new RemotePartyAddress("localhost", 5060);
        callManager = new CallManagerToVerify("localhost", 5060, callManagerConfigFile, simulatedSystem);

        // Create the simulated system, i.e. execution engine etc.
        simulatedSystem.create(callManager);

        // Create the sip stacks
        phoneAddress = ConfigurationReader.getInstance().getConfig().getRemoteParty().getSipProxy();
        Properties properties = setupSipUnitStackConfiguration(phoneAddress.getHost(), "sipStack");
        sipStack = new SipStack("udp", phoneAddress.getPort(), properties);
        properties = setupSipUnitStackConfiguration(phoneAddress.getHost(), "sipStackUas");
        sipStackUas = new SipStack("udp", 5080, properties);

        // Create the simulated phones
        simulatedPhone = createSimulatedPhone(phoneUser, phoneAddress, callmanagerUser, callmanagerAddress, sipStack);
        simulatedSecondPhone = createSimulatedPhone(secondPhoneUser, phoneAddress, callmanagerUser, callmanagerAddress, sipStack);
        simulatedUasPhone = createSimulatedPhone(thirdUasUser, new RemotePartyAddress("localhost", 5080), callmanagerUser, callmanagerAddress, sipStackUas);

        // Setup test information
        callProperties = createCallProperties();
    }

    public final void tearDown() throws Exception {
        // Wait a while to make sure that the SIP stack is ready to be deleted
        Thread.sleep(100);
        callManager.delete();

        simulatedSystem.delete();

        simulatedPhone.delete();
        simulatedSecondPhone.delete();
        simulatedUasPhone.delete();

        sipStack.dispose();
        sipStackUas.dispose();
    }

    public final void assertCallingParty() {
        assertNotNull(simulatedSystem.getActiveCall().
                getCallingParty().getSipUser());
    }

    public final void assertCalledParty() {
        assertNotNull(simulatedSystem.getActiveCall().
                getCalledParty().getSipUser());
    }

    protected final void assertCurrentState(Class expectedState) {
        simulatedSystem.waitForState(expectedState);
    }

    // ===================== Statistics methods =====================
    protected final void assertTotalConnectionStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                null,
                getCurrentCallDirection(),
                value, true);
    }

    protected final void assertCurrentConnectionStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                null,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertConnectedCallStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.CONNECTED,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertFailedCallStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.FAILED,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertDisconnectedCallStatistics(
            boolean nearEnd, Integer value) {

        CallResult result =
                nearEnd ? CallResult.NEARENDDISCONNECTED : CallResult.FARENDDISCONNECTED;
        waitForCallNumberStatistics(
                getCurrentCallType(),
                result,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertErrorCallStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.ERROR,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertAbandonedDisconnectedCallStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.ABANDONED,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertAbandonedRejectedCallStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.ABANDONED_REJECTED,
                getCurrentCallDirection(),
                value, false);
    }

    protected final void assertDroppedPacketsStatistics(Integer value) {
        waitForCallNumberStatistics(
                getCurrentCallType(),
                CallResult.DROPPEDENTITIES,
                getCurrentCallDirection(),
                value, false);
    }

    private CallDirection getCurrentCallDirection() {
        CallDirection direction = CallDirection.INBOUND;
        if (this instanceof OutboundSipUnitCase) {
             direction = CallDirection.OUTBOUND;
        }
        return direction;
    }

    private CallType getCurrentCallType() {
        CallType callType = CallType.VOICE_VIDEO_UNKNOWN;
        if (simulatedSystem.getActiveCall() != null) {
            callType = StatisticsCollector.getOMCallType(
                    simulatedSystem.getActiveCall().getCallType());
        }
        return callType;
    }

    private void waitForCallNumberStatistics(
            com.mobeon.masp.operateandmaintainmanager.CallType type,
            com.mobeon.masp.operateandmaintainmanager.CallResult result,
            com.mobeon.masp.operateandmaintainmanager.CallDirection direction,
            Integer expected, boolean total) {
        if (result == null) {
            if (total) {
                simulatedSystem.getServiceEnablerInfo().waitForTotalConnections(
                            type, direction, expected, TIMEOUT_IN_MILLI_SECONDS);
            } else {
                simulatedSystem.getServiceEnablerInfo().waitForCurrentConnections(
                            type, direction, expected, TIMEOUT_IN_MILLI_SECONDS);
            }
        } else {
            simulatedSystem.getServiceEnablerInfo().waitForNumberOfConnections(
                        type, result, direction, expected, TIMEOUT_IN_MILLI_SECONDS);
        }
    }

    protected final void assertProgressingEventReceived(boolean earlymedia)
            throws InterruptedException {
        ProgressingEvent progressingEvent =
                (ProgressingEvent)simulatedSystem.assertEventReceived(
                        ProgressingEvent.class, null);
        assertTrue("Call is not set correctly in ProgressingEvent",
                progressingEvent.getCall() == simulatedSystem.getActiveCall());
        assertTrue("isEarlyMedia is not set correctly in ProgressingEvent",
                progressingEvent.isEarlyMedia() == earlymedia);
    }

    protected final void assertFailedEventReceived(
            FailedEvent.Reason reason) throws InterruptedException {
        FailedEvent failedEvent =
                (FailedEvent)simulatedSystem.assertEventReceived(FailedEvent.class, null);
        assertTrue("Call is not set correctly in FailedEvent",
                failedEvent.getCall() == simulatedSystem.getActiveCall());
        assertTrue("Reason is not set correctly in FailedEvent",
                failedEvent.getReason() == reason);
    }

    protected final void assertFailedEventReceived(
            FailedEvent.Reason reason, int networkStatusCode)
            throws InterruptedException {
        FailedEvent failedEvent =
                (FailedEvent)simulatedSystem.assertEventReceived(FailedEvent.class, null);
        assertTrue("Call is not set correctly in FailedEvent",
                failedEvent.getCall() == simulatedSystem.getActiveCall());
        assertTrue("Reason is not set correctly in FailedEvent",
                failedEvent.getReason() == reason);
        assertEquals("NetworkStatusCode is not set correctly in FailedEvent",
                networkStatusCode, failedEvent.getNetworkStatusCode());

    }

    protected final void assertNotAllowedEventReceived(String message)
            throws InterruptedException {
        NotAllowedEvent notAllowedEvent =
                (NotAllowedEvent)simulatedSystem.assertEventReceived(
                        NotAllowedEvent.class, null);
        assertTrue("Call is not set correctly in FailedEvent",
                notAllowedEvent.getCall() == simulatedSystem.getActiveCall());
        assertEquals("Message is not set correctly in NotAllowedEvent.",
                message, notAllowedEvent.getMessage());
    }

    protected static void assertDispatchedCalls(int amountOfInitiatedCalls,
                                                int amountOfEstablishedCalls) {
        assertEquals("Amount of dispatched initiated calls is not correct.",
                amountOfInitiatedCalls,
                CMUtils.getInstance().getCallDispatcher().amountOfInitiatedCalls());
        assertEquals("Amount of dispatched established calls is not correct",
                amountOfEstablishedCalls,
                CMUtils.getInstance().getCallDispatcher().amountOfEstablishedCalls());
    }

    protected final void assertFarEndConnections() throws Exception {
        Set<Connection> farEndConnections = simulatedSystem.getActiveCall().getFarEndConnections();
        assertNotNull("FarEndConnections should not be null", farEndConnections);
        assertTrue("farEndConnections size should be greater than 0", farEndConnections.size() > 0);
        Connection[] connections = farEndConnections.toArray(new Connection[farEndConnections.size()]);
        InetAddress hostAddress = InetAddress.getByName(phoneAddress.getHost());
        assertEquals(new Connection("RTP", hostAddress, simulatedPhone.getRTPPort()), connections[0]);
        if (farEndConnections.size() > 2) {
            // Todo: detect this another way?
            // The additional connection is created by PhoneSimulator.addBody in tests in InviteTest
            assertEquals(new Connection("RTP", hostAddress, simulatedPhone.getRTPPort() + 2), connections[1]);
            assertEquals(new Connection("SIP", hostAddress, phoneAddress.getPort()), connections[2]);
        } else {
            assertEquals(new Connection("SIP", hostAddress, phoneAddress.getPort()), connections[1]);
        }
    }


    // ===================== Private methods =====================

    /**
     * This method is used to create a test setup of call properties.
     * The call properties contains:
     * <ul>
     * <li> A called party with a sip user set to
     * {@link phoneUser}@phoneHost</li>
     * <li>A calling party with restricted presentation indicator and with
     * telephone number 4321.</li>
     * <li>A max duration before connected timeout set to 5 *
     * {@link TIMEOUT_IN_MILLI_SECONDS}</li>
     * </ul>
     *
     * @return  Returns an instance of call properties that can be used as input
     *          in the sipunit test cases.
     */
    private CallProperties createCallProperties() {
        // Create called party
        CalledParty calledParty = new CalledParty();
        calledParty.setSipUser(phoneUser + "@" + phoneAddress.getHost());

        // Create calling party
        CallingParty callingParty = new CallingParty();
        callingParty.setPresentationIndicator(
                CallPartyDefinitions.PresentationIndicator.RESTRICTED);
        callingParty.setTelephoneNumber("4321");

        // Creaqte call properties
        CallProperties callProperties = new CallProperties();
        callProperties.setCalledParty(calledParty);
        callProperties.setCallingParty(callingParty);
        callProperties.setMaxDurationBeforeConnected(5*TIMEOUT_IN_MILLI_SECONDS);

        return callProperties;
    }

    /**
     * This method is used to create a simulated phone.
     *
     * @param phoneUser     The user name of the phone.
     * @param phoneAddress  The host and port of the phone.
     * @param remoteUser    The user name of the remote party the phone will
     *                      communicate with.
     * @param remoteAddress The host and port of the remote party the phone will
     *                      communicate with.
     *
     * @return              Returns a simulated phone instance.
     * @throws Exception    An exception is thrown if the phone could not be
     *                      created.
     */
    private static PhoneSimulator createSimulatedPhone(
            String phoneUser, RemotePartyAddress phoneAddress,
            String remoteUser, RemotePartyAddress remoteAddress,
            SipStack sipStack)
            throws Exception {

        PhoneSimulator simulatedPhone = new PhoneSimulator(
                sipStack,
                phoneUser, phoneAddress,
                remoteUser, remoteAddress,
                TIMEOUT_IN_MILLI_SECONDS);
        simulatedPhone.create();

        return simulatedPhone;
    }

    /**
     * This method is used to set-up configuration properties for the SIP UNIT
     * stack.
     * @return  Returns the configuration parameters to use when creating a
     *          SIP UNIT stack.
     */
    private Properties setupSipUnitStackConfiguration(String host, String stackName) {
        Properties properties = new Properties();

        properties.setProperty("javax.sip.IP_ADDRESS", host);
        properties.setProperty("javax.sip.STACK_NAME", stackName);
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
        
        if (doLogging) {
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                    "true");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "simulatedPhone_sipstacklog.txt");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "simulatedPhone_sipstackdebug.txt");
            properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG",
                    "simulatedPhone_sipstackbadmessages.txt");
        }

        return properties;
    }

    protected void assertCallRejected(int responseCode, PhoneSimulator phone,
                                      Integer currentConnectionStatistics,
                                      Integer totalConnectionStatistics,
                                      Integer failedCallStatistics,
                                      Integer errorCallStatistics) throws Exception {

        // Wait for Error response
        phone.assertResponseReceived(responseCode);

        // Verify statistics
        if(currentConnectionStatistics != null)
            assertCurrentConnectionStatistics(currentConnectionStatistics);
        if(totalConnectionStatistics != null)
            assertTotalConnectionStatistics(totalConnectionStatistics);
        if(failedCallStatistics != null)
            assertFailedCallStatistics(failedCallStatistics);
        if(errorCallStatistics != null)
            assertErrorCallStatistics(errorCallStatistics);
    }
}
