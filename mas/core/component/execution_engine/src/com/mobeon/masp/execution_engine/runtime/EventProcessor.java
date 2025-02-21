/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.EventHandler;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

import java.util.Queue;

/**
 * Preprocesses events designated for the associated {@link ExecutionContext},
 * and ensures the contexts engine is given an opportunity to process the event.
 * <p/>
 * The preprocessing mainly consist of locating an appropriate {@link EventHandler}
 * through the contexts {@link HandlerLocator} or
 * <em>any other appropriate means</em>. Some filtering may also be done, but this should
 * preferably be done through attaching an {@link com.mobeon.masp.execution_engine.runtime.event.deprecated.EventFilter} to the {@link EventHub}
 * servicing the EventProcessor.
 * <p/>
 * <b>Usage notes:</b><br>
 * The method {@linkplain #setExecutionContext} must always be called before
 * the first event is received.
 * <p/>
 * After creation, the processor is disabled, and calling {@linkplain #poll}
 * will always return <code>null</code> until {@linkplain #setEnabled} is
 * called.
 *
 * @author Mikael Anderssson
 */
public interface EventProcessor extends IEventReceiver {
    /**
     * Event entry containing an event and it's selected {@link EventHandler}
     */
    class Entry {
        private Event event;
        private EventHandler handler;

        public Entry(Event event, EventHandler handler) {
            this.event = event;
            this.handler = handler;
        }

        public Event getEvent() {
            return event;
        }

        public EventHandler getHandler() {
            return handler;
        }


        public String toString() {
            return "Entry{" + "event=" + event + ", handler=" + handler + '}';
        }
    }

    /**
     * Returns the enabled status of the processor.
     * <p/>
     * Initially this method returns <code>false</code>.
     *
     * @return <code>true</code> is the processor is enabled,
     *         <code>false</code> otherwise.
     */
    boolean isEnabled();

    /**
     * Enables or disables event processing.
     *
     * @param enabled The desired enabled status
     */
    void setEnabled(boolean enabled);

    /**
     * Polls for prepared event entries.
     * Will return <code>null</code> if no prepared entries exists, or
     * if the processor is <em>disabled</em>.
     *
     * @return A prepared entry, or <code>null</code>.
     */
    Entry poll();


    /**
     * Returns true if events are enabled and there are at least one queued event,
     * otherwise false
     * @return true if there are events in the queue, false otherwise
     */
    boolean hasEventsInQ();

    /**
     * Returns whether or not the EventProcessor has been stopped.
     * A stopped EventProcessor can not be restarted.
     *
     * @return <code>true</code> is {@link #stop} has been called.
     */
    boolean isStopped();

    /**
     * Stops the processor - and thus the engine - as <em>soon as possible</em>.
     * <p/>
     * This is what you want to use if you'd like to terminate a running engine
     * with extreme prejudice. It will not do any cleanup whatsoever.
     */
    void stop();

    /**
     * Sets the {@link ExecutionContext} currently owning this processor.
     * @param executionContext
     */
    void setExecutionContext(ExecutionContext executionContext);

    /**
     * Adds a detour to this processor.
     * <p>
     * The detour is run prior to accepting the event into the queue.
     * If it returns false, the element will be discarded.
     * A detour always runs, regardless of whether or not the processor
     * i enabled. It is used to handle platform level events.
     * @param detour
     */
    void addDetour(Detour detour);

    void removeDetour(Detour detour);

    void runAsNeeded();

    void injectEvent(Event event);

    ExecutionContext getExecutionContext();

    void dumpPendingEvents();

    EventQueue<SimpleEvent> getQueue();

    Queue<Event>getExternalEventQueue();

    public void processExternalEvents();

    void processEvent(Event event);

}
