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

import java.util.Vector;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpMediaDescription;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPrecondition.DirectionTag;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPrecondition.StatusType;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPrecondition.StrengthTag;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPrecondition;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPreconditionConf;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPreconditionCurr;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPreconditionDes;
import com.mobeon.masp.callmanager.sessionestablishment.PreconditionException.PreconditionExceptionCause;
import com.mobeon.masp.callmanager.sip.SipConstants;


/**
 * This class represent the local status table for precondition described in RFC3312 section 5.
 *
 */
public class PreconditionStatusTable {

    private static final ILogger log = ILoggerFactory.getILogger(PreconditionStatusTable.class);
    /**
     * The variables localDesiredStatus, remoteDesiredStatus and localCurrentStatus are final since current implementation would always
     * upgrade them to these value if it applies preconditions. 
     * 
     * Otherwise, the call would be rejected with a 4xx or 5xx response and these variables are not used.
     */
    private final static SdpPreconditionDes localDesiredStatus = 
            new SdpPreconditionDes(SdpPrecondition.PRECONDITION_TYPE_QOS, StrengthTag.MANDATORY, StatusType.LOCAL, DirectionTag.SENDRECV);
    private final static SdpPreconditionDes remoteDesiredStatus = 
            new SdpPreconditionDes(SdpPrecondition.PRECONDITION_TYPE_QOS,StrengthTag.MANDATORY, StatusType.REMOTE, DirectionTag.SENDRECV);

    private final static SdpPreconditionCurr localCurrentStatus = 
            new SdpPreconditionCurr(SdpPrecondition.PRECONDITION_TYPE_QOS,StatusType.LOCAL, DirectionTag.SENDRECV);
    private SdpPreconditionCurr remoteCurrentStatus;
    
    /**
     * true if the initial INVITE contains a valid precondition offer.
     * This is used to make sure the further precondition update do not down-grade the precondition state
     */
    private boolean validInitialPreconditionOfferReceived = false;
    
    /**
     * true if the last time update() was called, it contained a valid precondition offer
     */
    private boolean lastUpdateContainsValidPrecondition = false;
    
    /**
     * Indicate if this is the first time the update() method is called
     */
    private boolean firstUpdate = true;
    
    /**
     * 
     */
    public PreconditionStatusTable() {
        remoteCurrentStatus =  new SdpPreconditionCurr(SdpPrecondition.PRECONDITION_TYPE_QOS, StatusType.REMOTE, DirectionTag.NONE);
    }
    
