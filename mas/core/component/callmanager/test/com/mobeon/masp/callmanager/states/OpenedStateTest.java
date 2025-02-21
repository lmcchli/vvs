/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.loadregulation.states.MaxLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.HighLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.masp.callmanager.states.AdministrativeState.CALL_ACTION;

/**
 * OpenedState tester.
 * @author Malin Flodin
 */
public class OpenedStateTest extends AdministrativeStateCase {

    OpenedState openedState;

    protected void setUp() throws Exception {
        super.setUp();

        openedState = new OpenedState(
                (CallManagerController)cmControllerMock.proxy(), loadRegulator);
    }

    /**
     * Verifies that a forced close in {@link OpenedState} if there are no active
     * calls results in unregistration in the SSP. The state is set to
     * {@link ClosedState} and close completed is signalled.
     * @throws Exception if test case fails.
     */
    public void testCloseForcedWhenNoCalls() throws Exception {
        expectSspUnregistration();
        assertStateClosed();
        assertCloseCompleted();
        openedState.closeForced(closeForcedEvent);
    }

    /**
     * Verifies that a forced close in {@link OpenedState} if there are active calls
     * results in unregistration
     * in the SSP, signal to all active calls to lock. The state is set to
     * {@link ClosingForcedState}.
     * @throws Exception if test case fails.
     */
    public void testCloseForcedWhenCalls() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        expectSspUnregistration();
        assertLockingAllCalls();
        assertStateClosingForced();
        openedState.closeForced(closeForcedEvent);
    }

    /**
     * Verifies that an open in {@link OpenedState} will signal that
     * open is completed.
     * @throws Exception if test case fails.
     */
    public void testOpen() throws Exception {
        assertOpenCompleted();
        openedState.open(openEvent);
    }

    /**
     * Verifies that an unforced close in {@link OpenedState} if there are no
     * active calls results in unregistration in the SSP and a signal that
     * close is completed. The state is set to {@link ClosedState}.
     * @throws Exception if test case fails.
     */
    public void testCloseUnforcedWhenNoCalls() throws Exception {
        expectSspUnregistration();
        assertStateClosingUnforcedRejecting();
        assertCloseCompleted();
        assertStateClosed();
        openedState.closeUnforced(closeUnforcedEvent);
    }

    /**
     * Verifies that an unforced close in {@link OpenedState} if there are active
     * calls results in
     * unregistration in the SSP. The state is set to {@link ClosingUnforcedRejectingState}.
     * @throws Exception if test case fails.
     */
    public void testCloseUnforcedWhenCalls() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        expectSspUnregistration();
        assertStateClosingUnforcedRejecting();
        openedState.closeUnforced(closeUnforcedEvent);
    }

    /**
     * Verifies that an inbound call always is accepted regardless or current
     * load situation.
     * @throws Exception if test case fails.
     */
    public void testAddInboundCall() throws Exception {
        // Verifies that an inbound call while in normal load is accepted
        loadRegulator.updateThreshold(2, 1, 3);
        CALL_ACTION action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(1, loadRegulator.getCurrentCalls());

        // Verifies that an inbound call while in high load is accepted
        expectSspUnregistration();
        action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(2, loadRegulator.getCurrentCalls());

        // Verifies that an inbound call while in max load is redirected
        action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(3, loadRegulator.getCurrentCalls());
        action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.REDIRECT_CALL, action);
        assertEquals(3, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an inbound call would be accepted in {@link OpenedState}
     * as long as the load state is not
     * {@link com.mobeon.masp.callmanager.loadregulation.states.HighLoadState}
     * @throws Exception if test case fails.
     */
    public void testCheckCallAction() throws Exception {
        // Verifies that an inbound call while in normal load would be accepted
        loadRegulator.updateThreshold(2, 1, 3);
        CALL_ACTION action = openedState.checkCallAction();
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);

        // Add one call
        action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(1, loadRegulator.getCurrentCalls());

        // Verifies that an inbound call while in high load would be accepted
        action = openedState.checkCallAction();
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);

        //Add another call, high load is reached
        expectSspUnregistration();
        action = openedState.addInboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(2, loadRegulator.getCurrentCalls());

        // Verifies that an inbound call while in max load would be redirected
        action = openedState.addInboundCall(null);
        assertEquals(3, loadRegulator.getCurrentCalls());
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        action = openedState.checkCallAction();
        assertEquals(CALL_ACTION.REDIRECT_CALL, action);
    }

    /**
     * Verifies that an outbound call in normal or high load is accepted.
     * But during max load an outbound call is rejected.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCall() throws Exception {
        // Verifies that an outbound call while in normal load is accepted
        loadRegulator.updateThreshold(2, 1, 3);
        CALL_ACTION action = openedState.addOutboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(1, loadRegulator.getCurrentCalls());
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verifies that an outbound call while in high load is accepted
        expectSspUnregistration();
        action = openedState.addOutboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(2, loadRegulator.getCurrentCalls());
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verifies that an outbound call while in max load is rejected
        loadRegulator.addCall(null);
        assertEquals(3, loadRegulator.getCurrentCalls());
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
        action = openedState.addOutboundCall(null);
        assertEquals(3, loadRegulator.getCurrentCalls());
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies removing calls in opened state.
     * <p>
     * Verifies that removing a call while in normal load has no effects.
     * Also verifies that removing a call so that we go from high load to normal
     * load results in registering all SSPs
     * @throws Exception if test case fails.
     */
    public void testRemoveCall() throws Exception {
        // Verifies that removing a call while in normal load has no effects
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        openedState.removeCall(null);

        // Verifies that removing a call so that we go from high load to normal
        // load results in registering all SSPs
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        expectSspRegistration();
        openedState.removeCall(null);
    }

    /**
     * Verifies that registration to SSP is performed when updateThreshold
     * is called during high load and causes low water mark to be reached.
     * @throws Exception if test case fails.
     */
    public void testUpdateThresholdToNormalLoad() throws Exception {
        // Add calls up to high water mark.
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, loadRegulator.addCall(null));

        // Update threshold so that low water mark is reached
        UpdateThresholdEvent thresholdEvent = new UpdateThresholdEvent(4, 2, 6);
        expectSspRegistration();
        expectSetMaxConnections(6);
        openedState.updateThreshold(thresholdEvent);
    }

    /**
     * Verifies that all SSPs are unregistered when updateThershold during
     * normal load causes high water mark to be reached.
     * @throws Exception
     */
    public void testUpdateThresholdToHighLoad() throws Exception {
        // Normal load
        loadRegulator.updateThreshold(4, 2, 6);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);

        // Update threshold so that high water mark is reached
        UpdateThresholdEvent thresholdEvent = new UpdateThresholdEvent(2, 1, 4);
        expectSspUnregistration();
        expectSetMaxConnections(4);
        openedState.updateThreshold(thresholdEvent);
    }

}
