/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.VoiceXMLEventProcessor;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.ReturnEvent;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Queue;

public class SendReturn_T extends VXMLOperationBase {

    private static final ILogger log = ILoggerFactory.getILogger(SendReturn_T.class);

    private class ReturnValue extends ScriptableObject {
        public String getClassName() {
            return "ReturnValue";
        }
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        if (ex.getParent() == null) {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    "Tried to return from subdialog but there was no parent context", DebugInfo.getInstance());
            return;
        }
        Value v = ex.getValueStack().pop();
        Scope scope = ex.getCurrentScope();
        Scriptable theResult = null;
        Object returnValue = v.getValue();

        if (returnValue != null) {
            if (returnValue instanceof Pair) {
                //We are returning an event.
                Pair pair = (Pair) returnValue;
                SimpleEvent event = new SimpleEventImpl(pair.getName(), pair.getValue().toString(), DebugInfo.getInstance());

                //Stop and shutdown ( event returning case );
                stopAndShutdown(ex, new ReturnEvent(event));
                return;  //NOTE: Premature return

            } else {

                // We are returning a js value. Fill-in the value of all
                // namelist fields according to the current scope.

                //TODO: Don't use evaluate here, use get() with the proper
                //TODO: scoping rules

                String[] names = (String[]) returnValue;
                ReturnValue value = new ReturnValue();
                for (String name : names) {
                    Object o = scope.evaluate(name);
                    value.put(name, value, o);
                }
                theResult = value;
            }
        } else {
            theResult = new ReturnValue();

        }

        //Stop and shutdown ( variable returning case );
        stopAndShutdown(ex, new ReturnEvent(theResult));
    }

    private void stopAndShutdown(VXMLExecutionContext ex, ReturnEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Stop and shutdown");
        }
        VXMLExecutionContext contextParentOfAll = ex.getContextParentOfAll();
        EventStream parentOfAllEventStream = contextParentOfAll.getEventStream();

        try {
            parentOfAllEventStream.lockEvents(false);
            ex.shutdown(false);
            copyEventsNeverHandledInThisSubdialog(ex);
            contextParentOfAll.getEventProcessor().setEnabled(true);
            ex.getParent().getEventProcessor().doEvent(event);
        } finally {
            parentOfAllEventStream.unlockEvents(false);
        }
    }

    private void copyEventsNeverHandledInThisSubdialog(VXMLExecutionContext ex) {
        VoiceXMLEventProcessor toEventProcessor = ex.getParent().getEventProcessor();
        int num = toEventProcessor.getQueue().copyEvents(ex.getEventProcessor().getQueue());

        ex.getEventProcessor().getQueue().clear();
        Queue<Event> externalEventQueue = ex.getEventProcessor().getExternalEventQueue();
        Queue<Event> toExternalEventQueue = toEventProcessor.getExternalEventQueue();
        toExternalEventQueue.addAll(externalEventQueue);
        externalEventQueue.clear();

        if (log.isDebugEnabled()) {
            log.debug("Copying " + num + " events and " + toExternalEventQueue.size() + " external events that were never handled in this subdialog, back to invoking context");
        }
    }

    public String arguments() {
        return "";
    }

}

