/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.Event;

public interface Detour  {
    public boolean detourEvent(Event event);
    public boolean detourGlobalEvent(Event event);
}
