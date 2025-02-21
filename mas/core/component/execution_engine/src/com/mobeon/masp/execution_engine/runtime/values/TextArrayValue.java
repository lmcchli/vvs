/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

/**
 * @author Mikael Andersson
 */
public class TextArrayValue extends Mark {
    String[] value;

    public TextArrayValue() {
        kind = Kind.TEXT_ARRAY;
    }

    public TextArrayValue(String ... value) {
        this();
        this.value = value;
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitTextArray(ex, value);
    }
}
