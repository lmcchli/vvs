/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ValueBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

public class ECMAObjectValue extends ValueBase {

    private Object value;

    public ECMAObjectValue(Object value) {
        this.kind = Kind.ECMA;
        this.value = value;
    }

    public ECMAObjectValue() {
        this.kind = Kind.ECMA;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitECMAObject(ex,value);
    }
}