    /**
     * Update this session precondition local status table with a new SDP offer.
     * 
     * @param sdpMediaDescription The SDP media description selected in the initial INVITE or in UPDATE or PRACK request in the same session.
     *                            MUST NOT be NULL in the initial INVITE (the first time this method is called).
     * @param requirePreconditionExists If the SIP request contains a Require header with the 'precondition' option tag
     * @param supportedPreconditionExists If the SIP request contains a Supported header with the 'precondition' option tag
     * @param _100relExists If the SIP request contains a Require or Supported header with the '100rel' option tag
     * @return true if precondition are fulfilled
     * @throws PreconditionException if there is an error with the sdpMediaDescription and Require/Supported headers
     */
    public boolean update(SdpMediaDescription sdpMediaDescription, boolean requirePreconditionExists, 
                          boolean supportedPreconditionExists, boolean _100relExists) throws PreconditionException {

        final boolean isInitialInvite = firstUpdate;
        firstUpdate = false;
        resetPreconditionRequire(); 
        
        if(sdpMediaDescription == null) {
            if(requirePreconditionExists) {
                log.debug("update(): sdpMediaDescription is NULL with Require:precondition");
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to missing precondition offer.");
            }

            if(isInitialInvite) {
                log.debug("update(): Received initial INVITE without sdpMediaDescription, this is not supported.");
                throw new PreconditionException(PreconditionExceptionCause.SERVER_INTERNAL_ERROR);
            } else if (!validInitialPreconditionOfferReceived) {
                // Received PRACK or UPDATE without sdpMediaDescription after an initial INVITE without precondition.
                // Assuming precondition are fulfilled and resource are reserved. 
                return true; 
            } else {
                // Received PRACK or UPDATE without sdpMediaDescription after an initial INVITE with precondition.
                return isPreconditionFulfilled();
            }
        }

        Vector<SdpPreconditionCurr> currentStatus = sdpMediaDescription.getAttributes().getCurrentPreconditions();
        Vector<SdpPreconditionDes> desiredStatus = sdpMediaDescription.getAttributes().getDesiredPreconditions();
        Vector<SdpPreconditionConf> confirmStatus = sdpMediaDescription.getAttributes().getConfirmPreconditions();

        if(!desiredStatus.isEmpty() || !currentStatus.isEmpty()) { 
            /**
             *  precondition exist in sdp offer
             */
            if(isInitialInvite && !(requirePreconditionExists || supportedPreconditionExists)) {
                // Enforce precondition in require or supported only on initial Invite
                // (i.e. ok if not present in req or supp for PRACK or UPDATE)
                log.debug("update(): Precondition exist in sdp offer without precondition option-tag");
                throw new PreconditionException(PreconditionExceptionCause.EXTENSION_REQUIRED, SipConstants.EXTENSION_PRECONDITION);
            } else if(isInitialInvite && !_100relExists) {
                log.debug("update():  Received initial INVITE with Precondition in sdp offer without 100rel option-tag");
                throw new PreconditionException(PreconditionExceptionCause.EXTENSION_REQUIRED, SipConstants.EXTENSION_100REL);
            } else {
                if(!isInitialInvite && !validInitialPreconditionOfferReceived) {
                    log.debug("update(): Prack or Update request received with precondition in SDP offer after an initial INVITE without precondition");
                    throw new PreconditionException(PreconditionExceptionCause.FORBIDDEN);
                }
            }
        } else {
            /**
             * precondition do not exist in sdp offer
             */
            if(requirePreconditionExists) {
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to missing precondition offer.");
            } 
            
            if(isInitialInvite) {
                // Received initial INVITE without precondition.
                // Assuming precondition are fulfilled and resource are reserved. 
                return true; 
            } else if (!validInitialPreconditionOfferReceived) {
                // Received PRACK or UPDATE without precondition after an initial INVITE without precondition.
                // Assuming precondition are fulfilled and resource are reserved. 
                return true; 
            } else {
                log.debug("update(): Received PRACK or UPDATE without precondition after an initial INVITE with precondition.");
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to missing precondition offer.");
            }
        }

        if(log.isDebugEnabled()) {
            log.debug("Updating local PreconditionStatusTable with remote sdp offer: " + currentStatus + desiredStatus + confirmStatus);
        }
        
        // Validate desired status (this should be done before processing current status to reject unknown precondition type)
        validateDesiredStatus(desiredStatus, isInitialInvite, requirePreconditionExists);
               
        //Validate confirm status (this should be done before processing current status to reject unknown precondition type) 
        validateConfirmStatus(confirmStatus);
        
        // Validate current status and update current remote status (this should be done last since the Local Status Table should only be updated if this offer is valid)
        validateAndUpdateCurrentStatus(currentStatus);
        
        // Valid precondition offer has been received
        if(isInitialInvite) {
            validInitialPreconditionOfferReceived = true;
        }
        lastUpdateContainsValidPrecondition = true;
        
        if(log.isDebugEnabled()) {
            log.debug("Updated local PreconditionStatusTable, remoteCurrentStatus: " + remoteCurrentStatus);
        }
        return isPreconditionFulfilled();
    }

    public SdpPreconditionCurr getRemoteCurrentStatus() {
        return remoteCurrentStatus;
    }
    
    public void setRemoteCurrentStatus(SdpPreconditionCurr remoteCurrentStatus) {
        this.remoteCurrentStatus = remoteCurrentStatus;
    }
    
    public SdpPreconditionDes getLocalDesiredStatus() {
        return localDesiredStatus;
    }

    public SdpPreconditionDes getRemoteDesiredStatus() {
        return remoteDesiredStatus;
    }
    
    public SdpPreconditionCurr getLocalCurrentStatus() {
        return localCurrentStatus;
    }
    
    /**
     * If a SIP response should include a Require header with the 'precondition' option tag and the precondition status table in the SDP answer.
     * @return true if a valid precondition SDP offer has been received for the last request and precondition applies
     */
    public boolean isPreconditionRequire() {
        return lastUpdateContainsValidPrecondition;
    }
    
