/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.states.StateCase;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.SendTokenEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.DialEvent;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import org.jmock.Mock;

import javax.sip.address.SipURI;
import javax.sip.SipException;
import javax.sip.ClientTransaction;
import java.text.ParseException;

/**
 * Base class for state tests. Implements common methods for outbound callstate
 * tests.
 *
 * @author Malin Flodin
 */
abstract public class OutboundStateCase extends StateCase {
    // Mocked object specific for inbound calls

    // The mocked outbound call
    Mock mockOutboundCall;

    Mock mockedSipURI = mock(SipURI.class);
    Mock mockedAlternativeSipURI = mock(SipURI.class);
    Mock mockedIOutboundMediaStream = mock(IOutboundMediaStream.class);
    Mock mockedIInboundMediaStream = mock(IInboundMediaStream.class);

    // DTMF
    private ControlToken[] dtmf;

    protected static final Boolean LINGERING_CANCEL = true;
    protected static final Boolean LINGERING_BYE = false;

    // Events for outbound calls
    DialEvent dialEvent;
    SendTokenEvent sendTokenEvent;
    DisconnectEvent disconnectEvent;
    CallTimeoutEvent maxCallDurationTimeoutEvent;
    CallTimeoutEvent maxDurationBeforeConnectedTimeoutEvent;
    CallTimeoutEvent noResponseTimeoutEvent;

    protected CallProperties callProperties = new CallProperties();


