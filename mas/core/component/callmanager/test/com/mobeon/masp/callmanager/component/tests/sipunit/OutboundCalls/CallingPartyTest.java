/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import jakarta.activation.MimeType;
import javax.sip.RequestEvent;
import javax.sip.header.Header;
import javax.sip.message.Request;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

/**
 * Call Manager component test case to verify how calling party information is
 * set depending on which headers that are restricted to use for outbound calls.
 * @author Malin Nyfeldt
 */
public class CallingPartyTest extends OutboundSipUnitCase {

    public void setUp() throws Exception {
        super.setUp();

        MediaMimeTypes outboundMediaTypes = new MediaMimeTypes();
        outboundMediaTypes.addMimeType(new MimeType("audio/pcmu"));
    }

    /**
     * Verifies that a normal outbound call contains the Remote-Party-ID and
     * P-Asserted-Identity headers when no output headers are restricted.
     * @throws Exception when the test case fails.
     */
    public void testCallingPartyOutboundCallWhenNoHeadersAreRestricted()
            throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        // Wait for INVITE request
        RequestEvent reqEvt = simulatedPhone.assertRequestReceived(
                Request.INVITE, true, false);

        // Verify that a P-Asserted-Identity header is included
        PAssertedIdentityHeader pAssIdHeader =
                (PAssertedIdentityHeader)reqEvt.getRequest().getHeader(
                        PAssertedIdentityHeader.NAME);
        assertNotNull(pAssIdHeader);

        // Verify that a Remote-Party-ID header is included
        Header remotePartyIdHeader =
                reqEvt.getRequest().getHeader("Remote-Party-ID");
        assertNotNull(remotePartyIdHeader);


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
        assertCallIsDisconnected(false, NEAR_END, true);
    }

    /**
     * Verifies that a normal outbound call contains no Remote-Party-ID or
     * P-Asserted-Identity headers when all possible output headers are
     * restricted.
     * @throws Exception when the test case fails.
     */
    public void testCallingPartyOutboundCallWhenAllHeadersAreRestricted()
            throws Exception {
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Setup configuration
        ConfigurationManagerImpl configMgr = new ConfigurationManagerImpl();
        configMgr.setConfigFile(CallManagerTestContants.CALLMANAGER_WITH_REMOTEUA_XML);
        ConfigurationReader.getInstance().setInitialConfiguration(
                configMgr.getConfiguration());
        ConfigurationReader.getInstance().update();

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        // Wait for INVITE request
        RequestEvent reqEvt =
                simulatedPhone.assertRequestReceived(Request.INVITE, true, false);

        // Verify that a P-Asserted-Identity header is NOT included
        PAssertedIdentityHeader pAssIdHeader =
                (PAssertedIdentityHeader)reqEvt.getRequest().getHeader(
                        PAssertedIdentityHeader.NAME);
        assertNull(pAssIdHeader);

        // Verify that a Remote-Party-ID header is NOT included
        Header remotePartyIdHeader =
                reqEvt.getRequest().getHeader("Remote-Party-ID");
        assertNull(remotePartyIdHeader);


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
        assertCallIsDisconnected(false, NEAR_END, true);
    }



}
