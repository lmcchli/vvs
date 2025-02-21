/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import java.net.InetSocketAddress;

import org.jmock.Mock;
import org.jmock.core.stub.VoidStub;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.NegotiateEarlyMediaTypesEvent;
import com.mobeon.masp.callmanager.callhandling.events.ProxyEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.states.StateCase;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.ReliableResponseUsage;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.StackException;

/**
 * Base class for state tests. Implements common methods for inbound callstate
 * tests.
 *
 * @author Malin Flodin
 */
abstract public class InboundStateCase extends StateCase {

    // Mocked object specific for inbound calls

    // The mocked inbound call
    Mock mockInboundCall;
    
    Mock mockedIOutboundMediaStream = mock(IOutboundMediaStream.class);
    Mock mockedIInboundMediaStream = mock(IInboundMediaStream.class);
    

    // Client events for inbound calls
    AcceptEvent acceptEvent;
    NegotiateEarlyMediaTypesEvent negotiateEarlyMediaTypesEvent;
    RejectEvent rejectEvent;
    DisconnectEvent disconnectEvent;
    ProxyEvent proxyEvent;

    CallTimeoutEvent callNotAcceptedTimeoutEvent =
            new CallTimeoutEvent(CallTimerTask.Type.CALL_NOT_ACCEPTED);
    CallTimeoutEvent expiresTimeoutEvent =
            new CallTimeoutEvent(CallTimerTask.Type.EXPIRES);
    CallTimeoutEvent anotherTimeoutEvent =
            new CallTimeoutEvent(CallTimerTask.Type.CALL_NOT_CONNECTED);

    protected String toTag = "1234567890";


