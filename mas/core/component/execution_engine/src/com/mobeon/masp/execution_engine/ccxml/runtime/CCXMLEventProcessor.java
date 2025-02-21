/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.ccxml.compiler.Exit;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.CallManagerEventRule;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.StreamPlayEventRule;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.StreamRecordEventRule;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Operation;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.EventProcessorBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.PrioritizingQueue;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.LifecycleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.EndSessionEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import static com.mobeon.masp.execution_engine.runtime.event.rule.EventRules.*;
import com.mobeon.masp.execution_engine.runtime.event.rule.TargetRule;
import static com.mobeon.masp.util.Tools.startsWithOrEquals;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.DialogEventRule;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.PlayRule;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class CCXMLEventProcessor extends EventProcessorBase {

    private static final ILogger log = ILoggerFactory.getILogger(CCXMLEventProcessor.class);
    private EventStream.Extractor extractor;
    private String sessionId;
    private IEventReceiver eventSourceManager;

    public CCXMLEventProcessor() {
        eventQueue = new PrioritizingQueue<SimpleEvent>();
    }

    public void processEvent(Event event) {
        getExecutionContext().getSession().registerSessionInLogger();
        if (log.isDebugEnabled()) log.debug("Received " + event);
        if (! (event instanceof SimpleEvent)) {
            eventSourceManager.doEvent(event);
        } else {
            CCXMLEvent ccxmlEvent = (CCXMLEvent) event;
            if (startsWithOrEquals(ccxmlEvent.getEvent(), Constants.Event.CCXML_KILL_UNCONDITIONAL)) {
                exit();
                return;
            }
        }
        super.processEvent(event);
    }

    private void exit() {
        Operation[] exitOps = Exit.createExitOps(DebugInfo.getInstance());
        getExecutionContext().getEngine().executeAtomic(exitOps);
    }

    public void noHandler(SimpleEvent realEvent) {
        /*  The CCXML application executes an <exit>.
        * Handled here --> An unhandled "error.*" event.
        * Handled here --> An unhandled "ccxml.kill" event.
        * A "ccxml.kill.unconditional" event.
        */
        if (log.isInfoEnabled()) log.info("No handler found for event " + realEvent.getEvent());
        if (startsWithOrEquals(realEvent.getEvent(), Constants.VoiceXML.ERROR) || startsWithOrEquals(
                realEvent.getEvent(), Constants.Event.CCXML_KILL)) {
            exit();
        }
    }


    /**
     * If necessary, create an {@link EventStream.Extractor}
     * with the following rules:
     * <p/>
     * <ol>
     * <li>All events targeted for this {@linkplain ExecutionContext context}</li>
     * <li>Any event that is a DialogEvent which is not targeting a dialog</li>
     * </ol>
     *
     * @logs.error "SetExecutionContext called twice" - This is an internal error/bug
     */
    protected void onSetExecutionContext() {
        if (extractor == null) {
            sessionId = getExecutionContext().getSessionId();
            eventSourceManager = getExecutionContext().getEventSourceManager();
            EventStream stream = getExecutionContext().getEventStream();
            extractor = stream.new Extractor(
                    // Allow dialog events from subcontexts except if they
                    // are targeting a dialog (which we aren't)
                    and(
                            new DialogEventRule(),
                            not(new TargetRule(null, "dialog"))
                    ),
                    or(
                            // Allow dialog events from our-self and supercontexts
                            // except if they are targeting a dialog
                            // (which we aren't)
                            and(
                                    new DialogEventRule(),
                                    not(new TargetRule(null, "dialog"))
                            ),
                            // Accept all events targeting this context, except
                            // play-events (since we don't care about them)
                            and(new TargetRule(getExecutionContext().getContextId(), "context"),
                                    not(new PlayRule())),
                            // Accept all events targeting this ccxml session
                            new TargetRule(getExecutionContext().getContextId(), "ccxml"),
                            // Accept events from callmanager
                            new CallManagerEventRule(),
                            // Accept record events from callmanagar
                            new StreamRecordEventRule(),
                            // Accept pley events from callmanagar
                            new StreamPlayEventRule(),
                            // Accept event which started this processor
                            new ClassRule(LifecycleEvent.class),
                            new ClassRule(EndSessionEvent.class)

                    ), this);
            stream.add(extractor);
        } else {
            if (log.isDebugEnabled()) log.error("SetExecutionContext called twice !");
        }
    }

    public CCXMLExecutionContext getExecutionContext() {
        return (CCXMLExecutionContext) super.getExecutionContext();
    }
}
