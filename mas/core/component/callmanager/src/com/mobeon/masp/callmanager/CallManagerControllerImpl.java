/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.states.*;
import com.mobeon.masp.callmanager.states.AdministrativeState.CALL_ACTION;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.queuehandling.CommandExecutor;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.OpenEvent;
import com.mobeon.masp.callmanager.events.CloseUnforcedEvent;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.events.JoinRequestEvent;
import com.mobeon.masp.callmanager.events.UnjoinRequestEvent;
import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.events.JoinErrorEvent;
import com.mobeon.masp.callmanager.events.UnjoinedEvent;
import com.mobeon.masp.callmanager.events.UnjoinErrorEvent;
import com.mobeon.masp.callmanager.events.RemoveCallEvent;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.callmanager.callhandling.OutboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.callhandling.CallInternal;
import com.mobeon.masp.callmanager.callhandling.CallFactory;
import com.mobeon.masp.callmanager.callhandling.events.DialEvent;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.statistics.StatisticsEvent;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.Event;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is responsible for controlling the Call Manager. It handles
 * incoming SIP messages and is responsible for Service enabler operation such
 * as open/close.
 * <p>
 * Note that the methods
 * {@link CallManagerControllerImpl#handleOutboundCall(OutboundCallImpl)} and
 * {@link CallManagerControllerImpl#doCommand(EventObject)} are synchronized.
 * This is done to make sure that only one of an event or a createCall scenario
 * is handled at the same time. Outbound call creation cannot be handled as an
 * event since the decision whether to accept or reject the call must have been
 * made before the call is returned. Therefore, it is handled as a separate
 * method instead and therefore the synchronization is needed.
 * <p>
 * This class is thread safe.
 *
 * @author Malin Flodin
 */
public class CallManagerControllerImpl implements
        CallManagerController, ServiceEnablerOperate, CommandExecutor {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private enum RejectReason {
        OVERLOAD, ADMIN_STATE, INTERNAL_ERROR, SYSTEM_NOT_READY
    }

    // Administrative states
    // Thread-safe due to immutable, i.e set at construction and never changed
    private final OpenedState openedState;
    private final ClosingForcedState closingForcedState;
    private final ClosedState closedState;
    private final ClosingUnforcedRejectingState closingUnforcedRejectingState;

    // Load regulator
    // Thread-safe due to immutable, i.e set at construction and never changed
    private final LoadRegulator loadRegulator;

    // Thread-safe due to immutable, i.e set at construction time and never
    // changed
    private final SequenceGuaranteedEventQueue eventQueue =
            new SequenceGuaranteedEventQueue(this, CallManagerControllerImpl.class);

    // Thread-safe since only set or get in synchronized methods
    private AtomicReference<AdministrativeState> currentState =
            new AtomicReference<AdministrativeState>();

    // Indicates if it is ok to register SSPs added after initial startup or not
    private AtomicBoolean okToRegisterNewSsps = new AtomicBoolean(false);

    public CallManagerControllerImpl() {
        loadRegulator = new LoadRegulator();

        // Create states
        openedState = new OpenedState(this, loadRegulator);
        closedState = new ClosedState(this, loadRegulator);
        closingForcedState = new ClosingForcedState(this, loadRegulator);
        closingUnforcedRejectingState = new ClosingUnforcedRejectingState(this, loadRegulator);

        // Initialize state
        currentState.set(openedState);
    }


    //========== Public Methods not part of implemented Interface ============

    /**
     * Queues an event in the controller event queue. Each event is handled
     * one at a time in the order they arrived.
     * @param event
     */
    public void queueEvent(EventObject event) {
    	Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.queueEvent");
    	try {
    		eventQueue.queue(event);
    	} finally {
    		if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
    			CommonOamManager.profilerAgent.exitCheckpoint(perf);
    		}  
    	}
    }

    public boolean isOkToRegisterNewSsps() {
        return okToRegisterNewSsps.get();
    }

    public AdministrativeState getCurrentState() {
        return currentState.get();
    }

    /**
     * Handles a new outbound call.
     * This method checks with the current state if the call can be accepted or
     * not.
     * If the call can be accepted, a dial event is queued in the calls
     * event queue. Otherwise the call is rejected.
     * <p>
     * NOTE: An outbound call creation is not queued as an event in this class
     * but handled in this method instead. It is done this way in order to make
     * sure that the decision whether to accept or reject the call has been
     * made before {@link CallManagerImpl#createCall(CallProperties,
     * com.mobeon.common.eventnotifier.IEventDispatcher,
     * com.mobeon.masp.execution_engine.session.ISession)} returns.
     *
     * @param call
     */
    public synchronized void handleOutboundCall(OutboundCallImpl call) {
        if (log.isDebugEnabled())
            log.debug("Checking to see if a new outbound call can be accepted.");

        CALL_ACTION action = getCurrentState().addOutboundCall(call.getCallId());

        // Add an UNKNOWN call to the statistics. The call type is determined later.
        updateStatistics(new StatisticsEvent(
                CallType.VOICE_VIDEO_UNKNOWN,
                com.mobeon.masp.operateandmaintainmanager.CallDirection.OUTBOUND));

        if (action == CALL_ACTION.ACCEPT_CALL) {
            if (log.isDebugEnabled())
                log.debug("A new outbound call was accepted and a dial " +
                        "event is injected: " + call);
            call.queueEvent(new DialEvent());

        } else {
            if (log.isDebugEnabled())
                log.debug("A new outbound call was NOT accepted and the " +
                        "call is considered rejected.");

            if (getCurrentState() instanceof OpenedState) {
                call.reject("Outbound call is rejected due to the current " +
                        "load situation. Current calls: " +
                        loadRegulator.getCurrentCalls());
            } else {
                call.reject("Outbound call is rejected due to administrative " +
                        "state: " + currentState);
            }
        }
    }

    /**
     * Only for testing!
     * @return the LoadRegulator for the call manager.
     */
    public LoadRegulator getLoadRegulator() {
        return loadRegulator;
    }

    //====== Public methods intended to be used within the state machine ======

    public void setClosedState() {
        currentState.set(closedState);
    }

    public void setClosingForcedState() {
        currentState.set(closingForcedState);
    }

    public void setClosingUnforcedRejectingState() {
        currentState.set(closingUnforcedRejectingState);
    }

    public void setOpenedState() {
        currentState.set(openedState);
    }

    public void lockAllCalls(CloseForcedEvent closeForcedEvent) {
        Collection<CallInternal> calls =
                CMUtils.getInstance().getCallDispatcher().getAllCalls();
        for (CallInternal call : calls) {
            ((CallImpl)call).queueEvent(closeForcedEvent);
        }
    }

    public void closeCompleted() {
        CMUtils.getInstance().getServiceEnablerInfo().closed();
    }

    public void openCompleted() {
        CMUtils.getInstance().getServiceEnablerInfo().opened();
    }

    public void registerAllSsps() {
        okToRegisterNewSsps.set(true);
        CMUtils.getInstance().getRemotePartyController().registerAllSsps();
    }

    public void unregisterAllSsps() {
        okToRegisterNewSsps.set(false);
        CMUtils.getInstance().getRemotePartyController().unregisterAllSsps();
    }


    //================== ServiceEnablerOperate Methods  ====================

    public void open() {
        if (log.isInfoEnabled()) log.info("Open request received.");
        OpenEvent openEvent = new OpenEvent();
        queueEvent(openEvent);
    }

    public void close(boolean forced) {
        if (forced) {
            if (log.isInfoEnabled()) log.info("Close request received (forced).");
            CloseForcedEvent closeForcedEvent = new CloseForcedEvent();
            queueEvent(closeForcedEvent);
        } else {
            if (log.isInfoEnabled()) log.info("Close request received (unforced).");
            CloseUnforcedEvent closeUnforcedEvent = new CloseUnforcedEvent();
            queueEvent(closeUnforcedEvent);
        }
    }

    public void updateThreshold(
            int highWaterMark, int lowWaterMark, int threshold) {
        if (log.isInfoEnabled()) log.info("Request to update threshold received. HWM = " + highWaterMark +
                                          ", LWM = " + lowWaterMark + ", Threshold = " + threshold);
        UpdateThresholdEvent thresholdEvent = new UpdateThresholdEvent(
                highWaterMark, lowWaterMark, threshold);
        queueEvent(thresholdEvent);
    }

    public String getProtocol() {
        return CMUtils.getInstance().getProtocol();
    }

    public String toString() {
        return getProtocol() + ":" +
                CMUtils.getInstance().getLocalHost() + ":" +
                CMUtils.getInstance().getLocalPort();
    }

    //================== CommandExecutor Methods  ====================

    /**
     * This method is called when a SIP event in the controllers event queue
     * shall be processed.
     * <p>
     * Different events are handled differently:
     * <ul>
     * <li>{@link SipRequestEvent}: <br>
     * Processed using
     * {@link SipRequestDispatcher#dispatchSipResquestEvent(SipRequestEvent)}.
     * </li>
     * <li>{@link SipResponseEvent}: <br>
     * Processed using
     * {@link SipResponseDispatcher#dispatchSipResponseEvent(SipResponseEvent)}.
     * </li>
     * <li>{@link SipTimeoutEvent}: <br>
     * Processed using
     * {@link SipTimeoutDispatcher#dispatchSipTimeoutEvent(SipTimeoutEvent)}.
     * </li>
     * <li>{@link RemoveCallEvent}: <br>
     * Processed by calling {@link AdministrativeState#removeCall(String)}
     * on the current state.
     * </li>
     * <li>{@link CloseForcedEvent}: <br>
     * Processed by calling {@link AdministrativeState#closeForced(CloseForcedEvent)}
     * on the current state.
     * </li>
     * <li>{@link OpenEvent}: <br>
     * Processed by calling {@link AdministrativeState#open(OpenEvent)}
     * on the current state.
     * </li>
     * <li>{@link CloseUnforcedEvent}: <br>
     * Processed by calling {@link AdministrativeState#closeUnforced(CloseUnforcedEvent)}
     * on the current state.
     * </li>
     * <li>{@link UpdateThresholdEvent}: <br>
     * Processed by calling {@link LoadRegulator#updateThreshold(int, int, int)}.
     * </li>
     * <li>{@link JoinRequestEvent}: <br>
     * Processed by calling {@link #processJoinRequestEvent(JoinRequestEvent)}.
     * </li>
     * <li>{@link UnjoinRequestEvent}: <br>
     * Processed by calling {@link #processUnjoinRequestEvent(UnjoinRequestEvent)}.
     * </li>
     * </ul>
     * <p>
     * As described in the
     * {@link com.mobeon.masp.callmanager.queuehandling.CommandExecutor}, this
     * method must never throw an exception. Therefore, this method catches
     * all exceptions. If an exception is thrown, it is logged as an error.
     *
     * @param eventObject
     */
    public synchronized void doCommand(EventObject eventObject) {
        // Clearing session info from logger this is run in a thread picked
        // from a pool and has no session relation yet.
        log.clearSessionInfo();
        Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doCommand");
        Object perf2 = null;
        try {
            if (eventObject instanceof SipRequestEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doSipRequestEvent");
            	
                SipRequestEvent sipRequestEvent = (SipRequestEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing SIP request event: " + sipRequestEvent);
                sipRequestEvent.exitCheckPoint();
                processSipRequestEvent(sipRequestEvent);

            } else if (eventObject instanceof SipResponseEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doSipReponseEvent");
                SipResponseEvent sipResponseEvent = (SipResponseEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing SIP response event: " + sipResponseEvent);
                CMUtils.getInstance().getSipResponseDispatcher().
                        dispatchSipResponseEvent(sipResponseEvent);

            } else if (eventObject instanceof SipTimeoutEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doSipTimeoutEvent");
                SipTimeoutEvent sipTimeoutEvent = (SipTimeoutEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing SIP timeout event: " + sipTimeoutEvent);
                CMUtils.getInstance().getSipTimeoutDispatcher().
                        dispatchSipTimeoutEvent(sipTimeoutEvent);

            } else if (eventObject instanceof RemoveCallEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doRemoveCallEvent");
                RemoveCallEvent removeCallEvent = (RemoveCallEvent)eventObject;

                // Register session in logger since removeCall belongs to a session
                registerSessionInLogger(removeCallEvent.getCall());

                if (log.isDebugEnabled())
                    log.debug("Processing a request to remove a call.");

                getCurrentState().removeCall(removeCallEvent.getCall().getCallId());

            } else if (eventObject instanceof CloseForcedEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doCloseForcedEvent");
                CloseForcedEvent closeForcedEvent = (CloseForcedEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing a close request (forced).");
                getCurrentState().closeForced(closeForcedEvent);

            } else if (eventObject instanceof OpenEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doOpenEvent");
                OpenEvent openEvent = (OpenEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing an open request.");
                getCurrentState().open(openEvent);

            } else if (eventObject instanceof CloseUnforcedEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doCloseUnforcedEvent");
                CloseUnforcedEvent closeUnforcedEvent = (CloseUnforcedEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing a close request (unforced).");
                getCurrentState().closeUnforced(closeUnforcedEvent);

            } else if (eventObject instanceof UpdateThresholdEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doUpdateThresholdEvent");
                UpdateThresholdEvent event = (UpdateThresholdEvent)eventObject;
                if (log.isDebugEnabled())
                    log.debug("Processing a update threshold request.");
                getCurrentState().updateThreshold(event);

            } else if (eventObject instanceof JoinRequestEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doJoinRequestEvent");
                JoinRequestEvent joinEvent = (JoinRequestEvent)eventObject;

                // Register session in logger since a join belongs to a session
                registerSessionInLogger(joinEvent.getFirstCall());

                if (log.isDebugEnabled())
                    log.debug("Processing a join request.");
                processJoinRequestEvent(joinEvent);

            } else if (eventObject instanceof UnjoinRequestEvent) {
            	perf2 = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.CallManagerCTRL.doUnjoinRequestEvent");
                UnjoinRequestEvent unjoinEvent = (UnjoinRequestEvent)eventObject;

                // Register session in logger since an unjoin belongs to a session
                registerSessionInLogger(unjoinEvent.getFirstCall());

                if (log.isDebugEnabled())
                    log.debug("Processing an unjoin request.");
                processUnjoinRequestEvent(unjoinEvent);

            }

        } catch (Throwable e) {
            log.error("Exception occurred in doCommand. This must never " +
                    "happen! It suggests implementation error. Message: " +
                    e.getMessage(), e);
        } finally {
         	 if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                 CommonOamManager.profilerAgent.exitCheckpoint(perf);
                 if (perf2 != null ) {
                	 CommonOamManager.profilerAgent.exitCheckpoint(perf2);
                	 
                 }
             }  
        }
    }

    //================== Private Methods  ====================

    /**
     * Creates an inbound call based on the SIP request event.
     * The created call is inserted in the call dispatcher and the
     * sipRequestEvent is queued in the created call's event queue.
     * <p>
     * If the call cannot be created, the SIP request is rejected with a
     * SIP (Server Internal Error) response.
     *
     * @param sipRequestEvent MUST NOT be null.
     */
    private void createInboundCall(SipRequestEvent sipRequestEvent, SipRequestType requestType) {
	            
        if (log.isDebugEnabled())
            log.debug("Trying to create a new inbound call.");

        // Add an UNKNOWN call to the statistics. The call type is determined later.
        if (requestType != SipRequestType.SUBSCRIBE) {
	        updateStatistics(new StatisticsEvent(
	                CallType.VOICE_VIDEO_UNKNOWN,
	                com.mobeon.masp.operateandmaintainmanager.CallDirection.INBOUND));
        }
        
        // Check if MAS can accept incoming calls based on our checker status
        boolean systemReady = CommonMessagingAccess.getInstance().isSystemReady();
        
        if (!systemReady) {
            log.debug("isSystemReady: " + systemReady + ". The request is rejected.");
            rejectInboundCall(RejectReason.SYSTEM_NOT_READY, sipRequestEvent);
            profilerAgentCheckPoint("MAS.CM.In.Sip.Req.Invite.5.Reject.Sys");
        } else {
            log.debug("isSystemReady: " + systemReady + ". The request will be processed.");
			
			CALL_ACTION action = getCurrentState().addInboundCall(
			sipRequestEvent.getSipMessage().getCallId());

			if (action == CALL_ACTION.ACCEPT_CALL) {
				// Create a new inbound call
				try {

					if (requestType == SipRequestType.SUBSCRIBE) {
						// SUBSCRIBE
						CallFactory.createSubscribeCall(sipRequestEvent);
					} else {
						// INVITE
						// TODO: Can this be removed somehow to make better code?
						sipRequestEvent.setInitialInvite();
		
						CallFactory.createInboundCall(sipRequestEvent);
					}
				} catch (Exception e) {
					if (log.isDebugEnabled())
						log.debug("Exception when creating inbound call: " +
								e.getMessage(), e);
					rejectInboundCall(RejectReason.INTERNAL_ERROR, sipRequestEvent);
					profilerAgentCheckPoint("MAS.CM.In.Sip.Req.Invite.3.Reject.Excep");
				}
			} else if (action == CALL_ACTION.REDIRECT_CALL) {
				// Call is redirected due to high load.
				rejectInboundCall(RejectReason.OVERLOAD, sipRequestEvent);
				profilerAgentCheckPoint("MAS.CM.In.Sip.Req.Invite.4.Reject.Load");
			} else {
				rejectInboundCall(RejectReason.ADMIN_STATE, sipRequestEvent);
				profilerAgentCheckPoint("MAS.CM.In.Sip.Req.Invite.5.Reject.State");
			}
        }
        
    }

    /**
     * Retrieves the two calls from the joinEvent and joins them together.
     * A {@link JoinedEvent} is generated if the join completed successfully.
     * Otherwise, a {@link JoinErrorEvent} is generated.
     * @param joinEvent
     */
    private void processJoinRequestEvent(JoinRequestEvent joinEvent) {
        CallImpl call1 = (CallImpl) joinEvent.getFirstCall();
        CallImpl call2 = (CallImpl) joinEvent.getSecondCall();
        try {
            call1.join(call2);

            try {
                call2.join(call1);

                fireEvent(joinEvent.getEventDispatcher(),
                        new JoinedEvent(call1, call2));

            } catch (Exception e) {

                if (log.isInfoEnabled())
                    log.info("Could not join second call. Message = <" +
                            e.getMessage() + ">, <FirstCall=" + call1 +
                            ">, <SecondCall=" + call2 + ">");

                // If join fails for the second call, make sure that the
                // first call is not joined by making an unjoin.
                try {
                    call1.unjoin(call2);
                } catch (Exception e1) {
                    if (log.isDebugEnabled())
                        log.debug("Error occured when trying to unjoin a " +
                                "call due to error in join. It is ignored.");
                }

                fireEvent(joinEvent.getEventDispatcher(),
                        new JoinErrorEvent(call1, call2, e.getMessage()));
            }

        } catch (Exception e) {
            if (log.isInfoEnabled())
                log.info("Could not join first call. Message = <" +
                        e.getMessage() + ">, <FirstCall=" + call1 +
                        ">, <SecondCall=" + call2 + ">");

            fireEvent(joinEvent.getEventDispatcher(),
                    new JoinErrorEvent(call1, call2, e.getMessage()));
        }

    }

    /**
     * Retrieves the two calls from the unjoinEvent and tries to unjoin them.
     * An {@link UnjoinedEvent} is generated if the unjoin completed
     * successfully. Otherwise, an {@link UnjoinErrorEvent} is generated.
     * @param unjoinEvent
     */
    private void processUnjoinRequestEvent(UnjoinRequestEvent unjoinEvent) {
        boolean unjoinFailed = false;
        String failureMsg = "";
        CallImpl call1 = (CallImpl) unjoinEvent.getFirstCall();
        CallImpl call2 = (CallImpl) unjoinEvent.getSecondCall();

        try {
            call1.unjoin(call2);
        } catch (IllegalStateException e) {
            if (log.isInfoEnabled())
                log.info("Failure when unjoining first call. Message = " +
                        e.getMessage() + " <FirstCall=" + call1 +
                        "> and <SecondCall=" + call2 + ">");
            unjoinFailed = true;
            failureMsg = e.getMessage();
        }

        try {
            call2.unjoin(call1);
        } catch (IllegalStateException e) {
            if (log.isInfoEnabled())
                log.info("Failure when unjoining second call. Message = " +
                        e.getMessage() + " <FirstCall=" + call1 +
                        "> and <SecondCall=" + call2 + ">");
            unjoinFailed = true;
            failureMsg = failureMsg + " " + e.getMessage();
        }

        if (unjoinFailed)
            fireEvent(unjoinEvent.getEventDispatcher(),
                    new UnjoinErrorEvent(call1, call2, failureMsg));
        else
            fireEvent(unjoinEvent.getEventDispatcher(),
                    new UnjoinedEvent(call1, call2));

    }

    /**
     * Processes a SIP OPTIONS request.
     * The response to a SIP OPTIONS request should be the same as if the request
     * had been an INVITE.
     * This method therefore checks with the current state if a new inbound call
     * would have been accepted or not.
     * If a call would have been accepted, a SIP OK response is sent.
     * Otherwise a SIP Service Unavailable response is sent.
     * @param sipRequestEvent carries the SIP OPTIONS request.
     */
    private void processSipOptionsRequest(SipRequestEvent sipRequestEvent) {

        SipResponse sipResponse = null;
        try {

            CALL_ACTION action = getCurrentState().checkCallAction();
            if (action == CALL_ACTION.ACCEPT_CALL) {
                // A new inbound call would be accepted, create OK response
                sipResponse = CMUtils.getInstance().getSipResponseFactory().
                        createOkResponse(
                                sipRequestEvent, null,
                                ConfigurationReader.getInstance().
                                        getConfig().getRegisteredName());
            } else if (action == CALL_ACTION.REDIRECT_CALL) {
                String message = "A new inbound call would be rejected since " +
                        "the Service is experiencing high load. ";

                if (log.isInfoEnabled()) {
                    log.info(message + " A SIP 503 response is sent.");
                }

                sipResponse = CMUtils.getInstance().getSipResponseFactory().
                        createServiceUnavailableResponse(
                                sipRequestEvent, message);

            } else {
                // A new inbound call would NOT be accepted, create
                // temporarily unavailable response

                String message =
                        "A new inbound call would be rejected since the " +
                        "service is temporarily unavailable due to " +
                        "current administrative state: " + currentState;

                if (log.isInfoEnabled())
                    log.info(message +
                            ". Therefore the SIP OPTIONS request is " +
                            "rejected with a SIP 503 response.");

                sipResponse = CMUtils.getInstance().getSipResponseFactory().
                        createServiceUnavailableResponse(
                                sipRequestEvent, message);
            }

            // Add experienced operational status in a header. If we have come
            // this far we are UP.
            sipResponse.addOperationalStatusHeader(ExperiencedOperationalStatus.UP);

            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            log.error("Could not send " + sipResponse + ".", e);
        }
    }

    /**
     * This method processes a SIP request event.
     * <p>
     * It dispatches the SIP request event using
     * {@link SipRequestDispatcher#dispatchSipResquestEvent(SipRequestEvent)}
     * which handles all types of request except session creating requests and
     * OPTIONS requests. If the request is of one of those types, this method
     * returns an indication that the request should be handled here instead.
     * <P>
     * A session creating request is first validated. If validated ok, a new
     * inbound call is created using {@link #createInboundCall(SipRequestEvent)}.
     * <p>
     * A SIP OPTIONS request is first validated. If validated ok, the OPTIONS
     * request is further processed using
     * {@link #processSipOptionsRequest(SipRequestEvent)}.
     * @param sipRequestEvent
     */
    private void processSipRequestEvent(SipRequestEvent sipRequestEvent) {
            
        SipRequestType requestType =
                CMUtils.getInstance().getSipRequestDispatcher().
                        dispatchSipResquestEvent(sipRequestEvent);

        if (requestType == SipRequestType.SESSION_CREATING || requestType == SipRequestType.SUBSCRIBE) {
            
            Object perf = null;
            try{
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Sip.Req.Invite.1.Received");
                }

                boolean validatedOk = sipRequestEvent.validateGeneralPartOfRequest();

                if (validatedOk) {
    
                    if (log.isDebugEnabled())
                        log.debug("Invite or Subscribe was validated OK");
                    createInboundCall(sipRequestEvent,requestType);
    
                } else {
                    if (log.isDebugEnabled())
                        log.debug("A received out-of-dialog, session creating / subscribe " +
                                "SIP request was validated NOT OK. " +
                                "A Failed Event is generated.");
    
                    updateStatistics(new FailedEvent(
                            null,
                            FailedEvent.Reason.REJECTED_BY_NEAR_END,
                            CallDirection.INBOUND,
                            "General request validation failed.",
                            ConfigurationReader.getInstance().getConfig().
                                    getReleaseCauseMapping().
                                    getNetworkStatusCode(null, null)));
                }
            } finally {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }           

        } else if (requestType == SipRequestType.OPTIONS) {
            
            Object perf = null;
            try{
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Sip.Req.Option");
                }
            
                boolean validatedOk = sipRequestEvent.validateGeneralPartOfRequest();
    
                if (validatedOk) {
                    if (log.isDebugEnabled())
                        log.debug("Out-of-dialog SIP OPTIONS request was validated OK.");
                    processSipOptionsRequest(sipRequestEvent);
                }
            } finally {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }           
        }
    }

    /**
     * This method rejects an inbound call.
     * How, depends upon the reason:
     * <ul>
     * <li>
     * {@link RejectReason.ADMIN_STATE}: <br>
     * A SIP Temporarily Unavailable response is sent and a {@link FailedEvent}
     * is generated.
     * </li>
     * <li>
     * {@link RejectReason.OVERLOAD}: <br>
     * A SIP Moved Temporarily is sent and a {@link FailedEvent}
     * is generated.
     * </li>
     * <li>
     * {@link RejectReason.SYSTEM_NOT_READY}: <br>
     * A SIP Temporarily Unavailable response is sent and a {@link FailedEvent}
     * is generated.
     * </li>
     * <li>
     * Otherwise: <br>
     * A SIP Server Internal Error response is sent and an {@link ErrorEvent}
     * is generated.
     * </li>
     * </ul>
     * @param reason
     * @param sipRequestEvent
     */
    private void rejectInboundCall(
            RejectReason reason, SipRequestEvent sipRequestEvent) {
        String message;
        SipResponse sipResponse = null;
        Event event = null;

        int nsc = ConfigurationReader.getInstance().getConfig().
                getReleaseCauseMapping().getNetworkStatusCode(null, null);

        try {
            switch(reason) {
                case ADMIN_STATE:
                    message =
                            "The request is rejected since the Service " +
                            "is temporarily unavailable due to current " +
                            "administrative state: " + currentState;

                    log.warn(message + ". A SIP \"Temporarily Unavailable\" " +
                             "response is sent.");

                    event = new FailedEvent(null,
                            FailedEvent.Reason.REJECTED_BY_NEAR_END,
                            CallDirection.INBOUND, message, nsc);

                    sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createServiceUnavailableResponse(
                                    sipRequestEvent, message);

                    break;

                case OVERLOAD:
                    message =
                            "The request is rejected since the Service " +
                                    "is experiencing high load. ";
                    if (log.isInfoEnabled())
                        log.info(message +
                                "A SIP \"Service Unavailable\" response is sent.");

                    event = new FailedEvent(null,
                            FailedEvent.Reason.REJECTED_BY_NEAR_END,
                            CallDirection.INBOUND, message, nsc);

                    sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createServiceUnavailableResponse(
                                    sipRequestEvent, message);
                    break;

                case SYSTEM_NOT_READY:
                    message = "The request is rejected since the system is not ready.";

                    log.warn(message + ". A SIP \"Temporarily Unavailable\" " +
                            "response is sent (system not ready).");

                    event = new FailedEvent(null, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                            CallDirection.INBOUND, message, nsc);

                    sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createServiceUnavailableResponse(sipRequestEvent, message);
                    break;
                    
                default:
                    message = "The request is rejected due to internal error.";
                    if (log.isInfoEnabled())
                        log.info(message +
                                "A SIP \"Server Internal Error\" response is sent.");

                    event = new ErrorEvent(
                            null, CallDirection.INBOUND, message, false);

                    sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createServerInternalErrorResponse(sipRequestEvent);

                    break;
            }

            // Send the response
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);

        } catch (Exception e) {
            log.error("Could not send " + sipResponse + ".", e);
        }

        if (event != null) {
            // Report that a new inbound call failed or is being rejected
            updateStatistics(event);
        }
    }

    /**
     * Fires an event to the event dispatcher.
     * 
     * @param eventDispatcher
     * @param event
     */
    private void fireEvent(IEventDispatcher eventDispatcher, Event event) {
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

    private void updateStatistics(Event event) {
        List<CallEventListener> callEventListeners = CMUtils.getInstance().getCallEventListeners();
        for (CallEventListener callEventListener : callEventListeners) {
            callEventListener.processCallEvent(event);
        }
    }

    private void registerSessionInLogger(Call call) {
        if ((call != null) && (call.getSession() != null))
            call.getSession().registerSessionInLogger();
    }
    
    /**
     * Profiling method 
     * @param checkPoint
     */
    private void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            try {
                perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
            } finally {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }
}
