/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.runtime.values.Visitors;

/**
 * @author Mikael Andersson
 */
public abstract class ValueBase implements Value {

    protected Kind kind = Kind.MARK;

    public Kind getKind() {
        return kind;
    }

    public Object getValue() {
        return this;
    }

    public Object accept(ValueVisitor visitor) {
        return accept(null, visitor);
    }

    public String toString(ExecutionContext ex) {
        return (String) accept(ex, Visitors.getAsStringVisitor());
    }

    public String toString() {
        return (String) accept(Visitors.getAsStringVisitor());
    }

    public Object toECMA(ExecutionContext ex) {
        return accept(ex, Visitors.getAsECMAVisitor());
    }

    public Object toObject(ExecutionContext ex) {
        return accept(ex, Visitors.getAsObjectVisitor());
    }

}
