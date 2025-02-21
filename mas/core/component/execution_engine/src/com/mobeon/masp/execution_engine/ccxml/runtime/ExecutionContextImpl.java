/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.EventSourceManagerImpl;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.events.ApplicationEnded;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.runtime.Data;
import com.mobeon.masp.execution_engine.runtime.ExecutionContextBase;
import com.mobeon.masp.execution_engine.runtime.Statics;
import com.mobeon.masp.execution_engine.runtime.ValueStackImpl;
import com.mobeon.masp.execution_engine.runtime.event.CCXMLSelector;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistryImpl;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Mikael Andersson
 */
public class ExecutionContextImpl extends ExecutionContextBase implements CCXMLExecutionContext {

    private static final ILogger log = ILoggerFactory.getILogger(ExecutionContextBase.class);
    private Map<String, DialogStartEvent> preparedDialogs = new HashMap<String, DialogStartEvent>();
    private String stateVar = Constants.CCXML_STATEVAR;
    private String eventVar;
    private CCXMLData frameData;
    private EventSourceManager eventSourceManager = new EventSourceManagerImpl();

    // Supervise that we eventually get events from CallManager
    WaitSetNonBlocking ws;

    public ExecutionContextImpl(ISession session, PlatformAccessFactory platformAccessFactory, IServiceRequestManager serviceRequestManager, IConfigurationManager configurationManager) {
        super(
                session,
                new ValueStackImpl(),
                new ScopeRegistryImpl(null),
                new CCXMLEventHub(),
                new HandlerLocator(CCXMLSelector.instance()),
                new CCXMLEventProcessor(), new Statics(),
                new EventStream(),
                platformAccessFactory, "CCXMLContext", serviceRequestManager, configurationManager);
        getScopeRegistry().setExecutionContext(this);
        getEventProcessor().setExecutionContext(this);
        eventSourceManager.setOwner(this);
        eventSourceManager.setSession(session);

        getCurrentScope().declareReadOnlyVariable("id", getContextId());
        ws = new WaitSetNonBlocking(getEventProcessor(), this, configurationManager);
    }


    public void shutdown(boolean recursive) {

        // May be invoked several times if e.g. several dialogs crash simultaneously

        if (isShutdown.compareAndSet(false, true)) {
            try {
                getEventHub().close();

                getEventProcessor().stop();
                getEngine().stopExecuting();

                if (recursive) {
                    eventSourceManager.shutdownAll();
                    getSession().dispose();
                }
                eventSourceManager.getEventDispatcher().fireEvent(new ApplicationEnded(getSessionId()));
            } finally {
                // It is important to call this to de-register from eventDispatcher, hence the finally
                if (recursive) getEventStream().closeEntireDownstream();
                else getEventStream().close(); //Close eventStream
            }
        }
    }

    public void postProcess() {
        super.postProcess();
        //TODO: Hide eventVar between transitions

        switch (getExecutionResult()) {
            case EVENT_WAIT:
                getEngine().pauseExecuting();
                return;
            default:
                break;
        }
    }

    public DialogStartEvent fetchDialogEvent(String s) {

        return preparedDialogs.get(s);
    }

    public void updateState(String state) {
        getCurrentScope().setValue(stateVar, state);
    }

    public void bindStateTo(String varname) {

        stateVar = varname;
    }

    public String getState() {
        Object state = getCurrentScope().getValue(stateVar);
        if (state != null)
            return state.toString();
        else
            return "unknown";
    }


    public EventSourceManager getEventSourceManager() {
        return eventSourceManager;
    }


    public String getStateVarName() {
        return stateVar;
    }

    public String getEventVarName() {
        return eventVar;
    }

    public CCXMLEvent getEventVar() {
        Object value = getCurrentScope().getValue(getEventVarName());
        if (value instanceof CCXMLEvent)
            return (CCXMLEvent) value;
        else
            return null;
    }

    public void setEventVarName(String name) {
        eventVar = name;
        if (eventEntry != null) {
            getCurrentScope().setValue(name, eventEntry.getEvent());
        }
    }

    public void setCallManager(CallManager callManager) {
        eventSourceManager.setCallManager(callManager);
    }

    public void setEventDispatcher(IEventDispatcher dispatcher) {
        eventSourceManager.setEventDispatcher(dispatcher);
    }

    public IEventDispatcher getEventDispatcher() {
        return eventSourceManager.getEventDispatcher();
    }

    public void waitForEvent(String eventToFire, String messageForFiredEvent, int waitTime, Callable toInvokeWhenDelivered, Connection connection, String ... eventNames) {
        ws.addWaitFor(eventToFire, messageForFiredEvent, waitTime, toInvokeWhenDelivered, connection, eventNames);
    }

    public CallManager getCallManager() {
        return eventSourceManager.getCallManager();
    }

    public void setConnection(Connection connection) {

    }

    public void setFrameData(Data extraData) {
        this.frameData = (CCXMLData) extraData;
    }

    public CCXMLData getFrameData() {
        return frameData;
    }

    public Connection getCurrentConnection() {
        // Find the connection valid for the executing transition (if any)
        CCXMLEvent event = getEventVar();
        if (event == null) {
            return null;
        }
        return event.getConnection();
    }

    public synchronized void shutdownEverything() {
        shutdown(true);
    }


    public void dumpState() {
        log.warn("Dumping active CCXML context for " + getExecutingModule().getDocumentURI().toString());
        log.warn("Current CCXML state: " + getState());
    }
}

