package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-feb-18
 * Time: 17:51:06
 * To change this template use File | Settings | File Templates.
 */

public class EventHubWithOrderCheck implements EventHub {
    private List<String> expectedEventNames;

    public List<String> getFiredEventNames() {
        return firedEventNames;
    }

    private List<String> firedEventNames = new ArrayList<String>();


    public EventHubWithOrderCheck(){
    }

    public void fireEvent(String name, String message, DebugInfo debugInfo) {
        firedEventNames.add(name);
    }

    public void fireEvent(Event event) {
    }

    public void fireEvent(String name, DebugInfo instance) {
        firedEventNames.add(name);
    }

    public void set(EventStream.Injector injector) {
    }

    public void close() {
    }

    public void setOwner(ExecutionContext executionContext) {
    }

    public void fireContextEvent(String event, DebugInfo debugInfo) {
        firedEventNames.add(event);
    }

    public void fireContextEvent(String event, String message, DebugInfo debugInfo) {
        firedEventNames.add(event);
    }

    public void fireContextEventWithLocationInfo(String event, String message, DebugInfo debugInfo) {
        firedEventNames.add(event);
    }

    public void fireEvent(SimpleEvent ev, int delay) {
        firedEventNames.add(ev.getEvent());
    }

    public void fireContextEvent(final SimpleEvent ev, int delay) {
    }

    public void cancel(String sendid, Object related) {
    }

    @MockAction(DELEGATE)
    public void fireContextEvent(SimpleEvent event) {
        firedEventNames.add(event.getEvent());
    }
}
