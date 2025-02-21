/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.common.eventnotifier.Event;


public class LifecycleEvent implements Event {
    public static final LifecycleEvent START = new LifecycleEvent();
    public static final LifecycleEvent STOP = new LifecycleEvent();
}
