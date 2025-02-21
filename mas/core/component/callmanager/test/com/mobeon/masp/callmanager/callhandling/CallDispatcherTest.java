/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import javax.sip.Transaction;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequestMock;
import com.mobeon.masp.callmanager.sip.events.SipEvent;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.callhandling.states.inbound.IdleInboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.IdleOutboundState;

/**
 * CallDispatcher Tester.
 *
 * @author Malin Flodin
 */
public final class CallDispatcherTest extends MockObjectTestCase
{
    private final String EARLY_INBOUND_CALL_DIALOG_ID = "1234567890:fromtag";
    private final String ESTABLISHED_INBOUND_CALL_DIALOG_ID = "1234567890:totag:fromtag";
    private final String EARLY_OUTBOUND_CALL_DIALOG_ID = "1234567890:fromtag";
    private final String ESTABLISHED_OUTBOUND_CALL_DIALOG_ID = "1234567890:fromtag:totag";

    private CallDispatcher callDispatcher;
    private final SipRequestMock sipRequestMock = new SipRequestMock();
    private final Mock sipEventMock = new Mock(SipEvent.class);
    private final Mock transactionMock = new Mock(Transaction.class);

    // Stuff needed to create calls
    private final Mock inboundCallMock = new Mock(InboundCallInternal.class);
    private final Mock outboundCallMock = new Mock(OutboundCallInternal.class);

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        callDispatcher = new CallDispatcher();

        inboundCallMock.stubs().method("getCurrentState").will(returnValue(
                new IdleInboundState((InboundCallInternal)inboundCallMock.proxy())));
        outboundCallMock.stubs().method("getCurrentState").will(returnValue(
                new IdleOutboundState((OutboundCallInternal)outboundCallMock.proxy())));

