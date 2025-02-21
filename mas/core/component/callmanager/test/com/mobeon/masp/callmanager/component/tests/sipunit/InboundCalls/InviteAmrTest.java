/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;


import gov.nist.javax.sip.message.SIPResponse;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorCompletedInboundState;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.NumberCompletion;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.stream.RTPPayload;

import javax.sip.message.Response;
import javax.sip.message.Request;
import jakarta.activation.MimeType;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;


/**
 * Very messy. Should be cleaned up by restructuring the base classes to
 * make it more flexible in terms of using different flavors of codec's/SDP's
 */

/**
 * Call Manager component test case to verify INVITEs trying to create
 * inbound calls.
 *
 * @author Malin Nyfeldt
 */
public class InviteAmrTest extends InboundSipUnitCase {
    /** A logger instance. */
    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private String videoBody;
    private String voiceBody;
    private String voiceBodyFmtpError;
    private CallMediaTypes[] videoCallMediaTypesArray;
    private CallMediaTypes[] voiceCallMediaTypesArray;

    public void setUp() throws Exception {

	// Override the default config file to use.
	super.callManagerConfigFile = CallManagerTestContants.CALLMANAGER_AMR_XML;
	super.setUp();

	String timestamp = "0";
	videoBody =
	    "v=0\r\n"
	    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
	    + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "s=MAS prompt session\r\n"
	    + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "t=0 0\r\n"
	    + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 101\r\n"
	    + "b=AS:13\r\n"
	    + "a=rtpmap:96 AMR/8000\r\n"
	    + "a=rtpmap:101 telephone-event/8000\r\n"
	    + "a=fmtp:101 0-15\r\n"
	    + "a=fmtp:96 octet-align=1;mode-set=7\r\n"
	    + "a=ptime:40\r\n"
	    + "m=video " + (simulatedPhone.getRTPPort()+2) + " RTP/AVP 34\r\n"
	    + "b=AS:52\r\n"
	    + "a=rtpmap:34 H263/90000\r\n";

	voiceBody =
	    "v=0\r\n"
	    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
	    + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "s=MAS prompt session\r\n"
	    + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "t=0 0\r\n"
	    + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 101\r\n"
	    + "b=AS:13\r\n"
	    + "a=rtpmap:96 AMR/8000\r\n"
	    + "a=rtpmap:101 telephone-event/8000\r\n"
	    + "a=fmtp:96 mode-set=7; octet-align=1\r\n"
	    + "a=ptime:20\r\n"
	    + "a=maxptime:40\r\n";

	voiceBodyFmtpError =
	    "v=0\r\n"
	    + "o=" + "userXXX" + " " + timestamp + " " + timestamp
	    + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "s=MAS prompt session\r\n"
	    + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
	    + "t=0 0\r\n"
	    + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 101\r\n"
	    + "b=AS:13\r\n"
	    + "a=rtpmap:96 AMR/8000\r\n"
	    + "a=rtpmap:101 telephone-event/8000\r\n"
	    + "a=fmtp:96 mode-set=7; octet-align=1; robust-sorting=1\r\n"
	    + "a=ptime:20\r\n"
	    + "a=maxptime:40\r\n";

	MediaMimeTypes outboundVideoMediaTypes = new MediaMimeTypes();
	outboundVideoMediaTypes.addMimeType(new MimeType("audio/amr"));
	outboundVideoMediaTypes.addMimeType(new MimeType("video/h263"));
	videoCallMediaTypesArray =
	    new CallMediaTypes[]{new CallMediaTypes(outboundVideoMediaTypes, this)};


        MediaMimeTypes outboundVoiceMediaTypes = new MediaMimeTypes();
        outboundVoiceMediaTypes.addMimeType(new MimeType("audio/amr"));
        voiceCallMediaTypesArray =
                new CallMediaTypes[]{new CallMediaTypes(outboundVoiceMediaTypes, this)};

        // By default set session mock to return audio/amr as call media types array
        // (The default is otherwise audio/pcmu which is set in "clearSessionData" in SessionMock)
        simulatedSystem.setSessionData(
        	SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, voiceCallMediaTypesArray);


}



    private void assertNumberCompletion(NumberCompletion completion) {
        NumberCompletion numberCompletion = simulatedSystem.getActiveCall().
                getCallingParty().getNumberCompletion();
        if(numberCompletion != completion){
            fail("numberCompletion was not "+completion +" as expected: was "+numberCompletion);
        }
    }

