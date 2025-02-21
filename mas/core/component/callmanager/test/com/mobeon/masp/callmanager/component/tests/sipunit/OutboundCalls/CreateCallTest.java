/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedCompletedOutboundState;

import javax.sip.message.Response;
import jakarta.activation.MimeType;


/**
 * Call Manager component test case to verify creating outbound calls.
 * @author Malin Flodin
 */
public class CreateCallTest extends OutboundSipUnitCase {

    private String videoBody;
    private CallMediaTypes[] videoCallMediaTypesArray;

    public void setUp() throws Exception {
        super.setUp();

        String timestamp = "0";
        videoBody =
                    "v=0\r\n"
                    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
                    + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
                    + "s=MAS prompt session\r\n"
                    + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
                    + "t=0 0\r\n"
                    + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 0 101\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:101 telephone-event/8000\r\n"
                    + "a=fmtp:101 0-15\r\n"
                    + "a=ptime:40\r\n"
                    + "m=video " + (simulatedPhone.getRTPPort()+2) + " RTP/AVP 34\r\n";

        MediaMimeTypes outboundMediaTypes = new MediaMimeTypes();
        outboundMediaTypes.addMimeType(new MimeType("audio/pcmu"));
        outboundMediaTypes.addMimeType(new MimeType("video/h263"));
        videoCallMediaTypesArray =
                new CallMediaTypes[]{new CallMediaTypes(outboundMediaTypes, this)};
    }

