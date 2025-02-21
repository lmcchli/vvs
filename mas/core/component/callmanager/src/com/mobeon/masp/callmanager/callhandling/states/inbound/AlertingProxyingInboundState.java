/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.stack.SIPServerTransaction;

import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.ProxiedEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedInboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.Dialog;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Represents the sub state "Proxying" of {@link AlertingInboundState}.
 * This sub state is entered when entering the {@link AlertingNewCallInboundState}, i.e. due to a proxy request.
 * <p>
 * This sub state is entered when call manager's client proxies the initial SIP INVITE received.
 * <p>
 * This class is thread-safe.
 * All methods are synchronized to handle each event atomically.
 */
public class AlertingProxyingInboundState extends AlertingInboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    
    // Contains the final response code (3xx-6xx) to the initial INVITE or '-1' if a final response has not been received
    private int lastInviteFinalResponseCode = -1;

    public AlertingProxyingInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Alerting state (sub state Proxying)";
    }

    /**
     * Processes a SipResponse received from a UAS.
     * 
     * <p>
     * The PROXY handles responses to the following requests (and ignores responses to the other request types):
     * <ul>
     * <li> INVITE 
     * <li> CANCEL
     * <li> PRACK
     * </ul>
     * The SIP response is forwarded statelessly to the UAC.
     * 
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {

        if (log.isDebugEnabled())
            log.debug("A SIP (" + sipResponseEvent.getResponseCode() +
                    ") response was received for " + sipResponseEvent.getMethod() + " request.");

        // INVITE response
        Integer responseCode = sipResponseEvent.retrieveResponseCodeForMethod(Request.INVITE);
        if (responseCode != null) {
            int responseType = responseCode / 100;

            if (log.isInfoEnabled())
                log.info("Processing " + responseCode + " response.");

            if (responseType == 1) {
                process1xxResponseToInvite(sipResponseEvent);
            } else if (responseType == 2) {
                process2xxResponseToInvite(sipResponseEvent);
            } else {
                processSipResponseDefault(sipResponseEvent, true);
            }
            return;
        }

        // CANCEL response
        responseCode = sipResponseEvent.retrieveResponseCodeForMethod(Request.CANCEL);
        if (responseCode != null) {
            if (log.isInfoEnabled())
                log.info("Processing " + responseCode + " response to a CANCEL request.");

            processSipResponseDefault(sipResponseEvent, false);
            return;
        }

        // Check for response to PRACK
        responseCode = sipResponseEvent.retrieveResponseCodeForMethod(Request.PRACK);
        if (responseCode != null) {
            if (log.isInfoEnabled())
                log.info("Processing " + responseCode + " response to a PRACK request.");

            processSipResponseDefault(sipResponseEvent, false);
            return;
        }


        // SipResponse was not handled
        log.warn("Receiving a " + sipResponseEvent.getResponseCode() + " response to a " + sipResponseEvent.getMethod() + " request, ignored.");
    }

    /**
     * Processes a provisional response (1xx) received from a UAS for a SIP INVITE.
     * 
     * <p>
     * The SIP response is forwarded statelessly to the UAC.
     * In order to do so, the SipResponse is created without any {@link Transaction}
     * when sending the request to the {@link SipMessageSender}.
     * <p>
     * {@link Call} stays in Alerting_PROXYING state.
     * 
     * @param sipResponseEvent carries the SIP response.
     */
    private void process1xxResponseToInvite(SipResponseEvent sipResponseEvent) {
        try {
            int responseCode = sipResponseEvent.getResponseCode();

            if (responseCode == Response.TRYING) {
                if (log.isDebugEnabled())
                    log.debug("No special action is taken for the SIP " + responseCode + " response.");

                // Call must stay in Alerting.PROXYING state.
                // In PROXY mode, Call Manager does not send back the UAS SIP TRYING to the originator

            } else if (responseCode == Response.RINGING) {
                if (log.isDebugEnabled())
                    log.debug("The call is ringing.");

                // Call must stay in Alerting.PROXYING state.

                // Inform Call Manager's client about the progression
                ProgressingEvent progressingEvent = new ProgressingEvent(call, false, responseCode);
                call.fireEvent(progressingEvent);
                
                // Send the SIP RINGING response statelessly to UAC
                sipResponseEvent.getResponse().removeFirst(Via.NAME);
                SipResponse uasSipResponse = (SipResponse)sipResponseEvent.getSipMessage();
                SipResponse sipResponse = new SipResponse(sipResponseEvent.getResponse(), null, uasSipResponse.getSipProvider());
                sipMessageSender.sendResponse(sipResponse);

            } else {
                // All other SIP 1xx responses should be handled as a Session Progress response
                // Same behaviour as SIP RINGING.

                if (log.isDebugEnabled())
                    log.debug("A session progress has been received.");

                // Call must stay in Alerting.PROXYING state.

                // Inform Call Manager's client about the progression
                ProgressingEvent progressingEvent = new ProgressingEvent(call, false, responseCode);
                call.fireEvent(progressingEvent);

                // Send the SIP progress response statelessly to UAC
                sipResponseEvent.getResponse().removeFirst(Via.NAME);
                SipResponse uasSipResponse = (SipResponse)sipResponseEvent.getSipMessage();
                SipResponse sipResponse = new SipResponse(sipResponseEvent.getResponse(), null, uasSipResponse.getSipProvider());
                sipMessageSender.sendResponse(sipResponse);
            }

        } catch (Exception e) {
            String message = "IllegalStateException in process1xxResponseToInvite for call " + call;
            log.error(message);
            log.error(e.getMessage());

            call.setStateDisconnected(DisconnectedSubState.COMPLETED);

            // Inform Call Manager's client about the error
            ErrorEvent errorEvent = new ErrorEvent(call, CallDirection.INBOUND, message, true);
            call.fireEvent(errorEvent);
        }
    }

    /**
     * Processes a 2xx response received from a UAS for a SIP INVITE.
     * 
     * <p>
     * The SIP final is forwarded statelessly to the UAC.
     * In order to do so, the SipResponse is created without any {@link Transaction}
     * when sending the request to the {@link SipMessageSender}.
     * <p>
     * In PROXY mode, the {@link Call} does not wait for the ACK to complete the call
     * since the PROXY is not aware of this request.  SIP ACK is sent directly from UAC to UAS.  
     * <p>
     * {@link Call} goes to Disconnected.COMPLETED state.
     *
     * @param sipResponseEvent carries the SIP response.
     */
    private void process2xxResponseToInvite(SipResponseEvent sipResponseEvent) {
        if (log.isInfoEnabled())
            log.info("A SIP final response is received from UAS for SIP INVITE.");

        try {
            // Cancel NoAck timer (200OK is considered the ACK in PROXY mode)
            call.cancelNoAckTimer();

            // Set the state to disconnected before a disconnected event is sent.
            call.setStateDisconnected(DisconnectedSubState.COMPLETED);

            // Inform Call Manager's client about the progression
            ProxiedEvent proxiedEvent = new ProxiedEvent(call, sipResponseEvent.getResponseCode());
            call.fireEvent(proxiedEvent);

            // Send the 2xxResponse statelessly to UAC
            sipResponseEvent.getResponse().removeFirst(Via.NAME);
            SipResponse uasSipResponse = (SipResponse)sipResponseEvent.getSipMessage();
            SipResponse sipResponse = new SipResponse(sipResponseEvent.getResponse(), null, uasSipResponse.getSipProvider());
            sipMessageSender.sendResponse(sipResponse);

            // Remove pending SipStack transaction to avoid TX leak because 200OK sent statelessly 
            removePendingSipStackTransaction();
        } catch (Exception e) {
            String message = "IllegalStateException in process2xxResponseToInvite for call " + call;
            log.error(message);
            log.error(e.getMessage());

            call.setStateDisconnected(DisconnectedSubState.COMPLETED);

            // Inform Call Manager's client about the error
            ErrorEvent errorEvent = new ErrorEvent(call, CallDirection.INBOUND, message, true);
            call.fireEvent(errorEvent);
        }
    }
    
    /**
     * This method removes a pending SIPServerTransaction in the SipStack.
     * It is used by Call Manager when an incoming SipRequest is meant to be proxied.
     * In this case, the SIPServerTransaction never will be COMPLETED in the SipStack.
     * Instead of waiting on the cleaner thread (SipStack) to remove the transaction,
     * this method removes it explicitly.
     */
    private void removePendingSipStackTransaction() {
        try {
            SipRequestEvent sipRequestEvent = call.getInitialSipRequestEvent();
            if (sipRequestEvent != null) {
                SipProvider sipProvider = sipRequestEvent.getSipProvider();
                if (sipProvider != null) {
                    SipStackImpl sipStack = (SipStackImpl)sipProvider.getSipStack();
                    SIPServerTransaction sipServerTransaction = (SIPServerTransaction)sipRequestEvent.getServerTransaction();
                    if (sipStack != null && sipServerTransaction != null) {
                        sipStack.removeTransaction(sipServerTransaction);
                        //To avoid memory leaks also terminate the SIPServerTransaction 
                        //Since we send the 200ok statelessly, the SIP Stack does not automatically terminate the TX
                        //Keeping a not terminated TX will cause TX leaks
                        sipServerTransaction.terminate();
                    }
                }
            }
        } catch (Exception e) { ; }
    }

    /**
     * Generic method that processes responses (from a UAS to UAC) to the following cases:
     *
     * <ul>
     * <li>INVITE (3xx, 4xx, 5xx or 6xx)
     * <li>CANCEL
     * <li>PRACK
     * </ul>
     *
     * <p>
     * The SIP response is forwarded statelessly to the UAC.
     * <p>
     * Depending of the response type, the {@link Call} might stay in Alerting.PROXYING or move to Disconnected.COMPLETED state. 
     * <p>
     * {@link Call} goes to Disconnected.COMPLETED state.
     *
     * @param sipResponseEvent carries the SIP response.
     * @param inviteFinalResponse true if this SipResponseEvent is for a final response to the initial INVITE request
     */
    private void processSipResponseDefault(SipResponseEvent sipResponseEvent, boolean isInviteFinalResponse) {
        String responseMethod = sipResponseEvent.getSipMessage().getMethod();
        int responseCode = sipResponseEvent.getResponseCode();

        if (log.isInfoEnabled())
            log.info("A SIP response (" + responseCode + ") is received for " + responseMethod + " request.");

        try {
           
            // Store responseCode for ProxiedEvent 
            // ProxiedEvent is fired when the CallManager process the ACK
            if(isInviteFinalResponse) {
                if (log.isDebugEnabled()) {
                    if(lastInviteFinalResponseCode != -1) {
                        log.debug("A second final SIP response (" + responseCode + ") has been received for " + responseMethod + " request. " +
                                  "Previous SIP response was "  + lastInviteFinalResponseCode + ".");
                    }
                }
                
                // fire the event when we receive the ACK in processAck()
                lastInviteFinalResponseCode = responseCode;
            }

            // Send the response statelessly to UAC
            sipResponseEvent.getResponse().removeFirst(Via.NAME);
            SipResponse uasSipResponse = (SipResponse)sipResponseEvent.getSipMessage();
            SipResponse sipResponse = new SipResponse(sipResponseEvent.getResponse(), null, uasSipResponse.getSipProvider());
            sipMessageSender.sendResponse(sipResponse);

        } catch (Exception e) {
            String message = "IllegalStateException in processResponseToInvite for call " + call;
            log.error(message);
            log.error(e.getMessage());

            call.setStateDisconnected(DisconnectedSubState.COMPLETED);

            // Inform Call Manager's client about the error
            ErrorEvent errorEvent = new ErrorEvent(call, CallDirection.INBOUND, message, true);
            call.fireEvent(errorEvent);
        }
    }

    /**
     * SIP BYE request is forwarded in PROXY mode even though a SIP CANCEL SHOULD be sent by UAC
     * since the provisional phase is not completed yet.
     * 
     * @param sipRequestEvent carries the SIP request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        processSipRequest(sipRequestEvent);
    }

    /**
     * SIP BYE request is forwarded
     * 
     * @param sipRequestEvent carries the SIP request.
     */
    public synchronized void processCancel(SipRequestEvent sipRequestEvent) {
        processSipRequest(sipRequestEvent);
    }

    /**
     * SIP Prack request COULD be sent from UAC to the UAS:
     * <ul>
     * <li> directly to UAS (without transiting by the PROXY)
     * <li> via the PROXY
     * </ul>
     *
     * The current method is to support the 'via PROXY' case.
     * @param sipRequestEvent carries the SIP request.
     */
    public synchronized void processPrack(SipRequestEvent sipRequestEvent) {
        processSipRequest(sipRequestEvent);
    }

    //-------------------------------- Private methods -------------------------------- 

    private void processSipRequest(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP " + sipRequestEvent.getMethod() + " received in " + this + " for call " + call);

        try {
            // Create SipUri based on the UAS stored in the inboundCall 
            SipRequest uasSipInviteRequest = CMUtils.getInstance().getSipRequestFactory().createSipRequest(
                    call.getUas(),
                    sipRequestEvent);
            
            if (uasSipInviteRequest != null) {
                // Send the uasSipInviteRequest
                sipMessageSender.sendRequestStatelessly(uasSipInviteRequest);
            }

        } catch (Exception e) {
            log.error("Exception in SIP " + sipRequestEvent.getMethod() + ", "+ e.getMessage());

            // Let the UAC retry.
        }
    }

    /**
     * Handles an administrator lock request.
     * <p>
     * A {@link FailedEvent} is generated and the state is set to {@link FailedCompletedInboundState}.
     * 
     * The initial inbound INVITE is rejected with a SIP "Service Unavailable" response.
     * 
     * <p>
     * If the SIP error response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     */
    public synchronized void processLockRequest() {
        String message = "The Service is temporarily unavailable due to the current administrative state: Locked.";

        if (log.isDebugEnabled())
            log.debug("Lock requested in " + this + ". " + message);

        if (log.isInfoEnabled())
            log.info("Due to a lock request, the INVITE is rejected with SIP 503 response.");

        // Set call failed
        call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.INBOUND, message,
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(null, null)));

        call.sendErrorResponse(Response.SERVICE_UNAVAILABLE, call.getInitialSipRequestEvent(), message);
    }

    /**
     * Handles a Call timeout.
     * 
     * @param callTimeoutEvent carries information regarding the call timeout.
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {

        String message = "The inbound call has timed out (" + callTimeoutEvent.getType() +
            ") while in " + this + " state.  The call will be ended.";

        if (callTimeoutEvent.getType() == CallTimerTask.Type.CALL_NOT_ACCEPTED ||
            callTimeoutEvent.getType() == CallTimerTask.Type.EXPIRES  ||
            callTimeoutEvent.getType() == CallTimerTask.Type.NO_ACK) {

            if (log.isInfoEnabled())
                log.info(message + " CalledParty: " + call.getCalledParty().toString() + " dialogId=" + call.getInitialDialogId());

            call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
            call.fireEvent(new ErrorEvent(call, CallDirection.INBOUND, message, false));

            call.sendErrorResponse(Response.REQUEST_TIMEOUT, call.getInitialSipRequestEvent(), "Call was not processed in time.");

        } else {
            if (log.isInfoEnabled())
                log.info("The call timeout <" + callTimeoutEvent.getType() + "> is ignored.");
        }
    }

    /**
     * Handles a SIP timeout event.
     * <p/>
     * A BYE request is sent and the state is set to
     * {@link ErrorInboundState.ErrorSubState.COMPLETED}.
     *
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {

        if (log.isDebugEnabled())
            log.debug("SIP timeout expired in " + this + ".");

        // Cancel NoAck timer (200OK is considered the ACK in PROXY mode)
        call.cancelNoAckTimer();

        call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
        call.fireEvent(new ErrorEvent(
                call, CallDirection.INBOUND,
                "SIP timeout occurred. The call will be ended with a " +
                "SIP BYE request.", false));
        call.deleteStreams();

        sendByeRequest();
    }
    
    public synchronized void processAck(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP ACK received in " + this + " for call " + call);
                    
            if(lastInviteFinalResponseCode != -1) {
                
                try {
                    // Cancel NoAck timer 
                    call.cancelNoAckTimer();
    
                    // Set the state to disconnected before a disconnected event is sent
                    call.setStateDisconnected(DisconnectedSubState.COMPLETED);
                    
                    
                    // Inform Call Manager's client
                    ProxiedEvent proxiedEvent = new ProxiedEvent(call, lastInviteFinalResponseCode);
                    call.fireEvent(proxiedEvent);
                    
                    // Create SipUri based on the UAS stored in the inboundCall 
                    SipRequest uasSipInviteRequest = CMUtils.getInstance().getSipRequestFactory().createSipRequest(
                            call.getUas(),
                            sipRequestEvent);
                    
                    if (uasSipInviteRequest != null) {
                        // Send the uasSipInviteRequest
                        sipMessageSender.sendRequestStatelessly(uasSipInviteRequest);
                    }
                    
                    // Remove pending SipStack transaction
                    removePendingSipStackTransaction();
                    
                } catch (Exception e) {
                    String message = "IllegalStateException in processAck for call " + call;
                    log.error(message);
                    log.error(e.getMessage());

                    call.setStateDisconnected(DisconnectedSubState.COMPLETED);

                    // Inform Call Manager's client about the error
                    ErrorEvent errorEvent = new ErrorEvent(call, CallDirection.INBOUND, message, true);
                    call.fireEvent(errorEvent);
                }
            }
            else {
                if (log.isInfoEnabled())
                    log.info("Unexpected SIP ACK request received without a preceding final SIP response. It is ignored.");
            }
    }
}
