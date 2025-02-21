/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;

import java.util.ArrayList;

public class MockEventDispatcher implements IEventDispatcher {
    private static ILogger logger = ILoggerFactory.getILogger(MockEventDispatcher.class);
    private boolean eventFlag = false;
    private Event receivedEvent = null;

    public void addEventReceiver(IEventReceiver iEventReceiver) {
    }

    public void removeEventReceiver(IEventReceiver iEventReceiver) {
    }

    public void removeAllEventReceivers() {
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return null;
    }

    public int getNumReceivers() {
        return 0;
    }

    public void fireEvent(Event event) {
        logger.debug("Got event: " + event);
        receivedEvent = event;
        eventFlag = true;
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