    protected void setUp() throws Exception {
        super.setUp();

        mockOutboundCall = mock(OutboundCallInternal.class);
        mockOutboundCall.stubs().method("getCallId");
        mockOutboundCall.stubs().method("getNewCallId");
        mockOutboundCall.stubs().method("getCurrentInviteTransaction").
                will(returnValue(null));
        callProperties.setCallType(CallProperties.CallType.VOICE);

        setupMockedEvents();
        setupExpectations();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void assertHandleTimeoutEvent() {
        mockOutboundCall.expects(once()).method("handleTimeoutEvent");
    }

    void assertRemotePartyBlackListed() {
        SipURI uri = (SipURI)mockedSipURI.proxy();
        assertTrue(CMUtils.getInstance().getRemotePartyController().
                isRemotePartyBlackListed(uri.getHost() + ":" + uri.getPort()));
    }

    void assertPlay(Mock mockedCall) {
        mockedCall.expects(once()).method("playOnOutboundStream");
    }

    void assertInviteSent(boolean createFailed, boolean sendFailed)
            throws Exception {
        mockOutboundCall.expects(once()).method("getCalledParty").
                will(returnValue(new CalledParty()));
        mockOutboundCall.expects(once()).method("getCallingParty").
                will(returnValue(new CallingParty()));
        mockOutboundCall.stubs().method("getCallProperties").
                will(returnValue(callProperties));

        if (createFailed) {
            mockSipRequestFactory.expects(once()).method("createInviteRequest").
                    will(throwException(new ParseException("Error", 0)));
        } else {
            mockSipRequestFactory.expects(once()).method("createInviteRequest").
                    will(returnValue(null));
            mockOutboundCall.expects(once()).method("dialogCreated");
            if (sendFailed) {
                mockSipMessageSender.expects(once()).method("sendRequest").
                        will(throwException(new SipException()));
            } else {
                Mock mockTransaction = mock(ClientTransaction.class);
                mockTransaction.stubs().method("getDialog").will(returnValue(null));
                mockSipMessageSender.expects(once()).method("sendRequest")
                        .will(returnValue(mockTransaction.proxy()));
                mockOutboundCall.expects(once()).method("setDialog");
                mockOutboundCall.expects(once()).method("setCurrentInviteTransaction");
            }
        }
    }

    void assertMaxDurationBeforeConnectedTimerStarted() {
        mockOutboundCall.expects(once()).method("startNotConnectedTimer");
    }

    protected void assertRequestFailureReported() {
        mockOutboundCall.expects(once()).method("reportRequestFailure").
                with(ANYTHING, ANYTHING);
    }

    protected void assertResponseFailureReported()
            throws Exception {
        mockOutboundCall.expects(once()).method("reportResponseFailure");
    }

    protected void assertUnreliableResponse() {
        mockOutboundCall.expects(once()).method("isProvisionalResponseReliable").
                will(returnValue(false));
    }

    protected void assertReliableResponse() {
        mockOutboundCall.expects(once()).method("isProvisionalResponseReliable").
                will(returnValue(true));
    }

    void assertNewInviteSent(Boolean createdFailed, Boolean sendFailed) {
        mockOutboundCall.expects(once()).method("getInitialSipRequest").
                will(returnValue(null));
        if (createdFailed) {
            mockSipRequestFactory.expects(once()).method("createNewInviteRequest").
                    will(throwException(new ParseException("Error", 0)));
        } else {
            mockSipRequestFactory.expects(once()).method("createNewInviteRequest").
                    will(returnValue(null));
            mockOutboundCall.expects(once()).method("dialogCreated");

            if (sendFailed) {
                mockSipMessageSender.expects(once()).method("sendRequest").
                        will(throwException(new SipException()));
            } else {
                Mock mockTransaction = mock(ClientTransaction.class);
                mockTransaction.stubs().method("getDialog").will(returnValue(null));
                mockSipMessageSender.expects(once()).method("sendRequest")
                        .will(returnValue(mockTransaction.proxy()));
                mockOutboundCall.expects(once()).method("setDialog");
                mockOutboundCall.expects(once()).method("setCurrentInviteTransaction");
            }
        }
    }

    void assertTokenSent() {
        mockOutboundCall.expects(once()).method("sendTokens");
    }

    void assertStateConnected() {
        mockOutboundCall.expects(once()).method("setStateConnected");
    }

    void assertStateDisconnected(
            DisconnectedOutboundState.DisconnectedSubState substate) {
        mockOutboundCall.expects(once()).method("setStateDisconnected").
                with(eq(substate));
    }

    void assertStateError(ErrorOutboundState.ErrorSubState substate) {
        mockOutboundCall.expects(once()).method("setStateError").
                with(eq(substate));
    }

    void assertStateFailed(FailedOutboundState.FailedSubState substate) {
        mockOutboundCall.expects(once()).method("setStateFailed").
                with(eq(substate));
    }

    void assertStateProgressing(
            ProgressingOutboundState.ProgressingSubState substate) {
        mockOutboundCall.expects(once()).method("setStateProgressing").
                with(eq(substate));
    }

    void assertCallAlreadyRedirected() {
        mockOutboundCall.expects(once()).method("isRedirected").
                will(returnValue(true));
    }

    void assertCallNotAlreadyRedirected() {
        mockOutboundCall.expects(once()).method("isRedirected").
                will(returnValue(false));
    }

    void assertRedirectionAllowed() {
        mockOutboundCall.expects(once()).method("isRedirectionAllowed").
                will(returnValue(true));
    }

    void assertRedirectionNotAllowed() {
        mockOutboundCall.expects(once()).method("isRedirectionAllowed").
                will(returnValue(false));
    }

    void assertNextContact(SipURI contact, boolean willFail) {
        if (willFail)
            mockOutboundCall.expects(once()).method("getNewRemoteParty").
                    will(throwException(new ParseException("Error", 0)));
        else
            mockOutboundCall.expects(once()).method("getNewRemoteParty").
                    will(returnValue(contact));
    }

    void assertContactsRetrieved() {
        mockOutboundCall.expects(once()).method("retrieveContacts");
    }

    private void setupMockedEvents() {
        disconnectEvent = new DisconnectEvent();
        dialEvent = new DialEvent();
        sendTokenEvent = new SendTokenEvent(dtmf);
        maxCallDurationTimeoutEvent =
                new CallTimeoutEvent(CallTimerTask.Type.MAX_CALL_DURATION);
        maxDurationBeforeConnectedTimeoutEvent =
                new CallTimeoutEvent(CallTimerTask.Type.CALL_NOT_CONNECTED);
        noResponseTimeoutEvent =
                new CallTimeoutEvent(CallTimerTask.Type.NO_RESPONSE);
    }

    private void setupExpectations() throws Exception {
        mockOutboundCall.stubs().method("getDialog").will(returnValue(null));
        mockOutboundCall.stubs().method("getCallingParty").
                will(returnValue(new CallingParty()));
        mockOutboundCall.stubs().method("getCallProperties").
                will(returnValue(callProperties));
        mockOutboundCall.stubs().method("getOutboundCallMediaTypes").
                will(returnValue(null));
        mockOutboundCall.stubs().method("getConfiguredOutboundCallMediaTypes").
                will(returnValue(null));
        mockOutboundCall.stubs().method("getConfig").will(returnValue(
                ConfigurationReader.getInstance().getConfig()));
        mockedSipURI.stubs().method("getHost").will(returnValue("host"));
        mockedSipURI.stubs().method("getPort").will(returnValue(1234));
        mockedAlternativeSipURI.stubs().method("getHost").will(returnValue("host2"));
        mockedAlternativeSipURI.stubs().method("getPort").will(returnValue(1234));
        mockOutboundCall.stubs().method("getCurrentRemoteParty").
                will(returnValue(mockedSipURI.proxy()));
        mockOutboundCall.stubs().method("getPChargingVector").
                will(returnValue(null));
    }
    
    protected void assertWithPictureFastUpdate() throws Exception {
        mockOutboundCall.stubs().method("getOutboundStream").
			will(returnValue(mockedIOutboundMediaStream.proxy()));
        mockedIOutboundMediaStream.stubs().method("usesRTCPPictureFastUpdate").
			will(returnValue(true));
        mockOutboundCall.stubs().method("getInboundStream").
			will(returnValue(mockedIInboundMediaStream.proxy()));
        mockedIInboundMediaStream.stubs().method("getSenderSSRC").
			will(returnValue(129));
        mockedIOutboundMediaStream.stubs().method("sendPictureFastUpdate");
    }

    protected void assertWithoutPictureFastUpdate() throws Exception {
        mockOutboundCall.stubs().method("getOutboundStream").
			will(returnValue(mockedIOutboundMediaStream.proxy()));
        mockedIOutboundMediaStream.stubs().method("usesRTCPPictureFastUpdate").
			will(returnValue(false));
    }

    protected void assertGetOutboundStream() throws Exception {
        mockOutboundCall.expects(once()).method("getOutboundStream").
        will(returnValue(mockedIOutboundMediaStream.proxy()));
    }
    
}
