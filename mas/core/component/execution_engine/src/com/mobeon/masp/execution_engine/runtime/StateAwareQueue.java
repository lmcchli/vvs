/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;
import static com.mobeon.masp.execution_engine.compiler.Constants.VoiceXMLState.*;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.*;
import static com.mobeon.masp.execution_engine.runtime.event.rule.EventRules.*;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.ExecutionRelatedRule;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.PlayRule;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.RecordRule;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An event queue that is aware of the state of the execution context, and handles
 * events according to the current context state.
 *
 * @author David Looberger
 */
public class StateAwareQueue<T extends SimpleEvent> implements EventQueue<T> {
    private VXMLExecutionContext context = null;
    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();
    private static final ILogger log = ILoggerFactory.getILogger(StateAwareQueue.class);
    private final Object LOCK = new Object();
    private EventRule transitioningEventRule;
    private EventRule waitingEventRule;
    private EventRule finalProcessingEventRule;

    private class ConnectionEventWhileDisconnectedRule extends EventRuleBase {
        public boolean isValid(Event e) {
            if (e instanceof SimpleEvent) {
                return logIfValid(
                        ((SimpleEvent) e).getEvent().startsWith("connection.")
                                && context.initiatedDisconnect(), e);
            }
            return false;
        }

        public String toString() {
            return "isConnectionEventWhileDisconnecting()";
        }
    }

    private class FieldEventRule extends EventCategoryRuleBase {

        public Category categoryOf(Event e) {
            if (e instanceof SimpleEvent) {
                SimpleEvent se = (SimpleEvent) e;
                if (se.getTargetType() != null && se.getTargetType().startsWith(Constants.VoiceXML.FIELD + ":")) {
                    final FIAState f = context.getFIAState();
                    if (context.getVoiceXMLState() == Constants.VoiceXMLState.STATE_WAITING) {
                        if (f.getFieldId().equals(se.getTargetId())) {
                            return logIfNotFalse(Category.TRUE, e);
                        } else {
                            return logIfNotFalse(Category.INVALID, e);
                        }
                    } else {
                        return logIfNotFalse(Category.FALSE, e);
                    }
                }
            }
            return logIfNotFalse(Category.FALSE, e);
        }

        public String toString() {
            return "isRelatedToCurrentField()";
        }
    }

    private class AsyncEventRule extends EventCategoryRuleBase {
        FieldEventRule fieldEventRule = new FieldEventRule();
        public Category categoryOf(Event e) {
            if (e instanceof SimpleEvent) {
                SimpleEvent se = (SimpleEvent) e;

                // context events are thrown using <throw>, e.g. <throw event="nomatch">
                // and the AsyncEventRule does not consider those

                String targetType = se.getTargetType();
                if (targetType != null &&
                        !targetType.equals(Constants.Event.CONTEXT) &&
                        se.getEvent().startsWith("no")) {
                    if (se.getEvent().equals(Constants.VoiceXML.NOINPUT)) {
                        if (context.getFIAState().deliverNoInputResponse(context) && (fieldEventRule.categoryOf(se) == Category.TRUE)) {
                            return logIfNotFalse(Category.TRUE, e);
                        } else {
                            if(fieldEventRule.categoryOf(se) != Category.TRUE) {
                               log.info("fieldEventRule.categoryOf is not true for " + Constants.VoiceXML.NOINPUT);
                            }
                            return logIfNotFalse(Category.INVALID, e);
                        }
                    } else if (se.getEvent().equals(Constants.VoiceXML.NOMATCH)) {
                        if (context.getFIAState().deliverNoMatchResponse(context)) {
                            return logIfNotFalse(Category.TRUE, e);
                        } else {
                            return logIfNotFalse(Category.INVALID, e);
                        }

                    }
                }
            }
            return Category.FALSE;
        }
    }

    private class IgnoreInFinalProcessing extends EventCategoryRuleBase {
        private EventRule rule;

        public IgnoreInFinalProcessing(EventRule rule) {
            this.rule = rule;
        }

        public Category categoryOf(Event e) {
            Category category = rule.categoryOf(e);
            return logIfNotFalse(category != Category.FALSE && context.getFinalProcessingState()?Category.INVALID:category,e);
        }
    }
    
