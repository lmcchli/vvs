/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.DiversionParty;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.RestrictedOutboundHeaders;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;

import javax.sip.InvalidArgumentException;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.ClientTransaction;
import javax.sip.message.Response;
import javax.sip.header.CallIdHeader;
import javax.sip.header.TooManyHopsException;
import javax.sip.address.SipURI;
import javax.sip.address.URI;

import java.io.UnsupportedEncodingException;

import java.io.IOException;
import java.text.ParseException;

import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * Interface towards the SIP request factory. Creating new SIP requests is done
 * using this interface. Only used to simplify basic test.
 *
 * @author Malin Flodin
 */
public interface SipRequestFactory {

    // An enum containing the various types of INFO requests that can be sent.
    public enum InfoType {
        VIDEO_FAST_UPDATE
    }

    // Methods to create requests
    public SipRequest createAckRequest(Dialog dialog) throws SipException;

    public SipRequest createByeRequest(Dialog dialog,
                                       PChargingVectorHeader pChargingVector)
            throws SipException, ParseException;

    public SipRequest createCancelRequest (ClientTransaction transaction)
            throws SipException;

    public SipRequest createInviteRequest(SipURI remoteParty,
                                          CalledParty to,
                                          CallingParty from,
                                          DiversionParty diversion,
                                          String userAgent,
                                          int expiration,
                                          Boolean preventLoopback,
                                          String sdp, String callId,
                                          PChargingVectorHeader pChargingVector,
                                          RestrictedOutboundHeaders restrictedHeaders)
            throws InvalidArgumentException, ParseException;

    public SipRequest createInfoRequest(Dialog dialog,
                                        InfoType type,
                                        String body,
                                        PChargingVectorHeader pChargingVector)
            throws SipException, ParseException;

    public SipRequest createNewInviteRequest(SipURI contact,
                                             SipRequest initialInviteRequest,
                                             String userAgent,
                                             CallPartyDefinitions.PresentationIndicator pi,
                                             boolean preventLoopback,
                                             String callId,
                                             PChargingVectorHeader pChargingVector)
            throws InvalidArgumentException, ParseException;

    public SipRequest createOptionsRequest(String localHost, int localPort,
                                           String remoteHost, int remotePort,
                                           int cSeq)
            throws InvalidArgumentException, ParseException;

    /**
     * Creates a PRACK request for the given <param>response</param> within
     * the given <param>dialog</param>.
     *
     * @param dialog        The dialog within which the PRACK should be sent.
     * @param response      The reliable provisional response for which the PRACK
     * should be sent.
     * @param pChargingVector
     * @return              Returns the created PRACK request.
     * @throws SipException SipException is thrown if the request could not be
     *                      created, for example if the response is not sent
     *                      reliably.
     */
    public SipRequest createPrackRequest(Dialog dialog, Response response,
                                         PChargingVectorHeader pChargingVector)
            throws SipException, ParseException;

    public SipRequest createRegisterRequest(String registeredName,
                                            String remoteHost, int remotePort,
                                            int cSeq, CallIdHeader callIdHeader,
                                            Integer expires)
            throws ParseException, InvalidArgumentException;


    /**
     * Create a NOTIFY request of given event type.
     *
     * @param event - The type of event sent. Currently only "message-summary"
     * is supported.
     * @param sendTo - URI to send the request to. This will populate the
     * Request-URI and the To header.
     * @param sentFrom - URI from where the request is sent. This will populate
     * the From header and the Contact header.
     * @param cSeq
     * @param callIdHeader
     * @param body - The message body to include or null if no body should be
     * included. The Type of event will (for now) decide which type of
     * Content type to use for the body.
     * @param pChargingVector
     * @return a NOTIFY request.
     * @throws ParseException
     * @throws InvalidArgumentException
     */
    public SipRequest createNotifyRequest(String event,
                                          URI sendTo,
                                          URI sentFrom,
                                          int cSeq,
                                          CallIdHeader callIdHeader,
                                          String body,
                                          PChargingVectorHeader pChargingVector)
            throws ParseException, InvalidArgumentException;


    /**
     * Create Notify from dialog info file created from initial subscribe (solicited notifications)
     * @param event
     * @param dialogInfoFile
     * @param body
     * @return
     */
    public SipRequest createNotifyRequest(String mailboxId, 
                                          String dialogInfoFile, 
    		                              String body,
    		                              PChargingVectorHeader pChargingVector) 
            throws ParseException, InvalidArgumentException, IOException, SipException, TrafficEventSenderException; 


    /**
     * Create a SIP INVITE request based on a SipRequest
     * 
     * @param RemotePartyAddress
     * @param sourceSipRequestEvent
     * @return SipRequest
     */
    public SipRequest createSipRequest(RemotePartyAddress uas, SipRequestEvent sourceSipRequestEvent)
        throws ParseException, InvalidArgumentException, UnsupportedEncodingException, TooManyHopsException;



}

    
