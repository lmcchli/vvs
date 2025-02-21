/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public interface EventHub{
    public void fireEvent(String name, String message, DebugInfo debugInfo);

    public void fireEvent(Event event);

    public void fireEvent(String name, DebugInfo instance);

    public void set(EventStream.Injector injector);

    public void close();

    public void setOwner(ExecutionContext executionContext);

    public void fireContextEvent(String event, DebugInfo debugInfo);

    public void fireContextEvent(String event, String message, DebugInfo debugInfo);

    public void fireContextEventWithLocationInfo(String event, String message, DebugInfo debugInfo);    

    public void fireEvent(SimpleEvent ev, int delay);

    public void fireContextEvent(final SimpleEvent ev, int delay);    

    public void cancel(String sendid, Object related);

    public void fireContextEvent(SimpleEvent event);
}