        private boolean match(String input, String regex) {
	Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE);
	return pattern.matcher(input).find();
    }

    /**
     * Verifies that an INVITE with unsupported extensions results in a SIP
     * Bad Extension response.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnsupportedExtensions() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        // Add Require header with unsupported extension
        invite.addHeader(
                simulatedPhone.getHeaderFactory().
                        createRequireHeader("unsupportedExtension"));

        simulatedPhone.sendInvite(invite);

        // Wait for Bad Extension response
        simulatedPhone.assertResponseReceived(Response.BAD_EXTENSION);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that a timeout occurring after a call has been rejected due
     * to internal error can be handled and is ignored.
     * @throws Exception when the test case fails.
     */
    public void testInviteWhenStreamsCannotBeCreatedAndASipTimeoutIsReceived()
            throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // Delete the phones so the call will not be answered
        simulatedPhone.delete();
        simulatedSecondPhone.delete();

        // System tries to accept the call but the inbound stream could not
        // be created.
        simulatedSystem.createInboundStreamException();
        simulatedSystem.accept();

        // Wait for the error event and the state to be set to Error
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        simulatedSystem.waitForState(ErrorInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(0, 0);

        // Sleep a while so a SIP timeout can be received.
        Thread.sleep(3000);

    }

    /**
     * Verifies that an INVITE with unsupported Content-Encoding results in a
     * SIP Unsupported Media Type response with a Accept-Encoding header.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnsupportedContentEncoding() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        // Add unsupported Content-Encoding header
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createContentEncodingHeader("gzip"));

        simulatedPhone.sendInvite(invite);

        // Wait for Bad Extension response
        Response response = simulatedPhone.assertResponseReceived(
                Response.UNSUPPORTED_MEDIA_TYPE);

        // Verify that the response contains an Accept-Encoding header
        simulatedPhone.assertAcceptEncodingHeader(response, "identity");

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE with unsupported Content-Type results in a
     * SIP Unsupported Media Type response with a Accept header.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnsupportedContentType() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add unsupported ContentType header
        simulatedPhone.addBody(invite, "illegal", "type", null, false, "body".getBytes());

        simulatedPhone.sendInvite(invite);

        // Wait for Bad Extension response
        Response response = simulatedPhone.assertResponseReceived(
                Response.UNSUPPORTED_MEDIA_TYPE);

        // Verify that the response contains an Accept header
        simulatedPhone.assertAcceptHeader(response, "application", "sdp");
        simulatedPhone.assertAcceptHeader(response, "application", "media_control+xml");

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE with unsupported charset in the Content-Type
     * header field results in a
     * SIP Unsupported Media Type response with a Accept header.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnsupportedCharSet() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add unsupported charset in ContentType header
        simulatedPhone.addBody(invite, "illegal", "type",
                "UTF-16", false, videoBody.getBytes());

        simulatedPhone.sendInvite(invite);

        // Wait for Bad Extension response
        Response response = simulatedPhone.assertResponseReceived(
                Response.UNSUPPORTED_MEDIA_TYPE);

        // Verify that the response contains an Accept header
        simulatedPhone.assertAcceptHeader(response, "application", "sdp");
        simulatedPhone.assertAcceptHeader(response, "application", "media_control+xml");

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE with a supported "UTF-8" charset in the
     * Content-Type header field results in the call being accepted.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithSupportedCharSet() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add video SDP body
        simulatedPhone.addBody(invite, "application", "sdp",
                "UTF-8", false, videoBody.getBytes());
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call with null media Mime types.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, null);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, false, true);
    }

    /**
     * Verifies that an INVITE with invalid SDP offer results in a
     * SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithInvalidSdpOffer() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add invalid SDP body
        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, "y=body\r\n".getBytes());

        simulatedPhone.sendInvite(invite);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE without SDP offer results in a
     * call being established using default call type from configuration.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithoutSdpOffer() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();
        // Wait for Ringing response
        simulatedPhone.assertResponseReceived(Response.RINGING);

        // Wait for OK response
        SIPResponse response = (SIPResponse)simulatedPhone.assertResponseReceived(Response.OK);

        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 101$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7; octet-align=1; robust-sorting=0$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);
//        assertCallAccepting(RINGING);


        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, voiceBody.getBytes(), false);
        assertCallConnected();

        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }





    /**
     * Verifies that an INVITE with too low bandwidth in SDP offer results in a
     * SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnsupportedBandwidthInSdpOffer() throws Exception {

        // Setup the RTP payload types that shall be available during the
        // test case. The bandwidth for PCMU is set to lower than 64 kbits/s
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                96, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 14000, null));
        rtppayloads.add(new RTPPayload(
                101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Wait for Ringing response
        simulatedPhone.assertResponseReceived(Response.RINGING);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE without SDP offer results in a
     * call being established using the call type given in a Call-Info header.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithoutSdpOfferWithCallTypeFromCallInfo() throws Exception {
        // System accepts the call with media Mime types indicating VIDEO.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY,
                videoCallMediaTypesArray);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addCallInfoType(invite, CallProperties.CallType.VIDEO);
        simulatedPhone.sendInvite(invite);

        // System accepts the call with null media Mime types.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, null);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        Request ack = simulatedPhone.createRequest(
                Request.ACK, PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, null, false, false);
        simulatedPhone.addBody(ack, "application", "sdp",
                null, false, videoBody.getBytes());
        simulatedPhone.sendAck(ack);
        assertCallConnected();

        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, false, true);
    }

    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testNormalInboundCallSetup() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 101$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7; octet-align=1$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }


    /**
     * Verifies that an inbound call setup that contain unsupported
     * fmtp parameters for the AMR media gets rejected.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithInvalidFmtp() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyFmtpError.getBytes());

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }


    /**
     * This test case verifies that the call parameters are retrieved when an
     * INVITE is received.
     * @throws Exception when the test case fails.
     */
    public void testRetrievalOfCallParameters() throws Exception
    {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        assertCalledParty();
        assertCallingParty();
        assertRedirectingParty();

        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(0);


        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, false, true);
    }


    /**
     * Verifies that an INVITE indicating a video call will not be accepted if
     * the Call Manager client only whishes to use voice for Call Media Types.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithVideoButCallMediaTypesVoice() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add video SDP body
        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, videoBody.getBytes());
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        simulatedSystem.accept();

        simulatedPhone.assertResponseReceived(Response.RINGING);

        // Wait for "Not Acceptable Here" response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        // Wait for the failed event and the state to be set to FailedCompleted
        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE indicating a video call will not be accepted if
     * the Call Manager client only wishes to use voice for Call Media Types.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithVoiceButCallMediaTypesVideo() throws Exception {

        // System accepts the call with media Mime types indicating VIDEO.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY,
                videoCallMediaTypesArray);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        simulatedSystem.accept();

        simulatedPhone.assertResponseReceived(Response.RINGING);

        // Wait for "Not Acceptable Here" response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        // Wait for the failed event and the state to be set to FailedCompleted
        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an inbound video call can be setup.
     * @throws Exception when the test case fails.
     */
    public void testVideoCallSetup() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        // Add video SDP body
        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, videoBody.getBytes());
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call with null media Mime types.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, null);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, false, true);
    }

    /**
     * Verifies that an INVITE without SDP offer results in a
     * call being established.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithUnknownButOptionalBody() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);

        // Add unknown but optional body
        simulatedPhone.addBody(invite, "application", "xxx",
                null, true, "xxx".getBytes());
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);


        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, voiceBody.getBytes(), false);
        assertCallConnected();

        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, false, true);
    }

    /**
     * Verifies that an INVITE without SDP offer and an ACK with an illegal
     * Content-Type in the SDP answer results in the call being disconnected
     * due to lacking SDP answer.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithoutSdpOfferAndInvalidContentTypeInAck() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        Request ack = simulatedPhone.createRequest(
                Request.ACK, PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, null, false, false);
        simulatedPhone.addBody(ack, "application", "xxx",
                null, false, "xxx".getBytes());
        simulatedPhone.sendAck(ack);

        assertCallDisconnect(true, false);
    }

    /**
     * Verifies that an INVITE with an Expires time that is not accepted within
     * the expires time is rejected with a SIP Request Terminated response.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithExpiresTimerThatExpires() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.addExpiresHeader(invite, 2);
        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // Instead of accepting the call, wait until the expires timer expired.
        Thread.sleep(2500);

        // Wait for Request Terminated response
        simulatedPhone.assertResponseReceived(Response.REQUEST_TERMINATED);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that when an ACK is not received in the within a certain
     * configured time for an inbound call, the call is considered
     * disconnected. An error event is generated and the state is set to
     * error completed..
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInviteWhenNoAckOrSipTimeoutIsReceived() throws Exception {
        ConfigurationReader.getInstance().getConfig().setCallNotAcceptedTimer(1000);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,
                PhoneSimulator.NO_BODY, null, false, false);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBody.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        Thread.sleep(1200);

        // Wait for the state to be set to ErrorCompleted
        simulatedSystem.waitForState(ErrorCompletedInboundState.class);
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);
    }


    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testVfeNormalInboundCallSetupWithMultiAMRCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String timestamp = "0";


        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "-" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=-\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 98 99\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=0,2,4,7;mode-change-period=2;mode-change-neighbor=1;octet-align=1\r\n"
            + "a=rtpmap:98 AMR/8000\r\n"
            + "a=fmtp:98 mode-set=7;octet-align=1\r\n"
            + "a=rtpmap:99 telephone-event/8000\r\n"
            + "a=fmtp:99 0-15\r\n"
            + "a=maxtime:40\r\n";


        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 98 99$"));
        assertTrue(match(body,"^a=fmtp:98 mode-set=7;octet-align=1$"));

        assertTrue(match(body,"^a=fmtp:99 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testVfe2NormalInboundCallSetupWithMultiAMRCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String timestamp = "0";


        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "-" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=-\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 98 99\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=7;octet-align=1\r\n"
            + "a=rtpmap:98 AMR/8000\r\n"
            + "a=fmtp:98 mode-set=0,2,4,7;mode-change-period=2;mode-change-neighbor=1;octet-align=1\r\n"
            + "a=rtpmap:99 telephone-event/8000\r\n"
            + "a=fmtp:99 0-15\r\n"
            + "a=maxtime:40\r\n";

        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 99$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7;octet-align=1$"));

        assertTrue(match(body,"^a=fmtp:99 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testVfe3NormalInboundCallSetupWithSingleAMRCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String timestamp = "0";


        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "-" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=-\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 97\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=7;mode-change-period=2;mode-change-neighbor=1;octet-align=1\r\n"
            + "a=rtpmap:97 telephone-event/8000\r\n"
            + "a=fmtp:97 0-15\r\n"
            + "a=maxtime:40\r\n";


        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 97$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7;mode-change-period=2;mode-change-neighbor=1;octet-align=1$"));

        assertTrue(match(body,"^a=fmtp:97 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }



    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testVfe4NormalInboundCallSetupWithSingleAMRCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String timestamp = "0";


        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "-" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=-\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 97\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=7;octet-align=1\r\n"
            + "a=rtpmap:97 telephone-event/8000\r\n"
            + "a=fmtp:97 0-15\r\n"
            + "a=maxtime:40\r\n";


        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 97$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7;octet-align=1$"));

        assertTrue(match(body,"^a=fmtp:97 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }
    /**
     * Verifies that a normal inbound call setup.
     * It also tests the case where local mode-set is different from remote mode-set, local mode-set must be returned.
     * @throws Exception when the test case fails.
     */
    public void testVfeNormalInboundCallSetupWithPartialMathctAMRCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String timestamp = "0";


        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "userXXX" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=MAS prompt session\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 99\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=0,2,4,7;mode-change-period=2;mode-change-neighbor=1;octet-align=1\r\n"
            + "a=rtpmap:99 telephone-event/8000\r\n"
            + "a=fmtp:99 0-15\r\n"
            + "a=maxtime:40\r\n";


        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 99$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7;mode-change-period=2;mode-change-neighbor=1;octet-align=1$"));
        assertTrue(match(body,"^a=fmtp:99 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }


    /**
     * Mode-set test case where local mode-set is present and no remote mode-set specified (i.e all supported)
     * The local mode-set must be returned in the SDP answer.
     * @throws Exception when the test case fails.
     */
    public void testVfeInboundCallSetupWithLocalModeSetWithoutRemoteModeSet() throws Exception {

        // Setup the RTP payload types that shall be available during the test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(98, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

        String voiceBodyVfe1 =
            "v=0\r\n"
            + "o=" + "userXXX 0 0 IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=MAS prompt session\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 99\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-change-period=2;mode-change-neighbor=1;octet-align=1\r\n"
            + "a=rtpmap:99 telephone-event/8000\r\n"
            + "a=fmtp:99 0-15\r\n"
            + "a=maxtime:40\r\n";

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodyVfe1.getBytes());

        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();
        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 99$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7;mode-change-period=2;mode-change-neighbor=1;octet-align=1$"));
        assertTrue(match(body,"^a=fmtp:99 0-15$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));
        assertTrue(match(body,"^a=maxptime:30"));

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallSetupWithMultipleCodec() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

      simulatedSystem.setSessionData(
      SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY,
      videoCallMediaTypesArray);

        String timestamp = "0";


        String voiceBodytest =
            "v=0\r\n"
            + "o=" + "userXXX" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=MAS prompt session\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 111 0 8 101\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
            + "a=rtpmap:96 AMR/8000\r\n"
            + "a=fmtp:96 mode-set=7; octet-align=1; robust-sorting=0\r\n"
            + "a=rtcp:5125\r\n"
            + "a=maxptime:20\r\n"
            + "a=ptime:20\r\n"
            + "a=rtpmap:111 amr-wb/16000\r\n"
            + "a=fmtp:111 mode-set=0; octet-align=1; robust-sorting=0\r\n"
            + "a=rtpmap:0 pcmu/8000\r\n"
            + "a=rtpmap:8 pcma/8000\r\n"
            + "a=rtpmap:101 telephone-event/8000\r\n"
            + "a=fmtp:101 0-15,36\r\n"
            + "m=video " + (simulatedPhone.getRTPPort()+2) + " RTP/AVP 34\r\n"
            + "a=rtpmap:34 H263/90000\r\n"
            + "a=fmtp:34 QCIF=2\r\n"
            + "a=sendrecv\r\n"
            + "a=rtcp:5127\r\n";



        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                96, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodytest.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();
        assertNumberCompletion(NumberCompletion.UNKNOWN);

        // System accepts the call.
        simulatedSystem.accept();

        SIPResponse response = (SIPResponse)assertCallAccepting(RINGING);
        log.info("Response received:\n" + response.toString());
        String body = response.getMessageContent();


        assertTrue(match(body,"^m=audio (\\d+) RTP/AVP 96 101$"));
        assertTrue(match(body,"^a=fmtp:96 mode-set=7; octet-align=1; robust-sorting=0$"));
        assertTrue(match(body,"^m=video (\\d+) RTP/AVP 34 101$"));
        assertTrue(match(body,"^b=AS:13"));
        assertTrue(match(body,"^a=ptime:15"));    // These come from InboundMediaStreamMock
        assertTrue(match(body,"^a=maxptime:30")); //


        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }
    /**
     * Verifies that a normal inbound call setup.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallSetupWithMultipleCodecMissingAMRRtpMap() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);

      simulatedSystem.setSessionData(
      SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY,
      videoCallMediaTypesArray);

        String timestamp = "0";


        String voiceBodytest =
            "v=0\r\n"
            + "o=" + "userXXX" + " " + timestamp + " " + timestamp + " IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "s=MAS prompt session\r\n"
            + "t=0 0\r\n"
            + "a=sendrecv\r\n"
            + "c=IN IP4 " + simulatedPhone.getHost() + "\r\n"
            + "m=audio " + simulatedPhone.getRTPPort() + " RTP/AVP 96 111 0 8 101\r\n"
            + "b=RR:0\r\n"
            + "b=RS:0\r\n"
      //      + "a=rtpmap:96 AMR/8000\r\n"
      //      + "a=fmtp:96 mode-set=7; octet-align=1; robust-sorting=0\r\n"
            + "a=rtcp:5125\r\n"
            + "a=maxptime:20\r\n"
            + "a=ptime:20\r\n"
            + "a=rtpmap:111 amr-wb/16000\r\n"
            + "a=fmtp:111 mode-set=0; octet-align=1; robust-sorting=0\r\n"
            + "a=rtpmap:0 pcmu/8000\r\n"
            + "a=rtpmap:8 pcma/8000\r\n"
            + "a=rtpmap:101 telephone-event/8000\r\n"
            + "a=fmtp:101 0-15,36\r\n"
            + "m=video " + (simulatedPhone.getRTPPort()+2) + " RTP/AVP 34\r\n"
            + "a=rtpmap:34 H263/90000\r\n"
            + "a=fmtp:34 QCIF=2\r\n"
            + "a=sendrecv\r\n"
            + "a=rtcp:5127\r\n";



        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                96, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                99, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        simulatedPhone.addBody(invite, "application", "sdp",
                null, false, voiceBodytest.getBytes());


        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        simulatedSystem.accept();

        simulatedPhone.assertResponseReceived(Response.RINGING);

        // Wait for "Not Acceptable Here" response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        // Wait for the failed event and the state to be set to FailedCompleted
        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);

    }





}
