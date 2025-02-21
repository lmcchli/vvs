/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingInboundState.AlertingSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedInboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorInboundState.ErrorSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedInboundState.FailedSubState;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.configuration.ReliableResponseUsage;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;

/**
 * This class is used to interface the InboundCallImpl class.
 * <p>
 * It is used for test purposes only, i.e. to be able to mock the inbound call
 * during basic test.
 *
 * @author Malin Flodin
 */
public interface InboundCallInternal extends InboundCall, CallInternal {

    // Set inbound state
    public void setStateAlerting(AlertingSubState substate);
    public void setStateConnected();
    public void setStateDisconnected(DisconnectedSubState substate);
    public void setStateError(ErrorSubState substate);
    public void setStateFailed(FailedSubState substate);

    // Inbound call timers
    public void startNotAcceptedTimer();
    public void startExpiresTimer(SipRequestEvent sipRequestEvent);

    // Environment related
    public void loadService();

    // SIP related
    public SipRequestEvent getInitialSipRequestEvent();
    public boolean isPendingSdpACallHold();

    public void setAcceptReceivedInWaitForPrack(boolean flag);
    public boolean isAcceptReceivedInWaitForPrack();

    public void setEarlyMediaRequested(boolean flag);
    public boolean isEarlyMediaRequested();

    public void setUas(RemotePartyAddress uas);
    public RemotePartyAddress getUas();

    public boolean isPEarlyMediaPresentAndInactive();

    /**
     * This method is used to find out if 1xx responses shall be sent reliably
     * or not within the call.
     * <p>
     * If the initial INVITE requires the use of 100rel (using the Require
     * header field), all non-100 provisional responses shall be sent reliably.
     * Otherwise, if the initial INVITE supports the use of 100rel (using the
     * Supported header field) whether or not to send provisional responses
     * reliably depends upon configuration. The following configurations exists:
     * <ul>
     * <li>Send all provisional responses reliably (except of course for
     * 100 Trying as given by the RFC 3262).</li>
     * <li>Send no provisional responses reliably. </li>
     * <li>Only send provisional responses containing an SDP reliably.</li>
     * </ul>
     *
     * @return  {@link ReliableResponseUsage.YES} is returned if 1xx responses
     *          shall be sent reliably, {@link ReliableResponseUsage.NO} if not,
     *          and {@link ReliableResponseUsage.SDPONLY} is returned if
     *          provisional responses carrying SDP information shall be sent
     *          reliably.
     */
    public ReliableResponseUsage useReliableProvisionalResponses();

    /**
     * This method is used to find out if support for redirected RTP has been
     * activated. Support for redirected RTP is activated if three items has
     * been fulfilled:
     * <ul>
     * <li>It has been activated in the configuration for a certain set of
     * user agents.</li>
     * <li>The user agent in the initial INIVTE is found in the configured
     * list.</li>
     * <li>The initial INIVTE contains a redirection number indicating that
     * the original call was redirected.</li>
     * </ul>
     * @return  True is returned if support for redirected RTP has been activated
     *          as described above. False is returned otherwise.
     */
    public Boolean isSupportForRedirectedRtpActivated();
    
    
    // Performance related

    /**
     * If this method has not been called already, an INFO log is printed
     * containing the difference in time between the current time (for the play)
     * and the time when the initial INVITE was received.
     */
    public void logPlayTime();
    
    /**
     * Sends 3XX redirect response with contact as specified destination
     * @param destination
     * @param redirectCode
     * @param sipRequestEvent
     */
    public void sendRedirectResponse(RedirectDestination destination, RedirectStatusCode redirectCode, SipRequestEvent sipRequestEvent);
}
