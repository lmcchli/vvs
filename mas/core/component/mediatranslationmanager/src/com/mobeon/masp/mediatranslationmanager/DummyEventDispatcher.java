package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;

import java.util.ArrayList;

public class DummyEventDispatcher implements IEventDispatcher {
    public void addEventReceiver(IEventReceiver iEventReceiver) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeEventReceiver(IEventReceiver iEventReceiver) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllEventReceivers() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumReceivers() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fireEvent(Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fireGlobalEvent(Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
