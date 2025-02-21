/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.IEventDispatcher;

/**
 * Class that handles stack events.
 */
/*package*/ final class StackEventHandler {
    // For now, this class simply lets the notifier class handle
    // events. To shorten the time the calling thread spends handling
    // an event, this class could keep a pool of threads, each thread
    // managing its own event queue. Each notifier object should be
    // delegated a specific queue for all its events. Events are
    // simply pushed into the queue and handled by the thread.
    // This way, the calling thread does as little job as possible,
    // wich could be preferrable if it has a native origin.
    
    /* package */ StackEventHandler() {
        // No instances of this class should be created.
        // Is package-declared to make the testclass complete
        // (looks nice in the cobertura-reports).
    }
    
    /**
     * Gets an event notifier instance that can be used to dispatch
     * stack events to other components.
     * 
     * @param dispatcher Used to dispatch events to other components.
     * 
     * @throws IllegalArgumentException If <code>dispatcher</code> is 
     *                                  <code>null</code>.
     */
    /*package*/ static StackEventNotifier getEventNotifier(IEventDispatcher dispatcher) {
        return new StackEventNotifier(dispatcher);
    }
}