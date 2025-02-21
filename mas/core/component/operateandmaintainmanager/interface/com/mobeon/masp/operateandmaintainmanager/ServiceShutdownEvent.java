/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.eventnotifier.Event;

/**
 * This class is used to create a Service Shutdown Event. Each Shutdown event is identified by its instance.
 * Contains a service instanse that should be sent in the event.
 *
 */
public class ServiceShutdownEvent implements Event {

    private final ServiceEnablerInfo service;

    /**
     * Constructor
     */
    ServiceShutdownEvent(ServiceEnablerInfo service){
        this.service = service;
    }

    /**
     * Retrieves the service instanse that invoked the event
     *
     * @return Service
     */
    public ServiceEnablerInfo getService(){
        return service;
    }

    public String toString(){
        return "ServiceShutdownEvent";
    }

}
