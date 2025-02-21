/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.ReliableResponseUsage;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.stream.IInboundMediaStream;

/**
 * InboundCallImpl Tester.
 *
 * @author Malin Nyfeldt
 */
public class InboundCallImplTest extends MockObjectTestCase
{

    /** Object under test. */
    InboundCallImpl call;

    // MOCK OBJECTS

    /** Mock for the application management used when loading services. */
    Mock applicationManagementMock = new Mock(IApplicationManagment.class);

    /** Mock for the application execution used when loading services. */
    Mock applicationExecutionMock = new Mock(IApplicationExecution.class);

    /** Mock for the event dispatcher used when loading services. */
    Mock eventDispatcherMock = new Mock(IEventDispatcher.class);

    /** Mock for the session used when loading services. */
    Mock sessionMock = new Mock(ISession.class);

    // Mock for inbound stream
    Mock inboundStreamMock = new Mock(IInboundMediaStream.class);


    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());
        setupConfiguration();
        setupApplicationEnvironment();
        setupSession();
        setupStream();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testToString() throws Exception {
        //TODO: Test goes here...
    }

    public void testAccept() throws Exception {
        //TODO: Test goes here...
    }

    public void testReject() throws Exception {
        //TODO: Test goes here...
    }

    public void testNegotiateEarlyMediaTypes() throws Exception {
        //TODO: Test goes here...
    }

    public void testDisconnect() throws Exception {
        //TODO: Test goes here...
    }

    public void testStartNotAcceptedTimer() throws Exception {
        //TODO: Test goes here...
    }

    public void testStartExpiresTimer() throws Exception {
        //TODO: Test goes here...
    }

    public void testStartNoAckTimer() throws Exception {
        //TODO: Test goes here...
    }

    public void testCancelNoAckTimer() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Verifies that when the called party contains a telephone number, the call
     * uses the telephone number when loading a service.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenTelephoneNumberIsLoaded() throws Exception {
        setupForLoadServiceTest(null, "1234", null);
        assertServiceLoaded("1234");
        call.loadService();
    }

    /**
     * Verifies that when the called party contains a user name but not a
     * telephone number, the call uses the user name when loading a service.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenUserIsLoaded() throws Exception {
        setupForLoadServiceTest("user", null, null);
        assertServiceLoaded("user");
        call.loadService();
    }

    /**
     * Verifies that when the called party contains neither a user name nor a
     * telephone number, the call uses the default service name loading a service.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceOfDefaultService() throws Exception {
        setupForLoadServiceTest(null, null, "default");
        assertServiceLoaded("default");
        call.loadService();
    }

    /**
     * Verifies that when the called party contains neither a user name nor a
     * telephone number, and there is no default service, a
     * NullPointerException is thrown.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenDefaultServiceIsNull() throws Exception {
        setupForLoadServiceTest(null, null, null);
        try {
            call.loadService();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("Default service name is null.", e.getMessage());
        }
    }

    /**
     * Verifies that when the returned application execution instance is null, a
     * NullPointerException is thrown.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenExecutionInstanceIsNull() throws Exception {
        setupForLoadServiceTest(null, null, "default");
        applicationManagementMock.expects(once()).method("load").
                with(eq("default")).will(returnValue(null));
        try {
            call.loadService();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(
                    "Application Execution instance is null when loading service.",
                    e.getMessage());
        }
    }

    /**
     * Verifies that when the application execution instance returns an event
     * dispatcher that is null, a NullPointerException is thrown.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenEventDispatcherIsNull() throws Exception {
        setupForLoadServiceTest(null, null, "default");
        assertServiceLoaded("default");
        applicationExecutionMock.expects(once()).method("getEventDispatcher").
                will(returnValue(null));
        try {
            call.loadService();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(
                    "Event dispatcher is null in application execution.",
                    e.getMessage());
        }
    }

    /**
     * Verifies that when the session is null when loading a service, a
     * NullPointerException is thrown.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testLoadServiceWhenSessionIsNull() throws Exception {
        // Create an empty called party
        CallParameters callParameters = new CallParameters();
        CalledParty calledParty = new CalledParty();
        callParameters.setCalledParty(calledParty);

        // Create the call (parameters not needed during the test are set to null)
        call = new InboundCallImpl(
                null, null, null, callParameters, "default",
                (IApplicationManagment)applicationManagementMock.proxy(),
                null, null, null, ConfigurationReader.getInstance().getConfig());

        assertServiceLoaded("default");
        try {
            call.loadService();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("Session is null.", e.getMessage());
        }
    }

    public void testSetStateAlerting() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetStateConnected() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetStateDisconnected() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetStateError() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetStateFailed() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Verifies that the inbound call is able to determine when to use reliable
     * provisional responses.
     * Reliable provisional responses shall be used if required by the initial
     * INVITE or if supported by the initial INVITE in combination with certain
     * configuration parameters.
     * @throws Exception An exception is thrown if the test case fails.
     */
    public void testUseReliableProvisionalResponses() throws Exception {

        // Create necessary mock objects
        Mock sipMessageMock = new Mock(SipMessage.class);
        Mock sipRequestEventMock = new Mock(SipRequestEvent.class);
        sipRequestEventMock.stubs().method("getSipMessage").will(
                returnValue(sipMessageMock.proxy()));

        // Create the call (parameters not needed during the test are set to null)
        call = new InboundCallImpl(
                null, null, null, new CallParameters(), null, null, null, null,
                (SipRequestEvent)sipRequestEventMock.proxy(),
                ConfigurationReader.getInstance().getConfig());

        // Verify that if the initial SIP request Requires the use of "100rel"
        // the call shall indicate that provisional responses shall be sent
        // reliably.
        sipMessageMock.expects(once()).
                method("isReliableProvisionalResponsesRequired").
                will(returnValue(true));
        assertEquals(ReliableResponseUsage.YES,
                call.useReliableProvisionalResponses());

        // Verify that if the initial SIP request Supports the use of "100rel"
        // the call shall use the configuration to determine whether or not to
        // send provisional responses reliably.

        // For "sdponly" configuration
        sipMessageMock.stubs().method("isReliableProvisionalResponsesRequired").
                will(returnValue(false));
        sipMessageMock.stubs().method("isReliableProvisionalResponsesSupported").
                will(returnValue(true));
        call.getConfig().setReliableResponseUsage(ReliableResponseUsage.SDPONLY);
        assertEquals(ReliableResponseUsage.SDPONLY,
                call.useReliableProvisionalResponses());

        // For "no" configuration
        sipMessageMock.stubs().method("isReliableProvisionalResponsesRequired").
                will(returnValue(false));
        sipMessageMock.stubs().method("isReliableProvisionalResponsesSupported").
                will(returnValue(true));
        call.getConfig().setReliableResponseUsage(ReliableResponseUsage.NO);
        assertEquals(ReliableResponseUsage.NO,
                call.useReliableProvisionalResponses());

        // For "yes" configuration
        sipMessageMock.stubs().method("isReliableProvisionalResponsesRequired").
                will(returnValue(false));
        sipMessageMock.stubs().method("isReliableProvisionalResponsesSupported").
                will(returnValue(true));
        call.getConfig().setReliableResponseUsage(ReliableResponseUsage.YES);
        assertEquals(ReliableResponseUsage.YES,
                call.useReliableProvisionalResponses());

        // Verify that if the initial SIP request neither Requires nor Supports
        // the use of "100rel" provisional responses shall not be sent reliably.
        sipMessageMock.stubs().method("isReliableProvisionalResponsesRequired").
                will(returnValue(false));
        sipMessageMock.stubs().method("isReliableProvisionalResponsesSupported").
                will(returnValue(false));
        call.getConfig().setReliableResponseUsage(ReliableResponseUsage.YES);
        assertEquals(ReliableResponseUsage.NO,
                call.useReliableProvisionalResponses());
    }


    public void testProcessCallCommand() throws Exception {
        //TODO: Test goes here...
    }

    public void testErrorOccurred() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsJoinable() throws Exception {
        //TODO: Test goes here...
    }

    public void testCancelCallTimers() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetSessionLoggingData() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetInboundBitRate() throws Exception {
        setupForSimpleCallTest();

        call.setInboundStream((IInboundMediaStream) inboundStreamMock.proxy());

        assertTrue(call.getInboundBitRate() == 7000);
    }

    /** ************* ASSERT METHODS *******************/

    /**
     * This method is used to setup expectations of which service that will
     * be loaded.
     * @param serviceName The name of the service that should be loaded.
     */
    private void assertServiceLoaded(String serviceName) {
        applicationManagementMock.expects(once()).method("load").
                with(eq(serviceName)).
                will(returnValue(applicationExecutionMock.proxy()));
    }


    /** ************* SETUP METHODS *******************/

    /**
     * Sets up mock objects used to simulate the application enviroment.
     */
    private void setupApplicationEnvironment() {
        applicationExecutionMock.stubs().method("getEventDispatcher").
                will(returnValue(eventDispatcherMock.proxy()));
        applicationExecutionMock.stubs().method("setSession");
        applicationExecutionMock.stubs().method("start");
    }

    /**
     * Reads the call manager configuration.
     * @throws Exception
     */
    private void setupConfiguration() throws Exception {

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl configMgr = new ConfigurationManagerImpl();
        configMgr.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);

        // Initialize configuration now to be able to setup SSPs before CM
        ConfigurationReader.getInstance().setInitialConfiguration(
                configMgr.getConfiguration());
        ConfigurationReader.getInstance().update();
    }

    /**
     * Sets up mock objects used to simulate a session.
     */
    private void setupSession() {
        sessionMock.stubs().method("setSessionLogData");
        sessionMock.stubs().method("registerSessionInLogger");
        sessionMock.stubs().method("setMdcItems");
        sessionMock.stubs().method("setData");
    }

    private void setupStream() {
        inboundStreamMock.stubs().method("getInboundBitRate").will(returnValue(7000));
    }


    /**
     * Setup method that is used for tests of LoadService method.
     * @param user              The called party user.
     * @param telephoneNumber   The called party telephone number.
     * @param defaultService    The default service name.
     */
    private void setupForLoadServiceTest(
            String user, String telephoneNumber, String defaultService) {

        // Create a called party with the requested settings
        CallParameters callParameters = new CallParameters();
        CalledParty calledParty = new CalledParty();
        calledParty.setTelephoneNumber(telephoneNumber);
        calledParty.setSipUser(user);
        callParameters.setCalledParty(calledParty);

        // Create the call (parameters not needed during the test are set to null)
        call = new InboundCallImpl(
                null, null, null, callParameters, defaultService,
                (IApplicationManagment)applicationManagementMock.proxy(),
                (ISession)sessionMock.proxy(), null, null,
                ConfigurationReader.getInstance().getConfig());
    }

    private void setupForSimpleCallTest() {
        String user = "kalle";
        String telephoneNumber = "1212";
        String defaultService = "default";

        // Create a called party with the requested settings
        CallParameters callParameters = new CallParameters();
        CalledParty calledParty = new CalledParty();
        calledParty.setTelephoneNumber(telephoneNumber);
        calledParty.setSipUser(user);
        callParameters.setCalledParty(calledParty);

        // Create the call (parameters not needed during the test are set to null)
        call = new InboundCallImpl(
                null, null, null, callParameters, defaultService,
                (IApplicationManagment)applicationManagementMock.proxy(),
                (ISession)sessionMock.proxy(), null, null,
                ConfigurationReader.getInstance().getConfig());
    }

}
