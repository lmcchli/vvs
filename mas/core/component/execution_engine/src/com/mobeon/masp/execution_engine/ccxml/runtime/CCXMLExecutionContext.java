/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

import java.util.concurrent.Callable;

/**
 * @author Mikael Andersson
 */
public interface CCXMLExecutionContext extends ExecutionContext {

    public DialogStartEvent fetchDialogEvent(String s);

    public void bindStateTo(String varname);

    public String getStateVarName();

    public String getEventVarName();

    public void setEventVarName(String name);

    public void setCallManager(CallManager callManager);

    public EventSourceManager getEventSourceManager();

    public CCXMLEvent getEventVar();

    void shutdown(boolean recursive);

    public void setEventDispatcher(IEventDispatcher eventDispatcher);

    IEventDispatcher getEventDispatcher();    

    void waitForEvent(String eventToFire, String messageForFiredEvent, int waitTime, Callable toInvokeWhenDelivered, Connection connection, String ... eventNames);

    CallManager getCallManager();
}