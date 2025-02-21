/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.common.eventnotifier.Event;

public abstract class ExternalEventAdapter {

    public abstract void perform(EventSourceManager manager, Event related);
}
