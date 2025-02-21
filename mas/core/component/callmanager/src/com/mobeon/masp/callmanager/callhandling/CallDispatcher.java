/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.events.SipEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;

import javax.sip.message.Request;

/**
 *
 * Dispatches a dialog ID to a specific Call.
 * <p>
 * The dialog ID is created from the Call-ID, a local tag and a remote tag,
 * see RFC 3261.
 * <p>
 * The dialog ID changes during a call setup. For example, when an initial
 * INVITE is received, the To tag (in this case the local tag) has not been
 * set, thus the dialog ID is currently based on Call-ID and remote tag only.
 * This dialog ID is called an "early" dialog ID. When the local tag is chosen
 * it is sent to the peer and the dialog ID is updated. This dialog ID is an
 * "established" dialog ID.
 * <p>
 * However, SIP requests might still arrive with the "early" dialogID
 * for a period of time. Due to this transient behaviour, two dialog IDs will
 * map to one Call during a short period of time. When a SIP request or response
 * is received with the complete dialog ID, the mapping of  "early" dialog ID
 * is removed.
 * <p>
 * This class contains two maps, one with early dialogs (consisting of only
 * Call-ID and one tag) and one with established dialogs. A Call can be
 * placed in both maps during a short period of time.
 *
 * This class is thread-safe under the assumption that methods
 * {@link com.mobeon.masp.callmanager.callhandling.CallImpl#isEarlyDialogActive} and
 * {@link com.mobeon.masp.callmanager.callhandling.CallImpl#inactivateEarlyDialog} only are
 * used as described in CallImpl, i.e. only are used by this class.
 *
 * @author Malin Flodin
 */