    /**
     * Reset flag stating if the response MUST contain a Require header with the 'precondition' option tag and 
     * the precondition status table in the SDP answer after sending the response
     */
    public void resetPreconditionRequire() {
        this.lastUpdateContainsValidPrecondition = false;
    }
    
    /** 
     * 
     * @return true if the current status reaches or surpasses the threshold set by the desired status
     */
    private boolean isPreconditionFulfilled() {
        boolean result = false;
        
        try {
            //Only validate remote status since precondition for local status are always reached
            if(remoteCurrentStatus.isPreconditionReached(remoteDesiredStatus)) {
                result = true;
            }
        } catch(IllegalArgumentException iae) {
            log.warn("IllegalArgumentException in isPreconditionFulfilled()", iae);
        }
        
        return result;
    }
    
    /**
     * 
     * @param desiredStatus All Des SDP attributes
     * @param isInitialInvite if the precondition are part of the initial INVITE
     * @param requirePreconditionExists If the SIP request contains a Require header with the 'precondition' option tag
     * @throws PreconditionException If something is wrong with desiredStatus
     */
    private void validateDesiredStatus(Vector<SdpPreconditionDes> desiredStatus, boolean isInitialInvite, boolean requirePreconditionExists) throws PreconditionException {
        StatusFound uacLocalStatus = StatusFound.NOT_FOUND;
        StatusFound uacRemoteStatus = StatusFound.NOT_FOUND;

        //Parse all Des attributes to generate desired status table
        for(SdpPreconditionDes des : desiredStatus) {
            if (!SdpPrecondition.PRECONDITION_TYPE_QOS.equalsIgnoreCase(des.getPreconditionType())) {
                log.debug("validateDesiredStatus(): Unknown precondition-type received, only 'qos' is supported");
                throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE, generateSdpPreconditionUnknown(des));
            }

            if(des.getStrengthTag() == StrengthTag.MANDATORY) {
                // Fix for Samsung S6 and LG G4 (at least) - that use Supported (and NOT Require) for precondition in the
                // SIP header AND use des: mandatory strength for local in the SDP...
                // Bypassing this verification.                
                //if(isInitialInvite && !requirePreconditionExists) {
                //    log.debug("validateDesiredStatus(): Received INVITE with precondition attribute with mandatory strength-tag without Require:precondition header");
                //    throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE, generateSdpPreconditionFailure(des));
                //}
            } else {
                // Validate status was not down-graded
                if(!isInitialInvite) {
                    // Let us be more permissive and accept optional for remote (for the UAS)...
                    if(des.getStatusType()==StatusType.REMOTE || des.getStrengthTag()==StrengthTag.OPTIONAL) {
                        log.debug("validateDesiredStatus(): Accepting PRACK or UPDATE with remote desired strength optional (fix for iPhone problem)");
                    } else {
                        // desired status always upgraded to mandatory in answer, 
                        log.debug("validateDesiredStatus(): Received PRACK or UPDATE with down-graded precondition strength, rejecting...");
                        throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE, generateSdpPreconditionFailure(des));
                    }
                }
            }

