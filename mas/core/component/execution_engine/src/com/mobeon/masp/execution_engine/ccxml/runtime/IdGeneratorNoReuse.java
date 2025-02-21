/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

/**
 * @author David Looberger
 */
public class IdGeneratorNoReuse<T> extends IdGeneratorImpl<T> {
    private final Object mutex = new Object();

    public IdGeneratorNoReuse(String prefix) {
        super(prefix);
    }

    protected void release(IdImpl<T> connectionId) {
        // Simply discard the id
    }

    public Id<T> generateId() {
        Integer number = 0;
        Id<T> result;
        resetIfNeeded();
        number = sequenceNumber.addAndGet(1);
        result =  new IdGeneratorImpl.IdImplNoFinalizer<T>(this,number);

        return result;
    }

    private void resetIfNeeded() {
        synchronized (mutex) {
            if (sequenceNumber.intValue() > Integer.MAX_VALUE - 1) {
                sequenceNumber.getAndSet(0);
            }
        }
    }
}
