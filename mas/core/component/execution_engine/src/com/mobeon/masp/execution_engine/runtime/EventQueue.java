/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

/**
 * @author David Looberger
 */
public interface EventQueue<T> {  

    boolean offer(T p);

    T poll();

    int size() ;

    Object[] getEvents();

    /**
     * @param other
     * @return The number of events copied
     */
    int copyEvents(EventQueue<T> other);

    void clear();
}