        sipEventMock.stubs().method("getTransaction").will(
                returnValue(transactionMock.proxy()));
        sipEventMock.stubs().method("getEarlyDialogId").will(
                returnValue(EARLY_INBOUND_CALL_DIALOG_ID));
        sipEventMock.stubs().method("getEstablishedDialogId").will(
                returnValue(ESTABLISHED_INBOUND_CALL_DIALOG_ID));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        CMUtils.getInstance().delete();
    }

    /**
     * Verifies that null is returned when lookin up a call with
     * no sip event.
     *
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenSipEventIsNull() throws Exception {
        assertNull(callDispatcher.getCall(null));
    }

    /**
     * Verifies that null is returned when looking up a call with
     * transaction null.
     *
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenTransactionIsNull() throws Exception {
        sipEventMock.expects(once()).method("getTransaction").will(returnValue(null));
        sipEventMock.stubs().method("getSipMessage").will(
                returnValue(sipRequestMock.getSipRequest()));
        assertNull(callDispatcher.getCall((SipEvent)sipEventMock.proxy()));
    }

    /**
     * Verifies that null is returned when the given early dialogId is not found.
     *
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenCallNotFound() throws Exception {
        assertNull(callDispatcher.getCall((SipEvent)sipEventMock.proxy()));
    }

    /**
     * Verifies that correct call is returned for an inbound call "early" dialogId.
     *
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenInboundCallDialogIsEarly() throws Exception {
        assertEarlyInboundCall();

        sipEventMock.stubs().method("getEarlyDialogId").
                will(returnValue(EARLY_INBOUND_CALL_DIALOG_ID));
        assertEquals(inboundCallMock.proxy(),
                callDispatcher.getCall((SipEvent)sipEventMock.proxy()));
    }

    /**
     * Verifies that correct call is returned for an outbound call "early" dialogId.
     *
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenOutboundCallDialogIsEarly() throws Exception {
        assertEarlyOutboundCall();

        sipEventMock.expects(once()).method("getEarlyDialogId").
                will(returnValue(EARLY_OUTBOUND_CALL_DIALOG_ID));
        assertEquals(outboundCallMock.proxy(),
                callDispatcher.getCall((SipEvent)sipEventMock.proxy()));
    }

    /**
     * Verifies that correct call is returned for an "established" dialogId.
     * Outbound call.
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenOutboundCallDialogIsUpdated() throws Exception {
        assertEarlyOutboundCall();

        outboundCallMock.expects(once())
                .method("setEstablishedDialogId")
                .with(eq(ESTABLISHED_OUTBOUND_CALL_DIALOG_ID));

        // Update the call dispatcher with a new dialogId
        callDispatcher.updateOutboundCallDialogId(
                (OutboundCallInternal)outboundCallMock.proxy(),
                ESTABLISHED_OUTBOUND_CALL_DIALOG_ID, false);

        assertNumberOfCalls(1, 1);

        sipEventMock.expects(once()).method("getEstablishedDialogId").
                will(returnValue(ESTABLISHED_OUTBOUND_CALL_DIALOG_ID));
        assertEquals(outboundCallMock.proxy(),
                callDispatcher.getCall((SipEvent)sipEventMock.proxy()));
    }

    /**
     * Verifies that correct call is returned for an "established" dialogId.
     * Inbound call.
     * @throws Exception if test case fails.
     */
    public void testGetCallWhenInboundCallDialogIsEstablished() throws Exception {
        assertEstablishedInboundCall();

        assertEquals(inboundCallMock.proxy(),
                callDispatcher.getCall((SipEvent)sipEventMock.proxy()));

        assertNumberOfCalls(1, 1);
    }

    /**
     * Verifies that no update is made if dialogId is null.
     * @throws Exception if test case fails.
     */
    public void testUpdateCallsDialogIdWhenDialogIdIsNull() throws Exception {
        assertEarlyOutboundCall();

        callDispatcher.updateOutboundCallDialogId(
                (OutboundCallInternal)outboundCallMock.proxy(), null, false);
        assertNumberOfCalls(1, 0);
    }

    /**
     * Verifies that there is only an established dialog when an early dialog
     * is updated and removed.
     *
     * @throws Exception if test case fails.
     */
    public void testUpdateCallsDialogId() throws Exception {
        assertEarlyOutboundCall();

        outboundCallMock.expects(once())
                .method("setEstablishedDialogId")
                .with(eq(ESTABLISHED_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.expects(once())
                .method("inactivateEarlyDialog");

        callDispatcher.updateOutboundCallDialogId(
                (OutboundCallInternal)outboundCallMock.proxy(),
                ESTABLISHED_OUTBOUND_CALL_DIALOG_ID, true);
        assertNumberOfCalls(0, 1);
    }

    /**
     * Verifies that an early dialog can be removed.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallWhenEarlyDialogOnly() throws Exception {
        assertEarlyOutboundCall();

        callDispatcher.removeCall(EARLY_OUTBOUND_CALL_DIALOG_ID, null);
        assertNumberOfCalls(0, 0);
    }

    /**
     * Verifies that an early and established dialog can be removed.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallWhenBothEarlyAndEstablished() throws Exception {
        assertEstablishedInboundCall();

        callDispatcher.removeCall(
                EARLY_INBOUND_CALL_DIALOG_ID, ESTABLISHED_INBOUND_CALL_DIALOG_ID);
        assertNumberOfCalls(0, 0);
    }

    /**
     * Verifies that an established dialog can be removed.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallWhenEstablishedDialogOnly() throws Exception {
        assertEstablishedOutboundCall();

        callDispatcher.updateOutboundCallDialogId(
                (OutboundCallInternal)outboundCallMock.proxy(),
                ESTABLISHED_OUTBOUND_CALL_DIALOG_ID, true);
        assertNumberOfCalls(0, 1);

        callDispatcher.removeCall(null, ESTABLISHED_OUTBOUND_CALL_DIALOG_ID);
        assertNumberOfCalls(0, 0);
    }

    private void assertNumberOfCalls(int earlyCalls, int establishedCalls) {
        assertEquals(earlyCalls, callDispatcher.amountOfInitiatedCalls());
        assertEquals(establishedCalls, callDispatcher.amountOfEstablishedCalls());
    }


    /************************ Private methods ***************************/

    /**
     * Inserts an early Inbound call.
     */
    private void assertEarlyInboundCall() {
        inboundCallMock.expects(once())
                .method("setInitialDialogId")
                .with(eq(EARLY_INBOUND_CALL_DIALOG_ID));
        inboundCallMock.expects(once())
                .method("setEstablishedDialogId")
                .with(eq(ESTABLISHED_INBOUND_CALL_DIALOG_ID));

        callDispatcher.insertInboundCall(
                (InboundCallInternal)inboundCallMock.proxy(),
                (SipEvent)sipEventMock.proxy());
        assertNumberOfCalls(1, 1);
    }

    /**
     * Inserts an early Outbound call.
     */
    private void assertEarlyOutboundCall() {
        outboundCallMock.expects(once())
                .method("setInitialDialogId")
                .with(eq(EARLY_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.stubs()
                .method("getInitialDialogId")
                .will(returnValue(EARLY_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.stubs()
                .method("isEarlyDialogActive")
                .will(returnValue(true));

        callDispatcher.insertOutboundCall(
                (OutboundCallInternal)outboundCallMock.proxy(),
                sipRequestMock.getSipRequest());
        assertNumberOfCalls(1, 0);
    }

    /**
     * Inserts an established Inbound call.
     */
    private void assertEstablishedInboundCall() {
        inboundCallMock.expects(once())
                .method("setInitialDialogId")
                .with(eq(EARLY_INBOUND_CALL_DIALOG_ID));
        inboundCallMock.expects(once())
                .method("setEstablishedDialogId")
                .with(eq(ESTABLISHED_INBOUND_CALL_DIALOG_ID));
        inboundCallMock.stubs()
                .method("getEstablishedDialogId")
                .will(returnValue(ESTABLISHED_INBOUND_CALL_DIALOG_ID));

        callDispatcher.insertInboundCall(
                (InboundCallInternal)inboundCallMock.proxy(),
                (SipEvent)sipEventMock.proxy());
        assertNumberOfCalls(1, 1);
    }

    /**
     * Inserts an early Outbound call.
     */
    private void assertEstablishedOutboundCall() {
        outboundCallMock.expects(once())
                .method("setInitialDialogId")
                .with(eq(EARLY_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.stubs()
                .method("getInitialDialogId")
                .will(returnValue(EARLY_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.stubs()
                .method("isEarlyDialogActive")
                .will(returnValue(true));
        outboundCallMock.expects(once())
                .method("setEstablishedDialogId")
                .with(eq(ESTABLISHED_OUTBOUND_CALL_DIALOG_ID));
        outboundCallMock.expects(once())
                .method("inactivateEarlyDialog");

        callDispatcher.insertOutboundCall(
                (OutboundCallInternal)outboundCallMock.proxy(),
                sipRequestMock.getSipRequest());

        assertNumberOfCalls(1, 0);
    }

}
