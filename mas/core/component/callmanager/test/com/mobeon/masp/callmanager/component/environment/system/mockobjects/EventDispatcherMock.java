/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mock of IEventDispatcher used for testing.
 * This class is immutable.
 */
public class EventDispatcherMock implements IEventDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private final Collection<IEventReceiver> eventReceivers =
            new ConcurrentLinkedQueue<IEventReceiver>();
    private AtomicReference<Call> activeCall = new AtomicReference<Call>();
    private final Object callLock = new Object();

    public void clearCall() {
        synchronized(callLock) {
            activeCall = null;
        }
    }

    public void addEventReceiver(IEventReceiver rec) {
        eventReceivers.add(rec);
    }

    public void removeEventReceiver(IEventReceiver rec) {
        eventReceivers.remove(rec);
    }

    public void removeAllEventReceivers() {
        eventReceivers.clear();
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return new ArrayList<IEventReceiver>(eventReceivers);
    }

    public int getNumReceivers() {
        return eventReceivers.size();
    }

    public void fireEvent(Event e) {

        if (e instanceof AlertingEvent) {
            synchronized(callLock) {
                activeCall.set(((AlertingEvent)e).getCall());
                callLock.notify();
            }
        }

        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doEvent(e);
        }
    }

    public void fireGlobalEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doGlobalEvent(e);
        }
    }

    public Call waitForInboundCall(long timeoutInMilliSeconds) {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;

        synchronized(callLock) {
            while (this.activeCall.get() == null &&
                    (currentTime < startTime + timeoutInMilliSeconds)) {
                try {
                    callLock.wait(timeoutInMilliSeconds);
                    currentTime = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    log.debug("Interrupted while waiting for inbound call.");
                    break;
                }
            }
        }
        return this.activeCall.get();
    }

    public Call getActiveCall() {
        return activeCall.get();
    }

}
