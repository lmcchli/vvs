/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.Prioritized;

import java.net.URI;
import java.util.concurrent.Callable;

/**
 * @author Mikael Andersson
 */
public interface SimpleEvent extends Event, Prioritized, Callable {

    public String getEvent();

    public String getMessage();

    public URI getExecutingURI();

    public void setEvent(String event);

    public String getTargetType();

    public String getTargetId();

    public void defineTarget(String targetType, String targetId);


    public void setRelated(Event related);

    public Event getRelated();

    public void setExecutingURI(URI documentURI);

    public String getSendId();

}
