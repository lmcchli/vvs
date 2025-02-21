/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event;

import java.util.concurrent.atomic.AtomicInteger;

public class EventIdGenerator {
    private static AtomicInteger id = new AtomicInteger(0);

    public static int getId() {
        return id.getAndIncrement();
    }
}
