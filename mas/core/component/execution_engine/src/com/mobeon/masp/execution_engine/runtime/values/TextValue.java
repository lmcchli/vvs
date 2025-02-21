/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueBase;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

/**
 * @author Mikael Andersson
 */
public class TextValue extends ValueBase {

    String value;

    public TextValue(String value) {
        kind = Kind.TEXT;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitText(ex, value);
    }
}
