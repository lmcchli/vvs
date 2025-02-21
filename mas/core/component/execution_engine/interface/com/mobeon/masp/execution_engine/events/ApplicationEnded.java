/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.execution_engine.events;

import com.mobeon.common.eventnotifier.Event;

/**
 * @author mmawi
 */
public class ApplicationEnded implements Event {
    private String sessionId;

    public ApplicationEnded(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
