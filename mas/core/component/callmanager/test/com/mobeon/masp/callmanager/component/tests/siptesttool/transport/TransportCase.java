/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.siptesttool.transport;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.component.environment.EnvironmentConstants;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.TransportConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;

import java.net.InetAddress;
import java.util.HashMap;
import java.security.NoSuchAlgorithmException;

/**
 * This is a base class to use when constructing a test case that intends to
 * verify the transport of SIP messages with respect to TCP or UDP.
 *
 * @author Malin Nyfeldt
 */
public abstract class TransportCase extends MockObjectTestCase {

    /** A logger instance. */
    protected final ILogger log = ILoggerFactory.getILogger(getClass());


    /** Local host to listen on for TCP or UDP server. */
    protected final String localhost = "127.0.0.1";

    /** Local port to listen on for TCP or UDP server. */
    protected final int localport = 5090;

    /** Remote host Call Manager listen on. */
    protected String remotehost;

    /** Remote port Call Manager listen on. */
    protected final int remoteport = 5060;

    /** An instance of the simualted system, i.e. execution engine and similar.*/
    protected SystemSimulator simulatedSystem;

    /** An instance of callmanager, i.e. the system under test. */
    protected CallManagerToVerify callManager;

    /** Contains a mapping of parameters found in SIP messages created in the
     * tests. */
    protected HashMap<RawSipMessageParameter, String> messageParameters =
            new HashMap<RawSipMessageParameter, String>();

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public void setUp() throws Exception {
        System.gc();

        // Create the rest of the simulated system
        simulatedSystem = new SystemSimulator();

        // Create the system under test
        remotehost = "localhost";
        callManager = new CallManagerToVerify(
                remotehost, remoteport, CallManagerTestContants.CALLMANAGER_XML, simulatedSystem);

        // Create the simulated system, i.e. execution engine etc.
        simulatedSystem.create(callManager);

        setupMessageParameters();
    }

    private void setupMessageParameters() {
        messageParameters.put(RawSipMessageParameter.LOCAL_IP, "127.0.0.1");
        messageParameters.put(RawSipMessageParameter.LOCAL_PORT, "5090");
        messageParameters.put(RawSipMessageParameter.REMOTE_IP,
                remotehost);
        messageParameters.put(RawSipMessageParameter.REMOTE_PORT,
                Integer.toString(remoteport));
        messageParameters.put(RawSipMessageParameter.MEDIA_IP, "127.0.0.1");
        messageParameters.put(RawSipMessageParameter.MEDIA_PORT, "1111");
        messageParameters.put(RawSipMessageParameter.CALLED, "masUser");
        messageParameters.put(RawSipMessageParameter.CSEQ, "1");
    }

    public void tearDown() throws Exception {
        // Wait a while to make sure that the SIP stack is ready to be deleted
        Thread.sleep(50);
        callManager.delete();
        simulatedSystem.delete();
    }

    protected void assertCallReceivedBySystem(TransportConnection connection)
            throws Exception {
        // Wait for an Alerting event
        simulatedSystem.assertEventReceived(AlertingEvent.class, null);

        // Wait for Trying response
        waitForResponse("100", "INVITE", false, connection);
    }

    protected void assertRingingReceivedBySystem() throws Exception {
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
    }

    protected void assertCallConnected() throws Exception {
        // Wait for a Connected event
        simulatedSystem.assertEventReceived(ConnectedEvent.class, null);
    }

    protected void assertCallDisconnected(Class waitForState)
            throws Exception {
        // Wait for Disconnected event
        simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);

        // If set, wait for the indicated state.
        if (waitForState != null)
            simulatedSystem.waitForState(waitForState);
    }

    protected RawSipMessage waitForResponse(
            String expectedResponseCode,
            String expectedMethod,
            boolean skipOtherResponses,
            TransportConnection connection)
            throws InterruptedException, NoSuchAlgorithmException {

        RawSipMessage response = new RawSipMessage(connection.waitForSipMessage());
        assertTrue(response.isResponse());
        String receivedResponseCode = response.getResponseCode();

        if (skipOtherResponses) {
            while (!receivedResponseCode.equals(expectedResponseCode)) {
                response = new RawSipMessage(connection.waitForSipMessage());
                assertTrue(response.isResponse());
                receivedResponseCode = response.getResponseCode();
            }
        }

        assertEquals(expectedResponseCode, receivedResponseCode);
        assertEquals(expectedMethod, response.getMethod());
        return response;
    }

    protected RawSipMessage waitForRequest(
            String expectedMethod,
            TransportConnection connection)
            throws InterruptedException, NoSuchAlgorithmException {

        RawSipMessage request = new RawSipMessage(connection.waitForSipMessage());
        assertTrue(request.isRequest());
        assertEquals(expectedMethod, request.getMethod());
        return request;
    }

    protected CallProperties createCallProperties(
            String calledPartyNumber, String callingPartyNumber) {
        // Create called party
        CalledParty calledParty = new CalledParty();
        calledParty.setTelephoneNumber(calledPartyNumber);

        // Create calling party
        CallingParty callingParty = new CallingParty();
        callingParty.setTelephoneNumber(callingPartyNumber);

        // Create call properties
        CallProperties callProperties = new CallProperties();
        callProperties.setCalledParty(calledParty);
        callProperties.setCallingParty(callingParty);
        callProperties.setMaxDurationBeforeConnected(
                5* EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);

        return callProperties;
    }

}