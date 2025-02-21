/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.registration;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.RemotePartyControllerImpl;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.sipunit.SspSimulator;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.states.AdministrativeState;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorCompletedOutboundState;
import com.mobeon.masp.callmanager.registration.SspInstance;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.registration.states.UnregisteringState;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.stream.RTPPayload;

import javax.sip.RequestEvent;
import java.util.List;
import java.util.ArrayList;
import java.net.InetAddress;

import org.jmock.MockObjectTestCase;

/**
 * Base class used to setup necessary for tests of registratiosn using SipUnit.
 *
 * @author Malin Flodin
 */
public abstract class RegistrationCase extends MockObjectTestCase {

    protected static final int TIMEOUT_IN_MILLI_SECONDS = 10000;

    private ILogger log = ILoggerFactory.getILogger(getClass());

    protected SystemSimulator simulatedSystem;
    private CallManagerToVerify callManager;

    // SSP
    protected SspSimulator simulatedSsp;


    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Load classes so that first test case will not be slower and thus
        // cause SIP retransmissions
        try {
            com.mobeon.masp.callmanager.component.tests.sipunit.ClassLoadingCase.getInstance().loadClasses();
        } catch (ClassNotFoundException e) {
            fail("Class not loaded: " + e.getMessage());
        }

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(0, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);
    }

    public void setUp() throws Exception {
        System.gc();

        simulatedSystem = new SystemSimulator();

        // Create and initiate call manager
        callManager = new CallManagerToVerify(
                "localhost", 5060,
                CallManagerTestContants.CALLMANAGER_WITH_SSPS_XML, simulatedSystem);

        // Create the simulated system, i.e. execution engine etc.
        simulatedSystem.create(callManager);


        // Create the simulated phones
        List<RemotePartyAddress> ssps = ConfigurationReader.getInstance().
                getConfig().getRemoteParty().getSspList();
        assertEquals(1, ssps.size());

        simulatedSsp = new SspSimulator(
                ssps.get(0).getHost(),
                ssps.get(0).getPort(),
                callManager.getHostAddress(),
                TIMEOUT_IN_MILLI_SECONDS);
        simulatedSsp.create();
    }

    public void tearDown() throws Exception {
        // Wait a while to make sure that the SIP stack is ready to be deleted
        Thread.sleep(100);
        callManager.delete();
        simulatedSystem.delete();
        simulatedSsp.delete();
    }

    protected void assertStateRegistered() {
        waitForState(RegisteredState.class, TypeOfState.REGISTRATION_STATE);
    }

    protected void assertStateRegistering() {
        waitForState(RegisteringState.class, TypeOfState.REGISTRATION_STATE);
    }

    protected void assertStateUnregistered() {
        waitForState(UnregisteredState.class, TypeOfState.REGISTRATION_STATE);
    }

    protected void assertStateUnregistering() {
        waitForState(UnregisteringState.class, TypeOfState.REGISTRATION_STATE);
    }

    protected void assertCallStateDisconnected() {
        waitForState(DisconnectedCompletedOutboundState.class, TypeOfState.CALL_STATE);
    }

    protected void assertCallStateError() {
        waitForState(ErrorCompletedOutboundState.class, TypeOfState.CALL_STATE);
    }

    protected RequestEvent gotoRegisteringState() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // We should now be registered
        assertStateRegistering();

        return requestEvent;
    }

    protected void gotoRegisteredState() throws Exception {
        RequestEvent requestEvent = gotoRegisteringState();

        // Send OK response with contact header with expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        sipResponse.addContactHeaderExpiration(60);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // We should now be registered
        assertStateRegistered();
    }

    protected Event assertEventReceived(Class expectedEvent)
            throws InterruptedException {
        Event event = simulatedSystem.
            getReceivedEvent(TIMEOUT_IN_MILLI_SECONDS);
        assertNotNull("Expected to receive event but it was null.", event);
        assertTrue("Expected event: " + expectedEvent +
                " Received event: " + event.getClass(),
                expectedEvent.isInstance(event));
        return event;
    }

    // ======================= Private methods ==========================

    protected enum TypeOfState {
        CALL_STATE, REGISTRATION_STATE
    }
    private Object getCurrentState(TypeOfState type) {
        if (type == TypeOfState.REGISTRATION_STATE) {
            SspInstance sspInstance = ((RemotePartyControllerImpl)
                    CMUtils.getInstance().getRemotePartyController()).
                    getSspInstance(simulatedSsp.getSspId());
            return sspInstance.getCurrentState();
        } else {
            CallImpl call = (CallImpl)simulatedSystem.getActiveCall();
            return call.getCurrentState();
        }
    }

    private void waitForState(Class expectedState, TypeOfState type) {
        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(getCurrentState(type)) &&
            (System.currentTimeMillis() <
                    (startTime + TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "state " + expectedState + ".", e);
                return;
            }
        }

        Object state = getCurrentState(type);
        if (!expectedState.isInstance(state)) {
            fail("Timed out when waiting for state " + expectedState +
                ". Current state is " + state.getClass().getName());
        }
    }

    private AdministrativeState getCurrentAdminState() {
        return CMUtils.getInstance().getCmController().getCurrentState();
    }

    protected void waitForAdminState(Class expectedState) {
        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(getCurrentAdminState()) &&
            (System.currentTimeMillis() <
                    (startTime + TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "admin state " + expectedState + ".", e);
                return;
            }
        }

        AdministrativeState state = getCurrentAdminState();
        if (!expectedState.isInstance(state)) {
            fail("Timed out when waiting for admin state " + expectedState +
                ". Current state is " + state.getClass().getName());
        }
    }

}
