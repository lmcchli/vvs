package com.mobeon.masp.callmanager.callhandling;

import gov.nist.javax.sip.header.SubscriptionState;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.SIPHeaderNamesIms;
import gov.nist.javax.sip.message.SIPRequest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.sip.Dialog;
import javax.sip.header.EventHeader;
import javax.sip.message.Request;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.Connection;
import com.mobeon.masp.callmanager.SubscribeCall;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallCommandEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.callmanager.events.SubscribeEvent;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.sip.CallParameterRetrieval;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.RecordingProperties;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;

public class SubscribeCallImpl extends CallImpl implements SubscribeCall {

	private static final String SUBSCRIBE_SERVICE = "MWISubscribe";
	private static final int DEFAULT_EXPIRES = 3600;
	
	private final ILogger log = ILoggerFactory.getILogger(getClass());

	private static final Timer callTimer = new Timer();

	private SequenceGuaranteedEventQueue eventQueue =
		new SequenceGuaranteedEventQueue(this, SubscribeCallImpl.class);

	ISession session;
	IApplicationExecution applicationInstance;

	SipRequestEvent sipRequestEvent;
	CallParameters callParameters ;
	int expires;
	gov.nist.javax.sip.message.SIPRequest notifyTemplate;
	boolean isInitial = true;

	private final NotAcceptedTimerTask notAcceptedTimerTask = new NotAcceptedTimerTask(this);

	public SubscribeCallImpl(SipRequestEvent sipRequestEvent, CallManagerConfiguration config) {
	    super(config);
		this.sipRequestEvent = sipRequestEvent;
		this.callParameters = CallParameterRetrieval.getInstance().
				retrieveCallParameters(sipRequestEvent, ConfigurationReader.getInstance().getConfig());
		this.session = CMUtils.getInstance().getSessionFactory().create();
		expires = sipRequestEvent.getSipMessage().getExpireTimeFromExpiresHeader();
		if (expires == -1) {
			expires = DEFAULT_EXPIRES;
		} 
		int maxExpires = ConfigurationReader.getInstance().getConfig().getSubscribeMaxExpires();
		if (expires > maxExpires) {
			expires = maxExpires;
		}
		To to = (To)((SIPRequest)this.sipRequestEvent.getRequest()).getTo();
		if ( to.getTag() == null) {
			this.isInitial = true;
		} else {
			this.isInitial = false;
		}
	}

	public String getCallId() {
	    return sipRequestEvent.getSipMessage().getCallId();
	}

	public SequenceGuaranteedEventQueue getEventQueue() {
		return this.eventQueue;
	}

	/**
	 * Queues an event in the call event queue. Each event is handled
	 * one at a time in the order they arrived.
	 * @param event Event
	 */
	public void queueEvent(EventObject event) {
		if (log.isDebugEnabled()) {
			if (event instanceof SipRequestEvent) {
				log.debug("Queueing SIP request event with method: " +
						((SipRequestEvent)event).getMethod());
			} else {
				log.debug("Queueing event: " + event);
			}
		}
		eventQueue.queue(event);
	}


	/**
	 * Fires an event to the event dispatcher.
	 * @param event Event
	 */
	public void fireEvent(Event event) {
		IEventDispatcher eventDispatcher = applicationInstance.getEventDispatcher();

		if (log.isDebugEnabled())
			log.debug("Firing event: <EventDispatcher = " +
					eventDispatcher + ">, <Event = " + event + ">");

		if (eventDispatcher != null) {
			try {
				if (log.isInfoEnabled()) log.info("Fired event: " + event);
				eventDispatcher.fireEvent(event);
			} catch (Exception e) {
				log.error(
						"Exception occurred when firing event: " + e.getMessage(),
						e);
			}
		}
		updateStatistics(event);
	}

	public void startNotAcceptedTimer() {
	    callTimer.schedule(
				notAcceptedTimerTask,
				ConfigurationReader.getInstance().getConfig().getCallNotAcceptedTimer());

		if (log.isDebugEnabled())
			log.debug("Max Duration Before Accepted Timer has been scheduled to: " +
					ConfigurationReader.getInstance().getConfig().getCallNotAcceptedTimer());
	}