            switch(des.getStatusType()) {
                case LOCAL:
                    uacLocalStatus = updateDesiredStatus(des.getDirectionTag(), uacLocalStatus);
                    break;
                case REMOTE:
                    uacRemoteStatus = updateDesiredStatus(des.getDirectionTag(), uacRemoteStatus);
                    break;
                default: //E2E
                    log.debug("validateDesiredStatus(): precondition status-type 'e2e' is not supported, rejecting...");
                    throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE, generateSdpPreconditionFailure(des));
            }
        }
        
        // Validate if offer is supported
        if(uacLocalStatus == StatusFound.NOT_FOUND || uacRemoteStatus == StatusFound.NOT_FOUND) {
            log.debug("validateDesiredStatus(): Missing precondition desired attribute, rejecting...");
            throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
        } else if(uacLocalStatus != StatusFound.SENDRECV) {
            log.debug("validateDesiredStatus(): MAS only support desired precondition offer with direction tag sendrecv, rejecting...");
            throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE,
                                            generateSdpPreconditionFailure(StatusType.LOCAL, DirectionTag.valueOf(uacLocalStatus.name())));
        } else if(uacRemoteStatus != StatusFound.SENDRECV) {
            log.debug("validateDesiredStatus(): MAS only support desired precondition offer with direction tag sendrecv, rejecting...");
            throw new PreconditionException(PreconditionExceptionCause.PRECONDITION_FAILURE,
                                            generateSdpPreconditionFailure(StatusType.REMOTE, DirectionTag.valueOf(uacRemoteStatus.name())));
        }
    }
    
    /**
     * Validate current status and update current remote status 
     * @param currentStatus All Curr SDP attributes
     * @throws PreconditionException If something is wrong with currentStatus
     */
    private void validateAndUpdateCurrentStatus(Vector<SdpPreconditionCurr> currentStatus) throws PreconditionException {
        StatusFound uacLocalStatus = StatusFound.NOT_FOUND;
        StatusFound uacRemoteStatus = StatusFound.NOT_FOUND;
        
        for(SdpPreconditionCurr curr : currentStatus) {
            if (!SdpPrecondition.PRECONDITION_TYPE_QOS.equalsIgnoreCase(curr.getPreconditionType())) {
                // Unknown precondition type should have already been rejected when validating Desired Status. 
                // If this happens, it means the sdp precondition offer contains a Current Status line without a matching desired status
                log.debug("validateAndUpdateCurrentStatus(): Unknown precondition-type received, only 'qos' is supported");
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
            }
            
            switch(curr.getStatusType()) {
                case LOCAL:
                    if(uacLocalStatus != StatusFound.NOT_FOUND) {
                        log.debug("validateAndUpdateCurrentStatus(): duplicate local current status found, reject offer");
                        throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to invalid precondition offer.");
                    } else {
                        uacLocalStatus = StatusFound.valueOf(curr.getDirectionTag().name());
                    }
                    break;
                case REMOTE:
                    if(uacRemoteStatus != StatusFound.NOT_FOUND) {
                        log.debug("validateAndUpdateCurrentStatus(): duplicate remote current status found, reject offer");
                        throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to invalid precondition offer.");
                    } else {
                        uacRemoteStatus = StatusFound.valueOf(curr.getDirectionTag().name());
                    }
                    break;
                case E2E:
                    // e2e status type should have already been rejected when validating Desired Status. 
                    // If this happens, it means the sdp precondition offer contains a Current Status line without a matching desired status
                    log.debug("validateAndUpdateCurrentStatus(): precondition status-type 'e2e' is not supported, rejecting...");
                    throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
            }
        }
        
        if(uacLocalStatus == StatusFound.NOT_FOUND || uacRemoteStatus == StatusFound.NOT_FOUND) {
            log.debug("validateAndUpdateCurrentStatus(): Missing precondition current attribute, rejecting...");
            throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
        }
        
        // Validate status was not down-graded
        if(this.validInitialPreconditionOfferReceived) {
            // Compare MAS localCurrentStatus with client SDP offer (remoteStatus) 
            if(uacRemoteStatus != StatusFound.SENDRECV) { // MAS localCurrentStatus direction-tag is always SENDRECV
                log.debug("validateAndUpdateCurrentStatus(): uacRemoteStatus was downgraded, rejecting...");
                throw new PreconditionException(PreconditionExceptionCause.FORBIDDEN);
            }
            
            // Compare MAS remoteCurrentStatus with client SDP offer (localStatus) 
            StatusFound previousRemoteStatus = StatusFound.valueOf(remoteCurrentStatus.getDirectionTag().name());
            if(uacLocalStatus == StatusFound.SENDRECV || previousRemoteStatus == StatusFound.NONE || uacLocalStatus == previousRemoteStatus) {
                //offer is the same or upgraded, nothing to do...
                
            } else {
                //offer was down-graded, reject
                log.debug("validateAndUpdateCurrentStatus(): uacLocalStatus was downgraded, rejecting...");
                throw new PreconditionException(PreconditionExceptionCause.FORBIDDEN);
            }
        }

        // Update remote current status with the client local status (current local status in offer is current remote status for local precondition status table)
        remoteCurrentStatus = new SdpPreconditionCurr(SdpPrecondition.PRECONDITION_TYPE_QOS, StatusType.REMOTE, DirectionTag.valueOf(uacLocalStatus.name()));
    }
    
    /**
     * Validate confirm status
     * @param confirmStatus All Conf SDP attributes
     * @throws PreconditionException If something is wrong with confirmStatus
     */
    private void validateConfirmStatus(Vector<SdpPreconditionConf> confirmStatus) throws PreconditionException {
        
        for(SdpPreconditionConf conf : confirmStatus) {
            if (!SdpPrecondition.PRECONDITION_TYPE_QOS.equalsIgnoreCase(conf.getPreconditionType())) {
                // Unknown precondition type should have already been rejected when validating Desired Status. 
                // If this happens, it means the sdp precondition offer contains a Confirm Status line without a matching desired status
                log.debug("validateConfirmStatus(): Unknown precondition-type received, only 'qos' is supported");
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
            }
            
            switch(conf.getStatusType()) {
                case LOCAL:
                    //why is the uac asking us to confirm his own status? ignoring...
                    if(log.isDebugEnabled()) {
                        log.debug("validateConfirmStatus: received precondition conf line for LOCAL status-type :" + conf);
                    }
                    break;
                case REMOTE:
                    //MAS always answer SENDRECV, nothing to do...
                    if(log.isDebugEnabled()) {
                        log.debug("validateConfirmStatus: received precondition conf line for REMOTE status-type :" + conf);
                    }
                    break;
                case E2E:
                    // Invalid status type should have already been rejected when validating Desired Status. 
                    // If this happens, it means the sdp precondition offer contains a Confirm Status line without a matching desired status
                    log.debug("validateConfirmStatus(): precondition status-type 'e2e' is not supported, rejecting...");
                    throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to incomplete precondition offer.");
            } 
        }
    }
    
    private enum StatusFound {
        NOT_FOUND, SEND, RECV, SENDRECV, NONE;
    }
    /**
     * <p>
     * The following transition are supported: <br>
     * <table  border="1">
     * <tr><td> StatusFound previousStatus  </td><td>  DirectionTag desiredDirection </td><td> Action </td></tr>
     * <tr><td> NOT_FOUND </td><td> NONE/SEND/RECV/SENDRECV </td><td> Returns NONE/SEND/RECV/SENDRECV (the same as desiredDirection) </td></tr>
     * <tr><td> SEND </td><td> SEND </td><td> SENDRECV </td></tr>
     * <tr><td> RECV </td><td> SEND </td><td> SENDRECV </td></tr>
     * <tr><td colspan="2"> Anything else </td><td> throws PreconditionException (BAD_REQUEST) </td></tr>
     * </table>
     */
    private StatusFound updateDesiredStatus(DirectionTag desiredDirection, StatusFound previousStatus) throws PreconditionException {
        StatusFound status = StatusFound.NOT_FOUND;
        if(previousStatus == StatusFound.NOT_FOUND) {
            status = StatusFound.valueOf(desiredDirection.name());
        } else {
            // Segmented desired status on multiple lines
            if((desiredDirection == DirectionTag.SEND && previousStatus == StatusFound.RECV) || (desiredDirection == DirectionTag.RECV && previousStatus == StatusFound.SEND)) {
                status = StatusFound.SENDRECV;
            } else {
                log.debug("updateDesiredStatus(): duplicate desired status found, reject offer");
                throw new PreconditionException(PreconditionExceptionCause.BAD_REQUEST, "The request could not be understood due to invalid precondition offer.");
            }
        }
        return status;
    }
    
    private String generateSdpPreconditionUnknown(SdpPreconditionDes des) {
        return new SdpPreconditionDes(des.getPreconditionType(), StrengthTag.UNKNOWN, des.getStatusType(), des.getDirectionTag()).toString();
    }
  
    private String generateSdpPreconditionFailure(SdpPreconditionDes des) {
        return new SdpPreconditionDes(des.getPreconditionType(), StrengthTag.FAILURE, des.getStatusType(), des.getDirectionTag()).toString();
    }
    
    private String generateSdpPreconditionFailure(StatusType type, DirectionTag dir) {
        return new SdpPreconditionDes(SdpPrecondition.PRECONDITION_TYPE_QOS, StrengthTag.FAILURE, type, dir).toString();
    }

}
