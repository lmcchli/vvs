package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueBase;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

/**
 * @author Mikael Andersson
 */
public class ObjectValue extends ValueBase {

    private Object value;

    public ObjectValue(Object value) {
        this.kind = Kind.OBJECT;
        this.value = value;
    }

    public ObjectValue() {
        this.kind = Kind.OBJECT;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitObject(ex,value);
    }

}