    protected void setUp() throws Exception {
        super.setUp();

        mockInboundCall = mock(InboundCallInternal.class);

        setupMockedClientEvents();

        setupExpectations();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    void assertServiceLoaded(boolean failed) {
        if (failed) {
            mockInboundCall.expects(once()).method("loadService").
                    will(throwException(new NullPointerException("Error")));
        } else {
            mockInboundCall.expects(once()).method("loadService");
        }
    }

    void assertPlay(Mock mockedCall) {
        mockedCall.expects(once()).method("logPlayTime");
        mockedCall.expects(once()).method("playOnOutboundStream");
    }

    void assertStreamCreation(
            boolean inboundFailed, boolean outboundFailed) throws Exception {
        if (inboundFailed) {
            mockInboundCall.expects(once()).method("createInboundStream").
                    will(throwException(new StackException("Error")));
        } else {
            mockInboundCall.expects(once()).method("createInboundStream").
                    will(returnValue(inboundConnProps));

            if (outboundFailed) {
                mockInboundCall.expects(once()).method("createOutboundStream").
                        with(eq(sdpIntersection)).
                        will(throwException(new StackException("Error")));
            } else {
                mockInboundCall.expects(once()).method("createOutboundStream").
                        with(eq(sdpIntersection));
            }
        }
    }

    void assertInboundStreamCreation(boolean inboundFailed) throws Exception {
        if (inboundFailed) {
            mockInboundCall.expects(once()).method("createInboundStream").
                    will(throwException(new StackException("Error")));
        } else {
            mockInboundCall.expects(once()).method("createInboundStream").
                    will(returnValue(inboundConnProps));
        }
    }

    void assertOutboundStreamCreation(boolean outboundFailed) throws Exception {
        if (outboundFailed) {
            mockInboundCall.expects(once()).method("createOutboundStream").
            with(eq(sdpIntersection)).
            will(throwException(new StackException("Error")));
        } else {
            mockInboundCall.expects(once()).method("createOutboundStream").
            with(eq(sdpIntersection));
        }
    }

    void assertSdpAnswerCreated(boolean failed, String sdpAnswer) {
        mockInboundCall.stubs().method("getInboundConnectionProperties").
            will(returnValue(inboundConnProps));
    
        if (failed) {
            mockInboundCall.expects(once()).method("createSdpAnswer").
                    with(eq(sdpIntersection), eq(inboundConnProps)).
                    will(throwException(new SdpInternalErrorException("Error")));
        } else {
            mockInboundCall.expects(once()).method("createSdpAnswer").
                    with(eq(sdpIntersection), eq(inboundConnProps)).
                    will(returnValue(sdpAnswer));
        }
    }

    void assertNotAcceptedTimerStarted() {
        mockInboundCall.expects(once()).method("startNotAcceptedTimer");
    }

    void assertExpiresTimerStarted() {
        mockInboundCall.expects(once()).method("startExpiresTimer");
    }

    void assertRedirectedRtpActivated() {
        mockInboundCall.expects(once()).method("isSupportForRedirectedRtpActivated").
        will(returnValue(true));
    }
    
    void assertRemoteSdpOfferExists() {
        mockInboundCall.expects(once()).method("getRemoteSdp").
                will(returnValue(mockSessionDescription.proxy()));
    }

    void assertNoRemoteSdpOfferExists() {
        mockInboundCall.expects(once()).method("getRemoteSdp").
                will(returnValue(null));
    }

    void assertNoReliableResponses() {
        mockInboundCall.expects(once()).method("useReliableProvisionalResponses").
                will(returnValue(ReliableResponseUsage.NO));
    }

    void assertReliableResponses() {
        mockInboundCall.expects(once()).method("useReliableProvisionalResponses").
                will(returnValue(ReliableResponseUsage.YES));
    }

    void assertReliableResponsesForSdp() {
        mockInboundCall.expects(once()).method("useReliableProvisionalResponses").
                will(returnValue(ReliableResponseUsage.SDPONLY));
    }

    void assertCallTypeRetrievedFromConfiguration() {
        mockInboundCall.expects(once()).method("getCallType").will(
                returnValue(CallProperties.CallType.UNKNOWN));
        mockInboundCall.stubs().method("retrieveCallTypeFromConfiguration");
    }

    void assertStateAlerting(
            AlertingInboundState.AlertingSubState substate) {
        mockInboundCall.expects(once()).method("setStateAlerting").
                with(eq(substate));
    }
    void assertSetUas() {
        mockInboundCall.expects(once()).method("setUas");
    }
    void assertGetUas() {
        mockInboundCall.expects(atLeastOnce()).method("getUas").will(returnValue(proxyEvent.getUas()));
    }
    void assertGetInitialSipRequestEvent() {
        mockInboundCall.expects(once()).method("getInitialSipRequestEvent").will(returnValue(initialSipRequestEvent));
    }
    void assertStateConnected() {
        mockInboundCall.expects(once()).method("setStateConnected");
    }

    void assertStateDisconnected(
            DisconnectedInboundState.DisconnectedSubState substate) {
        mockInboundCall.expects(once()).method("setStateDisconnected").
                with(eq(substate));
    }

    void assertStateError(ErrorInboundState.ErrorSubState substate) {
        mockInboundCall.expects(once()).method("setStateError");
    }

    void assertStateFailed(FailedInboundState.FailedSubState substate) {
        mockInboundCall.expects(once()).method("setStateFailed").
                with(eq(substate));
    }

    private void setupMockedClientEvents() {
        acceptEvent = new AcceptEvent();
        negotiateEarlyMediaTypesEvent = new NegotiateEarlyMediaTypesEvent();
        rejectEvent = new RejectEvent();
        disconnectEvent = new DisconnectEvent();
        proxyEvent = new ProxyEvent(new RemotePartyAddress("1.2.3.4", 5061));
    }

    private void setupExpectations() throws Exception {
        mockInboundCall.stubs().method("getDialog").will(returnValue(null));
        mockInboundCall.stubs().method("getCalledParty").
                will(returnValue(null));
        mockInboundCall.stubs().method("getCallType").
                will(returnValue(CallProperties.CallType.VOICE));
        mockInboundCall.stubs().method("getInitialSipRequestEvent").
                will( returnValue(initialSipRequestEvent) );
        mockInboundCall.stubs().method("getOutboundCallMediaTypes").
                will(returnValue(null));
        mockInboundCall.stubs().method("getConfig").will(returnValue(
                ConfigurationReader.getInstance().getConfig()));
        mockInboundCall.stubs().method("getConfig").will(returnValue(
                ConfigurationReader.getInstance().getConfig()));
        mockInboundCall.stubs().method("getPChargingVector").
                will(returnValue(null));
        mockInboundCall.stubs().method("isSupportForRedirectedRtpActivated").
                will(returnValue(false));
    }
    
    protected void assertWithPictureFastUpdate() throws Exception {
        mockInboundCall.stubs().method("getOutboundStream").
			will(returnValue(mockedIOutboundMediaStream.proxy()));
        mockedIOutboundMediaStream.stubs().method("usesRTCPPictureFastUpdate").
			will(returnValue(true));
        mockInboundCall.stubs().method("getInboundStream").
			will(returnValue(mockedIInboundMediaStream.proxy()));
        mockedIInboundMediaStream.stubs().method("getSenderSSRC").
			will(returnValue(129));
        mockedIOutboundMediaStream.stubs().method("sendPictureFastUpdate");
    }

    protected void assertWithoutPictureFastUpdate() throws Exception {
        mockInboundCall.stubs().method("getOutboundStream").
			will(returnValue(mockedIOutboundMediaStream.proxy()));
        mockedIOutboundMediaStream.stubs().method("usesRTCPPictureFastUpdate").
			will(returnValue(false));
    }

}
