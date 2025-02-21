/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.loadregulation.states.MaxLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.HighLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.masp.callmanager.states.AdministrativeState.CALL_ACTION;

/**
 * ClosingUnforcedRejectingState tester.
 * @author Malin Flodin
 */
public class ClosingUnforcedRejectingStateTest extends AdministrativeStateCase {

    ClosingUnforcedRejectingState closingUnforcedRejectingState;

    protected void setUp() throws Exception {
        super.setUp();

        closingUnforcedRejectingState = new ClosingUnforcedRejectingState(
                (CallManagerController)cmControllerMock.proxy(), loadRegulator);
    }

    /**
     * Verifies that a forced close in {@link ClosingUnforcedRejectingState} when there is no active
     * calls results in a signal that close is completed.
     * The state is set to {@link ClosedState}.
     * @throws Exception if test case fails.
     */
    public void testCloseForcedWhenNoCalls() throws Exception {
        assertStateClosed();
        assertCloseCompleted();
        closingUnforcedRejectingState.closeForced(closeForcedEvent);
    }

    /**
     * Verifies that a forced close in {@link ClosingUnforcedRejectingState} when there are active
     * calls results in all calls are locked.
     * The state is set to {@link ClosingForcedState}.
     * @throws Exception if test case fails.
     */
    public void testCloseForcedWhenCalls() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        assertLockingAllCalls();
        assertStateClosingForced();
        closingUnforcedRejectingState.closeForced(closeForcedEvent);

        assertEquals(1, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an open in {@link ClosingUnforcedRejectingState} results in the
     * state set to Opened and open completed is signalled.
     * If the load is normal, registration towards SSPs is initiated.
     * @throws Exception if test case fails.
     */
    public void testOpenWhenLoadIsNormal() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        expectSspRegistration();
        assertStateOpened();
        assertOpenCompleted();
        closingUnforcedRejectingState.open(openEvent);
    }

    /**
     * Verifies that an open in {@link ClosingUnforcedRejectingState} results in the
     * state set to Opened and open completed is signalled.
     * If the load is high, registration towards SSPs is NOT initiated.
     * @throws Exception if test case fails.
     */
    public void testOpenWhenLoadIsHigh() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        assertStateOpened();
        assertOpenCompleted();
        closingUnforcedRejectingState.open(openEvent);

        assertEquals(2, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an unforced close in {@link ClosingUnforcedRejectingState} results in no
     * action.
     * @throws Exception if test case fails.
     */
    public void testCloseUnforced() throws Exception {
        closingUnforcedRejectingState.closeUnforced(closeUnforcedEvent);
    }

    /**
     * Verifies that an inbound call is rejected in {@link ClosingUnforcedRejectingState}.
     * @throws Exception if test case fails.
     */
    public void testAddInboundCall() throws Exception {
        CALL_ACTION action = closingUnforcedRejectingState.addInboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);

        assertEquals(0, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an inbound call would be rejected in {@link ClosingUnforcedRejectingState}.
     * @throws Exception if test case fails.
     */
    public void testCheckAction() throws Exception {
        CALL_ACTION action = closingUnforcedRejectingState.checkCallAction();
        assertEquals(CALL_ACTION.REJECT_CALL, action);

        assertEquals(0, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an outbound call is rejected in {@link ClosingUnforcedRejectingState}
     * if load is at max.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCallInMaxLoad() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        assertEquals(3, loadRegulator.getCurrentCalls());

        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        CALL_ACTION action = closingUnforcedRejectingState.addOutboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
        assertEquals(3, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an outbound call is rejected in {@link ClosingUnforcedRejectingState}
     * if load is at max.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCallInHighLoad() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        assertEquals(2, loadRegulator.getCurrentCalls());

        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        CALL_ACTION action = closingUnforcedRejectingState.addOutboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(3, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that an outbound call is rejected in {@link ClosingUnforcedRejectingState}
     * if load is at max.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCallInNormalLoad() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        assertEquals(0, loadRegulator.getCurrentCalls());

        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        CALL_ACTION action = closingUnforcedRejectingState.addOutboundCall(null);
        assertEquals(CALL_ACTION.ACCEPT_CALL, action);
        assertEquals(1, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that removing a call that results in no more calls causes the
     * state to be set to {@link ClosedState} and a signal that close is
     * completed.
     * Otherwise the state is left unchanged.
     * @throws Exception if test case fails.
     */
    public void testRemoveCall() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        assertEquals(2, loadRegulator.getCurrentCalls());

        closingUnforcedRejectingState.removeCall(null);
        assertEquals(1, loadRegulator.getCurrentCalls());

        assertStateClosed();
        assertCloseCompleted();
        closingUnforcedRejectingState.removeCall(null);
        assertEquals(0, loadRegulator.getCurrentCalls());
    }

    /**
     * Verifies that no SSP registration or unregistration is made in this
     * state when updateThreshold is called.
     * @throws Exception
     */
    public void testUpdateThreshold() throws Exception {
        UpdateThresholdEvent thresholdEvent;
        loadRegulator.updateThreshold(4,2,6);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);

        // High water mark is reached.
        thresholdEvent = new UpdateThresholdEvent(3,1,6);
        expectSetMaxConnections(6);
        closingUnforcedRejectingState.updateThreshold(thresholdEvent);

        // Low water mark is reached.
        thresholdEvent = new UpdateThresholdEvent(5,3,6);
        expectSetMaxConnections(6);
        closingUnforcedRejectingState.updateThreshold(thresholdEvent);
    }
}
