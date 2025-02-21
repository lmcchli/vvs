/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.mediaobject.IMediaObject;
import org.mozilla.javascript.NativeJavaObject;

/**
 * @author David Looberger
 */
public class AsObjectVisitor implements ValueVisitor {
    public Object visitText(ExecutionContext ex, String text) {
        return text;
    }

    public Object visitMark(ExecutionContext ex) {
        return null;
    }

    public Object visitMediaObject(ExecutionContext ex, IMediaObject obj) {
        return obj;
    }

    public Object visitECMAObject(ExecutionContext ex, Object so) {
        if(so instanceof NativeJavaObject) {
            NativeJavaObject o = (NativeJavaObject) so;
            return o.unwrap();
        }
        return so;
    }

    public Object visitObject(ExecutionContext ex, Object value) {
        return value;
    }

    public Object visitEvent(ExecutionContext ex, Event value) {
        return value;
    }

    public Object visitTextArray(ExecutionContext ex, String[] value) {
        return value;
    }
}
