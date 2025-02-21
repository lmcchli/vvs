/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.mediaobject.IMediaObject;

public class ValueVisitorImpl implements ValueVisitor {

    public enum VisitResult { NO_RESULT }

    public Object visitEvent(ExecutionContext ex, Event value) {
        return VisitResult.NO_RESULT;
    }

    public Object visitECMAObject(ExecutionContext ex, Object so) {
        return VisitResult.NO_RESULT;
    }

    public Object visitText(ExecutionContext ex, String text) {
        return VisitResult.NO_RESULT;
    }

    public Object visitTextArray(ExecutionContext ex, String text[]) {
        return VisitResult.NO_RESULT;
    }

    public Object visitMark(ExecutionContext ex) {
        return VisitResult.NO_RESULT;
    }

    public Object visitMediaObject(ExecutionContext ex, IMediaObject obj) {
        return VisitResult.NO_RESULT;
    }

    public Object visitObject(ExecutionContext ex, Object value) {
        return VisitResult.NO_RESULT;
    }
}
