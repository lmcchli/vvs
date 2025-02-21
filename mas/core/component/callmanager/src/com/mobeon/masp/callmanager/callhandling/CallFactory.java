/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.sdp.SdpSessionDescriptionFactory;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.CallParameterRetrieval;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 *  This class can be used to create inbound or outbound calls.
 *
 * @author Malin Nyfeldt
 */
public class CallFactory {

    private static final ILogger log =
            ILoggerFactory.getILogger(CallFactory.class);

    
    public static SubscribeCallImpl createSubscribeCall(SipRequestEvent sipRequestEvent) throws CallException {
    	CallManagerConfiguration config = ConfigurationReader.getInstance().getConfig();
      SubscribeCallImpl call = new SubscribeCallImpl(sipRequestEvent, config);
    	call.queueEvent(sipRequestEvent);
    	return call;
    }
    /**
     * Creates a new inbound call from a SipRequestEvent.
     * @param sipRequestEvent The SipRequestEvent MUST NOT be null.
     * @return A new inbound call is created and returned.
     * @throws CallException
     *      CallException is thrown if the call could not be created.
     */
    public static InboundCallImpl createInboundCall(
            SipRequestEvent sipRequestEvent)
            throws CallException {
    	 
    	

        if (sipRequestEvent.getDialog() == null)
            throw new CallException(
                    "Inbound call could not be created. Dialog is null.");
        Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallFactory.createInboundCall");
        try {
            CallManagerConfiguration config =
                    ConfigurationReader.getInstance().getConfig();

            CallParameters callParameters = CallParameterRetrieval.getInstance().
                    retrieveCallParameters(sipRequestEvent, config);

            InboundCallImpl call = new InboundCallImpl(
                    createSdpFactory(),
                    sipRequestEvent.getSipMessage().getCallInfoType(),
                    sipRequestEvent.getDialog(),
                    callParameters,
                    CMUtils.getInstance().getServiceName(),
                    CMUtils.getInstance().getApplicationManagement(),
                    CMUtils.getInstance().getSessionFactory().create(),
                    CMUtils.getInstance().getProtocol(),
                    sipRequestEvent,
                    config);


            PChargingVectorHeader pChargingVector = (PChargingVectorHeader)
                    sipRequestEvent.getRequest().getHeader(
                            PChargingVectorHeader.NAME);

            if (pChargingVector != null) {
                // Set the received P-Charging-Vector for the call
                // Add our configured value as term-ioi if it isn't set already (or localhost if not configured)
                if (pChargingVector.getTerminatingIOI() == null) {
                    String termIOI = ConfigurationReader.getInstance().getConfig().getTermIOI();
                    if(termIOI==null || (termIOI.equals(""))){
                        termIOI=CMUtils.getInstance().getLocalHost();
                    }
                    pChargingVector.setTerminatingIOI(termIOI);
                }
                call.setPChargingVector(pChargingVector);

            }
          //if configured to generate p-charging-vector
            else if (!call.getConfig().getDisablePChargingVectorGeneration()){
                // If we did not receive a P-Charging-Vector in the
                // request we will create our own.
                pChargingVector = CMUtils.getInstance().getSipHeaderFactory().
                        createPChargingVectorHeader(true);
                call.setPChargingVector(pChargingVector);

                // Add the P-Charging-Vector to the incoming request
                // TODO: Note that this will alter the incoming request.
                // This must be changed if the incoming request must be kept intact.
                // Currently this is the simplest solution to make the
                // new P-Charging-Header available when sending responses.
                sipRequestEvent.getRequest().addHeader(pChargingVector);
                
            }

            // Insert call in call dispatcher
            CMUtils.getInstance().getCallDispatcher().insertInboundCall(
                    call, sipRequestEvent);

            if (log.isInfoEnabled())
                log.info("A new inbound call is created. " + call);

            // Queue the request in the call to start executing
            sipRequestEvent.enterCheckPoint("MAS.CM.In.SIPReqEvent.QCall");
            call.queueEvent(sipRequestEvent);

            return call;
        } catch (Exception e) {
            throw new CallException(
                    "Inbound call could not be created. Message: " +
                            e.getMessage(), e);
        } finally {
	       	 if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	             CommonOamManager.profilerAgent.exitCheckpoint(perf);
	         }  
        }
    }


    /**
     * Creates a new outbound call from given callProperties.
     * @param callProperties
     * @param eventDispatcher
     * @param session
     * @param callId
     * @return A new outbound call is created and returned.
     * @throws CallException
     *      CallException is thrown if the call could not be created.
     */
    public static OutboundCallImpl createOutboundCall(
            CallProperties callProperties, IEventDispatcher eventDispatcher,
            ISession session, String callId) throws CallException {

        try {
            OutboundCallImpl call = new OutboundCallImpl(
                    callProperties,
                    eventDispatcher,
                    session,
                    callId,
                    createSdpFactory(),
                    ConfigurationReader.getInstance().getConfig());

            // Adding P-Charging-Vector
            PChargingVectorHeader chargingVector =
                    CMUtils.getInstance().getSipHeaderFactory().
                    createPChargingVectorHeader(false);
            call.setPChargingVector(chargingVector);

            if (log.isDebugEnabled())
                log.debug("A new outbound call is created. " + call);

            return call;
        } catch (Exception e) {
            throw new CallException(
                    "Outbound call could not be created. Message: " +
                            e.getMessage(), e);
        }
    }

    /**
     * Creates and initializes an SDP factory used to create and read
     * SDP information in SIP messages.
     * Each call must have one own instance of an SDP factory.
     * @return A new SdpSessionDescriptionFactory is created and returned.
     * @throws SdpInternalErrorException
     *  An SdpInternalErrorException is returned in the SDP factory could not
     *  be initialized due to problems in the SIP/SDP stack.
     */
    private static SdpSessionDescriptionFactory createSdpFactory()
            throws SdpInternalErrorException {
        SdpSessionDescriptionFactory sdpFactory = new SdpSessionDescriptionFactory();
        sdpFactory.init();
        return sdpFactory;
    }

}
