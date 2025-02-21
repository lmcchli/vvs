/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import gov.nist.javax.sip.address.SipUri;

import javax.sip.message.Request;
import javax.sip.message.Response;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.ProxiedEvent;

/**
 * Call Manager component test case to verify proxy of inbound calls.
 */
public class ProxyTest extends InboundSipUnitCase {

    /**
     * Verifies that proxying an inbound call results in forwarding SIP INVITE and PROXYING state.
     * @throws Exception when the test case fails.
     */
    public void testProxyInvite() throws Exception {

        ConfigurationReader.getInstance().getConfig().setApplicationProxyMode(true);

        // SIP INVITE creation (from simulatedPhone)
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,
                PhoneSimulator.WITH_BODY, null, false, false, false);
        SipUri su = (SipUri)invite.getRequestURI();
        su.setUser("sipUasPhone");
        invite.setRequestURI(su);

        // SIP INVITE sent (from simulatedPhone to simulatedUasPhone, via PROXY) 
        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // VMP proxies the call (by Execution Engine)
        simulatedSystem.proxy(new RemotePartyAddress("localhost", 5080));

        // Wait for forwarded SIP INVITE request received from simulatedUasPhone
        simulatedUasPhone.assertRequestReceived(Request.INVITE, true, false);

        Thread.sleep(1000);
    }

    /**
     * Complete PROXY scenario 
     * @throws Exception
     */
    public void testProxyComplete() throws Exception {

        ConfigurationReader.getInstance().getConfig().setApplicationProxyMode(true);

        // SIP INVITE creation (from simulatedPhone)
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,
                PhoneSimulator.WITH_BODY, null, false, false, false);
        SipUri su = (SipUri)invite.getRequestURI();
        su.setUser("sipUasPhone");
        invite.setRequestURI(su);

        // SIP INVITE sent (from simulatedPhone to simulatedUasPhone, via PROXY) 
        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // VMP proxies the call (by Execution Engine)
        simulatedSystem.proxy(new RemotePartyAddress("localhost", 5080));

        // Wait for forwarded SIP INVITE request received from simulatedUasPhone
        simulatedUasPhone.assertRequestReceived(Request.INVITE, true, false);

        // SIP TRYING response sent (from simulatedUas Phone to simulatedPhone, via PROXY) 
        simulatedUasPhone.trying();

        // SIP RINGING response sent (from simulatedUasPhone to simulatedPhone, via PROXY) 
        simulatedUasPhone.ring();

        // Wait for a Progressing event
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);

        // SIP RINGING response received by simulatedPhone 
        simulatedPhone.assertResponseReceived(Response.RINGING);

        // SIP 200OK response sent (from simulatedUas Phone to simulatedPhone, via PROXY) 
        simulatedUasPhone.sendResponse(Response.OK);

        // Wait for a Proxied event
        simulatedSystem.assertEventReceived(ProxiedEvent.class, null);

        // SIP 200 OK response received by simulatedPhone 
        simulatedPhone.assertResponseReceived(Response.OK);
        
        Thread.sleep(1000);

        // SIP 200OK response sent again (from simulatedUas Phone to simulatedPhone, via PROXY)
        // This scenario triggers a different path in CallManager since the call has been removed
        // in the previous 200OK message
        simulatedUasPhone.sendResponse(Response.OK);

        Thread.sleep(1000);
    }

    /**
     * Test the PROXY in case of receiving a PRACK request (from UAC to UAC).
     * Commented out since the current JAIN-Sip stack version does not handle this case.
     */
/*
    public void testProxyPrack() throws Exception {

        ConfigurationReader.getInstance().getConfig().setApplicationProxyMode(true);

        // SIP INVITE creation (from simulatedPhone)
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,
                PhoneSimulator.WITH_BODY, null, false, false, false);
        SipUri su = (SipUri)invite.getRequestURI();
        su.setUser("sipUasPhone");
        //su.setHost("127.0.0.1");
        ViaHeader viaHeader = (ViaHeader)invite.getHeader(ViaHeader.NAME);
        viaHeader.setHost("127.0.0.1");
        invite.setHeader(viaHeader);

        System.out.println("SipUri : " + su);
        invite.setRequestURI(su);

        // SIP INVITE sent (from simulatedPhone to simulatedUasPhone, via PROXY) 
        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // VMP proxies the call (by Execution Engine)
        simulatedSystem.proxy(new RemotePartyAddress("127.0.0.1", 5080));

        // Wait for forwarded SIP INVITE request received from simulatedUasPhone
        simulatedUasPhone.assertRequestReceived(Request.INVITE, true, false);

        // SIP TRYING response sent (from simulatedUas Phone to simulatedPhone, via PROXY) 
        simulatedUasPhone.trying();

        // SIP RINGING response sent (from simulatedUasPhone to simulatedPhone, via PROXY) 
        simulatedUasPhone.ring();

        // Wait for a Progressing event
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);

        // SIP RINGING response received by simulatedPhone 
        Response ringingResponse = simulatedPhone.assertResponseReceived(Response.RINGING);

        // SIP PRACK request sent to sipUasPhone 
        simulatedPhone.sendPrack(ringingResponse);

        // SIP 200OK response sent (from simulatedUas Phone to simulatedPhone, via PROXY) 
        simulatedUasPhone.sendResponse(Response.OK);

        // Wait for a Proxied event
        simulatedSystem.assertEventReceived(ProxiedEvent.class, null);

        // SIP 200 OK response received by simulatedPhone 
        simulatedPhone.assertResponseReceived(Response.OK);
        
        Thread.sleep(1000);
    }
*/
}
