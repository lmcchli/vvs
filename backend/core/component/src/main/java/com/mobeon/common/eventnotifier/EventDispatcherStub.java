package com.mobeon.common.eventnotifier;

import java.util.ArrayList;

/**
 * Stub class that implements IEventDispatcher.
 *
 * @author emahagl
 */
public class EventDispatcherStub implements IEventDispatcher {
    private ArrayList<IEventReceiver> eventReceivers = new ArrayList<IEventReceiver>();

    public void addEventReceiver(IEventReceiver rec) {
        eventReceivers.add(rec);
    }

    public void removeEventReceiver(IEventReceiver rec) {
        eventReceivers.remove(rec);
    }

    public void removeAllEventReceivers() {
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return eventReceivers;
    }

    public int getNumReceivers() {
        return eventReceivers.size();
    }

    public void fireEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doEvent(e);
        }
    }

    public void fireGlobalEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doGlobalEvent(e);
        }
    }
}
