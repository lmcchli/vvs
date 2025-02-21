/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * Visitor to visit {@link Value} instances for implemented different behaviour
 * to the Values type
 *
 * @author Mikael Andersson
 */
public interface ValueVisitor {
    public Object visitText(ExecutionContext ex, String text);

    public Object visitMark(ExecutionContext ex);

    public Object visitMediaObject(ExecutionContext ex, IMediaObject obj);

    public Object visitECMAObject(ExecutionContext ex, Object so);

    public Object visitEvent(ExecutionContext ex, Event value);

    public Object visitTextArray(ExecutionContext ex, String[] value);

    public Object visitObject(ExecutionContext ex, Object value);
}
