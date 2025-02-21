/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.Event;

import java.util.List;

public interface ValueStack  {
    public List<Value> popToMark();
    public void push(String text);
    public void pushMark();
    public void push(Event e);
    public void push(Object o);
    public void pushScriptValue(Object o);
    public void push(String[] text);
    /**
     * Gets the size of the stack
     * @return Size of the stack
     */
    public int size();

    /**
     * Places a new entry on the top of the stack
     * @param entry Entry to place on stack
     */
    public void push(Value entry);

    /**
     * Removes the top entry from the stack and returns it.
     * Returns null if you try pop beyoynd and of stack.
     * @return The removed entry
     */
    public Value pop();
    /**
     * Gets the entry on the top of the stack without deleting it.
     * @return The entry on the top of the stack
     */
    public Value peek();
    /**
     * Trims the stack to the specifed new size.
     * @param newSize The desired new size
     */
    public void prune(int newSize);

    public String popAsString(ExecutionContext ex);

    public List<Value> toList();
}
