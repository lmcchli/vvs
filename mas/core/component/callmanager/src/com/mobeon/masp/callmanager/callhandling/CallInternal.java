/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.sdp.SdpSessionDescription;
import com.mobeon.masp.callmanager.sdp.SdpIntersection;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.callmanager.sessionestablishment.PreconditionStatusTable;
import com.mobeon.masp.callmanager.sessionestablishment.UnicastStatus;
import com.mobeon.masp.callmanager.CallManagerLicensingException;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.common.eventnotifier.Event;

import javax.sip.Dialog;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * This class is used from {@link InboundCallInternal} and
 * {@link OutboundCallInternal} to interface the {@link InboundCallImpl} and
 * {@link OutboundCallImpl} classes. It collect common methods for inbound and
 * outbound calls.
 * <p>
 * It is used for test purposes only, i.e. to be able to mock a call
 * during basic test.
 *
 * @author Malin Flodin
 */
public interface CallInternal {

    // Configuration for the call
    public CallManagerConfiguration getConfig();


    // Call timers
    public void startNoAckTimer();
    public void cancelNoAckTimer();


    // Call handling related
    public void errorOccurred(String message, boolean alreadyDisconnected);
    public void setCallType(CallProperties.CallType callType);
    public void retrieveCallTypeFromConfiguration();


    // Environment related
    public void fireEvent(Event event);
    public void registerToReceiveEvents();
    public CallMediaTypes[] getOutboundCallMediaTypes();
    public CallMediaTypes[] getConfiguredOutboundCallMediaTypes();


    // Media control related
    public void parseMediaControlResponse(SipMessage sipMessage);
    public boolean containsMediaControl(SipRequestEvent sipRequestEvent);
    public boolean containsSdp(SipRequestEvent sipRequestEvent);

    // SDP related
    public String getLocalSdpAnswer();
    public String getLocalSdpOffer();
    public SdpSessionDescription getRemoteSdp();
    public SdpSessionDescription getPendingRemoteSdp();
    public void parseRemoteSdp(SipMessage sipMessage) throws SdpNotSupportedException;
    public void checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp()
            throws SdpNotSupportedException;
    public void checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp()
            throws SdpNotSupportedException;

    public SdpIntersection findSdpIntersection(
            CallMediaTypes[] callMediaTypes, boolean clearMediaInSession) throws SdpInternalErrorException;
    public SdpIntersection getSdpIntersection();

    public String createSdpAnswer(
            SdpIntersection sdpIntersection,
            ConnectionProperties connectionProperties)
            throws SdpInternalErrorException;
    public String createSdpOffer(ConnectionProperties connectionProperties)
            throws SdpInternalErrorException;
    
    public PreconditionStatusTable getPreconditionStatusTable();
    public UnicastStatus getUnicastStatus();

    // SIP related
    public void sendErrorResponse(int responseType,
                                  SipRequestEvent sipRequestEvent,
                                  String message);
    public void sendNotAcceptableHereResponse(
            SipRequestEvent sipRequestEvent, SipWarning warning);
    public void sendMethodNotAllowedResponse(
            SipRequestEvent sipRequestEvent);
    public void sendOkResponse(
            SipRequestEvent sipRequestEvent, boolean alreadyDisconnected);


    // Stream related
    public ConnectionProperties createInboundStream(SdpIntersection sdpIntersection)
    	throws StackException, SdpInternalErrorException, CallManagerLicensingException;
    public void createOutboundStream(SdpIntersection sdpIntersection)
            throws StackException, SdpInternalErrorException,
            IllegalStateException;
    public void deleteStreams();
    public void deleteOutboundStream();

    public void playOnOutboundStream(PlayEvent playEvent);
    public void recordOnInboundStream(RecordEvent recordEvent);
    public void sendTokens(ControlToken[] tokens);
    public void stopOngoingPlay(StopPlayEvent stopPlayEvent);
    public void stopOngoingRecord(StopRecordEvent stopRecordEvent);
    public void reNegotiatedSdpOnInboundStream(SdpIntersection sdpIntersection)
            throws SdpInternalErrorException;
    public ConnectionProperties getInboundConnectionProperties();

    public boolean isCallJoined();
    public CallToCall getJoinedToCall();


    // Used during transfer to keep track of the pending requests
    public void addPendingRequest(String id, SipRequestEvent sipRequestEvent);
    public SipRequestEvent getPendingRequest(String id);


    // For call dispatching
    public String getInitialDialogId();
    public String getEstablishedDialogId();
    public void setInitialDialogId(String initialDialogId);
    public void setEstablishedDialogId(String establishedDialogId);
    public boolean isEarlyDialogActive();
    public void inactivateEarlyDialog();

    // TODO: Drop 6! Can the need for this be removed?
    public Dialog getDialog();
    public void setDialog(Dialog dialog);

    // For debug info
    public CallState getCurrentState();

    public void addFarEndConnection(String protocol, String host, int port);

    // mmath: We need a place to store the P-Charging-Vector during a dialog
    public PChargingVectorHeader getPChargingVector();

}
