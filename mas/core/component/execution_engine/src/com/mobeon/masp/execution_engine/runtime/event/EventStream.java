/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.Detour;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRule;
import com.mobeon.masp.util.Tools;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A node on a event-bus.
 * <p/>
 * An EventStream object ( which is an unfortunate name ) is a node in a
 * doubly linked list implementing an bus-like structure for sending and
 * receiving events. It deviates from a bus-structure by three key details:
 * <ol>
 * <li>Nodes have a sense of ordering, in the way that delivery will always
 * take place in a defined order</li>
 * <li>An event is not guaranteed to reach all nodes, if a previous node
 * signals that it needs to handle the event exclusively</li>
 * <li>Events is sent in a direction. This can upstream,downstream, or both.</li>
 * </ol>
 *
 * @author Mikael Andersson
 */
public class EventStream {

    private final AtomicReference<EventStream> downStream = new AtomicReference<EventStream>();
    private final AtomicReference<EventStream> upStream = new AtomicReference<EventStream>();
    private final List<Extractor> extractorFittings = Collections.synchronizedList(new ArrayList<Extractor>());
    private final List<Swap> upstreamSwaps = Collections.synchronizedList(new ArrayList<Swap>());
    private final List<Injector> injectorFittings = Collections.synchronizedList(new ArrayList<Injector>());
    private final ConcurrentLinkedQueue<Swap> lazyUpstreamSwaps = new ConcurrentLinkedQueue<Swap>();

    /**
     * Lock used for ensuring atomicity semantics of swaps.
     * For the duration this lock is held,
     * no other locks may be taken, either explicitly o implicitly.
     */
    private final Object guaranteedProgressLock = new Object();

    private static final ILogger log = ILoggerFactory.getILogger(EventStream.class);
    private final String caller;
    private final ReentrantReadWriteLock eventLock = new ReentrantReadWriteLock();

    public EventStream() {
        caller = Tools.outerCaller(0, log.isDebugEnabled());
    }

    /**
     * Recursively close all EventStreams in the
     * downstream direction.
     * <p/>
     * This includes closing their associated injectors
     * also. This is done since injectors is assumed to
     * be subscribers to some other, external event system.
     */
    public void closeEntireDownstream() {
        closeInjectors();
        EventStream s = getDownstream();
        if (s != null) s.closeEntireDownstream();
    }

    /**
     * Closes all injectors.
     */
    private void closeInjectors() {
        for (Injector injector : injectorFittings) {
            injector.close();
        }
    }

    /**
     * Returns the downstream active at the time of the call.
     * <p/>
     * Is synchronized on guaranteedProgressLock
     *
     * @return EventStream The downstream at the time of the call.
     */
    public EventStream getDownstream() {
        synchronized (guaranteedProgressLock) {
            return downStream.get();
        }
    }

    /**
     * Closes this instance, and detaches us from any existing
     * downstream.
     */
    public void close() {
        EventStream stream = getDownstream();
        if (stream != null)
            stream.unswapUpstream(this);
        closeInjectors();
    }

    /**
     * Baseclass for Injectors and Extractors acting as a
     * container for rules allowing events to pass, or not
     * pass through this Valve.
     */
    public abstract class Valve {
        private final EventRule upstreamRule;
        private final EventRule downstreamRule;

        protected Valve(EventRule upstreamRule, EventRule downstreamRule) {
            this.upstreamRule = upstreamRule;
            this.downstreamRule = downstreamRule;
        }

        public EventRule getUpstreamRule() {
            return upstreamRule;
        }

        public EventRule getDownstreamRule() {
            return downstreamRule;
        }

        public boolean ruleDownstreamValid(Event e) {
            return downstreamRule != null && downstreamRule.isValid(e);
        }

        public boolean ruleUpstreamValid(Event e) {
            return upstreamRule != null && upstreamRule.isValid(e);
        }
    }

    /**
     * Injects events received through an IEventDispatcher.
     * <p/>
     * <em>Note: It's important that the owning EventStream is closed as
     * we otherwise will leave a dangling receiver in the event dispatcher.</em>
     */
    public class EventReceiverInjector extends Injector {
        private IEventDispatcher dispatcher;
        private boolean open = true;

        private IEventReceiver receiver = new IEventReceiver() {
            public void doEvent(Event event) {
                inject(event);
            }

            public void doGlobalEvent(Event event) {
                inject(event);
            }
        };

        public EventReceiverInjector(EventRule upstreamRule, EventRule downstreamRule, IEventDispatcher dispatcher) {
            super(upstreamRule, downstreamRule);
            this.dispatcher = dispatcher;
            dispatcher.addEventReceiver(receiver);
        }

