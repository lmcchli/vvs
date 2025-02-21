/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPResponse;

import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.registration.SspInstance;
import com.mobeon.masp.callmanager.notification.OutboundNotification;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This class is responsible for dispatching a SIP response event to a
 * corresponding call or active REGISTER transaction.
 *
 * @author Malin Flodin
 */
public class SipResponseDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public SipResponseDispatcher() {
    }

    public void dispatchSipResponseEvent(SipResponseEvent sipResponseEvent) {

        SspInstance sspInstance = null;
        OutboundNotification notification = null;
        CallImpl call = null;

        if (sipResponseEvent.getMethod().equals(Request.REGISTER)) {
            sspInstance =
                    CMUtils.getInstance().getRegistrationDispatcher().
                            getSspInstance(sipResponseEvent);
        } else if (sipResponseEvent.getMethod().equals(Request.NOTIFY)){
            notification = CMUtils.getInstance().getNotificationDispatcher().
                    getNotification(sipResponseEvent);
        } else {
            call = (CallImpl) CMUtils.getInstance().getCallDispatcher().getCall(
                    sipResponseEvent);
        }

        if (sspInstance != null) {
            if (log.isDebugEnabled())
                log.debug("Register response is queued in sspInstance: " +
                        sspInstance);
            sspInstance.queueEvent(sipResponseEvent);
        } else if (notification != null) {
            if (log.isDebugEnabled())
                log.debug("Notify response is queued in notification: " +
                        notification);
            notification.queueEvent(sipResponseEvent);
        } else if (call != null) {
            if (log.isDebugEnabled())
                log.debug("Response is queued in call: " + call);
            call.queueEvent(sipResponseEvent);
        } else {

            /**
             * The SIP response does not match any ongoing register procedure, notification procedure or 
             * active call. The response MUST be ignored EXCEPT in the following case:
             *
             * In Proxy mode, a UAS COULD send a second 200OK response (due to no ACK sent back from UAC to UAS).
             * In this case, Call Manager has already notified the Execution Engine (proxied event), removed the
             * application and dropped the call.
             * Therefore, an analyses must be performed on the packet itself to figure out if the SipResponse MUST be
             * forwarded (again) to the UAC.
             *
             * Conditions to forward the 200OK response: 
             * - Call Manager is topmost of the incoming SipResponse Via header
             * - There is, at least, another Via header in the SipResponse (i.e. Call Manager is not the final UAC, for outbound)
             */
            boolean proxyMode = ConfigurationReader.getInstance().getConfig().getApplicationProxyMode();
            Integer responseCode = sipResponseEvent.retrieveResponseCodeForMethod(Request.INVITE);

            if (proxyMode && responseCode != null && responseCode == Response.OK) {

                // Via header validation
                SIPResponse sipResponse = (SIPResponse)sipResponseEvent.getResponse();
                Via viaHeader = sipResponse.getTopmostVia();
                HostPort topViaHostPort = viaHeader.getSentBy();

                HostPort proxyHostPort = new HostPort();
                proxyHostPort.setHost(new Host(CMUtils.getInstance().getLocalHost()));
                proxyHostPort.setPort(CMUtils.getInstance().getLocalPort());

                // If Call Manager (PROXY) is in the SipResponse topmost Via header and not the last
                HostPort viaOverrideHostPort = ConfigurationReader.getInstance().getConfig().getViaOverrideHostPort();
                if ((topViaHostPort.equals(proxyHostPort)|| (viaOverrideHostPort!=null && topViaHostPort.equals(viaOverrideHostPort))) && (sipResponse.getViaHeaders().size() > 1)) {
                    try {
                        // Forward the 200OK statelessly to UAC
                        sipResponseEvent.getResponse().removeFirst(Via.NAME);
                        SipResponse uasSipResponse = (SipResponse)sipResponseEvent.getSipMessage();
                        SipResponse uacSipResponse = new SipResponse(sipResponseEvent.getResponse(), null, uasSipResponse.getSipProvider());
                        CMUtils.getInstance().getSipMessageSender().sendResponse(uacSipResponse);

                        // Log purpose
                        SIPResponse logSipResponse = (SIPResponse)uacSipResponse.getResponse(); 
                        Via logViaHeader = logSipResponse.getTopmostVia();
                        if (log.isInfoEnabled())
                            log.info("The SIP " + sipResponseEvent.getResponseCode() + " response did not match " +
                                    "any active call but has been identified as a SIP 200OK Response (Proxy mode) " +
                                    "which will be forwarded to UAC (" + logViaHeader.toString() + ")");
                    } catch (Exception e) {
                        log.warn("The SIP " + sipResponseEvent.getResponseCode());
                    }
                }
            } else {
                if (log.isInfoEnabled())
                    log.info("The SIP " + sipResponseEvent.getResponseCode() +
                            " response did not match any ongoing register procedure," +
                            "notification procedure or " +
                            "active call. The response is ignored. " +
                            "CallId=" + sipResponseEvent.getSipMessage().getCallId() +
                            " EarlyDialogId: " +
                            sipResponseEvent.getEarlyDialogId());
            }
        }
    }
}