    /**
     * Verifies that a normal inbound call setup when remote party is
     * at least one SSP instance.
     * @throws Exception when the test case fails.
     */
    public void testNormalOutboundCallSetupWithSsp() throws Exception {
        assertConfigurationContainsSsp();

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends redirection
        simulatedPhone.sendRedirect(
                Response.MOVED_TEMPORARILY, simulatedSecondPhone.getLocalContact());
        assertCallCreated(simulatedSecondPhone, true);

        // Second phone sends ok
        simulatedSecondPhone.acceptCall(null);
        assertCallAccepted(simulatedSecondPhone);

        // Phone disconnects the call
        simulatedSecondPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a normal inbound call setup when remote party is not an
     * SSP.
     * @throws Exception when the test case fails.
     */
    public void testNormalOutboundCallSetupWithoutSsp() throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a normal inbound call setup when remote party is
     * an outbound callserver. The configuration contains no SSP instance.
     * @throws Exception when the test case fails.
     */
    public void testNormalOutboundCallSetupWithOutboundCallServer() throws Exception {

        assertConfigurationContainsNoSsp();
        callProperties.setOutboundCallServerHost(simulatedPhone.getHost());
        callProperties.setOutboundCallServerPort(simulatedPhone.getPhonePort());

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that there is an error if the port in CallProperties is -1.
     * The configuration contains no SSP instance.
     * @throws Exception when the test case fails.
     */
    public void testNormalOutboundCallSetupWithNegativeOutboundCallServerPort() throws Exception {

        assertConfigurationContainsNoSsp();
        ConfigurationReader.getInstance().getConfig().
                setOutboundCallServerPort(simulatedPhone.getPhonePort());

        callProperties.setOutboundCallServerHost(simulatedPhone.getHost());
        callProperties.setOutboundCallServerPort(-1);

        // System creates an outbound call.
        boolean caughtException = false;
        try {
            simulatedSystem.createCall(callProperties);
        } catch (IllegalArgumentException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);
    }

    /**
     * Verifies that default port will be used if the port in CallProperties is not set.
     * The configuration contains no SSP instance.
     * @throws Exception when the test case fails.
     */
    public void testNormalOutboundCallSetupWithDefaultOutboundCallServerPort() throws Exception {

        assertConfigurationContainsNoSsp();
        ConfigurationReader.getInstance().getConfig().
                setOutboundCallServerPort(simulatedPhone.getPhonePort());

        callProperties.setOutboundCallServerHost(simulatedPhone.getHost());

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a redirection of an outbound call to the same contact as
     * the original outbound call results in a
     * {@link com.mobeon.masp.callmanager.events.FailedEvent} and that the
     * state is set to
     * {@link DisconnectedCompletedOutboundState}.
     * @throws Exception when the test case fails.
     */
    public void testRedirectionToSameContactAsBefore() throws Exception {
        // System creates outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends redirection to itself
        simulatedPhone.sendRedirect(
                Response.MOVED_TEMPORARILY, simulatedPhone.getLocalContact());

        assertCallRejected(Response.MOVED_TEMPORARILY);
    }

    /**
     * Verifies that a redirection of an already redirected outbound call
     * results in a
     * {@link com.mobeon.masp.callmanager.events.FailedEvent} and that the
     * state is set to
     * {@link DisconnectedCompletedOutboundState}.
     * This test case is only valid if remote party is one or more SSP instances.
     * @throws Exception when the test case fails.
     */
    public void testMultipleLevelOfRedirection() throws Exception {
        assertConfigurationContainsSsp();

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends redirection
        simulatedPhone.sendRedirect(
                Response.MOVED_TEMPORARILY, simulatedSecondPhone.getLocalContact());
        assertCallCreated(simulatedSecondPhone, true);

        // Second phone redirects also
        simulatedSecondPhone.sendRedirect(
                Response.MOVED_PERMANENTLY, "sip:5555@mobeon.com");

        assertCallRejected(Response.MOVED_PERMANENTLY);
    }

    /**
     * Verifies that if multiple Ringing responses are received, one
     * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} is generated for
     * each event.
     * @throws Exception when the test case fails.
     */
    public void testMultipleProgressing() throws Exception {
        // System creates a call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ringing again
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that an OK response with invalid SDP answer results in an
     * Error event and a BYE request.
     * @throws Exception when the test case fails.
     */
    public void testResponseWithInvalidSdpAnswer() throws Exception {
        gotoProgressingProceedingState();

        simulatedPhone.acceptCall("y=body\r\n");
        assertCallAcknowledged(simulatedPhone);
        assertCallDisconnect(true, false, true);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(1);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an OK response with no SDP answer results in a
     * Failed event and a BYE request.
     * @throws Exception when the test case fails.
     */
    public void testResponseWithNoSdpAnswer() throws Exception {
        gotoProgressingCallingState();

        simulatedPhone.acceptCall();
        assertCallAcknowledged(simulatedPhone);

        assertCallDisconnect(true, false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an OK response with no intersection between the SDP offer
     * and SDP answer results in a Failed event and a BYE request.
     * @throws Exception when the test case fails.
     */
    public void testNoSdpIntersection() throws Exception {
        gotoProgressingProceedingState();

        // Phone sends ok
        simulatedPhone.acceptCall("c=IN IP4 127.0.0.1\r\n"
                        + "m=video 1234 RTP/AVP 0 101\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n"
                        + "a=rtpmap:101 telephone-event/8000\r\n");
        assertCallAcknowledged(simulatedPhone);
        assertCallDisconnect(true, false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that if we try to setup a Video call, but the resulting call is
     * a voice call, a Failed event is generated and a BYE request sent.
     * @throws Exception when the test case fails.
     */
    public void testVideoCallResultingInVoiceCall() throws Exception {

        callProperties.setCallType(CallProperties.CallType.VIDEO);

        gotoProgressingCallingState();

        simulatedPhone.acceptCall(null);
        assertCallAcknowledged(simulatedPhone);

        assertCallDisconnect(true, false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that if we try to setup a Video call, but the resulting call is
     * a voice call, a Failed event is generated and a BYE request sent.
     * @throws Exception when the test case fails.
     */
    public void test5xxResponseToInvite() throws Exception {

        gotoProgressingCallingState();

        // Phone sends 5xx response
        simulatedPhone.sendResponse(599);

        assertCallRejected(599);
    }

    /**
     * Verifies that if we try to setup a Video call, but the resulting call is
     * a voice call, a Failed event is generated and a BYE request sent.
     * This test case is only valid if remote party is one or more SSP instances.
     * @throws Exception when the test case fails.
     */
    public void test5xxResponseToRedirectedInvite() throws Exception {

        assertConfigurationContainsSsp();

        gotoProgressingCallingState();

        // Phone sends redirection
        simulatedPhone.sendRedirect(
                Response.MOVED_TEMPORARILY, simulatedSecondPhone.getLocalContact());
        assertCallCreated(simulatedSecondPhone, true);

        // Phone sends 5xx response
        simulatedSecondPhone.sendResponse(599);

        assertCallRejected(599);
    }

    /**
     * Verifies that an {@link ErrorEvent} is generated if call properties is
     * null.
     * @throws Exception if test case failed.
     */
    public void testCreateCallWhenCallPropertiesIsNull() throws Exception
    {
        // System creates outbound call.
        try {
            simulatedSystem.createCall(null);
            fail("IllegalArgumentException should be thrown if CallProperties is null.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected. Caught " + e.getClass());
        }

        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(1);
    }

    /**
     * Verifies that an {@link ErrorEvent} is generated if call properties is
     * empty.
     * @throws Exception if test case failed.
     */
    public void testCreateCallWhenCallPropertiesIsEmpty() throws Exception
    {
        // System creates outbound call.
        try {
            simulatedSystem.createCall(new CallProperties());
            fail("IllegalArgumentException should be throws if CallProperties is empty.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected. Caught " + e.getClass());
        }

        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(1);
    }

    /**
     * Verifies that an {@link ErrorEvent} is generated
     * if call properties contain mandatory elements that are null.
     * @throws Exception if test case failed.
     */
    public void testCreateCallWhenCallPropertiesHasEmptyCalledParty() throws Exception
    {
        // Call properties contain empty called party
        CallProperties callProperties = new CallProperties();
        callProperties.setCalledParty(new CalledParty());

        // System creates outbound call.
        try {
            simulatedSystem.createCall(callProperties);
            fail("IllegalArgumentException should be throws if CallProperties is empty.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected. Caught " + e.getClass());
        }

        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(1);
    }

    /**
     * Verify that a call is created if call properties has no calling party
     * only if the outboundcallcallingparty configuration is set.
     * @throws Exception if test case fails.
     */
    public void testCreateCallWhenCallPropertiesHasNoCallingParty() throws Exception {
        // Call properties contain no calling party
        callProperties.setCallingParty(null);

        // System tries to create outbound call.
        // No calling party is configured, so createCall fails.
        try {
            simulatedSystem.createCall(callProperties);
            fail("IllegalArgumentException expected if CallingParty is null and not configured.");
        } catch (IllegalArgumentException e) {
            //OK
            simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        } catch (Exception e) {
            fail("IllegalArgumentException expected. Caught " + e.getClass());
        }

        // Calling party set in config.
        ConfigurationReader.getInstance().getConfig().setOutboundCallCallingParty("012345678");

        // System creates outbound call.
        // createCall will use configured calling party.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Verify statistics
        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(0);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verify that a call is created when call properties has an empty calling party
     * only if the outboundcallcallingparty configuration is set.
     * @throws Exception if test case fails.
     */
    public void testCreateCallWhenCallPropertiesHasEmptyCallingParty() throws Exception {
        // Call properties contain empty calling party
        callProperties.setCallingParty(new CallingParty());

        // System tries to create outbound call.
        // No calling party is configured, so createCall fails.
        try {
            simulatedSystem.createCall(callProperties);
            fail("IllegalArgumentException expected if CallingParty is null and not configured.");
        } catch (IllegalArgumentException e) {
            //OK
            simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        } catch (Exception e) {
            fail("IllegalArgumentException expected. Caught " + e.getClass());
        }

        // Calling party set in config.
        ConfigurationReader.getInstance().getConfig().setOutboundCallCallingParty("012345678");

        // System creates outbound call.
        // createCall will use configured calling party.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Verify statistics
        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(0);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verify that a call is created if call properties has no timeout.
     * @throws Exception if the test case fails.
     */
    public void testCreateCallWhenCallPropertiesHasNoTimeout() throws Exception {
        // Call properties contain calling and called party
        // but no timeout.
        callProperties.setMaxDurationBeforeConnected(0);

        // System creates outbound call.
        // createCall uses configured timeout.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Verify statistics
        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(0);
        assertErrorCallStatistics(0);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }


    /**
     * Verifies that an outbound video call fails if the SDP answer contains
     * voice.
     * @throws Exception when the test case fails.
     */
    public void testVideoCallWhenAnswerContainsVoice() throws Exception {
        callProperties.setCallType(CallProperties.CallType.VIDEO);
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY,
                videoCallMediaTypesArray);

        gotoProgressingProceedingState();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAcknowledged(simulatedPhone);
        assertCallDisconnect(true, false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an outbound voice call fails if the SDP answer contains
     * video.
     * @throws Exception when the test case fails.
     */
    public void testVoiceCallWhenAnswerContainsVideo() throws Exception {
        callProperties.setCallType(CallProperties.CallType.VOICE);
        gotoProgressingProceedingState();

        // Phone sends ok
        simulatedPhone.acceptCall(videoBody);
        assertCallAcknowledged(simulatedPhone);
        assertCallDisconnect(true, false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an outbound call during max load is rejected.
     * The call results in state {@link FailedCompletedOutboundState} and a
     * {@link FailedEvent} is sent.
     * @throws Exception when the test case fails.
     */
    public void testOutboundCallDuringMaxLoad() throws Exception {
        LoadRegulator loadRegulator =
                CMUtils.getInstance().getCmController().getLoadRegulator();

        loadRegulator.updateThreshold(2, 1, 3);
        loadRegulator.addCall("Call 1");
        loadRegulator.addCall("Call 2");
        loadRegulator.addCall("Call 3");

        assertEquals(3, loadRegulator.getCurrentCalls());

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        assertEquals(3, loadRegulator.getCurrentCalls());

        // Assert that FailedEvent is received.
        simulatedSystem.assertEventReceived(FailedEvent.class, null);

        loadRegulator.removeCall("Call 1");
        loadRegulator.removeCall("Call 2");
        loadRegulator.removeCall("Call 3");

        Thread.sleep(100);
        assertEquals(0, loadRegulator.getCurrentCalls());

        simulatedSystem.waitForState(FailedCompletedOutboundState.class);

        simulatedSystem.disconnect();
        simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
    }

    /**
     * Verifies that if a call is rejected with a SIP response containing a
     * Reason header field, the network status code is chosen basen on the
     * Reason header field and not the SIP response code.
     * @throws Exception when the test case fails.
     */
    public void testRejectInviteWithReason() throws Exception {

        gotoProgressingCallingState();

        // Phone sends 5xx response
        Response response = simulatedPhone.createResponse(501);
        response.addHeader(CMUtils.getInstance().getSipHeaderFactory().
                createQ850ReasonHeader(17, 0));
        simulatedPhone.sendResponse(response);

        assertFailedEventReceived(FailedEvent.Reason.REJECTED_BY_FAR_END, 614);

        // Wait for the state to be set to Failed
        simulatedSystem.waitForState(FailedCompletedOutboundState.class);

    }

}
