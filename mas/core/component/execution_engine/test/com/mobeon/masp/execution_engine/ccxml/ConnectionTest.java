/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.events.*;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLRuntimeCase;
import com.mobeon.masp.execution_engine.ccxml.runtime.ExecutionContextImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.EventHubWithOrderCheck;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.session.SessionImpl;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.stream.RecordFinishedEvent;
import com.mobeon.masp.stream.RecordFailedEvent;
import com.mobeon.masp.stream.PlayFinishedEvent;
import com.mobeon.masp.stream.PlayFailedEvent;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.*;

/**
 * @author Mikael Andersson
 */
public class ConnectionTest extends CCXMLRuntimeCase {

    private EventHubWithOrderCheck eventHubWithOrderCheck = new EventHubWithOrderCheck();

    public ConnectionTest(String event) {
        super(event);
    }

    public static Test suite() {
        return new TestSuite(ConnectionTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        mockExecutionContext.stubs().method("getEventHub").will(returnValue(eventHubWithOrderCheck));
    }


    /**
     * Exercises all transitions, at least once to
     * validate that no unwanted exceptions are thrown
     * and that thing generally behaves the way we want
     * them to.
     *
     * @throws Exception
     */
    public void testReceiveEvent() throws Exception {
        setupConfigurationManager();
        Set<Connection.Transition> seen = new HashSet<Connection.Transition>();
        ExecutionContextImpl executionContext = new ExecutionContextImpl(new SessionImpl(), platformFactoryInstance(), null, configurationManager);
        Connection c = new ConnectionImpl(executionContext);

        tryTransitions(c, seen);
    }

    private void tryTransitions(Connection c, Set<Connection.Transition> seen) {
        Map<Connection.AutomatonEvent, Connection.Transition> possible = c.getPossibleTransitions();
        if (possible == null) return;
        Set<Connection.AutomatonEvent> keys = possible.keySet();
        for (Connection.AutomatonEvent key : keys) {
            Connection test = c.clone();
            Connection.Transition transition = possible.get(key);
            if (!seen.contains(transition)) {
                seen.add(transition);
                test.receiveEvent(key, automatonEventToRelatedEvent(key));
                if (test.getState() != transition.getNewState())
                    die("Transition failed to transition to desired state ! " + transition);
                tryTransitions(test, seen);
            }
        }
    }


    /**
     * Validates that accept fails if we're in the
     * wrong state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testAcceptFailsWhenInWrongState() throws Exception {

        Call call = createCall(InboundCall.class);

        Connection c = new ConnectionImpl(getExecutionContext());
        c.setCall(call);

        c.accept();
        assertEvents(Constants.Event.ERROR_NOTALLOWED);

    }


    /**
     * Validates that accept succeeds for incoming connections
     * when we're in the ALERTING state.
     *
     * @throws Exception
     */
    public void testAccept() throws Exception {
        Call call = createCall(InboundCall.class);

        Connection c = new ConnectionImpl(getExecutionContext());
        c.setCall(call);

        //Step 1, go to alerting state
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
        assertEvents(Constants.Event.CONNECTION_ALERTING);

        //Step 2, call accept
        expect_Call_accept(call);
        c.accept();
    }

    /**
     * Validates that proxy succeeds for incoming connections
     * when we're in the ALERTING state.
     *
     * @throws Exception
     */
    public void testProxySucceedsWhenInAlertingState() throws Exception {
    	Connection c = getConnectionInState(Connection.State.ALERTING, null);
        assertTrue(c.getState() == Connection.State.ALERTING);

        expect_Call_proxy(c.getCall());
        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.PROXYING);
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Validates that proxy fails if we're in the
     * START state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenStart() throws Exception {
    	Connection c = getConnectionInState(Connection.State.START, null);
        assertTrue(c.getState() == Connection.State.START);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.START);
        assertEvents(Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * PROGRESSING state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenProgressing() throws Exception {
    	Connection c = getConnectionInState(Connection.State.PROGRESSING, null);
        assertTrue(c.getState() == Connection.State.PROGRESSING);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.PROGRESSING);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_PROGRESSING,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * PROXYING state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenProxying() throws Exception {
    	Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.PROXYING);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * CONNECTED state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenConnected() throws Exception {
    	Connection c = getConnectionInState(Connection.State.CONNECTED, null);
        assertTrue(c.getState() == Connection.State.CONNECTED);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.CONNECTED);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_CONNECTED,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * DISCONNECTED state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenDisconnected() throws Exception {
    	Connection c = getConnectionInState(Connection.State.DISCONNECTED, null);
        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.DISCONNECTED);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_DISCONNECTED,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * FAILED state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenFailed() throws Exception {
    	Connection c = getConnectionInState(Connection.State.FAILED, null);
        assertTrue(c.getState() == Connection.State.FAILED);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.FAILED);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_FAILED,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * ERROR state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenError() throws Exception {
    	Connection c = getConnectionInState(Connection.State.ERROR, null);
        assertTrue(c.getState() == Connection.State.ERROR);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.ERROR);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.ERROR_CONNECTION,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Validates that proxy fails if we're in the
     * FORCED_DISCONNECT state, and that the error event is sent.
     *
     * @throws Exception
     */
    public void testProxyFailsWhenForcedDisconnect() throws Exception {
    	Connection c = getConnectionInState(Connection.State.FORCED_DISCONNECT, null);
        assertTrue(c.getState() == Connection.State.FORCED_DISCONNECT);

        c.proxy("172.123.45", 1111);
        assertTrue(c.getState() == Connection.State.FORCED_DISCONNECT);
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		//In the FORCED_DISCONNECT state, there are no TransitionActions; so, 
        		//a Constants.Event.CONNECTION_DISCONNECTED would not be generated here.
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Test that if a Failed event is received when the
     * connection is in state "connected", the result will be that "failed"
     * is sent on the event hub.
     */
    public void testFailedWhenConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, null);

        // At this state, send failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Error event is received when the
     * connection is in state "connected", the result will be that "error"
     * is sent on the event hub.
     */
    public void testErrorWhenConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, null);

        // At this state, send failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Disconnected event is received when the
     * connection is in state "connected", the result will be that "disconnected"
     * is sent on the event hub.
     */
    public void testDisconnectedWhenConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, null);

