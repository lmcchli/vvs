/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.Detour;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.util.Stack;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.ccxml.runtime.WaitInstance;
import com.mobeon.masp.execution_engine.ccxml.runtime.Entry;

import java.util.List;
import java.util.logging.Logger;

public class WaitSet implements Detour {

    protected Stack<WaitInstance> waitStack = new Stack<WaitInstance>();
    protected EventProcessor eventProcessor;

    public WaitSet(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
        eventProcessor.addDetour(this);
    }

    public synchronized boolean detourEvent(Event event) {
        if (waitStack.size() <= 0) return false;
        WaitInstance instance = waitStack.peek();
        if (instance != null) {
            if (!instance.shouldTerminate(event)) {
                boolean match = instance.tryMatch(event);
                if (match) {
                    return onMatchedEvent(instance, event);
                }
            } else {
                onTerminateEvent();
                return false;
            }
        }
        return false;
    }

    protected void onTerminateEvent() {
        eventProcessor.setEnabled(true);
        waitStack.pop();
    }

    /**
     *
     * @param instance
     * @param event
     * @return true if event is handled to completion
     */
    protected boolean onMatchedEvent(WaitInstance instance, Event event) {
        if(instance.isInjectOnMatch){
            instance.getExecutionContext().getEventProcessor().injectEvent(event);
        }
        if(instance.getWaitSetProduct() != null){
            instance.getWaitSetProduct().realExecute(instance.getExecutionContext(), event);
        }
        if(instance.isStopWaitingOnMatch()){
            waitStack.pop();
        }
        eventProcessor.setEnabled(true);
        return true;
    }

    public void stopWaiting(){
        waitStack.pop();
        eventProcessor.setEnabled(true);
    }

    public synchronized boolean detourGlobalEvent(Event event) {
        return false;//TODO: Tom implementation
    }

    public synchronized void waitFor(
    boolean forAll, List<Entry> entries, List<Entry> terminateEntriess, ExecutionContext ec, WaitSetProduct e, boolean stopWaitingOnMatch, boolean injectOnMatch) {
        waitStack.push(new WaitInstance(forAll, entries,terminateEntriess, ec, e, stopWaitingOnMatch, injectOnMatch));
    }
}
