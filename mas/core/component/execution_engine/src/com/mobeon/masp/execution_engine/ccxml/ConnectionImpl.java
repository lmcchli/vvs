/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.Connection;
import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.SubscribeEvent;
import com.mobeon.masp.execution_engine.ApplicationWatchdog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors.CCXMLMirror;
import com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors.MirrorBase;
import com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors.VoiceXMLMirror;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.wrapper.SessionInfoHelper;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.*;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionImpl extends MirrorBase implements com.mobeon.masp.execution_engine.ccxml.Connection {


    private Call call;
    private String connectionId;

    //connectionIdRef is here to prevent garbate collection of the connections Id.
    private final Id<BridgeParty> connectionIdRef;

    private final CCXMLExecutionContext originatingContext;
    private final DebugInfo debugInfo;
    private final List<ExecutionContext> contexts;
    private final EventSender sender;
    private final AtomicReference<State> state;
    private ConnectionImplConfig config;
    private boolean sessionInfoReleased = false;

    /**
     * Stuff related to passivess timeouts
     */
    private static ScheduledThreadPoolExecutor timeoutscheduler = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
    static {
        /**
         * Create and schedule recurring task to purge cancelled ongoingTimeout object.
         * The JVM does not release reference to ScheduledFuture until the delay expires even if they are cancelled.
         * So this will actively purge them. This is necessary when ongoingTimeout is scheduled with a long delay.
         */
        Runnable purgeTimeoutSchedulerTask = new Runnable() {
            @Override
            public void run() {
                timeoutscheduler.purge();
            }
        };
        
        timeoutscheduler.scheduleWithFixedDelay(purgeTimeoutSchedulerTask, 90L, 90L, TimeUnit.SECONDS);
    }
    private ScheduledFuture ongoingTimeout = null;

    private MirrorBase mirror = new CCXMLMirror(this);
    private final Object stateLock = new Object();


    public static EventSender createEventSender(final CCXMLExecutionContext context) {
        return new EventSender() {
            public void sendEvent(CCXMLEvent event) {
                if (!event.isSourceRelatedDefined()) {
                    event.defineSourceRelated(context);
                }
                context.getEventHub().fireContextEvent(event);
            }

        };
    }

    public ConnectionImpl(ConnectionImpl toCopy) {
        state = new AtomicReference<State>();
        contexts = new ArrayList<ExecutionContext>();
        connectionId = toCopy.connectionId;
        originatingContext = toCopy.originatingContext;
        sender = createEventSender(originatingContext);
        debugInfo = toCopy.debugInfo;
        mirror = toCopy.mirror;
        config = toCopy.config;
        setCall(toCopy.call);
        contexts.addAll(toCopy.contexts);
        connectionIdRef = toCopy.connectionIdRef;
        connectionId = toCopy.connectionId;
        setState(toCopy.state.get());
    }

    public ConnectionImpl(ExecutionContext executionContext) {
        state = new AtomicReference<State>();
        contexts = new ArrayList<ExecutionContext>();
        sender = createEventSender((CCXMLExecutionContext) executionContext);
        originatingContext = (CCXMLExecutionContext) executionContext;
        Id<BridgeParty> connectionId1 = IdGeneratorImpl.PARTY_GENERATOR.generateId();
        connectionIdRef = connectionId1;
        connectionId = connectionId1.toString();
        put(Constants.CCXML.CONNECTION_ID, this, connectionId);
        debugInfo = DebugInfo.getInstance(this);

        setState(State.START);
        contexts.add(executionContext);
        config = new ConnectionImplConfig(executionContext.getConfigurationManager());
    }

    public Map<String, Object> getHash() {
        return mirror.getHash();
    }

    public RedirectingParty getRedirectingParty() {
        RedirectingParty redirectingParty = null;
        if (isInboundCall()) {
            redirectingParty = getAsInboundCall().getRedirectingParty();
        }
        return redirectingParty;
    }

    public Call getCall() {
        return call;
    }

    private InboundCall getAsInboundCall() {
        if (call instanceof InboundCall) {
            return (InboundCall) call;
        }
        return null;
    }

    private OutboundCall getAsOutboundCall() {
        if (call instanceof OutboundCall) {
            return (OutboundCall) call;
        }
        return null;
    }

    private boolean isInboundCall() {
        return call instanceof InboundCall;
    }


    public String getBridgePartyId() {
        return connectionId;

    }
    
    private SubscribeCall getAsSusbcribeCall() {
        if (call instanceof SubscribeCall) {
            return (SubscribeCall) call;
        }
        return null;
	}

	private boolean isSusbcribeCall() {
		return call instanceof SubscribeCall;
	}

    public final void sendEvent(String eventName, Event related, EventTarget target) {
        CCXMLEvent event = CCXMLEvent.create(eventName, originatingContext, this, debugInfo, related);
        if (event != null) {

            // if we deliver alerting to the application, start a timer checking that it does
            // accept/reject in time
            if (eventName.equals(Constants.Event.CONNECTION_ALERTING)) {
                startPassivessTimer(config.getAcceptTimeout(), Constants.Event.CONNECTION_ALERTING, "An alerting event was delivered to the application, but the application did not accept/reject call in time");

                	if (isSusbcribeCall() ){
	                	SubscribeCall subscribe = (SubscribeCall) call;
	                	event.defineProperty("userAgent", subscribe.getUserAgentNumber(), READONLY);
	                	event.defineProperty("isInitial", subscribe.getIsInitial(), READONLY);
	                	event.defineProperty("dialogInfo", subscribe.getDialogInfo(), READONLY);
	                	int expires = subscribe.getExpires();              	
	                	event.defineProperty("expires", expires, READONLY);

                	} else {
                		//TODO Log something in INFO stating that failed to extract subscribe information from event
                	}
          
                        
            } 
            if (eventName.equals(Constants.Event.CONNECTION_CONNECTED)) {
            	//If its a SIP subscribe event extract the info from the subscribecall and put it into the CCXML event
                if (related instanceof SubscribeEvent ){
                		SubscribeEvent subscribeEvent = (SubscribeEvent) related;
	                	Call call = subscribeEvent.getCall();
	                	if (isSusbcribeCall() ){
		                	SubscribeCall subscribe = (SubscribeCall) call;
		                	event.defineProperty("userAgent", subscribe.getUserAgentNumber(), READONLY);
		                	event.defineProperty("dialogInfo", subscribe.getDialogInfo(), READONLY);
		                	int expires = subscribe.getExpires();              	
		                	event.defineProperty("expires", expires, READONLY);
	                	} else {
	                		//TODO Log something in INFO stating that failed to extract subscribe information from event
	                	}
                }        
            }
            event.setRelated(related);
            mutateTarget(event, target);
            sendEvent(event);
        } else {
            if (getLog().isDebugEnabled()) getLog().debug("Failed to create event for " + event + " at " + debugInfo);
        }
    }

    private void mutateTarget(CCXMLEvent event, EventTarget target) {
        switch (target) {
            case CONTEXT:
                break;
            case CCXML:
                event.defineTarget("ccxml", originatingContext.getContextId());
                break;
        }
    }

    public final void sendEvent(String eventName, Event related) {
        sendEvent(eventName, related, EventTarget.CONTEXT);
    }

    /**
     * Transitions from {@link State#START}
     * <ul>
     * <li>{@linkplain AutomatonEvent#EVENT_ALERTING connection.alerting} ->  {@link State#ALERTING}</li>
     * <li>{@link Connection#createCall &lt;createcall&gt;} ->  {@link State#PROGRESSING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR error.connection} ->  {@link State#ERROR}</li>
     * </ul>
     */
    private static final Transition[] startTransitions = {new Transition(
            State.START, AutomatonEvent.EVENT_ALERTING, State.ALERTING, TransitionActions.EVENT_ALERTING),
            new Transition(
                    State.START, AutomatonEvent.CREATECALL, State.PROGRESSING),
            new Transition(
                    State.START, AutomatonEvent.EVENT_ERROR, State.ERROR,
                    TransitionActions.EVENT_ERROR)};

    /**
     * Transitions from {@link State#ALERTING}
     * <ul>
     * <li>{@link #accept &lt;accept&gt;} ->  {@link State#CONNECTED}</li>
     * <li>{@link Connection#reject &lt;reject&gt;} ->  {@link State#FAILED}</li>
     * <li>{@link #redirect &lt;redirect&gt;} -> {@link State#DISCONNECTED}</li>
     * <li>{@link #merge &lt;merge&gt;} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ALERTING connection.alerting} ->  {@link State#ALERTING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_FAILED connection.failed} ->  {@link State#FAILED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_SIGNAL connection.signal} ->  {@link State#ALERTING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR error.connection} ->  {@link State#ERROR}</li>
     * <li>{@link #proxy &lt;proxy&gt;} ->  {@link State#PROXYING}</li>
     * </ul>
     */
    private static final Transition[] alertingTransitions = {
            new Transition(
                    State.ALERTING, AutomatonEvent.ACCEPT, State.ALERTING),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_CONNECTED, State.CONNECTED, TransitionActions.ACCEPTED),
            new Transition(
                    State.ALERTING, AutomatonEvent.REJECT, State.FAILED),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_DISCONNECTED,
                    State.DISCONNECTED, TransitionActions.EVENT_DISCONNECTED),
           new Transition(State.ALERTING,AutomatonEvent.REDIRECT, State.ALERTING), 
           new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_REDIRECTED, State.DISCONNECTED, TransitionActions.EVENT_REDIRECTED),
            new Transition(
                    State.ALERTING, AutomatonEvent.MERGE, State.DISCONNECTED),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_ALERTING,
                    State.ALERTING, TransitionActions.EVENT_ALERTING),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_FAILED, State.FAILED,
                    TransitionActions.EVENT_FAILED),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_SIGNAL,
                    State.ALERTING, TransitionActions.EVENT_SIGNAL),
            new Transition(
                    State.ALERTING, AutomatonEvent.EVENT_ERROR, State.ERROR,
                    TransitionActions.EVENT_ERROR),
            new Transition(
                    State.ALERTING, AutomatonEvent.PLAY,
                    State.PLAYING_ALERTING),
            new Transition(
                    State.ALERTING, AutomatonEvent.RECORD,
                    State.RECORDING_ALERTING),
            new Transition(
            		State.ALERTING, AutomatonEvent.PROXY, State.PROXYING)};

    /**
     * Transitions from {@link State#PROGRESSING}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#FAILED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_PROGRESSING connection.progressing} ->  {@link State#PROGRESSING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_CONNECTED connection.connected} ->  {@link State#CONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_FAILED connection.failed} ->  {@link State#FAILED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR error.connection} ->  {@link State#ERROR}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_PROXIED} ->  {@link State#DISCONNECTED}</li>
     * </ul>
     */
    private static final Transition[] progressingTransitions = {new Transition(
            State.PROGRESSING, AutomatonEvent.REDIRECT, State.FAILED),
            new Transition(
                    State.PROGRESSING, AutomatonEvent.EVENT_PROGRESSING,
                    State.PROGRESSING, TransitionActions.EVENT_PROGRESSING),
            new Transition(
                    State.PROGRESSING, AutomatonEvent.EVENT_CONNECTED,
                    State.CONNECTED, TransitionActions.EVENT_CONNECTED),
            new Transition(
                    State.PROGRESSING, AutomatonEvent.EVENT_FAILED,
                    State.FAILED, TransitionActions.EVENT_FAILED),
            new Transition(
                    State.PROGRESSING, AutomatonEvent.EVENT_DISCONNECTED,
                    State.DISCONNECTED, TransitionActions.EVENT_DISCONNECTED),
            new Transition(
                    State.PROGRESSING, AutomatonEvent.EVENT_ERROR,
                    State.ERROR, TransitionActions.EVENT_ERROR),
            new Transition(
            		State.PROGRESSING, AutomatonEvent.EVENT_PROXIED,
            		State.DISCONNECTED, TransitionActions.EVENT_PROXIED)};

    /**
     * Transitions from {@link State#PROXYING}
     * <ul>
     * <li>{@linkplain AutomatonEvent#EVENT_PROGRESSING} ->  {@link State#PROGRESSING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_PROXIED ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_DISCONNECTED} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR} ->  {@link State#ERROR}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_FAILED} ->  {@link State#FAILED}</li>
     * </ul>
     */
    private static final Transition[] proxyingTransitions = {
            new Transition(
                    State.PROXYING, AutomatonEvent.EVENT_PROGRESSING,
                    State.PROGRESSING, TransitionActions.EVENT_PROGRESSING),
            new Transition(
                    State.PROXYING, AutomatonEvent.EVENT_PROXIED,
                    State.DISCONNECTED, TransitionActions.EVENT_PROXIED),
            new Transition(
                    State.PROXYING, AutomatonEvent.EVENT_DISCONNECTED,
                    State.DISCONNECTED, TransitionActions.EVENT_DISCONNECTED),
            new Transition(
                    State.PROXYING, AutomatonEvent.EVENT_ERROR,
                    State.ERROR, TransitionActions.EVENT_ERROR),
            new Transition(
                    State.PROXYING, AutomatonEvent.EVENT_FAILED,
                    State.FAILED, TransitionActions.EVENT_FAILED)};

    /**
     * Transitions from {@link State#CONNECTED}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#DISCONNECTED}</li>
     * <li>{@link #merge &lt;merge&gt;} ->  {@link State#DISCONNECTED}</li>
     * <li>{@link #disconnect &lt;merge&gt;} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_DISCONNECTED connection.disconnected} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_SIGNAL connection.signal} ->  {@link State#CONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR error.connection} ->  {@link State#ERROR}</li>
     * </ul>
     */
    private static final Transition[] connectedTransitions = {new Transition(
            State.CONNECTED, AutomatonEvent.REDIRECT, State.DISCONNECTED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.MERGE,
                    State.DISCONNECTED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.DISCONNECT,
                    State.DISCONNECTED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.EVENT_FAILED,
                    State.FAILED, TransitionActions.EVENT_FAILED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.EVENT_DISCONNECTED,
                    State.DISCONNECTED, TransitionActions.EVENT_DISCONNECTED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.EVENT_SIGNAL,
                    State.CONNECTED, TransitionActions.EVENT_SIGNAL),
            new Transition(
                    State.CONNECTED, AutomatonEvent.EVENT_ERROR, State.ERROR,
                    TransitionActions.EVENT_ERROR),
            new Transition(
                    State.CONNECTED, AutomatonEvent.RECORD,
                    State.RECORDING_CONNECTED),
            new Transition(
                    State.CONNECTED, AutomatonEvent.PLAY,
                    State.PLAYING_CONNECTED),};

    /**
     * Transitions from {@link State#RECORDING}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@link #merge &lt;merge&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@link #disconnect &lt;merge&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_DISCONNECTED connection.disconnected} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_SIGNAL connection.signal} ->  {@link State#RECORDING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_ERROR error.connection} ->  {@link State#ERROR}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FINISHED record.finished} ->  {@link State#CONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FAILED record.failed} ->  {@link State#CONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#UNRECORD record.stop} ->  {@link State#CONNECTED}</li>
     * </ul>
     */
    private static final Transition[] recordingAlertingTransitions = {
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.REDIRECT, 
                    State.RECORD_FINISHING_ALERTING),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.MERGE,
                    State.RECORD_FINISHING_ALERTING),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.DISCONNECT,
                    State.RECORD_FINISHING_ALERTING),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_DISCONNECTED,
                    State.RECORD_FINISHING_ALERTING,
                    TransitionActions.RECORD_FINISHING),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_SIGNAL,
                    State.RECORDING_ALERTING, TransitionActions.EVENT_SIGNAL),
            new Transition(
                      State.RECORDING_ALERTING, AutomatonEvent.EVENT_ERROR,
                      State.RECORD_FINISHING_ALERTING_ERROR,
                      TransitionActions.RECORD_FINISHING_ALERTING_ERROR),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_RECORD_FINISHED,
                    State.ALERTING, TransitionActions.RECORD_FINISHED),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_RECORD_FAILED,
                    State.ALERTING, TransitionActions.RECORD_FAILED),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.UNRECORD,
                    State.ALERTING),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_FAILED,
                    State.RECORD_FINISHING_ALERTING_FAILED,
                    TransitionActions.RECORD_FINISHING_ALERTING_FAILED),
            new Transition(
                    State.RECORDING_ALERTING, AutomatonEvent.EVENT_CONNECTED,
                    State.RECORDING_CONNECTED),
    };
    
    private static final Transition[] recordingConnectedTransitions = {
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.REDIRECT, State.RECORD_FINISHING_CONNECTED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.MERGE,
                    State.RECORD_FINISHING_CONNECTED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.DISCONNECT,
                    State.RECORD_FINISHING_CONNECTED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.EVENT_DISCONNECTED,
                    State.RECORD_FINISHING_CONNECTED,
                    TransitionActions.RECORD_FINISHING),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.EVENT_SIGNAL,
                    State.RECORDING_CONNECTED, TransitionActions.EVENT_SIGNAL),
            new Transition(
                     State.RECORDING_CONNECTED, AutomatonEvent.EVENT_ERROR,
                     State.RECORD_FINISHING_ERROR,
                     TransitionActions.RECORD_FINISHING_ERROR),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.EVENT_RECORD_FINISHED,
                    State.CONNECTED, TransitionActions.RECORD_FINISHED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.EVENT_RECORD_FAILED,
                    State.CONNECTED, TransitionActions.RECORD_FAILED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.UNPLAY,
                    State.CONNECTED),
            new Transition(
                    State.RECORDING_CONNECTED, AutomatonEvent.EVENT_FAILED,
                    State.RECORD_FINISHING_FAILED,
                    TransitionActions.RECORD_FINISHING_FAILED),
    };
    
    private static final Transition[] recordingFailedTransitions = {
        new Transition(
            State.RECORD_FINISHING_FAILED, AutomatonEvent.EVENT_RECORD_FINISHED, State.FAILED,
            TransitionActions.RECORD_FINISHED_AFTER_FAILED),
        new Transition(
            State.RECORD_FINISHING_FAILED, AutomatonEvent.EVENT_RECORD_FAILED, State.FAILED,
            TransitionActions.RECORD_FAILED_AFTER_FAILED),
    };
    
    private static final Transition[] recordingAlertingFailedTransitions = {
        new Transition(
                State.RECORD_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_RECORD_FINISHED, State.FAILED,
                TransitionActions.RECORD_FINISHED_AFTER_FAILED),
        new Transition(
                State.RECORD_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_RECORD_FAILED, State.FAILED,
                TransitionActions.RECORD_FAILED_AFTER_FAILED),
       new Transition(
                State.RECORD_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_DISCONNECTED, State.FAILED,
                TransitionActions.RECORD_FAILED_AFTER_FAILED),
    };
    
    private static final Transition[] recordingErrorTransitions = {
            new Transition(
            State.RECORD_FINISHING_ERROR, AutomatonEvent.EVENT_RECORD_FINISHED, State.ERROR,
            TransitionActions.RECORD_FINISHED_AFTER_ERROR),
            new Transition(
            State.RECORD_FINISHING_ERROR, AutomatonEvent.EVENT_RECORD_FAILED, State.ERROR,
            TransitionActions.RECORD_FAILED_AFTER_ERROR),
    };

    private static final Transition[] recordingErrorAlertingTransitions = {
        new Transition(
        State.RECORD_FINISHING_ALERTING_ERROR, AutomatonEvent.EVENT_RECORD_FINISHED, State.ERROR,
        TransitionActions.RECORD_FINISHED_AFTER_ERROR),
        new Transition(
        State.RECORD_FINISHING_ALERTING_ERROR, AutomatonEvent.EVENT_RECORD_FAILED, State.ERROR,
        TransitionActions.RECORD_FAILED_AFTER_ERROR),
};
    
    /**
     * Transitions from {@link State#RECORD_FINISHING_CONNECTED}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FINISHED record.finished} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FAILED record.failed} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#UNRECORD record.stop} ->  {@link State#DISCONNECTED}</li>
     * </ul>
     */
    private static final Transition[] recordingFinishingConnectedTransitions = {
            new Transition(
                    State.RECORD_FINISHING_CONNECTED, AutomatonEvent.EVENT_RECORD_FINISHED, State.DISCONNECTED,
                    TransitionActions.RECORD_FINISHED_AFTER_HANGUP),
            new Transition(
                    State.RECORD_FINISHING_CONNECTED,
                    AutomatonEvent.EVENT_RECORD_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.RECORD_FAILED_AFTER_HANGUP),
            new Transition(
                    State.RECORD_FINISHING_CONNECTED, AutomatonEvent.UNRECORD,
                    State.DISCONNECTED,
                    TransitionActions.EVENT_DISCONNECTED),
    };

    /**
     * Transitions from {@link State#RECORD_FINISHING}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FINISHED record.finished} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FAILED record.failed} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#UNRECORD record.stop} ->  {@link State#DISCONNECTED}</li>
     * </ul>
     */
    private static final Transition[] recordingFinishingAlertingTransitions = {
            new Transition(
                    State.RECORD_FINISHING_ALERTING, AutomatonEvent.EVENT_RECORD_FINISHED, State.DISCONNECTED,
                    TransitionActions.RECORD_FINISHED_AFTER_HANGUP),
            new Transition(
                    State.RECORD_FINISHING_ALERTING,
                    AutomatonEvent.EVENT_RECORD_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.RECORD_FAILED_AFTER_HANGUP),
            new Transition(
                    State.RECORD_FINISHING_ALERTING, AutomatonEvent.UNRECORD,
                    State.DISCONNECTED,
                    TransitionActions.EVENT_DISCONNECTED),
    };
    
    private static final Transition[] playingConnectedTransitions = {new Transition(
            State.PLAYING_CONNECTED, AutomatonEvent.REDIRECT, State.PLAY_FINISHING_CONNECTED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.MERGE,
                    State.PLAY_FINISHING_CONNECTED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.DISCONNECT,
                    State.PLAY_FINISHING_CONNECTED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.EVENT_DISCONNECTED,
                    State.PLAY_FINISHING_CONNECTED,
                    TransitionActions.PLAY_FINISHING),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.EVENT_SIGNAL,
                    State.PLAYING_CONNECTED, TransitionActions.EVENT_SIGNAL),
            new Transition(
                     State.PLAYING_CONNECTED, AutomatonEvent.EVENT_ERROR,
                     State.PLAY_FINISHING_ERROR,
                     TransitionActions.PLAY_FINISHING_ERROR),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.EVENT_PLAY_FINISHED,
                    State.CONNECTED, TransitionActions.PLAY_FINISHED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.EVENT_PLAY_FAILED,
                    State.CONNECTED, TransitionActions.PLAY_FAILED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.UNPLAY,
                    State.CONNECTED),
            new Transition(
                    State.PLAYING_CONNECTED, AutomatonEvent.EVENT_FAILED,
                    State.PLAY_FINISHING_FAILED,
                    TransitionActions.PLAY_FINISHING_FAILED),
    };

    private static final Transition[] playingFailedTransitions = {
            new Transition(
            State.PLAY_FINISHING_FAILED, AutomatonEvent.EVENT_PLAY_FINISHED, State.FAILED,
            TransitionActions.PLAY_FINISHED_AFTER_FAILED),
            new Transition(
            State.PLAY_FINISHING_FAILED, AutomatonEvent.EVENT_PLAY_FAILED, State.FAILED,
            TransitionActions.PLAY_FAILED_AFTER_FAILED),

    };

    private static final Transition[] playingErrorTransitions = {
            new Transition(
            State.PLAY_FINISHING_ERROR, AutomatonEvent.EVENT_PLAY_FINISHED, State.ERROR,
            TransitionActions.PLAY_FINISHED_AFTER_ERROR),
            new Transition(
            State.PLAY_FINISHING_ERROR, AutomatonEvent.EVENT_PLAY_FAILED, State.ERROR,
            TransitionActions.PLAY_FAILED_AFTER_ERROR),
    };

    /**
     * Transitions from {@link State#RECORD_FINISHING}
     * <ul>
     * <li>{@link #redirect &lt;redirect&gt;} ->  {@link State#RECORD_FINISHING}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FINISHED record.finished} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#EVENT_RECORD_FAILED record.failed} ->  {@link State#DISCONNECTED}</li>
     * <li>{@linkplain AutomatonEvent#UNRECORD record.stop} ->  {@link State#DISCONNECTED}</li>
     * </ul>
     */
    private static final Transition[] playFinishingConnectedTransitions = {new Transition(
            State.PLAY_FINISHING_CONNECTED, AutomatonEvent.EVENT_PLAY_FINISHED, State.DISCONNECTED,
            TransitionActions.PLAY_FINISHED_AFTER_HANGUP),
            new Transition(
                    State.PLAY_FINISHING_CONNECTED,
                    AutomatonEvent.EVENT_PLAY_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.PLAY_FAILED_AFTER_HANGUP),
            new Transition(
                    State.PLAY_FINISHING_CONNECTED, AutomatonEvent.UNPLAY,
                    State.DISCONNECTED,
                    TransitionActions.EVENT_DISCONNECTED),};

    private static final Transition[] playingAlertingTransitions = {
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.REDIRECT, State.PLAY_FINISHING_ALERTING),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.MERGE,
                    State.PLAY_FINISHING_ALERTING),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.DISCONNECT,
                    State.PLAY_FINISHING_ALERTING),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_DISCONNECTED,
                    State.PLAY_FINISHING_CONNECTED,
                    TransitionActions.PLAY_FINISHING),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_SIGNAL,
                    State.PLAYING_ALERTING, TransitionActions.EVENT_SIGNAL),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_ERROR,
                    State.PLAY_FINISHING_ALERTING_ERROR,
                    TransitionActions.PLAY_FINISHING_ALERTING_ERROR),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_PLAY_FINISHED,
                    State.ALERTING, TransitionActions.PLAY_FINISHED),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_PLAY_FAILED,
                    State.ALERTING, TransitionActions.PLAY_FAILED),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.UNPLAY,
                    State.ALERTING),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_CONNECTED,
                    State.PLAYING_CONNECTED),
            new Transition(
                    State.PLAYING_ALERTING, AutomatonEvent.EVENT_FAILED,
                    State.PLAY_FINISHING_ALERTING_FAILED,
                    TransitionActions.PLAY_FINISHING_ALERTING_FAILED),
    };

    private static final Transition[] playingAlertingFailedTransitions = {
            new Transition(
            State.PLAY_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_PLAY_FINISHED, State.FAILED,
            TransitionActions.PLAY_FINISHED_AFTER_FAILED),
            new Transition(
            State.PLAY_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_PLAY_FAILED, State.FAILED,
            TransitionActions.PLAY_FAILED_AFTER_FAILED),
            new Transition(
            State.PLAY_FINISHING_ALERTING_FAILED, AutomatonEvent.EVENT_DISCONNECTED, State.FAILED,
            TransitionActions.PLAY_FAILED_AFTER_FAILED),
    };

    private static final Transition[] playingAlertingErrorTransitions = {
            new Transition(
            State.PLAY_FINISHING_ALERTING_ERROR, AutomatonEvent.EVENT_PLAY_FINISHED, State.ERROR,
            TransitionActions.PLAY_FINISHED_AFTER_ERROR),
            new Transition(
            State.PLAY_FINISHING_ALERTING_ERROR, AutomatonEvent.EVENT_PLAY_FAILED, State.ERROR,
            TransitionActions.PLAY_FAILED_AFTER_ERROR),
    };



    private static final Transition[] playFinishingAlertingTransitions = {new Transition(
            State.PLAY_FINISHING_ALERTING, AutomatonEvent.EVENT_PLAY_FINISHED, State.DISCONNECTED,
            TransitionActions.PLAY_FINISHED_AFTER_HANGUP),
            new Transition(
                    State.PLAY_FINISHING_ALERTING,
                    AutomatonEvent.EVENT_PLAY_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.PLAY_FAILED_AFTER_HANGUP),
            new Transition(
                    State.PLAY_FINISHING_ALERTING, AutomatonEvent.UNPLAY,
                    State.DISCONNECTED,
                    TransitionActions.EVENT_DISCONNECTED),};

    // if we get some playFinished etc after disconnect, make sure they are processed,
    // since the VXML probably is hung waiting for them.
    // if the VXML tries to play/record we stay in disconnected since the VXML has no chance to know if we just got disconnected
    private static final Transition[] disconnectedTransitions = {
            new Transition(
                    State.DISCONNECTED, AutomatonEvent.EVENT_PLAY_FINISHED, State.DISCONNECTED,
                    TransitionActions.PLAY_FINISHED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.EVENT_PLAY_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.PLAY_FAILED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.EVENT_DISCONNECTED,
                    State.DISCONNECTED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.EVENT_RECORD_FINISHED,
                    State.DISCONNECTED,
                    TransitionActions.RECORD_FINISHED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.EVENT_RECORD_FAILED,
                    State.DISCONNECTED,
                    TransitionActions.RECORD_FAILED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.PLAY,
                    State.DISCONNECTED),
            new Transition(
                    State.DISCONNECTED,
                    AutomatonEvent.RECORD,
                    State.DISCONNECTED)};

    // If we forced disconnect we silently discard all events
    private static final Transition[] forcedDisconnectTransitions = {
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_ALERTING, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_PROGRESSING, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_PROXIED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_SIGNAL, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_CONNECTED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_PLAY_FINISHED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_PLAY_FAILED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_RECORD_FINISHED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_RECORD_FAILED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_DISCONNECTED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_FAILED, State.FORCED_DISCONNECT),
            new Transition(
                    State.FORCED_DISCONNECT, AutomatonEvent.EVENT_ERROR, State.FORCED_DISCONNECT),};

    // If we are in ERROR state we stay there in case of a received event.
    // The event is sent to the application though.
    private static final Transition[] errorTransitions = {
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_ALERTING, State.ERROR, TransitionActions.EVENT_ALERTING),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_PROGRESSING, State.ERROR, TransitionActions.EVENT_PROGRESSING),
            new Transition(
            		State.ERROR, AutomatonEvent.EVENT_PROXIED, State.ERROR, TransitionActions.EVENT_PROXIED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_SIGNAL, State.ERROR, TransitionActions.EVENT_SIGNAL),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_CONNECTED, State.ERROR, TransitionActions.EVENT_CONNECTED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_PLAY_FINISHED, State.ERROR, TransitionActions.PLAY_FINISHED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_PLAY_FAILED, State.ERROR, TransitionActions.PLAY_FAILED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_RECORD_FINISHED, State.ERROR, TransitionActions.RECORD_FINISHED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_RECORD_FAILED, State.ERROR, TransitionActions.RECORD_FAILED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_DISCONNECTED, State.ERROR, TransitionActions.EVENT_DISCONNECTED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_FAILED, State.ERROR, TransitionActions.EVENT_FAILED),
            new Transition(
                    State.ERROR, AutomatonEvent.EVENT_ERROR, State.ERROR, TransitionActions.EVENT_ERROR)
    };

    // If we are in FAILED state we stay there in case of a received event.
    // The event is sent to the application though.
    private static final Transition[] failedTransitions = {
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_ALERTING, State.FAILED, TransitionActions.EVENT_ALERTING),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_PROGRESSING, State.FAILED, TransitionActions.EVENT_PROGRESSING),
	        new Transition(
	                State.FAILED, AutomatonEvent.EVENT_PROXIED, State.FAILED, TransitionActions.EVENT_PROXIED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_SIGNAL, State.FAILED, TransitionActions.EVENT_SIGNAL),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_CONNECTED, State.FAILED, TransitionActions.EVENT_CONNECTED),
            new Transition(State.FAILED, AutomatonEvent.PLAY, State.FAILED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_PLAY_FINISHED, State.FAILED, TransitionActions.PLAY_FINISHED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_PLAY_FAILED, State.FAILED, TransitionActions.PLAY_FAILED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_RECORD_FINISHED, State.FAILED, TransitionActions.RECORD_FINISHED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_RECORD_FAILED, State.FAILED, TransitionActions.RECORD_FAILED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_DISCONNECTED, State.FAILED, TransitionActions.EVENT_DISCONNECTED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_FAILED, State.FAILED, TransitionActions.EVENT_FAILED),
            new Transition(
                    State.FAILED, AutomatonEvent.EVENT_ERROR, State.FAILED, TransitionActions.EVENT_ERROR)
    };


    public static final Map<State, Map<AutomatonEvent, Transition>> stateTable;

    static {
        stateTable = new EnumMap<State, Map<AutomatonEvent, Transition>>(State.class);
        stateTable.put(State.START, createTransitionMap(startTransitions));
        stateTable.put(State.ALERTING, createTransitionMap(alertingTransitions));
        stateTable.put(State.PROGRESSING, createTransitionMap(progressingTransitions));
        stateTable.put(State.PROXYING,  createTransitionMap(proxyingTransitions));
        stateTable.put(State.CONNECTED, createTransitionMap(connectedTransitions));
        stateTable.put(State.DISCONNECTED, createTransitionMap(disconnectedTransitions));
        stateTable.put(State.FORCED_DISCONNECT, createTransitionMap(forcedDisconnectTransitions));
        stateTable.put(State.ERROR, createTransitionMap(errorTransitions));
        stateTable.put(State.FAILED, createTransitionMap(failedTransitions));
        
        stateTable.put(State.RECORDING_ALERTING, createTransitionMap(recordingAlertingTransitions));
        stateTable.put(State.RECORDING_CONNECTED, createTransitionMap(recordingConnectedTransitions));     
        stateTable.put(State.RECORD_FINISHING_CONNECTED, createTransitionMap(recordingFinishingConnectedTransitions));
        stateTable.put(State.RECORD_FINISHING_ALERTING, createTransitionMap(recordingFinishingAlertingTransitions));         
        stateTable.put(State.RECORD_FINISHING_ALERTING_FAILED, createTransitionMap(recordingAlertingFailedTransitions));
        stateTable.put(State.RECORD_FINISHING_FAILED, createTransitionMap(recordingFailedTransitions));       
        stateTable.put(State.RECORD_FINISHING_ERROR, createTransitionMap(recordingErrorTransitions));
        stateTable.put(State.RECORD_FINISHING_ALERTING_ERROR, createTransitionMap(recordingErrorAlertingTransitions));
        
        stateTable.put(State.PLAYING_CONNECTED, createTransitionMap(playingConnectedTransitions));
        stateTable.put(State.PLAYING_ALERTING, createTransitionMap(playingAlertingTransitions));      
        stateTable.put(State.PLAY_FINISHING_CONNECTED, createTransitionMap(playFinishingConnectedTransitions));
        stateTable.put(State.PLAY_FINISHING_ALERTING, createTransitionMap(playFinishingAlertingTransitions));
        stateTable.put(State.PLAY_FINISHING_FAILED, createTransitionMap(playingFailedTransitions));
        stateTable.put(State.PLAY_FINISHING_ALERTING_FAILED, createTransitionMap(playingAlertingFailedTransitions));
        stateTable.put(State.PLAY_FINISHING_ERROR, createTransitionMap(playingErrorTransitions));
        stateTable.put(State.PLAY_FINISHING_ALERTING_ERROR, createTransitionMap(playingAlertingErrorTransitions));

    }

    private static Map<AutomatonEvent, Transition> createTransitionMap(Transition[] transitions) {
        Map<AutomatonEvent, Transition> m = new HashMap<AutomatonEvent, Transition>();
        for (Transition t : transitions) {
            m.put(t.getAutomatonEvent(), t);
        }
        return m;

    }


    public void receiveEvent(AutomatonEvent automatonEvent, Event related) {
        synchronized (stateLock) {
            Map<AutomatonEvent, Transition> transitions = getPossibleTransitions();
            if (null != transitions) {
                Transition transition = transitions.get(automatonEvent);
                if (null != transition) {
                    if (getLog().isDebugEnabled()) getLog().debug("Performing " + transition);
                    transition.exitingState(this, related);
                    setState(transition.getNewState());

                    if (isInTerminalState()) {
                        signalTerminalState();
                    } else {
                        signalStateChanging();
                    }

                    transition.enteringState(this, related);
                } else {
                    ignoredEvent(automatonEvent);
                }
            } else {
                eventInTerminalState(automatonEvent);
                signalTerminalState();
            }
        }
    }

    private void signalStateChanging() {
        ApplicationWatchdog.instance().signalConnectionStateChanging(originatingContext.getSession().getIdentity(), this);
    }

    private void signalTerminalState() {
        ApplicationWatchdog.instance().signalConnectionInTerminalState(originatingContext.getSession().getIdentity(), this);
    }

    private void setState(State newState) {
        state.set(newState);
        SessionInfo si = getSessionInstance();
        if (si != null) {
            try {
                si.setConnetionState(SessionInfoHelper.getCallState(newState));
                if (newState == State.DISCONNECTED ||
                        newState == State.FORCED_DISCONNECT ||
                        newState == State.ERROR ||
                        newState == State.FAILED) {
                    if (getLog().isDebugEnabled()) getLog().debug("Releasing SessionInfo handle");
                    originatingContext.getSessionInfoFactory().returnSessionInstance(si);
                    sessionInfoReleased = true;
                }
            } catch (IlegalSessionInstanceException e) {
                Ignore_illegalSessionInstanceException(e);
            }
        }

        put(Constants.CCXML.STATE, this, state.toString());
    }

    private void Ignore_illegalSessionInstanceException(IlegalSessionInstanceException e) {

    }

    public Map<AutomatonEvent, Transition> getPossibleTransitions() {
        return stateTable.get(state.get());
    }

    /**
     * @param automatonEvent
     * @logs.error "Ignoring event <automatonEvent> in state <state>" - A state machine received the unexpected event <automatonEvent> when in state <state>
     */
    protected void ignoredEvent(AutomatonEvent automatonEvent) {
        ILogger log = getLog();
        log.warn("Ignoring event " + automatonEvent + " in state " + state);
    }

    /**
     * @param automatonEvent
     * @logs.error "Received event <automatonEvent>  in terminal state "+state" - A state machine received the unexpected event <automatonEvent> when the sate machine was in state <state>
     */
    protected void eventInTerminalState(AutomatonEvent automatonEvent) {
        ILogger log = getLog();
        log.error("Received event " + automatonEvent + " in terminal state " + state);
    }

    public void createCall(CallProperties callProperties) {
        receiveEvent(AutomatonEvent.CREATECALL, null);
        getEventSourceManager().placeCall(this, callProperties);
    }

    public void accept() {
        synchronized (stateLock) {
            Map<AutomatonEvent, Transition> possibleTransitions = getPossibleTransitions();
            if (possibleTransitions != null && possibleTransitions.containsKey(AutomatonEvent.ACCEPT)) {
                if (isInboundCall()) {
                    // Stop the timer that was started when alerting event was delivered to the application
                    stopPassivenessTimer();
                    getAsInboundCall().accept();
                }else if (isSusbcribeCall()) {
                	stopPassivenessTimer();
                    getAsSusbcribeCall().accept();
                	
                }else {
                    sendEvent(Constants.Event.ERROR_NOTALLOWED, "Accept needs an inbound call or subscribe leg !", null);
                }
            } else {
                sendEvent(Constants.Event.ERROR_NOTALLOWED, "Accept not valid when connection state is " + state, null);
            }
        }
    }

    public void proxy(String server, int port){
    	synchronized (stateLock) {
    		Map<AutomatonEvent, Transition> possibleTransitions = getPossibleTransitions();
    		if (possibleTransitions != null && possibleTransitions.containsKey(AutomatonEvent.PROXY)){
    			if (isInboundCall()) {
    				// Stop the timer that was started when alerting event was delivered to the application
    				stopPassivenessTimer();
    				setState(possibleTransitions.get(AutomatonEvent.PROXY).getNewState());
    				signalStateChanging();
    				getAsInboundCall().proxy(new RemotePartyAddress(server, port));
    			} else {
    				sendEvent(Constants.Event.ERROR_NOTALLOWED, "Proxy needs an inbound call leg !", null);
    			}
    		} else {
    			sendEvent(Constants.Event.ERROR_NOTALLOWED, "Proxy not valid when connection state is " + state, null);
    		}
    	}
    }

	private void sendEvent(String eventName, String message, Event related) {
        CCXMLEvent event = CCXMLEvent.create(eventName, message, originatingContext, this, debugInfo);
        if (event != null) {
            sendEvent(event);
        }
    }

    /**
     * @param rejectEventTypeName
     * @param reason
     * @logs.error "Invalid call state for call <call> when trying to transition to <REJECT> according to our state <state>, we should have an inbound call." - The CCXML application tried to execute the <reject> tag on a non-inbound call
     */
    public void reject(String rejectEventTypeName, String reason) {
        synchronized (stateLock) {
            Map<AutomatonEvent, Transition> possibleTransitions = getPossibleTransitions();
            if (possibleTransitions != null && possibleTransitions.containsKey(AutomatonEvent.REJECT)) {
                if (isInboundCall()) {
                    // Stop the timer that was started when alerting event was delivered to the application
                    cleanup();
                    stopPassivenessTimer();                    
                    getAsInboundCall().reject(rejectEventTypeName, reason);
                } else if (isSusbcribeCall()){
                	cleanup();
                    stopPassivenessTimer();
                	getAsSusbcribeCall().reject(reason);
                    
            	} else {
                    sendEvent(Constants.Event.ERROR_NOTALLOWED, "Reject needs an inbound or subscribe call leg !", null);
                }
            } else {
                sendEvent(Constants.Event.ERROR_NOTALLOWED, "Reject not valid when connection state is " + state, null);
            }
        }
    }

    public void redirect() {
    }

    
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode) {
        synchronized (stateLock) {
            Map<AutomatonEvent, Transition> possibleTransitions = getPossibleTransitions();
            if (possibleTransitions != null && possibleTransitions.containsKey(AutomatonEvent.REDIRECT)) {
                if (isInboundCall()) {
                    // Stop the timer that was started when alerting event was delivered to the application
                    stopPassivenessTimer();
                    getAsInboundCall().redirect(destination, redirectCode);               
                }else {
                    sendEvent(Constants.Event.ERROR_NOTALLOWED, "Redirect needs an inbound call!", null);
                }
            } else {
                sendEvent(Constants.Event.ERROR_NOTALLOWED, "Redirect not valid when connection state is " + state, null);
            }
        }
        
    }
    public void merge() {
    }

    public void forcedDisconnect() {
        synchronized (stateLock) {
            if (getPossibleTransitions() != null)
                setState(State.FORCED_DISCONNECT);
        }
        disconnect();
    }

    public void disconnect() {
        signalTerminalState();
        if (isInboundCall()) {
            // Stop the timer that was started when alerting event was delivered to the application
            stopPassivenessTimer();
            getAsInboundCall().disconnect();
        } else if (isSusbcribeCall()){
            // Stop the timer that was started when alerting event was delivered to the application
            stopPassivenessTimer();
        
    	}else {
            getAsOutboundCall().disconnect();
        }
    }

    public void record() {
        SessionInfo sessionInstance = getSessionInstance();
        if (sessionInstance != null) {
            try {
                sessionInstance.setInboundActivity(CallActivity.RECORD);
            } catch (IlegalSessionInstanceException e) {
            }
        }
        receiveEvent(AutomatonEvent.RECORD, null);
    }

    public void stopRecording() {
        receiveEvent(AutomatonEvent.UNRECORD, null);
    }

    public void play() {
        SessionInfo sessionInstance = getSessionInstance();
        if (sessionInstance != null) {
            try {
                sessionInstance.setOutboundActivity(CallActivity.PLAY);
            } catch (IlegalSessionInstanceException e) {
            }
        }
        receiveEvent(AutomatonEvent.PLAY, null);
    }

    public void stopPlaying() {
        receiveEvent(AutomatonEvent.UNPLAY, null);
    }


    public void setCall(Call call) {
        this.call = call;
        SessionInfo si = getSessionInstance();
        if (si != null) {
            try {
                // All connections that are not the first connection shall have session ID as session Initiator.
                // if null we ARE the initiating connection, event source manager just does not know it yet
                com.mobeon.masp.execution_engine.ccxml.Connection initiatingConnection = originatingContext.getEventSourceManager().getInitiatingConnection();
                if (initiatingConnection == this ||
                        initiatingConnection == null) {
                    Object sessionInitiator = originatingContext.getSession().getData(ISession.SESSION_INITIATOR);
                    if (sessionInitiator != null && sessionInitiator instanceof String) {
                        String s = (String) sessionInitiator;
                        si.setSessionInitiator(s);
                    }
                } else {
                    si.setSessionInitiator(SessionInfoHelper.getMonitorId(originatingContext));
                }

                Object data = originatingContext.getSession().getData(ISession.SERVICE_NAME);
                if (data != null && data instanceof String) {
                    String serviceName = (String) data;
                    si.setService(serviceName);
                }

                si.setConnetionType(SessionInfoHelper.getCallType(call.getCallType()));
                if (isInboundCall()) {
                    si.setDirection(CallDirection.INBOUND);
                } else if (isSusbcribeCall()){
                	si.setDirection(CallDirection.INBOUND);
                	
                } else {
                    si.setDirection(CallDirection.OUTBOUND);
                }
                if (call.getCallingParty() != null) {
                    si.setANI(call.getCallingParty().getTelephoneNumber());
                }
                if (call.getCalledParty() != null) {
                    si.setDNIS(call.getCalledParty().getTelephoneNumber());
                }
                if (call instanceof InboundCall) {
                    InboundCall inboundCall = (InboundCall) call;
                    RedirectingParty redirectingParty = inboundCall.getRedirectingParty();
                    if (redirectingParty != null) {
                        si.setRDNIS(redirectingParty.getTelephoneNumber());
                    }
                }
            } catch (IlegalSessionInstanceException e) {
                Ignore_illegalSessionInstanceException(e);
            }
        }
    }

    private void setCallType(Call call) {
        Object type = Undefined.instance;
        if (call != null) {
            CallProperties.CallType ct = call.getCallType();
            if (ct == CallProperties.CallType.VOICE) {
                type = Constants.CallProperties.VOICE;
            } else if (ct == CallProperties.CallType.VIDEO) {
                type = Constants.CallProperties.VIDEO;
            }
            put(Constants.CCXML._CALLTYPE, this, type);
        }
    }


    public String toString() {
        return "Connection{" + "connectionId=" + getBridgePartyId() + '}';
    }

    public State getState() {
        return state.get();
    }

    public com.mobeon.masp.execution_engine.ccxml.Connection clone() {
        return new ConnectionImpl(this);
    }

    public ScriptableObject getVoiceXMLMirror() {

        return new VoiceXMLMirror(this);
    }


    private EventSourceManager getEventSourceManager() {
        return originatingContext.getEventSourceManager();
    }


    public String getClassName() {
        return "Connection";
    }

    public String getSessionId() {
        return originatingContext.getSessionId();
    }

    public boolean join(BridgeParty otherParty, boolean fullDuplex, boolean implicit) {

        Bridge bridge = new Bridge(getEventSourceManager(), sender, this, otherParty, fullDuplex, implicit);
        bridge.join(implicit);

        return true;
    }

    public boolean unjoin(BridgeParty otherParty) {
        Bridge bridge = getEventSourceManager().findBridge(this, otherParty);
        if (bridge != null) {
            bridge.unjoin(false);
            return true;
        }
        return false;
    }


    public void onEarlyMedia() {
        try {
            SessionInfo si = getSessionInstance();
            if (si != null) {
                si.setConnetionType(SessionInfoHelper.getCallType(call.getCallType()));
                setFarEndProperties(si);
            }
        } catch (IlegalSessionInstanceException e) {
            Ignore_illegalSessionInstanceException(e);
        }
        sendEvent(CCXMLEvent.create(Constants.Event.MOBEON_PlATFORM_EARLYMEDIARESOURCEAVAILABLE, "Early media available", originatingContext, this, DebugInfo.getInstance()));

    }

    public void onEarlyMediaFailed() {
        sendEvent(CCXMLEvent.create(Constants.Event.MOBEON_PlATFORM_EARLYMEDIARESOURCEFAILED, "Early media failed", originatingContext, this, DebugInfo.getInstance()));
    }

    public void onAlerting() {
        try {
            SessionInfo si = getSessionInstance();
            if (si != null) {
                si.setFarEndConProp("");
            }

        } catch (IlegalSessionInstanceException e) {
            Ignore_illegalSessionInstanceException(e);
        }
    }

    public void onProgressing(ProgressingEvent realEvent) {
        try {
            SessionInfo si = getSessionInstance();
            if (si != null) {
                if (realEvent.isEarlyMedia()) {
                    setFarEndProperties(si);
                }
            }
        } catch (IlegalSessionInstanceException e) {
            Ignore_illegalSessionInstanceException(e);
        }
    }

    public void onConnected() {
        try {
            SessionInfo si = getSessionInstance();
            if (si != null) {
                si.setConnetionType(SessionInfoHelper.getCallType(call.getCallType()));
                setFarEndProperties(si);
            }

            // call type is not available until connected event, that is, now.
            setCallType(call);
        } catch (IlegalSessionInstanceException e) {
            Ignore_illegalSessionInstanceException(e);
        }
    }

    private void setFarEndProperties(SessionInfo si) throws IlegalSessionInstanceException {
        Set<Connection> connections = call.getFarEndConnections();
        String farEndProp = "";
        for (Connection connection : connections) {
            farEndProp += connection.getProtocol() + " " + connection.getIpAddress() + ":" + connection.getPort() + "; ";
        }
        si.setFarEndConProp(farEndProp);
    }

    public void onPlayEnded() {
        SessionInfo sessionInstance = getSessionInstance();
        if (sessionInstance != null) {
            try {
                sessionInstance.setOutboundActivity(CallActivity.IDLE);
            } catch (IlegalSessionInstanceException e) {
                Ignore_illegalSessionInstanceException(e);
            }
        }
    }

    public void onRecordEnded() {
        SessionInfo sessionInstance = getSessionInstance();
        if (sessionInstance != null) {
            try {
                sessionInstance.setInboundActivity(CallActivity.IDLE);
            } catch (IlegalSessionInstanceException e) {
                Ignore_illegalSessionInstanceException(e);
            }
        }
    }

    public int getCallManagerWaitTimeout() {
        return config.getCallManagerWaitTime();
    }

    public int getCreateCallAdditionalTimeout() {
        return config.getCreateCallAdditionalTimeout();
    }

    public void cleanup() {
        ApplicationWatchdog.instance().signalConnectionCleanedUp(this, originatingContext.getSession().getIdentity());
        stopPassivenessTimer();
    }

    public void sendEvent(CCXMLEvent event, EventTarget target) {
        mutateTarget(event, target);
        sender.sendEvent(event);
    }

    public void sendEvent(CCXMLEvent event) {
        sender.sendEvent(event);
    }

    private void startPassivessTimer(long time, String timerName, String message) {
        if (ongoingTimeout != null) {
            ongoingTimeout.cancel(false);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Starting passiveness timeout:" + timerName);
        }
        ongoingTimeout = timeoutscheduler.schedule(new PassivenessTimeoutSender(this, originatingContext, timerName, message), time, TimeUnit.MILLISECONDS);
    }

    private void stopPassivenessTimer() {
        if (ongoingTimeout != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Stopping a passiveness timeout");
            }
            // Allow ongoing tasks to complete
            ongoingTimeout.cancel(false);
        }
    }

    /**
     * Callback, called from PassivenessTimeoutSender when timeout triggers
     *
     * @param timerName
     */
    private void onTriggeredPassivenessTimeout(String timerName) {
        if (timerName.equals(Constants.Event.CONNECTION_ALERTING)) {
            ILogger log = getLog();
            if (log.isDebugEnabled()) {
                log.debug("Passiveness timeout for alerting event triggered, disconnect the call");
            }
            disconnect();
        }
    }

    private SessionInfo getSessionInstance() {

        if (sessionInfoReleased) return null;

        SessionInfo sessionInstance = null;
        SessionInfoFactory sessionInfoFactory = originatingContext.getSessionInfoFactory();
        if (sessionInfoFactory == null) {
            if (getLog().isDebugEnabled()) getLog().debug("sessionInfoFactory was null");
        } else {
            sessionInstance = sessionInfoFactory.getSessionInstance(SessionInfoHelper.getMonitorId(originatingContext), connectionId);
            if (sessionInstance == null) {
                if (getLog().isDebugEnabled()) getLog().debug("sessionInstance was null");
            }
        }
        return sessionInstance;
    }

    public boolean isInTerminalState() {
        Map<AutomatonEvent, Transition> transitions = getPossibleTransitions();
        if (transitions != null) {
            for (Map.Entry<AutomatonEvent, Transition> entry : transitions.entrySet()) {
                if (entry.getValue().getNewState() != getState()) {
                    return false;
                }
            }
        }
        return true;
    }

    public ExecutionContext getExecutionContext() {
        return originatingContext;
    }

    private static class PassivenessTimeoutSender implements Runnable {
        private static final ILogger log = ILoggerFactory.getILogger(PassivenessTimeoutSender.class);

        public PassivenessTimeoutSender(ConnectionImpl connection, CCXMLExecutionContext context, String timerName, String message) {
            this.connection = connection;
            this.context = context;
            this.timerName = timerName;
            this.message = message;
        }

        private final ExecutionContext context;
        private String message;
        private String timerName;
        private ConnectionImpl connection;


        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Passiveness timeout triggered:" + timerName + ":" + message);
            }
            connection.onTriggeredPassivenessTimeout(timerName);
            context.getEventHub().fireContextEvent(Constants.Event.ERROR_CONNECTION, message, DebugInfo.getInstance());
        }

    }



}