public class CallDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private final ConcurrentHashMap<String, CallInternal> earlyDialogs =
            new ConcurrentHashMap<String, CallInternal>();
    private final ConcurrentHashMap<String, CallInternal> establishedDialogs =
             new ConcurrentHashMap<String, CallInternal>();
    private HostPort proxyHostPort = new HostPort();
    
    public CallDispatcher() {
        proxyHostPort.setHost(new Host(CMUtils.getInstance().getLocalHost()));
        proxyHostPort.setPort(CMUtils.getInstance().getLocalPort());
    }

    /**
     * @return amount of initiated ("early") calls.
     */
    public int amountOfInitiatedCalls() {
        return earlyDialogs.size();
    }

    /**
     * @return amount of established calls (i.e. a final dialog ID exists).
     */
    public int amountOfEstablishedCalls() {
        return establishedDialogs.size();
    }

    

    
    /**
     * @return a collection of all existing calls. The collection is empty
     * (and not null) if there is no existing calls.
     */
    public synchronized Collection<CallInternal> getAllCalls() {
        Collection<CallInternal> callSet = new HashSet<CallInternal>();
        Collection<CallInternal> earlyCalls = earlyDialogs.values();
        Collection<CallInternal> establishedCalls = establishedDialogs.values();

        for (CallInternal call : earlyCalls) {
            callSet.add(call);
        }
        for (CallInternal call : establishedCalls) {
            callSet.add(call);
        }

        return callSet;
    }

    /**
     * The given dialog is mapped to a corresponding Call.
     * @return The matching Call. Null if none is found or if dialogId is
     * null.
     */
    public synchronized CallInternal getCall(SipEvent sipEvent) {

        CallInternal call = null;
        String dialogId;

        if (sipEvent == null) {
            if (log.isDebugEnabled())
                log.debug("Trying to locate a call but the sipEvent is null.");
        } else {
            if (sipEvent.getTransaction() == null) {

                /**
                 * Call Manager SHALL reject a SipEvent without a transaction (client or server)
                 * EXCEPT in the case of the Call Manager acting as a PROXY server.
                 * 
                 * Since PROXY requests are sent out statelessly, there are no SIPTransaction related to the SipResponses.  
                 * Therefore, a lookup must be perform on the establishedDialogs and earlyDialogs in order
                 * to trace the call.
                 * 
                 * Since PROXY responses to the initial INVITE are forwarded statelessly, and these responses can have a tag parameter
                 * added to the To header, the SIPTransaction related to an ACK request will not be found.
                 * Therefore, a lookup must be perform on the establishedDialogs and earlyDialogs in order
                 * to trace the call.
                 * 
                 * If found, the call DOES NOT HAVE to be in Alerting.Proxying state since the call might be in COMPLETED
                 * state and receiving a second 200Ok response.
                 */
                if (ConfigurationReader.getInstance().getConfig().getApplicationProxyMode() && sipEvent instanceof SipResponseEvent) {
                    // Via header
                    SipResponseEvent sre = (SipResponseEvent)sipEvent;
                    SIPResponse sr = (SIPResponse)sre.getResponse();
                    Via viaHeader = sr.getTopmostVia();
                    HostPort topViaHostPort = viaHeader.getSentBy();

                    HostPort viaOverrideHostPort = ConfigurationReader.getInstance().getConfig().getViaOverrideHostPort();
                    // If Call Manager (PROXY) is on top of the Via headers
                    if (topViaHostPort.equals(proxyHostPort) || (viaOverrideHostPort!=null && topViaHostPort.equals(viaOverrideHostPort))) {
                        // Retrieve the call using the callId, fromTag and ToTag
                        call = getCallFromNonTransactionalResponse(sipEvent);

                        if (call != null) {
                            if (log.isDebugEnabled())
                                log.debug("Call " + call.getInitialDialogId() + " found for a PROXY stateless response (no transaction related). " +
                                        call.getCurrentState());
                            return call;
                        } else {
                            if (log.isDebugEnabled())
                                log.debug("No Call found, PROXY mode ON and SipResponse ViaHeader matches the ProxyHostPort");
                        }
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("No Call found, PROXY mode ON but SipResponse ViaHeader " + topViaHostPort +
                                    " does not match the ProxyHostPort configuration " + proxyHostPort);
                    }
                }
                else if (ConfigurationReader.getInstance().getConfig().getApplicationProxyMode() && sipEvent instanceof SipRequestEvent) {

                    // This is needed when Call Manager (PROXY) receives an ACK request for a 3xx-6xx response sent statelessly in AlertingProxyingInboundState
                    // It is currently restricted to ACK requests since it's only needed for them with current functionality. 
                    if( sipEvent.getMethod().equals(Request.ACK) ) {
                        // Retrieve the call using the callId, fromTag and ToTag
                        call = getCallFromNonTransactionalResponse(sipEvent);
                    }
                        
                    if (call != null) {
                        if (log.isDebugEnabled())
                            log.debug("Call " + call.getInitialDialogId() + " found for a PROXY stateless ACK request (no transaction related). " +
                                    call.getCurrentState());
                        return call;
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("No Call found, PROXY mode ON for SIP " + sipEvent.getMethod() + " request.");
                    }
                } 
                else {
                    if (log.isDebugEnabled())
                        log.debug("No Call found and PROXY mode is OFF.");
                }
                return null;
            }

            dialogId = sipEvent.getEstablishedDialogId();

            if (dialogId == null) {
                if (log.isDebugEnabled())
                    log.debug("Cannot retrieve established dialog ID." +
                            " sipMessage callId=< " +
                            sipEvent.getSipMessage().getCallId().toLowerCase() + " >");
                return null;
            }

            if (log.isDebugEnabled())
                log.debug("Looking for call with dialogId=" + dialogId);
            call = establishedDialogs.get(dialogId);

            if (call == null) {
                dialogId = sipEvent.getEarlyDialogId();

                if (dialogId == null) {
                    if (log.isDebugEnabled())
                        log.debug("Cannot retrieve early dialog ID." +
                                " sipMessage callId=< " +
                                sipEvent.getSipMessage().getCallId().toLowerCase() + " >");
                    return null;
                }

                if (log.isDebugEnabled())
                    log.debug("Looking for early call with dialogId=" + dialogId);

                call = earlyDialogs.get(dialogId);
            }

            if (log.isDebugEnabled()) {
                if (call == null) {
                    log.debug("No call matched the dialogId=" + dialogId);
                }
            }
        }
        return call;
    }

    /**
     * by performing search over the request's transaction
     * @param sipEvent
     * @return
     */
    private synchronized CallInternal getCallFromNonTransactionalResponse(SipEvent sipEvent) {
        CallInternal call = null;
        String dialogId;

        // Lookup for establishedDialogId
        dialogId = sipEvent.getSipMessage().getCallId();
        if (sipEvent.getSipMessage().getFromHeaderTag() != null)
            dialogId += ":" + sipEvent.getSipMessage().getFromHeaderTag();
        if (sipEvent.getSipMessage().getToHeaderTag() != null)
            dialogId += ":" + sipEvent.getSipMessage().getToHeaderTag();
        dialogId = dialogId.toLowerCase();
        call = establishedDialogs.get(dialogId);

        if (call == null) {
            // Lookup for earlyDialog
            dialogId = sipEvent.getSipMessage().getCallId();
            if (sipEvent.getSipMessage().getFromHeaderTag() != null)
                dialogId += ":" + sipEvent.getSipMessage().getFromHeaderTag();
            dialogId = dialogId.toLowerCase();
            call = earlyDialogs.get(dialogId);
        }
        return call;
    }

    /**
     * Inserts a call in the "early" dialogs map.
     *
     * @param call
     * @param sipMessage
     */
    public synchronized void insertOutboundCall(
            OutboundCallInternal call, SipMessage sipMessage) {

        if ((call == null) || (sipMessage == null)) {
            log.error("Trying to insert an outbound call but parameter is null. " +
                    "Call: " + call + ", SipMessage: " + sipMessage);

        } else {
            String earlyDialogId = sipMessage.createEarlyDialogIdForOutboundCall();

            if (log.isDebugEnabled())
                log.debug("Inserting outbound call with early dialogId=" + earlyDialogId);

            earlyDialogs.put(earlyDialogId, call);
            call.setInitialDialogId(earlyDialogId);
        }

        if (log.isInfoEnabled())
            log.info("CallStates: " + debugInfo());
    }

    /**
     * Inserts the given inbound call in the "early" and "established" dialog map.
     * If the dialogId is null, no insertion is made.
     *
     * @param call The call to insert. MUST NOT be null. If null, an error log
     * is generated.
     * @param sipEvent The sipRequestEvent to create the dialog id from.
     */
    public synchronized void insertInboundCall(
            InboundCallInternal call, SipEvent sipEvent) {

        if ((call == null) || (sipEvent == null)) {
            log.error("Trying to insert an inbound call but parameter is null. " +
                    "Call: " + call + ", SipEvent: " + sipEvent);
        } else {
            // Insert early mapping
            String earlyDialogId = sipEvent.getEarlyDialogId();
            if (log.isDebugEnabled())
                log.debug("Inserting inbound call with early dialogId=" + earlyDialogId);
            earlyDialogs.put(earlyDialogId, call);
            call.setInitialDialogId(earlyDialogId);

            // Insert established mapping
            String dialogId = sipEvent.getEstablishedDialogId();
            if (log.isDebugEnabled())
                log.debug("Inserting inbound call with dialogId=" + dialogId);
            establishedDialogs.put(dialogId, call);
            call.setEstablishedDialogId(dialogId);
        }

        if (log.isInfoEnabled())
            log.info("CallStates: " + debugInfo());
    }

    /**
     * Updates the dialogId for the given call with the established dialogId.
     * This method only adds a mapping from the established dialogId to the call
     * in the "established" dialog map.  If the established dialogId is null,
     * no insertion to the "established" dialog map is done.
     * @param call The call. MUST NOT be null. If null, an error log is generated.
     * @param establishedDialogId The established dialogId. MUST NOT be null. If
     * @param removeEarlyDialogId
     */
    public synchronized void updateOutboundCallDialogId(
            CallInternal call, String establishedDialogId,
            Boolean removeEarlyDialogId)
    {
        if ((call == null) || (establishedDialogId == null)) {
            log.error("Trying to update identification for an outbound call " +
                    "but parameter is null. Call: " + call +
                    ", DialogId: " + establishedDialogId);
        } else {
            if (log.isDebugEnabled())
                log.debug("Updating outbound call with established dialogId=" +
                        establishedDialogId);

            establishedDialogs.put(establishedDialogId, call);
            call.setEstablishedDialogId(establishedDialogId);

            if ((removeEarlyDialogId) && (call.isEarlyDialogActive())) {
                call.inactivateEarlyDialog();
                earlyDialogs.remove(call.getInitialDialogId(), call);
            }
        }

    }

    /**
     * Removes the call from the "early" and/or "established" maps.
     *
     * @param initialDialogId The early dialog id
     * @param establishedDialogId The established dialog id
     */
    public synchronized void removeCall(String initialDialogId, String establishedDialogId) {
        if (initialDialogId != null) {
            if (log.isDebugEnabled())
                log.debug("Removing early call with dialogId=" + initialDialogId);
            earlyDialogs.remove(initialDialogId);
        }

        if (establishedDialogId != null) {
            if (log.isDebugEnabled())
                log.debug("Removing established call with dialogId=" + establishedDialogId);
            establishedDialogs.remove(establishedDialogId);
        }

        if (log.isInfoEnabled())
            log.info("CallStates: " + debugInfo());
    }


    private String debugInfo() {
        String info;
        Collection<CallInternal> earlyCalls = earlyDialogs.values();
        Collection<CallInternal> establishedCalls = establishedDialogs.values();

        HashMap<String, Integer> stateMap = new HashMap<String, Integer>();

        for (CallInternal call : earlyCalls) {
            CallState state = call.getCurrentState();

            Integer counter;
            if ((counter = stateMap.get(state.getClass().getName())) == null) {
                stateMap.put(state.getClass().getName(), 1);
            } else {
                stateMap.put(state.getClass().getName(), ++counter);
            }
        }
        for (CallInternal call : establishedCalls) {
            CallState state = call.getCurrentState();
            Integer counter;
            if ((counter = stateMap.get(state.getClass().getName())) == null) {
                stateMap.put(state.getClass().getName(), 1);
            } else {
                stateMap.put(state.getClass().getName(), ++counter);
            }
        }
        info = stateMap.toString();

        return info;
    }
}
