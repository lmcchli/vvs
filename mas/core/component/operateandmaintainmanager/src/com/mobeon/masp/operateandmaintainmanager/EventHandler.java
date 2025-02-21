package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.events.MASStarted;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.configuration.ConfigurationChanged;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class EventHandler implements  IEventReceiver {
    private OperateMAS operate;
    private ILogger log;

    EventHandler(OperateMAS operate){
        this.operate = operate;
        log = ILoggerFactory.getILogger(EventHandler.class);
    }


    /**
     * Check the type of the event. If it is of the types DemoEvent or
     * DemoAbstractCallEvent (or its subclasses) it is handled. Otherwise it
     * is discarded.
     *
     * @param event
     */
    public void doEvent(Event event) {
        /*if (event instanceof ServiceShutdownEvent) {
            //log.debug("Received event "+ event.toString() );
            ServiceShutdownEvent serviceEvent = (ServiceShutdownEvent)event;
            ServiceEnabler serviceEnabler = (ServiceEnabler)serviceEvent.getService();
            try {
                serviceEnabler.shutdownComplete();
            } catch (Exception e) {
                log.error("Unable to complete shutdown ["+e.getMessage() +"]");
            }
        } */

    }

    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            operate.reloadConfig();
        }

        // uncomment this block when EE tells that mas is started
        // and change mas_started = false in the variable init.
        if (event instanceof MASStarted) {
            log.debug("MAS started event cauth");
            operate.masStarted();
        }

    }


}
