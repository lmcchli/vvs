/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.component.tests.sipunit.notification;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.sipunit.NotifyReceiverSimulator;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.states.AdministrativeState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.RemoteParty;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.NamedValue;

import javax.sip.RequestEvent;
import java.util.Collection;
import java.net.InetAddress;

/**
 * Base class used to setup necessary for tests of notifications using SipUnit.
 *
 * @author Mats Hägg
 */
public abstract class NotificationCase extends MockObjectTestCase {

    private ILogger log = ILoggerFactory.getILogger(getClass());

    protected static final int TIMEOUT_IN_MILLI_SECONDS = 10000;

    protected SystemSimulator simulatedSystem;
    private CallManagerToVerify callManager;

    protected NotifyReceiverSimulator notifyReceiverSim;


    public void setUp() throws Exception {
        System.gc();
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        simulatedSystem = new SystemSimulator();

        // Create and initiate call manager
        callManager = new CallManagerToVerify(
                "localhost", 5060,
                CallManagerTestContants.CALLMANAGER_WITH_REMOTEUA_XML, simulatedSystem);

        // Create the simulated system
        simulatedSystem.create(callManager);

        assertTrue(ConfigurationReader.getInstance().
                getConfig().getRemoteParty().isSipProxy());
    }

    /**
     * Setup the simulator for receiveing notify requests.
     * @param simUri - URI for receiving simulator. Should match the to address in notify request.
     * @param simHost
     * @param simPort
     * @throws Exception
     */
    public void setupNotifySim(String simUri, String simHost, int simPort) throws Exception {
        
        if(notifyReceiverSim != null)
            teardownNotifySim();

        notifyReceiverSim = new NotifyReceiverSimulator(
                simUri,
                simHost,
                simPort,
                TIMEOUT_IN_MILLI_SECONDS);
        notifyReceiverSim.create();

    }

    public void teardownNotifySim() {
        if (notifyReceiverSim != null) {
            notifyReceiverSim.delete();
        }
    }

    public void tearDown() throws Exception {

        teardownNotifySim();
        
        // Wait a while to make sure that the SIP stack is ready to be deleted
        Thread.sleep(100);
        callManager.delete();
        simulatedSystem.delete();
    }


    protected void sendNotify(Collection<NamedValue<String,String>> params) throws Exception {

        callManager.getCallManager().sendSipMessage("messagewaiting",
                simulatedSystem.getEventDispatcher(),
                simulatedSystem.getSessionMock(),
                params);
    }


    protected RequestEvent receiveNotify() throws Exception {
        // Wait for NOTIFY request
        return notifyReceiverSim.assertNotifyReceived();
    }

    protected void doNotifyOk() throws Exception {
        RequestEvent requestEvent = receiveNotify();

        // Send OK response with contact header with expires time
        SipResponse sipResponse = notifyReceiverSim.createOkResponse(requestEvent);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

    }

    protected Event assertEventReceived(Class expectedEvent)
            throws InterruptedException {
        Event event = simulatedSystem.
            getReceivedEvent(NotificationCase.TIMEOUT_IN_MILLI_SECONDS);
        assertNotNull("Expected to receive event but it was null.", event);
        assertTrue("Expected event: " + expectedEvent +
                " Received event: " + event.getClass(),
                expectedEvent.isInstance(event));
        return event;
    }




    private AdministrativeState getCurrentAdminState() {
        return CMUtils.getInstance().getCmController().getCurrentState();
    }

    protected void waitForAdminState(Class expectedState) {
        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(getCurrentAdminState()) &&
            (System.currentTimeMillis() <
                    (startTime + NotificationCase.TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                if (log.isDebugEnabled())
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


    protected void assertConfigurationContainsNoSsp() {
        // Override configuration and make sure it contains no SSP
        ConfigurationReader.getInstance().getConfig().removeRemoteParty();
    }

    protected void assertConfigurationContainsSsp() {
        // Override configuration and make sure it contains at least one SSP
        RemoteParty remoteParty = new RemoteParty();
        remoteParty.addSsp("SSP1", 1);
        ConfigurationReader.getInstance().getConfig().setRemoteParty(remoteParty);
    }
}
