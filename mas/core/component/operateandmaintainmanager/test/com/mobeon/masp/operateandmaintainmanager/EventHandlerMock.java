package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.eventnotifier.Event;
//import com.mobeon.masp.execution_engine.eventnotifier.IEventReceiver;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.eventnotifier.IEventReceiver;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */


public class EventHandlerMock implements  IEventReceiver {


    public boolean globalEvent = false;
    public boolean nonGlobalEvent = false;
    public boolean serviceShutdownEvent = false;
    public boolean configurationChanged = false;


    /**
     * Check the type of the event. If it is of the types DemoEvent or
     * DemoAbstractCallEvent (or its subclasses) it is handled. Otherwise it
     * is discarded.
     *
     * @param event
     */

    public void doEvent(Event event) {
        nonGlobalEvent=true;

        if (event instanceof ServiceShutdownEvent) {
            serviceShutdownEvent = true;

        }

    }


    public void doGlobalEvent(Event event) {
        globalEvent=true;

        if (event instanceof ConfigurationChanged) {
            configurationChanged = true;
        }

    }


}
