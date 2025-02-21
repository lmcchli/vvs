/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.execution_engine.runtime.ValueBase;
import com.mobeon.common.eventnotifier.Event;

/**
 * @author Mikael Andersson
 */
public class EventValue extends ValueBase {

    Event value;

    public EventValue(Event value) {
        this();
        this.value = value;
    }

    public EventValue() {
        kind = Kind.EVENT;
    }

    public Event getValue() {
        return value;
    }

    public void setValue(Event value){
        this.value = value;
    }

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitEvent(ex,value);
    }

}