        // At this state, send disconnected

        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Failed event is received when the
     * connection is in state "alerting", the result will be that "failed"
     * is sent on the event hub.
     */
    public void testFailedWhenAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, null);

        // At this state, send failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Error event is received when the
     * connection is in state "alerting", the result will be that "error"
     * is sent on the event hub.
     */
    public void testErrorWhenAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, null);

        // At this state, send failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Disconnected event is received when the
     * connection is in state "alerting", the result will be that "disconnected"
     * is sent on the event hub.
     */
    public void testDisconnectedWhenAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, null);

        // At this state, send disconnected

        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Alerting event is received when the
     * connection is in state "proxying", the result will be that the
     * event is ignored.  The connection stays in the "proxying" state and 
     * no new event is sent on the event hub. 
     * @throws Exception 
     */
    public void testAlertingWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send alerting
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);

        assertTrue(c.getState() == Connection.State.PROXYING);

        // Check that events were sent in expected order
        // We get only the original alerting event; second one is ignored.
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Test that if a Connected event is received when the
     * connection is in state "proxying", the result will be that the
     * event is ignored.  The connection stays in the "proxying" state and 
     * no new event is sent on the event hub.
     * @throws Exception 
     */
    public void testConnectedWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send connected
        c.receiveEvent(Connection.AutomatonEvent.EVENT_CONNECTED, null);

        assertTrue(c.getState() == Connection.State.PROXYING);

        // Check that events were sent in expected order
        // We get only the original alerting event; the connected event is ignored.
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Test that if a Progressing event is received when the
     * connection is in state "proxying", the result will be that "progressing"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testProgressingWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send progressing
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROGRESSING, new ProgressingEvent(c.getCall(), false));

        assertTrue(c.getState() == Connection.State.PROGRESSING);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_PROGRESSING);
    }

    /**
     * Test that if a Failed event is received when the
     * connection is in state "proxying", the result will be that "failed"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testFailedWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send failed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Error event is received when the
     * connection is in state "proxying", the result will be that "error"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testErrorWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send error event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Disconnected event is received when the
     * connection is in state "proxying", the result will be that "disconnected"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testDisconnectedWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send disconnected event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a create call event is received when the
     * connection is in state "proxying", the result will be that the
     * event is ignored.  The connection stays in the "proxying" state and 
     * no new event is sent on the event hub.
     * (This case should never happen since execution of the createcall tag
     * will always cause a new Connection to be created; but for completeness of 
     * testing all events when in the "proxying" state, this case is included here.) 
     * @throws Exception 
     */
    public void testCreateCallWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send create call
        c.receiveEvent(Connection.AutomatonEvent.CREATECALL, null);

        assertTrue(c.getState() == Connection.State.PROXYING);

        // Check that events were sent in expected order
        // We get only the original alerting event; the create call event is ignored.
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Test that if a accept event is received when the
     * connection is in state "proxying", the result will be that the
     * connection stays in the "proxying" state and "not allowed error" 
     * event is sent on the event hub. 
     * @throws Exception 
     */
    public void testAcceptWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, execute accept
        c.accept();

        assertTrue(c.getState() == Connection.State.PROXYING);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Test that if a reject event is received when the
     * connection is in state "proxying", the result will be that the
     * connection stays in the "proxying" state and "not allowed error" 
     * event is sent on the event hub. 
     * @throws Exception 
     */
    public void testRejectWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, execute reject
        c.reject("", "");

        assertTrue(c.getState() == Connection.State.PROXYING);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.ERROR_NOTALLOWED);
    }

    /**
     * Test that if a disconnect event is received (execution of ccxml disconnect tag) 
     * when the connection is in state "proxying", the result will be that the disconnect()
     * method of InboundCall will be called.
     * @throws Exception 
     */
    public void testDisconnectWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);
        assertEvents(Constants.Event.CONNECTION_ALERTING);
        
        expect_Call_disconnect(c.getCall());
        c.disconnect();
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "start", the result will be that the
     * event is ignored.  The connection stays in the "start" state and 
     * no new event is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenStart() throws Exception {
        Connection c = getConnectionInState(Connection.State.START, null);
        assertTrue(c.getState() == Connection.State.START);

        // At this state, send proxied
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.START);

        // Check that no events were sent; the proxied event is ignored.
        assertEvents();
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "alerting", the result will be that the
     * event is ignored.  The connection stays in the "alerting" state and 
     * no new event is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, null);
        assertTrue(c.getState() == Connection.State.ALERTING);

        // At this state, send proxied
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.ALERTING);

        // Check that events were sent in expected order
        // We get only the original alerting event; the proxied event is ignored.
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "progressing", the result will be that "proxied" then "disconnected"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenProgressing() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROGRESSING, null);
        assertTrue(c.getState() == Connection.State.PROGRESSING);

        // At this state, send proxied
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_PROGRESSING,
                Constants.Event.CONNECTION_PROXIED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "proxying", the result will be that "proxied" then "disconnected"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenProxying() throws Exception {
        Connection c = getConnectionInState(Connection.State.PROXYING, null);
        assertTrue(c.getState() == Connection.State.PROXYING);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_PROXIED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "connected", the result will be that the
     * event is ignored.  The connection stays in the "connected" state and 
     * no new event is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, null);
        assertTrue(c.getState() == Connection.State.CONNECTED);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.CONNECTED);

        // Check that events were sent in expected order
        // We get only the original alerting event; the proxied event is ignored.
        assertEvents(Constants.Event.CONNECTION_ALERTING,
        		Constants.Event.CONNECTION_CONNECTED);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "disconnected", the result will be that 
     * the Proxied event will be ignored.
     * @throws Exception 
     */
    public void testProxiedWhenDisconnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.DISCONNECTED, null);
        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "forced disconnected", the result will be that 
     * the Proxied event will be ignored.
     * @throws Exception 
     */
    public void testProxiedWhenForceDisconnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.FORCED_DISCONNECT, null);
        assertTrue(c.getState() == Connection.State.FORCED_DISCONNECT);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.FORCED_DISCONNECT);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "failed", the result will be that 
     * the state remains "failed" but "proxied" and "disconnected"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenFailed() throws Exception {
        Connection c = getConnectionInState(Connection.State.FAILED, null);
        assertTrue(c.getState() == Connection.State.FAILED);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_FAILED,
                Constants.Event.CONNECTION_PROXIED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Proxied event is received when the
     * connection is in state "error", the result will be that 
     * the state remains "error" but "proxied" and "disconnected"
     * is sent on the event hub.
     * @throws Exception 
     */
    public void testProxiedWhenError() throws Exception {
        Connection c = getConnectionInState(Connection.State.ERROR, null);
        assertTrue(c.getState() == Connection.State.ERROR);

        // At this state, send proxied event
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.ERROR_CONNECTION,
                Constants.Event.CONNECTION_PROXIED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Failed event and PlayFinished event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "failed"
     * are sent on the event hub.
     */
    public void testFailedPlayFinishedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send failed and playFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a PlayFinished event and a Failed event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "failed"
     * are sent on the event hub.
     */
    public void testPlayFinishedFailedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Failed event and PlayFinished event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "failed"
     * are sent on the event hub.
     */
    public void testFailedPlayFinishedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send failed and playFinished
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a PlayFinished and Failed event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "failed"
     * are sent on the event hub.
     */
    public void testPlayFinishedFailedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and failed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a PlayFinished event and a Disconnected event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testPlayFinishedDisconnectedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and disconnected

        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Disconnected and PlayFinished event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testDisconnectedPlayFinishedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send disconnected and playFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED_HANGUP,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Disconnected event and PlayFinished event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testDisconnectedPlayFinishedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send disconnected and playFinished
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED_HANGUP,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a PlayFinished and Disconnected event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testPlayFinishedDisconnectedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and disconnected
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a PlayFinished event and a Error event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "error"
     * are sent on the event hub.
     */
    public void testPlayFinishedErrorWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and error

        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Error and a PlayFinished event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.finished" and "error"
     * are sent on the event hub.
     */
    public void testErrorPlayFinishedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send error and playFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a PlayFinished and Error event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "error"
     * are sent on the event hub.
     */
    public void testPlayFinishedErrorWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFinished and error
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Error and PlayFinished event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.finished" and "error"
     * are sent on the event hub.
     */
    public void testErrorPlayFinishedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send error and playFinished
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Failed event and RecordFinished event is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "failed"
     * are sent on the event hub.
     */
    public void testFailedRecordFinishedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send failed and recordFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a RecordFinished and Failedevent is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "failed"
     * are sent on the event hub.
     */
    public void testRecordFinishedFailedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFinished and failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a RecordFinished and Disconnected is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testRecordFinishedDisconnectedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFinished and disconnected

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Disconnected and RecordFinished is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "disconnected"
     * are sent on the event hub.
     */
    public void testDisconnectedRecordFinishedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send disconnected and recordFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED_HANGUP,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Error and RecordFinished is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "error"
     * are sent on the event hub.
     */
    public void testErrorRecordFinishedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send Error and recordFinished

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a RecordFinished and Error is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.finished" and "error"
     * are sent on the event hub.
     */
    public void testRecordFinishedErrorWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFinished and Error

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FINISHED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Failed event and PlayFailed event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.failed" and "failed"
     * are sent on the event hub.
     */
    public void testFailedPlayFailedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send failed and playFailed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a PlayFailed event and a Failed event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.failed" and "failed"
     * are sent on the event hub.
     */
    public void testPlayFailedFailedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send playFailed and failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Failed event and PlayFailed event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "failed"
     * are sent on the event hub.
     */
    public void testFailedPlayFailedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send failed and playFailed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a PlayFailed and Failed event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "failed"
     * are sent on the event hub.
     */
    public void testPlayFailedFailedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFailed and failed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a Disconnected event and PlayFailed event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "disconnected"
     * are sent on the event hub.
     */
    public void testDisconnectedPlayFailedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send disconnected and playFailed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED_HANGUP,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a PlayFailed and Disconnected event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "disconnected"
     * are sent on the event hub.
     */
    public void testPlayFailedDisconnectedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFailed and disconnected
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a PlayFailed event and a Error event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.failed" and "error"
     * are sent on the event hub.
     */
    public void testPlayFailedErrorWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send playFailed and error

        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Error and a PlayFailed event is received when the
     * connection is in state "playing-connected", the result will be that expectedEvents "play.failed" and "error"
     * are sent on the event hub.
     */
    public void testErrorPlayFailedWhenPlayingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.PLAY);

        // At this state, send error and playFailed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.PLAY_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a PlayFailed and Error event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "error"
     * are sent on the event hub.
     */
    public void testPlayFailedErrorWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send playFailed and error
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Error and PlayFailed event is received when the
     * connection is in state "playing-alerting", the result will be that expectedEvents "play.failed" and "error"
     * are sent on the event hub.
     */
    public void testErrorPlayFailedWhenPlayingAlerting() throws Exception {
        Connection c = getConnectionInState(Connection.State.ALERTING, Connection.AutomatonEvent.PLAY);

        // At this state, send error and playFailed
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.PLAY_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a Failed event and RecordFailed event is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "failed"
     * are sent on the event hub.
     */
    public void testFailedRecordFailedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send failed and recordFailed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a RecordFailed and Failedevent is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "failed"
     * are sent on the event hub.
     */
    public void testRecordFailedFailedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFailed and failed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);

        assertTrue(c.getState() == Connection.State.FAILED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED,
                Constants.Event.CONNECTION_FAILED);
    }

    /**
     * Test that if a RecordFailed and Disconnected is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "disconnected"
     * are sent on the event hub.
     */
    public void testRecordFailedDisconnectedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFailed and disconnected

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Disconnected and RecordFailed is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "disconnected"
     * are sent on the event hub.
     */
    public void testDisconnectedRecordFailedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send disconnected and recordFailed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);

        assertTrue(c.getState() == Connection.State.DISCONNECTED);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED_HANGUP,
                Constants.Event.CONNECTION_DISCONNECTED);
    }

    /**
     * Test that if a Error and RecordFailed is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "error"
     * are sent on the event hub.
     */
    public void testErrorRecordFailedWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send Error and recordFailed

        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    /**
     * Test that if a RecordFailed and Error is received when the
     * connection is in state "recording-connected", the result will be that expectedEvents "record.failed" and "error"
     * are sent on the event hub.
     */
    public void testRecordFailedErrorWhenRecordingConnected() throws Exception {
        Connection c = getConnectionInState(Connection.State.CONNECTED, Connection.AutomatonEvent.RECORD);

        // At this state, send recordFailed and Error

        c.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED, null);
        c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);

        assertTrue(c.getState() == Connection.State.ERROR);

        // Check that events were sent in expected order
        assertEvents(Constants.Event.CONNECTION_ALERTING,
                Constants.Event.CONNECTION_CONNECTED,
                Constants.Event.RECORD_FAILED,
                Constants.Event.ERROR_CONNECTION);
    }

    private void assertEvents(String ... expectedEvents) {
        int i=0;
        for (String expectedEvent : expectedEvents) {
            String firedEvent = eventHubWithOrderCheck.getFiredEventNames().get(i);
            if(! expectedEvent.equals(firedEvent))
                fail("Expected "+ expectedEvent + ", was "+firedEvent);
            i++;

        }
        int numberOfFiredEvents = eventHubWithOrderCheck.getFiredEventNames().size();
        if(i != numberOfFiredEvents)
            fail("Expected size was "+i + " but fired size was "+numberOfFiredEvents);
    }

    private Connection getConnectionInState(Connection.State state,
                                            Connection.AutomatonEvent mediaAction) throws Exception {
        Call call = createCall(InboundCall.class);
        Connection c = new ConnectionImpl(getExecutionContext());
        c.setCall(call);
        switch(state){
        	case START:
        		break;
            case ALERTING:
                c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
                break;
            case PROGRESSING:
                c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
                expect_Call_proxy(call);
                c.proxy("172.123.45", 1111);
                c.receiveEvent(Connection.AutomatonEvent.EVENT_PROGRESSING, new ProgressingEvent(call, false));
            	break;
            case PROXYING:
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
                expect_Call_proxy(call);
                c.proxy("172.123.45", 1111);
                break;
            case CONNECTED:
                c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
                expect_Call_accept(call);
                c.accept();
                c.receiveEvent(Connection.AutomatonEvent.EVENT_CONNECTED, null);
                break;
            case DISCONNECTED:
                c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
                expect_Call_accept(call);
                c.accept();
                c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
            	break;
            case FAILED:
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, null);
            	break;
            case ERROR:
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_ERROR, null);
            	break;
            case FORCED_DISCONNECT:
            	c.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, null);
            	expect_Call_disconnect(call);
            	c.forcedDisconnect();
                c.receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, null);
            	break;
            default:
                fail("Unexpected state "+ state+ " in getConnectionInState");
                break;
        }
        if(mediaAction != null){

            switch(mediaAction){
                case PLAY:
                    c.play();
                    break;
                case RECORD:
                    c.record();
                    break;
                default:
                    fail("Unexpected mediaAction "+ mediaAction+ " in getConnectionInState");
                    break;
            }
        }
        return c;
    }

    private Event automatonEventToRelatedEvent(Connection.AutomatonEvent key) {

        switch(key){

            case EVENT_ALERTING:
                return new AlertingEvent(createCall(InboundCall.class));
            case EVENT_PROGRESSING:
                return new ProgressingEvent(createCall(OutboundCall.class), false);
            case EVENT_PROXIED:
            	return new ProxiedEvent(createCall(InboundCall.class));
            case EVENT_DISCONNECTED:
                return new DisconnectedEvent(createCall(InboundCall.class),
                        DisconnectedEvent.Reason.FAR_END,
                        false);
            case EVENT_FAILED:
                return new FailedEvent(createCall(InboundCall.class),
                        FailedEvent.Reason.FAR_END_ABANDONED,
                        CallDirection.INBOUND,
                        "just a string",
                        605);
            case EVENT_CONNECTED:
                return new ConnectedEvent(createCall(InboundCall.class));
            case EVENT_ERROR:
                return new ErrorEvent(createCall(InboundCall.class),
                        CallDirection.INBOUND,
                        "just a string",
                        false);
            case EVENT_RECORD_FINISHED:
                return new RecordFinishedEvent(new Object(), RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
            case EVENT_RECORD_FAILED:
                return new RecordFailedEvent(new Object(), RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, "just a string");
            case EVENT_PLAY_FINISHED:
                return new PlayFinishedEvent(new Object(), PlayFinishedEvent.CAUSE.PLAY_FINISHED, 100);
            case EVENT_PLAY_FAILED:
                return new PlayFailedEvent(new Object(), "ssss");
            case EVENT_SIGNAL:
            case CREATECALL:
            case ACCEPT:
            case PROXY:
            case REJECT:
            case REDIRECT:
            case MERGE:
            case DISCONNECT:
            case RECORD:
            case UNRECORD:
            case PLAY:
            case UNPLAY:
            case EARLY_MEDIA_AVAILABLE:
                return null;

            default:
                fail("default swicth in test case reached; update test case");
                return null;
        }
    }
}