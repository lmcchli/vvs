/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedOutboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorOutboundState.ErrorSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedOutboundState.FailedSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState.ProgressingSubState;

import javax.sip.ClientTransaction;
import javax.sip.address.SipURI;
import java.text.ParseException;

/**
 * This class is used to interface the OutboundCallImpl class.
 * <p>
 * It is used for test purposes only, i.e. to be able to mock the outbound call
 * during basic test.
 *
 * @author Malin Flodin
 */
public interface OutboundCallInternal extends OutboundCall, CallInternal {

    // Configuration for the call
    public CallManagerConfiguration getConfig();

    // Set outbound state
    public void setStateConnected();
    public void setStateDisconnected(DisconnectedSubState substate);
    public void setStateError(ErrorSubState substate);
    public void setStateFailed(FailedSubState substate);
    public void setStateProgressing(ProgressingSubState substate);

    // Outbound call redirection
    public SipURI getCurrentRemoteParty();
    public SipURI getNewRemoteParty() throws ParseException;
    public boolean isRedirected();

    /**
     * This method returns true if redirection is allowed to be handled for the
     * call, or if the call should be considered completed when a 3xx response
     * is received.
     * One-level of redirection is allowed to be handled if there are one or
     * more SSP instances configured as remote party. Otherwise, redirections
     * are not allowed.
      * @return True if redirection is allowed to be handled. False otherwise.
     */
    public boolean isRedirectionAllowed();
    
    /**
     * This method is used to find out if a SIP response was sent reliably or
     * not.
     *
     * @return  True is returned if the response was sent reliably.
     *          False otherwise.
     */
    public boolean isProvisionalResponseReliable(SipResponseEvent sipResponseEvent);

    public void retrieveContacts(SipResponseEvent sipResponseEvent);

    // Outbound call properties
    public CallProperties getCallProperties();
    public String getCallId();

    // Outbound call timers
    public void startNotConnectedTimer();
    public void startNoResponseTimer();
    public void startCallNotConnectedExtensionTimer();
    public void cancelNoResponseTimer();
    public void cancelCallNotConnectedExtensionTimer();

    // SIP related
    public SipRequest getInitialSipRequest();

    // TODO: Phase 2! Can the need for these be removed?
    public void dialogCreated(SipRequest sipRequest);
    public void setCurrentInviteTransaction(ClientTransaction transaction);
    public ClientTransaction getCurrentInviteTransaction();

    public String getNewCallId();

}
