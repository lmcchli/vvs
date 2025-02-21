package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * Copyright 2007 Mobeon AB
 * Date: 2007-apr-23
 *
 * @author mmath
 */
public class PChargingVectorTest extends InboundSipUnitCase {

    private PChargingVectorHeader pcv1;
    private final String REMOTE_ICID = "abc123";
    private final String REMOTE_GENERATED_AT = "remoteGenAtHost";
    private final String REMOTE_ORIG_IOI = "remoteOrigIoiHost";


    public void setUp() throws Exception {
        super.setUp();
        pcv1 = ((HeaderFactoryImpl)simulatedPhone.getHeaderFactory()).
                        createPChargingVectorHeader(REMOTE_ICID);
        pcv1.setICIDGeneratedAt(REMOTE_GENERATED_AT);
        pcv1.setOriginatingIOI(REMOTE_ORIG_IOI);
    }

    /**
     * Verifies a normal inbound call with P-Charging-Vector present
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithPChargingVector() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);


        invite.addHeader(pcv1);

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        Response response;
        PChargingVectorHeader pcv;
        // Wait for Ringing response
        response = simulatedPhone.assertResponseReceived(Response.RINGING);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);

        assertCorrectRemotePCV(pcv);

        // Wait for OK response
        response = simulatedPhone.assertResponseReceived(Response.OK);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies a normal inbound call without P-Charging-Vector present
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithoutPChargingVector() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        Response response;
        PChargingVectorHeader pcv;
        // Wait for Ringing response
        response = simulatedPhone.assertResponseReceived(Response.RINGING);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertNotNull(pcv);
        String icid = pcv.getICID();
        assertNotNull(icid);
        assertEquals(CMUtils.getInstance().getLocalHost(), pcv.getTerminatingIOI());
        assertEquals(CMUtils.getInstance().getLocalHost(), pcv.getICIDGeneratedAt());
        assertNull(pcv.getOriginatingIOI());

        // Wait for OK response
        response = simulatedPhone.assertResponseReceived(Response.OK);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertEquals(icid, pcv.getICID());
        assertEquals(CMUtils.getInstance().getLocalHost(), pcv.getTerminatingIOI());
        assertEquals(CMUtils.getInstance().getLocalHost(), pcv.getICIDGeneratedAt());
        assertNull(pcv.getOriginatingIOI());

        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies a normal inbound call with P-Charging-Vector present.
     * System hangs up.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithPChargingVectorNearEndBye() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        invite.addHeader(pcv1);

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        Response response;
        PChargingVectorHeader pcv;
        // Wait for Ringing response
        response = simulatedPhone.assertResponseReceived(Response.RINGING);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        // Wait for OK response
        response = simulatedPhone.assertResponseReceived(Response.OK);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // System disconnects the call
        simulatedSystem.disconnect();

        // Wait for BYE request
        RequestEvent requestEvt =
                simulatedPhone.assertRequestReceived(Request.BYE,false,false);

        sendOkForBye(requestEvt,true);

        pcv = (PChargingVectorHeader)
                requestEvt.getRequest().getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        assertCallIsDisconnected(false, NEAR_END, true);

        assertDroppedPacketsStatistics(10);
    }

    /**
     * Verifies an error response for inbound call with P-Charging-Vector present.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithPChargingVectorErrorResponse() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        // Add unsupported Content-Encoding header
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createContentEncodingHeader("gzip"));

        invite.addHeader(pcv1);

        simulatedPhone.sendInvite(invite);

        // Wait for Bad Extension response
        Response response = simulatedPhone.assertResponseReceived(
                Response.UNSUPPORTED_MEDIA_TYPE);

        PChargingVectorHeader pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);

        assertCorrectRemotePCV(pcv);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);

    }

    /**
     * Verifies a normal inbound call with P-Charging-Vector present.
     * System initiates video fast update.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithPChargingVectorVFU() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);


        invite.addHeader(pcv1);

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        Response response;
        PChargingVectorHeader pcv;
        // Wait for Ringing response
        response = simulatedPhone.assertResponseReceived(Response.RINGING);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);

        assertCorrectRemotePCV(pcv);

        // Wait for OK response
        response = simulatedPhone.assertResponseReceived(Response.OK);
        pcv = (PChargingVectorHeader)
                response.getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Send video fast update
        simulatedSystem.initiateVideoFastUpdate();

        // Wait for INFO request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.INFO, false, true);

        // Send OK response
        response = simulatedPhone.createResponse(requestEvent, 200);
        simulatedPhone.sendResponse(requestEvent, response);

        // Make sure the VFU contains the correct P-Charging-Vector
        pcv = (PChargingVectorHeader)
                requestEvent.getRequest().getHeader(PChargingVectorHeader.NAME);
        assertCorrectRemotePCV(pcv);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertDroppedPacketsStatistics(10);
    }




    private void assertCorrectRemotePCV(PChargingVectorHeader pcv) {
        assertEquals(REMOTE_ICID, pcv.getICID());
        assertEquals(REMOTE_ORIG_IOI, pcv.getOriginatingIOI());
        assertEquals(REMOTE_GENERATED_AT, pcv.getICIDGeneratedAt());
        assertEquals(CMUtils.getInstance().getLocalHost(), pcv.getTerminatingIOI());
    }


}
