/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.states.AdministrativeState.CALL_ACTION;

/**
 * ClosedState tester.
 * @author Malin Flodin
 */
public class ClosedStateTest extends AdministrativeStateCase {

    ClosedState closedState;

    protected void setUp() throws Exception {
        super.setUp();

        closedState = new ClosedState(
                (CallManagerController)cmControllerMock.proxy(), loadRegulator);
    }

    /**
     * Verifies that a forced close in {@link ClosedState} will signal that
     * close is completed.
     * @throws Exception if test case fails.
     */
    public void testCloseForced() throws Exception {
        assertCloseCompleted();
        closedState.closeForced(closeForcedEvent);
    }

    /**
     * Verifies that an open in {@link ClosedState} results in the state set
     * to Opened and open completed is signalled.
     * If the load is normal, registration towards SSPs is initiated.
     * @throws Exception if test case fails.
     */
    public void testOpenWhenLoadIsNormal() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        expectSspRegistration();
        assertOpenCompleted();
        assertStateOpened();
        closedState.open(openEvent);
    }

    /**
     * Verifies that an open in {@link ClosedState} results in the state set
     * to Opened and open completed is signalled.
     * If the load is high, registration towards SSPs is NOT initiated.
     * @throws Exception if test case fails.
     */
    public void testOpenWhenLoadIsHigh() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        assertStateOpened();
        assertOpenCompleted();
        closedState.open(openEvent);
    }

    /**
     * Verifies that an unforced close in {@link ClosedState} results in no action
     * except signalling closeCompleted.
     * @throws Exception if test case fails.
     */
    public void testCloseUnforced() throws Exception {
        assertCloseCompleted();
        closedState.closeUnforced(closeUnforcedEvent);
    }

    /**
     * Verifies that an inbound call is rejected in {@link ClosedState}.
     * @throws Exception if test case fails.
     */
    public void testAddInboundCall() throws Exception {
        CALL_ACTION action = closedState.addInboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that an inbound call would be rejected in {@link ClosedState}.
     * @throws Exception if test case fails.
     */
    public void testCheckCallAction() throws Exception {
        CALL_ACTION action = closedState.checkCallAction();
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that an outbound call is rejected in {@link ClosedState}.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCall() throws Exception {
        CALL_ACTION action = closedState.addOutboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that removing a call that results in no more calls causes the
     * state to be set to {@link ClosedState}.
     * Otherwise the state is left unchanged.
     * @throws Exception if test case fails.
     */
    public void testRemoveCall() throws Exception {
        closedState.removeCall(null);
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
        closedState.updateThreshold(thresholdEvent);

        // Low water mark is reached.
        thresholdEvent = new UpdateThresholdEvent(5,3,6);
        expectSetMaxConnections(6);
        closedState.updateThreshold(thresholdEvent);
    }
}
