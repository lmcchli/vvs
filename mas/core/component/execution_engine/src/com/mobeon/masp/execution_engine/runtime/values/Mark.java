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
public class Mark extends ValueBase {

    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return visitor.visitMark(ex);
    }

    public boolean equals(Object obj) {
        return obj != null && obj instanceof Mark;
    }
}
