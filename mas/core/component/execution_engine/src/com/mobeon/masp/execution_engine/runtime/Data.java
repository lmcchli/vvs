/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.util.Prototype;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Domain specific data instance stored in a StackFrame.
 * <p>
 * <b>Threading: </b>This class is <em>not</em> safe for concurrent access by multiple threads
 * @author Mikael Andersson
 */
public class Data implements Prototype {

    /**
     * Depth of {@link com.mobeon.masp.execution_engine.runtime.ValueStack} at the time the
     * referring frame was created.
     */
    public int depth;

    /**
     * List of destructors to execute when unwinding
     * the stack.
     */
    public List<Executable> destructors;

    /**
     * Number of constructors that was called while
     * executing in the referring stack frame.
     */
    public int constructorProgress;

    public Data() {}

    private Data(int depth, List<Executable> destructors, int constructorProgress) {
        this.depth = depth;
        this.destructors = destructors == null?null: Collections.unmodifiableList(new ArrayList<Executable>(destructors));
        this.constructorProgress = constructorProgress;
    }

    /**
     * Copies the contents of another Data instance
     * into this instance. The copying is shallow.
     * @param toCopy Instance to copy data from
     */
     public Data copy(Data toCopy) {
        this.depth = toCopy.depth;
        this.destructors = toCopy.destructors;
        this.constructorProgress = toCopy.constructorProgress;
        return this;
    }

    /**
     * Performs a deep copy of this instance and returns the
     * copy as a new instance.
     * @return The cloned instance
     */
    public Data duplicate() {
        return new Data(depth,destructors,constructorProgress);
    }

    /**
     * Performs a shallow copy of this instance and returns the
     * copy as a new instance.
     * @return The cloned instance
     */
    public Data clone() {
        return new Data().copy(this);
    }


    public String toString() {
        return getClassName()+"{" + "depth=" + depth + ", destructors=" + destructors + ", constructorProgress=" +
        constructorProgress + '}';
    }

    public String getClassName() {
        return "Data";
    }

    public void clear() {
        depth = 0;
        destructors = null;
        constructorProgress = 0;
    }
}
