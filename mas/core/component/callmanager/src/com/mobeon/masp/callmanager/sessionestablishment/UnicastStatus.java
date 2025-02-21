/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2013.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.masp.callmanager.sessionestablishment;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpMediaDescription;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.sessionestablishment.UnicastException.UnicastExceptionCause;
import com.mobeon.masp.callmanager.sip.SipConstants;


/**
 * This class represent the status of a Unicast stream as described in RFC3264 section 6.1.
 *
 */
public class UnicastStatus {

    private static final ILogger log = ILoggerFactory.getILogger(UnicastStatus.class);

    private SdpTransmissionMode currentTransmissionMode = null;

    /**
     * Indicate if this is the first time the update() method is called
     */
    private boolean firstUpdate = true;

    /**
     * Update this session Unicast status with a new SDP offer.
     * 
     * <p>
     * The following transition are supported: <br>
     * <table  border="1">
     * <tr><td> From  </td><td>  To </td><td> Action </td></tr>
     * <tr><td> inactive </td><td> inactive </td><td> none (session establishment still pending) </td></tr>
     * <tr><td> inactive </td><td> sendrecv </td><td> passed (unicast condition passed) </td></tr>
     * <tr><td> sendrecv </td><td> sendrecv </td><td> passed (unicast condition passed) </td></tr>
     * </table>
     * 
     * @param sdpMediaDescription The SDP media description selected in the initial INVITE or in UPDATE or PRACK request in the same session.
     *                            MUST NOT be NULL in the initial INVITE (the first time this method is called).
     * @param _100relExists If the SIP request contains a Require or Supported header with the '100rel' option tag
     * @return true if unicast condition fulfilled
     * @throws UnicastException if the SdpMediaDescription contains an unsupported transition, if the sdpMediaDescription param is NULL in the initial INVITE
     *                          or if the unicast status is set to 'inactive' and the _100relExists param is false
     */
    public boolean update(SdpMediaDescription sdpMediaDescription, boolean _100relExists) throws UnicastException {

        final boolean isInitialInvite = firstUpdate;
        firstUpdate = false;

        if(sdpMediaDescription == null) {
            if(isInitialInvite) {
                log.debug("update(): Received initial INVITE without sdpMediaDescription, this is not supported.");
                throw new UnicastException(UnicastExceptionCause.SERVER_INTERNAL_ERROR);
            } else {
                // Received PRACK or UPDATE without sdpMediaDescription after an initial INVITE with precondition.
                return isUnicastFulfilled();
            }
        }

        SdpTransmissionMode uacTransmissionModeReceived = sdpMediaDescription.getAttributes().getTransmissionMode();
        if(log.isDebugEnabled()) {
            log.debug("update(): Updating unicast status with remote transmission mode: " + uacTransmissionModeReceived);
        }

        if(uacTransmissionModeReceived == null) {
            /**
             * RFC3264 6.1 : If an offered media stream is listed as sendrecv (or if there is no direction attribute
             *               at the media or session level, in which case the stream is sendrecv by default) 
             */
            uacTransmissionModeReceived = SdpTransmissionMode.SENDRECV;
        }

        switch(uacTransmissionModeReceived) {
            case SENDRECV:
                currentTransmissionMode = SdpTransmissionMode.SENDRECV;
                break;
            case INACTIVE:
                if (isInitialInvite && !_100relExists) {
                    log.debug("update():  Received initial INVITE with Unicast transmission mode set to inactive in sdp offer without 100rel option-tag");
                    throw new UnicastException(UnicastExceptionCause.EXTENSION_REQUIRED, SipConstants.EXTENSION_100REL);
                } else if(currentTransmissionMode != SdpTransmissionMode.SENDRECV) {
                    currentTransmissionMode = SdpTransmissionMode.INACTIVE;
                } else {
                    log.debug("update(): Unicast downgraded from sendrecv to inactive, rejecting");
                    throw new UnicastException(UnicastExceptionCause.FORBIDDEN);
                }
                break;
            default: //RECVONLY, SENDONLY
                if(log.isDebugEnabled()) {
                    log.debug("update(): Received SDP offer with unsupported Unicast transmission mode: " + uacTransmissionModeReceived + ", rejecting");
                }
                throw new UnicastException(UnicastExceptionCause.FORBIDDEN, uacTransmissionModeReceived.toString());
        }

        if(log.isDebugEnabled()) {
            log.debug("update(): Updated unicast status to: " + currentTransmissionMode);
        }
        return isUnicastFulfilled();
    }

    /** 
     * 
     * @return true if the last transmission mode received is 'sendrecv'
     */
    private boolean isUnicastFulfilled() {

        return currentTransmissionMode == SdpTransmissionMode.SENDRECV;
    }
}

