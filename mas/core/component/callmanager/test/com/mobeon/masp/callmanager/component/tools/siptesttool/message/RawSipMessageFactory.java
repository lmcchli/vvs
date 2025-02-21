/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.message;

import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;

import java.util.HashMap;
import java.security.NoSuchAlgorithmException;

/**
 * Contains static methods to create raw SIP messages based on previously
 * received SIP messages.
 * @author Malin Nyfeldt
 */
public class RawSipMessageFactory {

    public static RawSipMessage createAckFromOk(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage ok) throws NoSuchAlgorithmException {
        RawSipMessage ack = new RawSipMessage(
                SipToolConstants.ackRequest, messageParameters);
        ack.setParameter(RawSipMessageParameter.LOCAL_TAG, ok.getFromTag());
        ack.setParameter(RawSipMessageParameter.REMOTE_TAG, ok.getToTag());
        ack.setParameter(RawSipMessageParameter.CALL_ID, ok.getCallId());
        return ack;
    }

    public static RawSipMessage createAckFromNonOk(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage response) throws NoSuchAlgorithmException {
        RawSipMessage ack = new RawSipMessage(
                SipToolConstants.ackRequestSameTransaction, messageParameters);
        
        ack.setParameter(
                RawSipMessageParameter.LAST_CALL_ID,
                response.getCallIdHeader());
        ack.setParameter(
                RawSipMessageParameter.LAST_FROM,
                response.getFromHeader());
        ack.setParameter(
                RawSipMessageParameter.LAST_TO,
                response.getToHeader());
        ack.setParameter(
                RawSipMessageParameter.LAST_VIA,
                response.getViaHeader());
        
        return ack;
    }

    public static RawSipMessage createPrackFromProvisionalResponse(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage response) throws NoSuchAlgorithmException {
        RawSipMessage prack = new RawSipMessage(
                SipToolConstants.prackRequest, messageParameters);
        prack.setParameter(RawSipMessageParameter.LOCAL_TAG, response.getFromTag());
        prack.setParameter(RawSipMessageParameter.REMOTE_TAG, response.getToTag());
        prack.setParameter(RawSipMessageParameter.CALL_ID, response.getCallId());
        prack.setParameter(RawSipMessageParameter.RESPONSE_CSEQ, response.getCSeq());
        prack.setParameter(RawSipMessageParameter.RESPONSE_RSEQ, response.getRSeq());
        return prack;
    }

    public static RawSipMessage createByeFromOk(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage ok) throws NoSuchAlgorithmException {
        RawSipMessage bye = new RawSipMessage(
                SipToolConstants.byeRequestForInboundCall, messageParameters);
        bye.setParameter(RawSipMessageParameter.LOCAL_TAG, ok.getFromTag());
        bye.setParameter(RawSipMessageParameter.REMOTE_TAG, ok.getToTag());
        bye.setParameter(RawSipMessageParameter.CALL_ID, ok.getCallId());
        return bye;
    }

    public static RawSipMessage createOptionsFromOk(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage ok) throws NoSuchAlgorithmException {
        RawSipMessage options = new RawSipMessage(
                SipToolConstants.optionsRequestForInboundCall, messageParameters);
        options.setParameter(RawSipMessageParameter.LOCAL_TAG, ok.getFromTag());
        options.setParameter(RawSipMessageParameter.REMOTE_TAG, ok.getToTag());
        options.setParameter(RawSipMessageParameter.CALL_ID, ok.getCallId());
        return options;
    }

    public static RawSipMessage createOkFromBye(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage bye)
            throws NoSuchAlgorithmException {
        RawSipMessage ok = new RawSipMessage(
                SipToolConstants.okResponseForBye, messageParameters);
        ok.setParameter(
                RawSipMessageParameter.LAST_CALL_ID,
                bye.getCallIdHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_CSEQ,
                bye.getCSeqHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_FROM,
                bye.getFromHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_TO,
                bye.getToHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_VIA,
                bye.getViaHeader());
        return ok;
    }

    public static RawSipMessage createTryingFromInvite(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage invite, String localTag)
            throws NoSuchAlgorithmException {
        RawSipMessage trying = new RawSipMessage(
                SipToolConstants.tryingResponse, messageParameters);
        trying.setParameter(
                RawSipMessageParameter.LAST_CALL_ID,
                invite.getCallIdHeader());
        trying.setParameter(
                RawSipMessageParameter.LAST_CSEQ,
                invite.getCSeqHeader());
        trying.setParameter(
                RawSipMessageParameter.LAST_FROM,
                invite.getFromHeader());
        trying.setParameter(
                RawSipMessageParameter.LAST_TO,
                invite.getToHeader());
        trying.setParameter(
                RawSipMessageParameter.LAST_VIA,
                invite.getViaHeader());
        if (localTag != null)
            trying.setParameter(
                    RawSipMessageParameter.LOCAL_TAG, localTag);
        return trying;
    }

    public static RawSipMessage createRingingFromInvite(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage invite,
            String localTag)
            throws NoSuchAlgorithmException {
        RawSipMessage ringing = new RawSipMessage(
                SipToolConstants.ringingResponse, messageParameters);
        ringing.setParameter(
                RawSipMessageParameter.LAST_CALL_ID,
                invite.getCallIdHeader());
        ringing.setParameter(
                RawSipMessageParameter.LAST_CSEQ,
                invite.getCSeqHeader());
        ringing.setParameter(
                RawSipMessageParameter.LAST_FROM,
                invite.getFromHeader());
        ringing.setParameter(
                RawSipMessageParameter.LAST_TO,
                invite.getToHeader());
        ringing.setParameter(
                RawSipMessageParameter.LAST_VIA,
                invite.getViaHeader());
        if (localTag != null)
            ringing.setParameter(
                    RawSipMessageParameter.LOCAL_TAG, localTag);
        return ringing;
    }

    public static RawSipMessage createOkFromInvite(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage invite, String localTag)
            throws NoSuchAlgorithmException {
        RawSipMessage ok = new RawSipMessage(
                SipToolConstants.okResponseForInvite, messageParameters);
        ok.setParameter(
                RawSipMessageParameter.LAST_CALL_ID,
                invite.getCallIdHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_CSEQ,
                invite.getCSeqHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_FROM,
                invite.getFromHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_TO,
                invite.getToHeader());
        ok.setParameter(
                RawSipMessageParameter.LAST_VIA,
                invite.getViaHeader());
        if (localTag != null)
            ok.setParameter(
                    RawSipMessageParameter.LOCAL_TAG, localTag);
        return ok;
    }

    public static RawSipMessage createByeFromAck(
            HashMap<RawSipMessageParameter, String> messageParameters,
            RawSipMessage ack)
            throws NoSuchAlgorithmException {
        RawSipMessage bye = new RawSipMessage(
                SipToolConstants.byeRequestForOutboundCall, messageParameters);
        bye.setParameter(RawSipMessageParameter.LOCAL_TAG, ack.getToTag());
        bye.setParameter(RawSipMessageParameter.REMOTE_TAG, ack.getFromTag());
        bye.setParameter(RawSipMessageParameter.CALL_ID, ack.getCallId());
        return bye;
    }

}