    private class DisconnectReceivedRule extends EventCategoryRuleBase {
        public Category categoryOf(Event e) {        
            if (e instanceof SimpleEvent) {
                SimpleEvent se = (SimpleEvent) e;

                // context events are thrown using <throw>, e.g. <throw event="nomatch">
                // and the AsyncEventRule does not consider those

                String targetType = se.getTargetType();
                if (targetType != null &&
                        targetType.equals(Constants.VoiceXML.DIALOG) && se.getEvent().equals("connection.disconnect.hangup") &&  (context.getVoiceXMLState() == Constants.VoiceXMLState.STATE_TRANSITIONING) && ((context.getExecutionResult() == ExecutionResult.EVENT_WAIT) || context.isDisconnectReady())) {
                    log.debug("DisconnectReceivedRule returing true "+",context state="+context.getState()+",context execResult="+context.getExecutionResult()+",voicexml state="+context.getVoiceXMLState());
                    return logIfNotFalse(Category.TRUE, e);
                } 
            }       
        
            return Category.FALSE;
        }
    }  

    /**
     * @param p
     * @return
     * @logs.error "Failed to add event to queue. Event will be lost!" - Execution engine failed to add an event to a queue.
     */
    public boolean offer(T p) {
        synchronized (LOCK) {
            boolean ret;
            if (p != null)
                ret = queue.offer(p);
            else
                ret = false;
            if (!ret) {
                log.error("Failed to add event to queue. Event will be lost!");
            } else {
                // log.debug("Event put on queue. Queue size is " + queue.size());
            }
            return ret;
        }
    }

    public T poll() {
        synchronized (LOCK) {
            T ret = null;
            if (context == null || context.getVoiceXMLState() == STATE_WAITING) {
                // Return the first entry in the queue, if the queue is non-empty,or null otherwise
                ret = getValidEntry(waitingEventRule);

                // If we are in final processing state and we got there due to a hangup event
                // sent to VoiceXML, we ignore most events (this makes
                // ApplicationVXMLDisconnectTagTest.testCCXMLDisconnectTag1 work)
            } else if(context.getFinalProcessingState() && !context.initiatedDisconnect()) {
                ret = getValidEntry(finalProcessingEventRule);
            } else {
                ret = getValidEntry(transitioningEventRule);
            }
            return ret;
        }
    }

    /**
     * @param rule
     * @return
     * @logs.error ""Failed to take/remove the event <entry> from the queue. Trying next." - Execution engine failed to remove the event <entry> from a queue.
     */
    private T getValidEntry(EventRule rule) {
        T ret = null;

        for (T entry : queue) {
            EventRule.Category c;
            if (rule != null && (c = rule.categoryOf(entry)) != EventRule.Category.FALSE) {
                if (c == EventRule.Category.INVALID) {
                    if (log.isDebugEnabled()) log.debug("Discarding event " + entry.getEvent());
                    queue.remove(entry);
                    continue;
                }
                if (queue.remove(entry)) {
                    ret = entry;
                    break;
                } else {
                    log.error("Failed to take/remove the event " + entry + " from the queue. Trying next");
                }
            } else {
                if (log.isDebugEnabled()) log.debug("Deferring event " + entry.getEvent());
            }
        }

        if (!queue.isEmpty() && ret == null)
            if (log.isDebugEnabled())
                log.debug("Returning null with non-empty queue " + queue.size());

        return ret;
    }

    public int size() {
        synchronized (LOCK) {
            return queue.size();
        }
    }

    public void setContext(VXMLExecutionContext ctx) {
        synchronized (LOCK) {
            context = ctx;
            transitioningEventRule = or(
                    new AsyncEventRule(),
                    new FieldEventRule(),
                    new PlayRule(),
                    new IgnoreInFinalProcessing(new RecordRule()),
                    new ExecutionRelatedRule(),
                    new ConnectionEventWhileDisconnectedRule(),
                    new TargetRule(ctx.getContextId(), "context"),
                    new DisconnectReceivedRule());

        }
        //Order matters ! Rules that may return INVALID should be placed first due to shortcut evaluation !
        waitingEventRule = or(
                new AsyncEventRule(),
                new FieldEventRule(),
                EventRules.TRUE_RULE
        );

        finalProcessingEventRule =  or(
                    new ExecutionRelatedRule(),
                    new ConnectionEventWhileDisconnectedRule(),
                    new TargetRule(ctx.getContextId(), "context"));
    }

    public Object[] getEvents() {
        return queue.toArray();
    }

    public int copyEvents(EventQueue<T> other) {
        synchronized (LOCK) {
            Object[] otherEvents = other.getEvents();
            for (Object o : otherEvents) {
                queue.offer((T) o);
            }
            return otherEvents.length;
        }
    }

    public void clear() {
        synchronized (LOCK) {
            queue.clear();
        }
    }
}
