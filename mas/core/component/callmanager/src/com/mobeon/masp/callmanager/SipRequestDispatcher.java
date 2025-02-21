/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.message.SipResponse;

import javax.sip.Dialog;
import javax.sip.Transaction;
import javax.sip.message.Request;

/**
 * This class is responsible for dispatching a SIP request event to its
 * corresponding call or to the out-of-dialog handler if the request is
 * received out-of-dialog.
 *
 * @author Malin Flodin
 */
public class SipRequestDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public SipRequestDispatcher() {
    }

    /**
     * This method is used to dispatch a SIP request event to its
     * corresponding call. It returns the {@link SipRequestType}. If the SIP
     * request is a {@link SipRequestType.SESSION_CREATING} it is up to the
     * caller of this method to create a new session. If the SIP request is a
     * {@link SipRequestType.OPTIONS} it is up to the caller of this method to
     * respond to that OPTIONS request in the same way as for a new session
     * request.
     * <p>
     * If an active call was found, this is a mid dialog requests
     * and is further processed using
     * {@link #handleMidDialogRequest(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent,
     * com.mobeon.masp.callmanager.callhandling.CallImpl)}.
     * <br>
     * If an active call was not found, how to handle the call is based on the
     * precense of the To tag so the request is further processed using
     * {@link #handleSipRequestBasedOnToTag(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent)}.
     *
     * @param sipRequestEvent
     * @return {@link SipRequestType}.
     */
    public SipRequestType dispatchSipResquestEvent(
            SipRequestEvent sipRequestEvent) {
    	Object perf = null;
       
    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.SipReqDisp.dispatch");
        }
    	 
    	try {
	        SipRequestType requestType = SipRequestType.OTHER_NON_SESSION_CREATING;
	
	        CallImpl call = (CallImpl) CMUtils.getInstance().getCallDispatcher().
	                getCall(sipRequestEvent);
	
	        if (call == null) {
	            if (log.isDebugEnabled())
	                log.debug("Matching call was not found. The To tag is examined " +
	                        "to determine how to handle the request.");
	            requestType = handleSipRequestBasedOnToTag(sipRequestEvent);
	        } else {
	            // Matching active call was found. Call is received mid-dialog
	            if (log.isDebugEnabled())
	                log.debug("Matching call was found for SIP request event. " +
	                        "It is handled as mid-dialog.");
	            handleMidDialogRequest(sipRequestEvent, call);
	        }

	        return requestType; 
    	} finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
    	}
    }


    //================== Private Methods  ====================

    /**
     * This method is used to process a SIP request event based on the existence
     * of a To tag in the request. This method is used if a matching call was
     * not found in
     * {@link #dispatchSipResquestEvent(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent)}.
     * <br>
     * It returns the {@link SipRequestType} of the SIP request. If the SIP
     * request is a {@link SipRequestType.SESSION_CREATING} it is up to the
     * caller of this method to create a new session.
     * * <p>
     * If the To tag is set in the request, this suggests a mid dialog request.
     * But no no matching call was found so the request is further processed
     * using {@link #handleNoTransactionFound(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent)}.
     * <p>
     * If the To tag was not set, this suggests an out-of-dialog request. The
     * request is further processed using
     * {@link #handleOutOfDialogRequest(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent)}.
     *
     * @param sipRequestEvent
     * @return {@link SipRequestType}.
     */
    private SipRequestType handleSipRequestBasedOnToTag(
            SipRequestEvent sipRequestEvent) {

        SipRequestType requestType = SipRequestType.OTHER_NON_SESSION_CREATING;

        if (sipRequestEvent.getToTag() != null) {
            // The server transaction is null or the dialog
            // identifier does not match any existing dialogs/calls.
            // CallManager may have restarted or may have received a
            // request for a different UAS. The call is rejected.

            if (sipRequestEvent.getMethod().equals(Request.SUBSCRIBE)) {
           		log.debug("ToTag present - assuming re-subscribe");
                return SipRequestType.SUBSCRIBE;    
            }
            if (log.isDebugEnabled())
                log.debug("The To tag indicates that the SIP request was " +
                        "received mid-dialog but a matching call was not found. ");
            handleNoTransactionFound(sipRequestEvent);
        } else {
            // Call is received out-of-dialog.
            if (log.isDebugEnabled())
                log.debug("The SIP request was received out-of-dialog.");
            requestType = handleOutOfDialogRequest(sipRequestEvent);
        }

        return requestType;
    }

    /**
     * Handles a SIP request event received out-of-dialog.
     * <br>
     * It returns the {@link SipRequestType} of the SIP request. If the SIP
     * request is a {@link SipRequestType.SESSION_CREATING} it is up to the
     * caller of this method to create a new session.
     * <p>
     * If a SIP transaction was found for the SIP request, the request is
     * validated according to the rules for a general request as described in
     * section 8.2 in RFC 3261 (using the method
     * {@link com.mobeon.masp.callmanager.sip.events.SipRequestEvent#validateGeneralPartOfRequest()}).
     * <p>
     * If the SIP request was not validated OK, it is discarded.
     * If the SIP request was validated OK, it is further processed. If the SIP
     * request is a session creating message (i.e. an INVITE)
     * {@link SipRequestType.SESSION_CREATING} is returned.
     * Otherwise the SIP request is handled using
     * {@link #handleOutOfDialogRequest(
     * com.mobeon.masp.callmanager.sip.events.SipRequestEvent)} and
     * {@link SipRequestType.OTHER_NON_SESSION_CREATING} is returned.
     *
     * @param sipRequestEvent MUST NOT be null
     * @return {@link SipRequestType}.
     */
    private SipRequestType handleOutOfDialogRequest(
            SipRequestEvent sipRequestEvent) {

        SipRequestType requestType = SipRequestType.OTHER_NON_SESSION_CREATING;

        // Generate a new To tag for out-of-dialog requests
        String method = sipRequestEvent.getMethod();
        if ((!method.equals(Request.ACK)) && (!method.equals(Request.CANCEL)) &&
            (!method.equals(Request.BYE)) && (!method.equals(Request.PRACK))) {

            // Do not generate ToTag in Proxy mode
            if (!ConfigurationReader.getInstance().getConfig().getApplicationProxyMode()) {
                sipRequestEvent.generateToTag();
            }
        }

        Transaction transaction = sipRequestEvent.getTransaction();
        if (transaction == null) {
            // A transaction could not be created for the out-of-dialog request.
            if (log.isDebugEnabled())
                log.debug("No transaction could be created for out-of-dialog SIP " +
                        sipRequestEvent.getMethod() + " request.");
            handleNoTransactionFound(sipRequestEvent);
        } else {

            Dialog dialog = transaction.getDialog();
            if ((sipRequestEvent.getMethod().equals(Request.INVITE)) &&
                    (dialog.getState() == null)) {
                requestType = SipRequestType.SESSION_CREATING;
            } else if (sipRequestEvent.getMethod().equals(Request.OPTIONS)) {
                requestType = SipRequestType.OPTIONS;
            } else if (sipRequestEvent.getMethod().equals(Request.SUBSCRIBE)) {
                requestType = SipRequestType.SUBSCRIBE;    
            } else {
                if (log.isDebugEnabled())
                    log.debug("The SIP request is not a session creating " +
                            "request, it is handled out-of-dialog.");

                handleNoTransactionFound(sipRequestEvent);
            }
        }

        return requestType;
    }

    /**
     * Handles a SIP request event received mid dialog.
     * <p>
     * Validates first the request according to the rules for a general request
     * as described in section 8.2 in RFC 3261 (using the method
     * {@link com.mobeon.masp.callmanager.sip.events.SipRequestEvent#validateGeneralPartOfRequest()}).
     * <p>
     * If the SIP request is validated OK, the request event is queued in the
     * active call's event queue where it later is processed and
     * validated according to the rules for the specific SIP method requested.
     * Otherwise, the SIP request is discarded.
     *
     * @param sipRequestEvent MUST NOT be null
     * @param call MUST NOT be null
     */
    private void handleMidDialogRequest(
            SipRequestEvent sipRequestEvent, CallImpl call) {
        if (log.isDebugEnabled())
            log.debug("The SIP request event (" + sipRequestEvent +
                    ") was received mid-dialog.");

        boolean validatedOk = sipRequestEvent.validateGeneralPartOfRequest();

        if (validatedOk) {
            // Method specific validation is done in the call's state machine.
            // Queue the request event in the call's event queue.
            if (log.isDebugEnabled())
                log.debug("The SIP request event (" + sipRequestEvent + ") was " +
                        "validated ok. It is queued in the call (" + call + ").");
            call.queueEvent(sipRequestEvent);
        } else {
            if (log.isDebugEnabled())
                log.debug("The SIP request event (" + sipRequestEvent +
                        ") was not validated OK. It is discarded.");
        }
    }


    /**
     * This methods handles a SIP request event for which a transaction was
     * not found.
     * <P>
     * If the SIP request is an ACK, it is discarded. Otherwise a SIP
     * "Transaction Does Not Exist" is sent as response.
     * @param sipRequestEvent
     */
    private void handleNoTransactionFound(SipRequestEvent sipRequestEvent) {
        if (sipRequestEvent.getMethod().equals(Request.ACK)) {
            if (log.isInfoEnabled()) log.info("No transaction can be found for a SIP ACK request. " +
                                              "It is ignored.");
        } else {
            String method = sipRequestEvent.getMethod();
            try {
                if (log.isInfoEnabled()) log.info("No transaction can be found for a SIP " + method +
                                                  " request. It is rejected with a SIP 481 response.");
                SipResponse sipResponse =
                        CMUtils.getInstance().getSipResponseFactory().
                                createTransactionDoesNotExistResponse(sipRequestEvent);
                CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
            } catch (Exception e) {
                log.error("SIP 481 response could not be sent for the SIP " +
                        method + " request. The request is ignored.", e);
            }
        }

    }
}
