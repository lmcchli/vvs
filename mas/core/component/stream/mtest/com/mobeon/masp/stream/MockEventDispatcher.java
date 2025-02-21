/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;

public class MockEventDispatcher implements IEventDispatcher {
    private static ILogger logger = ILoggerFactory.getILogger(MockEventDispatcher.class);
    private boolean eventFlag = false;
    private Event receivedEvent = null;
    ArrayList<IEventReceiver> receivers;

    public MockEventDispatcher() {
        receivers = new ArrayList<IEventReceiver>();
    }

    public void addEventReceiver(IEventReceiver iEventReceiver) {
        receivers.add(iEventReceiver);
    }

    public void removeEventReceiver(IEventReceiver iEventReceiver) {
    }

    public void removeAllEventReceivers() {
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return receivers;
    }

    public int getNumReceivers() {
        return receivers.size();
    }

    public void fireEvent(Event event) {
        if (logger.isDebugEnabled()) logger.debug("Got event: " + event);
        receivedEvent = event;
        eventFlag = true;
        
        for (IEventReceiver receiver : receivers) {
            receiver.doEvent(event);
        }
    }

    public void fireGlobalEvent(Event event) {
    }

    public boolean isEventFlag() {
        return eventFlag;
    }

    public void clearEventFlag() {
        this.eventFlag = false;
    }

    public Event getEvent() {
        return receivedEvent;
    }
}