        public void close() {
            if (open) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing " + receiver + " from eventReceiver");
                }
                dispatcher.removeEventReceiver(receiver);
                if (log.isDebugEnabled())
                    log.debug("Done removing from eventReceiver. Number of injectors:" + injectorFittings.size());
                open = false;
            }
        }
    }

    /**
     * Injects any event that is valid under the rules supplied on creation.
     *
     * @logs.warn "<module> attempted to send <event> but it was rejected by <class name>" - The module <module> attempted to send the event <event> but it was rejected by <class name>
     */
    public class Injector extends Valve {

        public Injector(EventRule upstreamRule, EventRule downstreamRule) {
            super(upstreamRule, downstreamRule);
        }

        /**
         * Inject an event.
         * <p/>
         * Sends event to upstream or downstream if the respective
         * rules are valid.
         * <em>Performs local delivery if upstream rule are valid.</em>
         *
         * @param e An Event
         */
        public void inject(Event e) {
            boolean anyValid = false;
            if (ruleUpstreamValid(e)) {
                anyValid = true;
                receiveUpstreamEvent(e);
            }
            if (ruleDownstreamValid(e)) {
                anyValid = true;
                sendDownstream(e);
            }

            if (!anyValid) {
                log.warn(Tools.outerCaller(0, log.isDebugEnabled()) + " attempted to send " + e + " but it was rejected by  " + this);
            }
        }

        public void close() {
        }

        public String toString() {
            return "Injector{ upstreamRule=" + getUpstreamRule() + ", downstreamRule=" + getDownstreamRule() + " }";
        }
    }


    /**
     * Simple functor for easy extension of Extractors.
     * <p/>
     * NOTE: Should probably be removed, unecessary code.
     */
    private abstract class FunctionBooleanOfEvent {
        public abstract boolean call(Event e);

        public abstract String toString();
    }

    /**
     * Describes a specific upstreamSwap that has ocurred, or will
     * occur.
     * <p/>
     * In essence this acts as a continuation, allowing us to reorder
     * som events in time, and to backtrack to a previous state.
     */
    private class Swap {
        private EventStream swappedIn;
        private EventStream swappedOut;

        public Swap(EventStream swappedIn, EventStream swappedOut) {
            this.swappedIn = swappedIn;
            this.swappedOut = swappedOut;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Swap swap = (Swap) o;

            return swappedIn.equals(swap.swappedIn);

        }

        public int hashCode() {
            return swappedIn.hashCode();
        }
    }

    /**
     * Extracts any event valid according to the rules supplied at creation, and delivers these to an IEventReceiver
     */
    public class Extractor extends Valve {

        FunctionBooleanOfEvent func;

        public Extractor(EventRule fromUpstreamRule, EventRule fromDownstreamRule, final IEventReceiver receiver) {
            super(fromUpstreamRule, fromDownstreamRule);

            func = new FunctionBooleanOfEvent() {
                public boolean call(Event e) {
                    receiver.doEvent(e);
                    return false;
                }

                public String toString() {
                    return receiver.toString();
                }
            };
        }

        public Extractor(EventRule fromUpstreamRule, EventRule fromDownstreamRule, final Detour detour) {
            super(fromUpstreamRule, fromDownstreamRule);

            func = new FunctionBooleanOfEvent() {
                public boolean call(Event e) {
                    return detour.detourEvent(e);
                }

                public String toString() {
                    return detour.toString();
                }
            };
        }

        public boolean handleEventFromDownstream(Event e) {
            if(log.isDebugEnabled())
                log.debug("Handling event from downstream");
            if (ruleDownstreamValid(e)) {
                return func.call(e);
            }
            return false;
        }

        public boolean handleEventFromUpstream(Event e) {
            if(log.isDebugEnabled())
                log.debug("Handling event from upstream");
            if (ruleUpstreamValid(e)) {
                return func.call(e);
            }
            return false;
        }

        public EventStream getOwner() {
            return EventStream.this;

        }

        public String toString() {
            return "Extractor{ upstreamRule=" + getUpstreamRule() + ", downstreamRule=" + getDownstreamRule() + ", func=" + func
                    + " }";
        }
    }

    /**
     * Receives an event from upstream and perform local delivery.
     * <p/>
     * Takes a read-lock on eventLock.
     *
     * @param e An Event
     */
    private void receiveDownstreamEvent(Event e) {
        try {
            lockEvents(true);
            if (receiveEvent(e, true)) sendDownstream(e);
        } finally {
            unlockEvents(true);
        }
    }

    /**
     * Receive an event from downStream and perform local delivery.
     * Takes a read-lock on eventLock
     *
     * @param e An Event
     */
    private void receiveUpstreamEvent(Event e) {
        try {
            lockEvents(true);
            if (receiveEvent(e, false)) {
                sendUpstream(e);
            }
        } finally {
            unlockEvents(true);
        }
    }

    /**
     * Lock readLock as a read- or writelock
     * <p/>
     * <em>It's not allowed for methods internal to
     * an EventStream or it's delegate to lock the
     * eventLock as a writelock. It will most likely
     * cause nasty deadlocks</em>
     *
     * @param readLock True - take readlock, otherwise take writelock
     */
    public void lockEvents(boolean readLock) {
        if (readLock) {
            eventLock.readLock().lock();
        } else {
            eventLock.writeLock().lock();
        }
    }

    /**
     * Unlocks readLock by it's read- or writelock personality.
     *
     * @param readLock True - unlock readlock, False - unlock writelock
     */
    public void unlockEvents(boolean readLock) {
        if (readLock) {
            eventLock.readLock().unlock();
        } else {
            eventLock.writeLock().unlock();
        }
    }

    private boolean receiveEvent(Event e, boolean fromUpstream) {
        boolean send = true;
        int length = extractorFittings.size();

        //We must use indexed access because extractorFittings
        //might be altered during the call by additional
        //extractors beeing added. Those extractors should
        //not receive this event.
        for (int i = 0; i < length; i++) {
            Extractor fitting = extractorFittings.get(i);
            if (checkIfHandled(fitting, e, fromUpstream)) {
                send = false;
            }
        }
        return send;
    }

    private boolean checkIfHandled(Extractor fitting, Event e, boolean fromUpstream) {
        if (fromUpstream) return fitting.handleEventFromUpstream(e);
        else return fitting.handleEventFromDownstream(e);
    }

    private void sendDownstream(Event e) {
        EventStream stream = getDownstream();
        if (stream != null) {
            stream.receiveDownstreamEvent(e);
        }
    }


    private void sendUpstream(Event e) {
        EventStream stream = forcePendingSwaps();
        if (stream != null) {
            stream.receiveUpstreamEvent(e);
        }
    }

    public EventStream forcePendingSwaps() {
        EventStream stream;
        do {
            synchronized (guaranteedProgressLock) {
                Swap swap = lazyUpstreamSwaps.poll();
                if (swap != null) {
                    executeUpstreamSwap(swap);
                }
            }
        } while (lazyUpstreamSwaps.size() > 0);
        stream = getUpstream();
        return stream;
    }

    private EventStream getUpstream() {
        synchronized (guaranteedProgressLock) {
            return upStream.get();
        }
    }

    public EventStream createUpstream(boolean attach) {
        EventStream es = new EventStream();
        if (attach) {
            swapUpstream(es);
        }
        return es;
    }

    public void swapUpstream(EventStream swapIn) {
        synchronized (guaranteedProgressLock) {
            lazyUpstreamSwaps.offer(new Swap(swapIn, getUpstream()));
        }
        forcePendingSwaps();
    }

    private void executeUpstreamSwap(Swap swap) {
        EventStream swapIn = swap.swappedIn;
        upstreamSwaps.add(swap);

        EventStream stream = getUpstream();
        if (stream != null) stream.disconnectDownstream();
        swapIn.connectDownstream(this);
        upStream.set(swapIn);
    }

    private void connectDownstream(EventStream eventStream) {
        synchronized (guaranteedProgressLock) {
            downStream.set(eventStream);
        }
    }

    private void disconnectDownstream() {
        synchronized (guaranteedProgressLock) {
            downStream.set(null);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Swap) {
            Swap swap = (Swap) o;
            return swap.swappedIn == this;
        }
        return false;
    }

    public int hashCode() {
        return (upstreamSwaps != null ? upstreamSwaps.hashCode() : 0);
    }

    /**
     * @param swapOut
     * @logs.warn "<module> initiated a bogus swap on EventStream created by <caller>" : An incorrect swap in an internal data structure was attempted by <caller>
     */
    public void unswapUpstream(EventStream swapOut) {
        synchronized (guaranteedProgressLock) {
            int i = upstreamSwaps.indexOf(swapOut);
            if (i >= 0) {
                Swap swap = upstreamSwaps.get(i);
                while (i <= upstreamSwaps.size() - 1) {
                    Swap oldSwap = upstreamSwaps.remove(upstreamSwaps.size() - 1);
                    if (log.isDebugEnabled()) log.debug("Removed old swap " + oldSwap);
                }
                EventStream stream = getUpstream();
                if (stream != null) stream.disconnectDownstream();
                upStream.set(swap.swappedOut);
                if (swap.swappedOut != null)
                    swap.swappedOut.connectDownstream(this);
            } else {
                log.warn(Tools.outerCaller(0, log.isDebugEnabled()) + " initiated a bogus swap on EventStream created by " + caller);
            }
        }
    }

    public void add(Injector injector) {
        if (log.isDebugEnabled())
            log.debug(Tools.outerCaller(0, log.isDebugEnabled()) + " added injector " + injector + " to event stream created at " + caller);
        synchronized (guaranteedProgressLock) {
            injectorFittings.add(injector);
        }
    }

    /**
     * @param extractor
     * @logs.error "Tried to add an extractor associated with a different EventStream !" - Execution engine tried to add an extractor to an internal data structure but failed to do so.
     * @logs.error "Tried to add already added Extractor !" - Execution engine tried to add an extractor to an internal data structure but it was already added.
     */
    public void add(Extractor extractor) {
        if (log.isDebugEnabled())
            log.debug(Tools.outerCaller(0, log.isDebugEnabled()) + " added extractor " + extractor + " to event stream created at " + caller);
        if (extractor.getOwner() != this) {
            log.error("Tried to add an extractor associated with a different EventStream !");
        }
        if (extractorFittings.contains(extractor)) {
            log.error("Tried to add already added Extractor !");
        }
        synchronized (guaranteedProgressLock) {
            extractorFittings.add(extractor);
        }
    }
}
