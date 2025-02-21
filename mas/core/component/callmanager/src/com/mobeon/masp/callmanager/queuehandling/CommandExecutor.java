/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.queuehandling;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * Interface implemented by all classes that needs to execute commands queued in
 * a SequenceGuaranteedEventQueue.
 *
 * @author Malin Flodin
 */
public interface CommandExecutor {
    /**
     * This method is called when an event in the SequenceGuaranteedEventQueue
     * shall be processed.
     *
     * This method will always be called by only one thread at a time and the
     * events are processed in the same order as they were added to the queue.
     *
     * NOTE: This method must never throw any type of exception.
     * The reason for this is that the exception cannot be handled in any good
     * way by the SequenceGuaranteedEventQueue, it is simply logged and ignored.
     *
     * @param eventObject The event to process.
     */
    public void doCommand(EventObject eventObject);
}
