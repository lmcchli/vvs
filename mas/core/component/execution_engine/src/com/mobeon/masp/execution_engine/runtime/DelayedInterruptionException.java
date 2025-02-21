/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

public class DelayedInterruptionException extends RuntimeException {
    public DelayedInterruptionException(String event) {
        super(event);
    }
}