	private void updateStatistics(Event event) {
		List<CallEventListener> callEventListeners = CMUtils.getInstance().getCallEventListeners();
		for (CallEventListener callEventListener : callEventListeners) {
			callEventListener.processCallEvent(event);
		}
	}

	// TODO - can the applicationInstance be shared?
	// copy paste from other service but I don't see any reason why this must be sync 
	public synchronized void loadService(String service) {

		if (log.isDebugEnabled()) log.debug("Trying to load service: " + service);
		this.applicationInstance = CMUtils.getInstance().getApplicationManagement().load(service);
		if (this.applicationInstance == null) {
			// TODO - change to specific Exception
			throw new NullPointerException(
			"Application Execution instance is null when loading service.");
		}
		this. session = CMUtils.getInstance().getSessionFactory().create();


		applicationInstance.setSession(session);

		//session.setMdcItems(sessionMdcItems);  - not sure what is that??
		session.setData(ISession.SESSION_INITIATOR, CMUtils.getInstance().getProtocol());

		applicationInstance.start();
	}

	@Override
	public void doCommand(EventObject eventObject) {

		//setSessionLoggingData(); - not sure what is that

		if (log.isDebugEnabled())
			log.debug("DoCommand: " + eventObject);

		try {

			if (eventObject instanceof SipRequestEvent) {
				SipRequestEvent sipRequestEvent = (SipRequestEvent)eventObject;
	          	String method = sipRequestEvent.getMethod();
            	if (method.equals(Request.SUBSCRIBE) ) {
					EventHeader eventHdr = (EventHeader)sipRequestEvent.getRequest().getHeader(EventHeader.NAME);
					if (eventHdr != null && eventHdr.getEventType().equalsIgnoreCase(SipConstants.MWI_EVENT_TYPE)) {
						//log.debug("Processing Subscribe - sending alerting event: " + this.toString());
					    callTimer.schedule(
								notAcceptedTimerTask,
								ConfigurationReader.getInstance().getConfig().getCallNotAcceptedTimer());

						if (log.isDebugEnabled())
							log.debug("Max Duration Before Accepted Timer has been scheduled to: " +
									ConfigurationReader.getInstance().getConfig().getCallNotAcceptedTimer());
						loadService(SUBSCRIBE_SERVICE);
						
						fireEvent(new AlertingEvent(this));
						
					} else {
						String reason = null;
						if (eventHdr == null) {
							reason = "No event type in the request";
						} else {
							reason = "Bad event: " + eventHdr.getEventType();
						} 
						SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().createBadEventResponse(sipRequestEvent, reason);
						CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
					}
				}
			} else if (eventObject instanceof AcceptEvent) {
				//cancel timer 
				notAcceptedTimerTask.cancel();
				SipResponse sipResponse = null;
				if (this.isInitial) {
					// send sip 202 OK
					sipResponse = CMUtils.getInstance().getSipResponseFactory().
								createAcceptedResponse(sipRequestEvent);
				} else {
					// send sip 200 OK
					sipResponse = CMUtils.getInstance().getSipResponseFactory().
								createOkResponse(sipRequestEvent, null, null);
				}
				sipResponse.addExpiresHeader(this.getExpires()); // TODO - the mas configurable expires should go here
				CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
				// delete the dialog - there is no reason to keep the dialog alive
				// the transaction is cleaned by the system after 35 seconds
				Dialog dialog = sipRequestEvent.getTransaction().getDialog();
				
				notifyTemplate = (gov.nist.javax.sip.message.SIPRequest) dialog.createRequest(Request.NOTIFY);
				
				dialog.delete();
				
				if (this.isInitial) {
					// Store the initial dialog info - using this event in order to have both tags
					fireEvent(new SubscribeEvent(this)); 
				}

				// Remove the subscribe call from LoadRegulator
				this.removeSubscribeCall(false);

			} else if (eventObject instanceof RejectEvent) {
				// cancel timer
				notAcceptedTimerTask.cancel();
				// send sip error 
				String reason = ((RejectEvent)eventObject).getReason();
				SipResponse sipResponse = null;
				if (reason.equals("403")) {
					sipResponse = CMUtils.getInstance().getSipResponseFactory().createForbiddenResponse(sipRequestEvent);
				} else if (reason.equals("481")) {
					sipResponse = CMUtils.getInstance().getSipResponseFactory().createTransactionDoesNotExistResponse(sipRequestEvent);
				}
				CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);

				// Remove the subscribe call from LoadRegulator
				this.removeSubscribeCall(false);

			} else if (eventObject instanceof CallTimeoutEvent) {
				CallTimeoutEvent timeout =  (CallTimeoutEvent) eventObject;
				if (timeout.getType() ==  CallTimerTask.Type.CALL_NOT_ACCEPTED) {
					SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().createServerInternalErrorResponse(sipRequestEvent);
					CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
				}

				// Remove the subscribe call from LoadRegulator
				this.removeSubscribeCall(false);

			} else {
				log.error("Un consumed event");

				// Remove the subscribe call from LoadRegulator
				this.removeSubscribeCall(true);
			}

		} catch (Throwable e) {
 			String errorMsg = "Exception occurred in doCommand. Event is: "  + eventObject + " Error message is: " + e.getMessage();
			log.error(errorMsg, e);

			// Remove the subscribe call from LoadRegulator
			this.removeSubscribeCall(true);
		}

	}
	
	public String getIsInitial() {
		if (this.isInitial) {
			return "true";
		} else {
			return "false";
		}
	}
	
	public String getDialogInfo() {
		try {
		    Properties prop = new Properties();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			if (notifyTemplate == null) {
				// re-subscribe
				// TODO - very ugly as it requires a parsing in the ccxml - change to multiple tag/callid parameters
				Object header = ((SIPRequest)this.sipRequestEvent.getRequest()).getFrom();
				if (header != null ) {
					prop.put(SIPHeader.FROM, header.toString());
				}
				header = ((SIPRequest)this.sipRequestEvent.getRequest()).getTo();
				if (header != null ) {
					prop.put(SIPHeader.TO, header.toString());
				}
				header = ((SIPRequest)this.sipRequestEvent.getRequest()).getCallId();
				if (header != null ) {
					prop.put(SIPHeader.CALL_ID, header.toString());
				}
			} else {
                // initial subscribe
				Object header = this.notifyTemplate.getRequestURI();
				if (header != null ) {
					prop.put(SubscribeCall.NOTIFY_REQUEST_URI, header.toString());
				}
				header = this.notifyTemplate.getFrom();
				if (header != null ) {
					prop.put(SIPHeader.FROM, header.toString());
				}
				header = this.notifyTemplate.getTo();
				if (header != null ) {
					prop.put(SIPHeader.TO, header.toString());
				}
	
				header = this.notifyTemplate.getCallId();
				if (header != null ) {
					prop.put(SIPHeader.CALL_ID, header.toString());
				}
				//add the subscription state 
				SubscriptionState ssHeader = new SubscriptionState();
				if (this.getExpires() != 0) { 	
					ssHeader.setState(SubscriptionState.ACTIVE);
				} else {
					ssHeader.setState(SubscriptionState.TERMINATED);
				}
				prop.put(SIPHeader.SUBSCRIPTION_STATE, ssHeader.toString());
				
				//TODO - fix the PChargingVector - check rfc if it must be reused from subscribe
				
				header = this.notifyTemplate.getHeader(SIPHeaderNamesIms.P_CHARGING_VECTOR);
				if (header != null ) {
					prop.put(SIPHeaderNamesIms.P_CHARGING_VECTOR, header.toString());
				}
				
				header = this.notifyTemplate.getRouteHeaders();
				if (header != null ) {
					prop.put(SIPHeader.ROUTE, header.toString());
				}
				
				header = this.notifyTemplate.getCSeq();
				if (header != null ) {
					prop.put(SIPHeader.CSEQ, header.toString());
				}
				
				/*
				 *TODO - should this contact be used?
				header = this.notifyTemplate.getContactHeader();
				if (header != null ) {
					prop.put(SIPHeader.CONTACT, header.toString());
				}
				*/

			}
			prop.store(byteStream, null);
			return byteStream.toString();
		} catch (Exception e) {
			// no reason to fail here
			log.error("Exception extracting the dialog information:" + e);
			return null;
		}

	}
	
	
	// SubscribeCall methods
	public void accept() {
		log.debug("Accept called");
		AcceptEvent acceptEvent = new AcceptEvent();
		queueEvent(acceptEvent);
	}

	@Override
	public void reject(String reason) {
		log.debug("Reject called");
		RejectEvent rejectEvent = new RejectEvent(reason);
		queueEvent(rejectEvent);

	}

	public int getExpires() {
		return expires;

	}

	public String getUserAgentNumber() {
		String userAgentNumber = this.getCallingParty().getTelephoneNumber();
		if (userAgentNumber == null) {
			// use the sip uri if then
			userAgentNumber = this.getCallingParty().getUri();
		}
		return userAgentNumber;
	}


	public String toString() {
		return "";
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("Expires=");
		sb.append(this.getExpires());
		sb.append(" userAgentNumber=");
		sb.append(this.getUserAgentNumber());
		sb.append(" mailboxId=");
	 	sb.append(this.getMailboxId());
	 	if (this.notifyTemplate == null) {
	 		return sb.toString();
	 	}
		sb.append(" dialogInfo=");
		try {
			sb.append(this.getDialogInfo());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
		*/
	}

	
	public void timeoutCall(CallTimerTask.Type type) {
		CallTimeoutEvent timeoutEvent = new CallTimeoutEvent(type);
		queueEvent(timeoutEvent);
	}

	class NotAcceptedTimerTask extends TimerTask {
		private final SubscribeCallImpl call;

		public NotAcceptedTimerTask(SubscribeCallImpl call) {
			this.call = call;
		}

		public void run() {
			CallTimeoutEvent timeoutEvent = new CallTimeoutEvent(CallTimerTask.Type.CALL_NOT_ACCEPTED);
			call.queueEvent(timeoutEvent);
		}
	}

	public void removeSubscribeCall(boolean stopTimers) {
	    removeCall();

	    if (stopTimers) {
	        cancelCallTimers();
	    }
	}

	public void cancelCallTimers() {
	    notAcceptedTimerTask.cancel();
	}

	public void errorOccurred(String message, boolean alreadyDisconnected) {
	    if (log.isInfoEnabled()) log.info(message);
	    removeSubscribeCall(true);
	}

	public boolean isJoinable() {
	    return false;
	}

	public void setSessionLoggingData() {
	    return;
	}

	void processCallCommand(CallCommandEvent callCommandEvent) {
	    return;
	}

	// from Call
	public CallType getCallType() {
		return sipRequestEvent.getSipMessage().getCallInfoType();
	}

	@Override
	public CalledParty getCalledParty() {
		return callParameters.getCalledParty();

	}

	@Override
	public CallingParty getCallingParty() {
		return callParameters.getCallingParty();
	}

	@Override
	public String getProtocolName() {
		return CMUtils.getInstance().getProtocol();
	}

	@Override
	public String getProtocolVersion() {
		return CMUtils.getInstance().getVersion();
	}

	@Override
	public ISession getSession() {
		return this.session;
	}
    




	// TODO - generalize the Call interface and remove the bellow methods

	@Override
	public Set<Connection> getFarEndConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInboundBitRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IInboundMediaStream getInboundStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOutboundMediaStream getOutboundStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void play(Object id, IMediaObject mediaObject,
			PlayOption playOption, long cursor) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void play(Object id, IMediaObject[] mediaObjects,
			PlayOption playOption, long cursor) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void record(Object id, IMediaObject recordMediaObject,
			RecordingProperties properties) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void record(Object id, IMediaObject playMediaObject,
			IMediaObject recordMediaObject, RecordingProperties properties)
	throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopPlay(Object id) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopRecord(Object id) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}



}
