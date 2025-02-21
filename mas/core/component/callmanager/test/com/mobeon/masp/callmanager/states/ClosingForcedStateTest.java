/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.states.AdministrativeState.CALL_ACTION;

/**
 * ClosingForcedState tester.
 * @author Malin Flodin
 */
public class ClosingForcedStateTest extends AdministrativeStateCase {

    ClosingForcedState closingForcedState;

    protected void setUp() throws Exception {
        super.setUp();

        closingForcedState = new ClosingForcedState(
                (CallManagerController)cmControllerMock.proxy(), loadRegulator);
    }

    /**
     * Verifies that a forced close in {@link ClosingForcedState} is ignored.
     * @throws Exception if test case fails.
     */
    public void testCloseForced() throws Exception {
        closingForcedState.closeForced(closeForcedEvent);
    }

    /**
     * Verifies that an open in {@link ClosingForcedState} results in the state set
     * to Opened and open completed is signalled.
     * If the load is normal, registration towards SSPs is initiated.
     * @throws Exception if test case fails.
     */
    public void testOpenWhenLoadIsNormal() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        expectSspRegistration();
        assertStateOpened();
        assertOpenCompleted();
        closingForcedState.open(openEvent);
    }

    /**
     * Verifies that an open in {@link ClosingForcedState} results in the state set
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
        closingForcedState.open(openEvent);
    }

    /**
     * Verifies that an unforced close in {@link ClosingForcedState} is ignored.
     * @throws Exception if test case fails.
     */
    public void testCloseUnforced() throws Exception {        
        closingForcedState.closeUnforced(closeUnforcedEvent);
    }

    /**
     * Verifies that an inbound call is rejected in {@link ClosingForcedState}.
     * @throws Exception if test case fails.
     */
    public void testAddInboundCall() throws Exception {
        CALL_ACTION action = closingForcedState.addInboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that an inbound call would be rejected in {@link ClosingForcedState}.
     * @throws Exception if test case fails.
     */
    public void testCheckCallAction() throws Exception {
        CALL_ACTION action = closingForcedState.checkCallAction();
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that an outbound call is rejected in {@link ClosingForcedState}.
     * @throws Exception if test case fails.
     */
    public void testAddOutboundCall() throws Exception {
        CALL_ACTION action = closingForcedState.addOutboundCall(null);
        assertEquals(CALL_ACTION.REJECT_CALL, action);
    }

    /**
     * Verifies that removing a call that results in no more calls causes the
     * state to be set to {@link ClosedState} and that close completed is
     * signalled.
     * Otherwise the state is left unchanged.
     * @throws Exception if test case fails.
     */
    public void testRemoveCall() throws Exception {
        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall(null);
        loadRegulator.addCall(null);
        closingForcedState.removeCall(null);
        assertStateClosed();
        assertCloseCompleted();
        closingForcedState.removeCall(null);
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
        closingForcedState.updateThreshold(thresholdEvent);

        // Low water mark is reached.
        thresholdEvent = new UpdateThresholdEvent(5,3,6);
        expectSetMaxConnections(6);
        closingForcedState.updateThreshold(thresholdEvent);
    }
}
