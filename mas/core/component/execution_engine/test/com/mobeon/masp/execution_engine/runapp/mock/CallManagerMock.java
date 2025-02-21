package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.events.UnjoinedEvent;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.execution_engine.ServiceEnabler;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGenerator;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;

import jakarta.activation.MimeType;
import java.util.concurrent.ExecutorService;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 * CallManager mock for junit testing of the execution engine
 *
 * @author Tomas Stenlund
 */
public class CallManagerMock extends BaseMock implements CallManager, ServiceEnabler {

    private String host;
    private int port;
    private String service;

    /**
     * Holds the applicationManagement
     */
    IApplicationManagment applicationManagement;

    /**
     * Holds the stream factory
     */
    IStreamFactory streamFactory;

    private boolean earlyMediaInProgressing = false;
    private ExecutorService executorService;

    private Set<Connection> outboundFarEndConnections = new HashSet<Connection>();
    private Set<Connection> inboundFarEndConnections = new HashSet<Connection>();
    private boolean sendProgressingEvent = true;
    private SipMessageResponseEvent sipMessageResponseEvent;
    private boolean sendPlayFailedAfterDelay;
    private int delayBeforePlayFailed;

    public ServiceEnablerOperate initService(String service, String host, int port) throws ServiceEnablerException {
        log.info("MOCK: CallManagerMock.initService"+service+host+port);
        this.host = host;
        this.port = port;
        this.service = service;
        return new ServiceEnablerOperateMock(service, host, port);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setSipMessageResponseEvent(SipMessageResponseEvent sipMessageResponseEvent) {
        this.sipMessageResponseEvent = sipMessageResponseEvent;
    }

    public enum EventType {NONE,
        ERROR_NOT_ALLOWED, ERROR_CONNECTION, PROXY_EVENT, CONNECTED_EVENT, DISCONNECTED_EVENT, FAILED_EVENT,
        EARLYMEDIAAVAILABLE_EVENT, EARLYMEDIAFAILED_EVENT, RECORD_FINISHED, RECORD_FAILED,
        WITHHOLD     // meaning: make no response at all
    };
    private EventType outboundCallEventAfterConnected = EventType.NONE;
    int milliSecondsUntilGeneration;

    private EventType responseToCreateCall = EventType.CONNECTED_EVENT;

    private EventType responseToNegotiateEarlyMedia = EventType.EARLYMEDIAAVAILABLE_EVENT;
    private EventType responseToAccept = EventType.CONNECTED_EVENT;
    private EventType responseToRecord = EventType.RECORD_FINISHED;
    private EventType responseToProxy = EventType.PROXY_EVENT;

    private boolean withholdJoinAttempt = false;
    private boolean withholdUnjoinAttempt = false;
    private boolean withholdRejectAttempt = false;
    private boolean withholdDisconnectAttempt = false;
    private boolean withholdEarlyMediaAttempt = false;
    private boolean withholdsendSIPMessage = false;



    private int delayBeforeResponseToCreateCall = 0;
    private int delayBeforeResponseAccept = 0;
    private int delayBeforeResponseToDisconnect = 0;

    private Object extraDataForResponseToCreateCall = null;

    /**
     * Create the call manager mock
     */
    public CallManagerMock() {
        super ();
        log.info ("MOCK: CallManagerMock.CallManagerMock");
    }

    /*
    * Neede for autowiring
    */
    public void init() throws ServiceEnablerException {
        if ((getApplicationManagment() == null) ||
                (getStreamFactory() == null)) {
            throw new IllegalStateException("Init was called prior to " +
                    "setting necessary fields. ApplicationManagement: " +
                    getApplicationManagment() + " , StreamFactory: " +
                    getStreamFactory());
        }
    }

    /**
     * Simulates an outbound call from the execution engine
     *
     * @param callProperties Properties for the call
     * @param eventDispatcher The dispatcher associated with the application
     * @param session The session associated with the application
     * @return a Call
     */
    public OutboundCall createCall(CallProperties callProperties,
                                   IEventDispatcher eventDispatcher,
                                   ISession session)
    {
        log.info("BTC: CallManagerMock.createCall");
        CallingParty callingParty = callProperties.getCallingParty();
        if(callingParty != null){
            String number = callingParty.getTelephoneNumber();
            log.info("BTC: CallManagerMock.createCall:CallingNumber="+number);
            CallPartyDefinitions.PresentationIndicator presentationIndicator = callingParty.getPresentationIndicator();
            log.info("BTC: CallManagerMock.createCall:PresentationIndicator="+presentationIndicator);
            log.info("BTC: CallManagerMock.createCall:callType="+callProperties.getCallType());
            log.info("BTC: CallManagerMock.createCall:outboundcallserverhost="+callProperties.getOutboundCallServerHost());
            log.info("BTC: CallManagerMock.createCall:outboundcallserverport="+callProperties.getOutboundCallServerPort());
        }

        OutboundCallMock outboundCallMock = new OutboundCallMock(executorService,
                applicationManagement,
                eventDispatcher,
                callProperties,
                responseToCreateCall,
                delayBeforeResponseToCreateCall,
                extraDataForResponseToCreateCall,
                outboundCallEventAfterConnected,
                milliSecondsUntilGeneration,
                earlyMediaInProgressing,
                outboundFarEndConnections,
                sendProgressingEvent);
        outboundCallMock.dial();
        return outboundCallMock;
    }

    /**
     * Returns with the application manager for this call manager.
     *
     * @return return with the application management object.
     */
    public IApplicationManagment getApplicationManagment() {
        return applicationManagement;
    }

    public void setSupervision(com.mobeon.masp.operateandmaintainmanager.Supervision s) {
    }

    /**
     * Sets the application manager for this call manager.
     *
     * @param applicationManagement
     */
    public void setApplicationManagment(
            IApplicationManagment applicationManagement) {
        this.applicationManagement = applicationManagement;
    }

    /**
     * Returns with the stream factory for this call manager.
     *
     * @return Returns with a stream factory.
     */
    public IStreamFactory getStreamFactory() {
        return streamFactory;
    }

    /**
     * Sets the stream factory for this call manager.
     *
     * @param streamFactory
     */
    public void setStreamFactory(IStreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    /**
     * Join two calls.
     *
     * @param firstCall
     * @param secondCall
     * @param eventDispatcher
     */
    public void join(Call firstCall, Call secondCall,
                     IEventDispatcher eventDispatcher) {
        log.info("MOCK: CallManagerMock.join:"+firstCall+":"+secondCall);
        if(withholdJoinAttempt){
            log.info("MOCK: CallManagerMock.join: withholding the join");
        } else {
            eventDispatcher.fireEvent(new JoinedEvent(firstCall,secondCall));
        }
    }

    /**
     * Unjoin two calls.
     *
     * @param firstCall
     * @param secondCall
     * @param eventDispatcher
     */
    public void unjoin(Call firstCall, Call secondCall,
                       IEventDispatcher eventDispatcher) {
        log.info("MOCK: CallManagerMock.unjoin:"+firstCall+":"+secondCall);
        if(withholdUnjoinAttempt){
            log.info("MOCK: CallManagerMock.unjoin: withholding the unjoin");
        } else {
            eventDispatcher.fireEvent(new UnjoinedEvent(firstCall,secondCall));
        }
    }

    public void sendSipMessage(String method, IEventDispatcher eventDispatcher,
                               ISession session,
                               Collection<NamedValue<String,String>> parameters){
        log.info("MOCK: CallManagerMock.sendSipMessage. Collection size: "+parameters.size());
        for (NamedValue<String,String> nameValue : parameters) {
            log.info("MOCK: CallManagerMock.sendSipMessage. Parameter "+nameValue.getName() + ":"+nameValue.getValue());
        }
        if(withholdsendSIPMessage){
            log.info("MOCK: CallManagerMock.sendSipMessage: withholding the response");
        } else {
            eventDispatcher.fireEvent(sipMessageResponseEvent);
        }
    }

    /*
    ** This method initiates an inbound call to the execution engine to
    ** simulate load and execution of ccxml and voicexml applications.
    */

    /**
     * Initiates an inbound call to a specific service but do not start it.
     *
     * @param service The name of the service to execute
     * @param a The A number information, calling party.
     * @param b The B number information, called party.
     * @param c The C number information, redirecting information.
     * @return An inbound call whith an attached service that executes.
     */
    public InboundCallMock createInboundCall (String service,
                                              CallingParty a,
                                              CalledParty b,
                                              RedirectingParty c,
                                              int inboundBitRate)
    {
        return createInboundCall (service, a, b, c, CallType.VOICE, null, inboundBitRate);
    }

    /**
     * Creates an inbound call with a certain call type.
     *
     * @param service
     * @param a
     * @param b
     * @param c
     * @param ct
     * @return InboundCallMock
     */
    public InboundCallMock createInboundCall (String service,
                                              CallingParty a,
                                              CalledParty b,
                                              RedirectingParty c,
                                              CallType ct,
                                              MimeType m,
                                              int inboundBitRate)
    {
        log.info("BTC: CallManagerMock.createInboundCall:"+host+":"+port+":"+service);
        InboundCallMock icm = new InboundCallMock (this.executorService,
                this.applicationManagement,
                this.streamFactory,
                this.responseToAccept,
                this.responseToProxy,
                this.withholdRejectAttempt,
                this.withholdDisconnectAttempt,
                this.withholdEarlyMediaAttempt,
                this.responseToNegotiateEarlyMedia,
                this.responseToRecord,
                this.delayBeforeResponseAccept,
                this.delayBeforeResponseToDisconnect,
                this.inboundFarEndConnections,
                this.sendPlayFailedAfterDelay,
                this.delayBeforePlayFailed,
                inboundBitRate);
        icm.setCallParameters (a, b, c);
        icm.setCallType (ct);
        if(m != null){
            icm.setMimeTypeInSession(m);
        }
        icm.setSessionFactory(getSessionFactory());
        icm.loadService(service);
        return icm;
    }
    
    
	public SubscribeCallMock createSubscribeCall(String service,
			CallingParty a, CalledParty b, RedirectingParty c, CallType ct,
			MimeType m, int inboundBitRate) {
		log.info("BTC: CallManagerMock.createInboundCall:" + host + ":" + port
				+ ":" + service);
		SubscribeCallMock icm = new SubscribeCallMock(this.executorService,
				this.applicationManagement, this.withholdRejectAttempt,
				this.delayBeforeResponseAccept,
				this.inboundFarEndConnections, this.sendPlayFailedAfterDelay,
				this.delayBeforePlayFailed, inboundBitRate);
		
		icm.setCalledParty(b);
		icm.setCallingParty(a);
		
		icm.setCallType(ct);
		icm.setSessionFactory(getSessionFactory());
		icm.loadService(service);
		return icm;
	}
    

    public void setOutboundCallEventAfterConnected(EventType e, int milliSecondsUntilGeneration){
        this.outboundCallEventAfterConnected = e;
        this.milliSecondsUntilGeneration = milliSecondsUntilGeneration;
    }

    public void setResponseToCreateCall(CallManagerMock.EventType e, Object extraData){
        this.responseToCreateCall = e;
        this.extraDataForResponseToCreateCall = extraData;
    }

    public void setResponseToNegotiateEarlyMedia(EventType e) {
        this.responseToNegotiateEarlyMedia = e;
    }

    public void setResponseToRecord(EventType e) {
     this.responseToRecord = e;
    }

    public void setResponseToAccept(EventType e) {
        this.responseToAccept = e;
    }

    public void setResponseToProxy(EventType e) {
        this.responseToProxy = e;
    }
    
    public void setEarlyMediaInProgressing(boolean earlyMediaInProgressing) {
        this.earlyMediaInProgressing = earlyMediaInProgressing;
    }

    public void setDelayBeforeResponseToPlay(int delayBeforeResponseToPlay) {
        StreamFactoryMock streamFactorymock = (StreamFactoryMock) streamFactory;
        streamFactorymock.setDelayBeforeResponseToPlay(delayBeforeResponseToPlay);
    }

    public void setDelayBeforeResponseToCreateCall(int delayBeforeResponseToCreateCall) {
        this.delayBeforeResponseToCreateCall = delayBeforeResponseToCreateCall;
    }

    public void setDelayBeforeResponseToDisconnect(int delayBeforeResponseToDisconnect) {
        this.delayBeforeResponseToDisconnect = delayBeforeResponseToDisconnect;
    }

    public void setDelayBeforeResponseToAccept(int delayBeforeResponseAccept) {
        this.delayBeforeResponseAccept = delayBeforeResponseAccept;
    }

    public void setSessionIdGenerator(IdGenerator<ISession> idGen) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setWithholdJoinAttempt(boolean withholdJoinAttempt) {
        this.withholdJoinAttempt = withholdJoinAttempt;
    }

    public void setWithholdUnjoinAttempt(boolean withholdUnjoinAttempt) {
        this.withholdUnjoinAttempt = withholdUnjoinAttempt;
    }

    public void setWithholdRejectAttempt(boolean withholdRejectAttempt) {
        this.withholdRejectAttempt = withholdRejectAttempt;
    }

    public void setWithholdDisconnectAttempt(boolean withholdDisconnectAttempt) {
        this.withholdDisconnectAttempt = withholdDisconnectAttempt;
    }

    public void setWithholdnegotiateEarlyMedia(boolean withholdnegotiateEarlyMedia) {
        this.withholdEarlyMediaAttempt = withholdnegotiateEarlyMedia;
    }

    public void setWithholdsendSIPMessage(boolean withholdsendSIPMessage) {
        this.withholdsendSIPMessage = withholdsendSIPMessage;
    }

    public void setOutboundFarEndConnections(Set<Connection> outboundFarEndConnections) {
        this.outboundFarEndConnections = outboundFarEndConnections;
    }

    public void setInboundFarEndConnections(Set<Connection> inboundFarEndConnections) {
        this.inboundFarEndConnections = inboundFarEndConnections;
    }

    public void setSendProgressingEvent(boolean sendProgressingEvent) {
        this.sendProgressingEvent = sendProgressingEvent;
    }

    public void setDelayBeforePlayFailed(int delayBeforePlayFailed) {
        this.delayBeforePlayFailed = delayBeforePlayFailed;
    }

    public void setSendPlayFailedAfterDelay(boolean sendPlayFailedAfterDelay) {
        this.sendPlayFailedAfterDelay = sendPlayFailedAfterDelay;
    }
}
