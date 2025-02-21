/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.util.Prototype;

/**
 * A snapshot of an engines current execution state.
 *
 * Used by {@link Engine} to provide backtracking
 * information in the case of an application, or
 * platform error.
 *
 * @author Mikael Andersson
 */
public class Snapshot implements Prototype {

    public StackFrame frame;
    public int stackDepth;

    public Snapshot(Data prototype) {
        frame = new StackFrame(prototype.duplicate());
    }

    private Snapshot(StackFrame frame,int stackDepth) {
        this.stackDepth = stackDepth;
        this.frame = frame.duplicate();
    }

    private Snapshot() {}

    public void assignFrame(StackFrame frame) {
       this.frame.copy(frame);
    }

    public String toString() {
        return frame.toString();
    }

    public Snapshot duplicate(){
        return new Snapshot(frame,stackDepth);
    }

    public Snapshot copy(Snapshot toCopy) {
        this.frame = toCopy.frame;
        this.stackDepth = toCopy.stackDepth;
        return this;
    }

    public Snapshot clone() {
        return new Snapshot().copy(this);
    }
}
