/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

public class EngineStackExhausted extends RuntimeException {
    public EngineStackExhausted(String message) {
        super(message);
    }
}
