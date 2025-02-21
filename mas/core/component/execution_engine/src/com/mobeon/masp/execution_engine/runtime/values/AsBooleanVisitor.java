/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.mediaobject.IMediaObject;

public class AsBooleanVisitor implements ValueVisitor {

    public Object visitECMAObject(ExecutionContext ex, Object ecmaVar) {
        return ex.getCurrentScope().toBoolean(ecmaVar)?Boolean.TRUE:Boolean.FALSE;
    }

    public Object visitEvent(ExecutionContext ex, Event value) {
        return Boolean.FALSE;
    }

    public Object visitText(ExecutionContext ex, String text) {
        return Boolean.parseBoolean(text);
    }

    public Object visitMark(ExecutionContext ex) {
        return Boolean.FALSE;
    }

    public Object visitMediaObject(ExecutionContext ex, IMediaObject obj) {
        return Boolean.FALSE;
    }

    public Object visitTextArray(ExecutionContext ex, String[] value) {
        return Boolean.FALSE;
    }

    public Object visitObject(ExecutionContext ex, Object value) {
        return Boolean.FALSE;
    }
}
