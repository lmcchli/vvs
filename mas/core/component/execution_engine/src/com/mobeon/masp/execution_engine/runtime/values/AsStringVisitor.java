/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.mediaobject.IMediaObject;

public class AsStringVisitor implements ValueVisitor {

    public Object visitEvent(ExecutionContext ex, Event value) {
        return value.toString();
    }

    public Object visitECMAObject(ExecutionContext ex, Object ecmaVar) {
        return ecmaVar.toString();
    }

    public Object visitText(ExecutionContext ex, String text) {
        return text;
    }

    public Object visitMark(ExecutionContext ex) {
        return "";
    }

    public Object visitObject(ExecutionContext ex, Object value) {
        return value != null?value.toString():"";
    }

    public Object visitMediaObject(ExecutionContext ex, IMediaObject obj) {
        //return obj.getFileURLAsString();
        return null; //TODO: getFileURLAsString does not exist anymore
    }

    public Object visitTextArray(ExecutionContext ex, String[] value) {
        StringBuffer buf = new StringBuffer();
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                String s = value[i];
                buf.append(s);
                if (i + 1 < value.length)
                    buf.append(" ");
            }
        }
        return buf.toString();
    }
}
