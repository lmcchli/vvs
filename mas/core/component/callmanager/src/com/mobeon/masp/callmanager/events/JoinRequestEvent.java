/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.common.eventnotifier.IEventDispatcher;

/**
 * This class is an event that carries all information regarding a join
 * requested by the Call Manager client.
 * <p>
 * This class is immutable.
 * @author Malin Flodin
 */
public final class JoinRequestEvent implements EventObject {

    private final Call firstCall;
    private final Call secondCall;
    private final IEventDispatcher eventDispatcher;

    public JoinRequestEvent(Call firstCall, Call secondCall,
                            IEventDispatcher eventDispatcher) {
        this.firstCall = firstCall;
        this.secondCall = secondCall;
        this.eventDispatcher = eventDispatcher;
    }

    public Call getFirstCall() {
        return firstCall;
    }

    public Call getSecondCall() {
        return secondCall;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public String toString() {
        return "JoinRequestEvent";
    }
}
