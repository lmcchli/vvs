/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILogger;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

/**
 * @author Mikael Andersson
 */
public interface Connection extends BridgeParty, Scriptable {

    enum State {
        // Initial state
        START,
        ALERTING,
        PROGRESSING,
        PROXYING,
        CONNECTED,
        // Terminal states
        DISCONNECTED,
        FAILED,
        ERROR,
        FORCED_DISCONNECT,
        
        //PLAYING
        PLAYING_CONNECTED,
        PLAY_FINISHING_CONNECTED,
        PLAYING_ALERTING,
        PLAY_FINISHING_ALERTING,
        PLAY_FINISHING_FAILED,
        PLAY_FINISHING_ALERTING_FAILED,
        PLAY_FINISHING_ERROR,
        PLAY_FINISHING_ALERTING_ERROR,
        
        //RECORDING
        RECORDING_CONNECTED,
        RECORD_FINISHING_CONNECTED,
        RECORDING_ALERTING,
        RECORD_FINISHING_ALERTING,       
        RECORD_FINISHING_FAILED,
        RECORD_FINISHING_ALERTING_FAILED,              
        RECORD_FINISHING_ALERTING_ERROR,
        RECORD_FINISHING_ERROR
              
    }

    enum AutomatonEvent {
        EVENT_ALERTING,
        EVENT_PROGRESSING,
        EVENT_PROXIED,
        EVENT_SIGNAL,
        EVENT_DISCONNECTED,
        EVENT_FAILED,
        EVENT_CONNECTED,
        EVENT_REDIRECTED,
        EVENT_ERROR,
        EVENT_RECORD_FINISHED,
        EVENT_RECORD_FAILED,
        EVENT_PLAY_FINISHED,
        EVENT_PLAY_FAILED,
        CREATECALL,
        ACCEPT,
        REJECT,
        REDIRECT,
        MERGE,
        PROXY,
        DISCONNECT,
        RECORD,
        UNRECORD,
        PLAY,
        UNPLAY,
        EARLY_MEDIA_AVAILABLE
    }

    abstract class Action {
        public abstract void perform(Connection connection, Event related);

        public abstract String describe();

        public String toString() {
            return describe();
        }
    }

    class Transition {
        private final State currentState;
        private final State newState;
        private final AutomatonEvent automatonEvent;
        private Action enterAction;
        private Action exitAction;

        public Transition(State currentState, AutomatonEvent automatonEvent, State newState) {
            this.currentState = currentState;
            this.newState = newState;
            this.automatonEvent = automatonEvent;
        }

        public Transition(State currentState, AutomatonEvent automatonEvent, State newState, Action enteringState) {
            this(currentState, automatonEvent, newState);
            enterAction = enteringState;
        }

        public Transition(
                State currentState, AutomatonEvent automatonEvent, State newState, Action exitingState, Action enteringState) {
            this(currentState, automatonEvent, newState);
            exitAction = exitingState;
            enterAction = enteringState;
        }

        public void exitingState(Connection connection, Event related) {
            if (null != exitAction) exitAction.perform(connection, related);
        }

        public void enteringState(Connection connection, Event related) {
            if (null != enterAction) {
                enterAction.perform(connection, related);
            }
        }

        public String toString() {
            return "Transition{" + "currentState=" + currentState + ", newState=" + newState + ", automatonEvent="
                    + automatonEvent + ", enterAction=" + enterAction + ", exitAction=" + exitAction + '}';
        }

        public final State getNewState() {
            return newState;
        }

        public final AutomatonEvent getAutomatonEvent() {
            return automatonEvent;
        }
    }

    Call getCall();

    void sendEvent(String eventName, Event related);

    void sendEvent(String event, Event related, EventTarget context);

    void receiveEvent(AutomatonEvent automatonEvent, Event related);

    Map<AutomatonEvent, Transition> getPossibleTransitions();

    void createCall(CallProperties callProperties);

    void accept();
    
    void proxy(String server, int port);

    void reject(String rejectEventTypeName, String reason);

    void redirect();

    void merge();

    void forcedDisconnect();

    void disconnect();

    void record();

    void stopRecording();

    void play();

    void stopPlaying();

    void setCall(Call call);

    State getState();

    Connection clone();

    ScriptableObject getVoiceXMLMirror();

    ILogger getLog();

    void onEarlyMedia();

    void onEarlyMediaFailed();

    void onAlerting();

    void onProgressing(ProgressingEvent realEvent);

    void onConnected();

    void onPlayEnded();

    void onRecordEnded();

    int getCallManagerWaitTimeout();

    int getCreateCallAdditionalTimeout();

    void cleanup();

    boolean isInTerminalState();

    ExecutionContext getExecutionContext();

    /**
     * Redirects an inbound call to a new destination
     * @param destination redirection destination 
     * @param redirectCode protocol specific redirection code 
     */
    void redirect(RedirectDestination destination, RedirectStatusCode redirectCode);
}

