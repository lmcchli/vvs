package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

/**
 * @author Mikael Andersson
 */

public class EventHubDummy implements EventHub {

    private DefaultExpectTarget expectTarget;
    private RuntimeData data;

    public EventHubDummy(DefaultExpectTarget expectTarget,RuntimeData data) {
        this.expectTarget = expectTarget;
        this.data = data;
    }


    public void fireEvent(String name, String message, DebugInfo debugInfo) {
    }

    public void fireEvent(Event event) {
    }

    public void fireEvent(String name, DebugInfo instance) {
    }

    public void set(EventStream.Injector injector) {
    }

    public void fireEvent(SimpleEvent ev, int delay) {
    }

    public void fireContextEvent(final SimpleEvent ev, int delay) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() {
    }

    public void setOwner(ExecutionContext executionContext) {
    }

    @MockAction(DELEGATE)
    public void fireContextEvent(String event, DebugInfo debugInfo) {
        expectTarget.EventHub_fireContextEvent(event,debugInfo);
    }

    public void fireContextEvent(String event, String message, DebugInfo debugInfo) {
    }

    public void fireContextEventWithLocationInfo(String event, String message, DebugInfo debugInfo) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancel(String sendid, Object related) {
    }

    public void fireContextEvent(SimpleEvent event) {
    }
}
