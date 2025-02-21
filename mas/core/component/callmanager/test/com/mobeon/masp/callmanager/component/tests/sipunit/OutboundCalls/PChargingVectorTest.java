package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * Call Manager component test case to verify creating outbound calls.
 * @author Malin Flodin
 */
public class PChargingVectorTest extends OutboundSipUnitCase {


    public void setUp() throws Exception {
        super.setUp();

        MediaMimeTypes outboundMediaTypes = new MediaMimeTypes();
        outboundMediaTypes.addMimeType(new MimeType("audio/pcmu"));
    }


    /**
     * Verifies that a normal outbound call setup contains the P-Charging-Vector
     * @throws Exception when the test case fails.
     */
    public void testPChargingVectorOutboundCall() throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        // Wait for INVITE request
        RequestEvent reqEvt = simulatedPhone.assertRequestReceived(Request.INVITE, true, false);
        PChargingVectorHeader pcvHdr = (PChargingVectorHeader)reqEvt.getRequest().
                getHeader(PChargingVectorHeader.NAME);
        assertNotNull(pcvHdr);
        assertNotNull(pcvHdr.getICID());
        assertTrue(pcvHdr.getICID().length() > 0);
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getOriginatingIOI());
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getICIDGeneratedAt());
        assertNull(pcvHdr.getTerminatingIOI());

        // Wait for the state to be set to Progressing
        simulatedSystem.waitForState(ProgressingOutboundState.class);

        // Verify that the call dispatcher is updated with a new initiated call
        assertDispatchedCalls(1, 0);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        simulatedSystem.disconnect();

        reqEvt = simulatedPhone.assertRequestReceived(Request.BYE, false, false);

        sendOkForBye(reqEvt,true);

        pcvHdr = (PChargingVectorHeader)reqEvt.getRequest().
                getHeader(PChargingVectorHeader.NAME);
        assertNotNull(pcvHdr);
        assertNotNull(pcvHdr.getICID());
        assertTrue(pcvHdr.getICID().length() > 0);
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getOriginatingIOI());
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getICIDGeneratedAt());
        assertNull(pcvHdr.getTerminatingIOI());

        assertCallIsDisconnected(false, NEAR_END, true);
    }

}
