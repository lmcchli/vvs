/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public class OnlyTextVisitor extends ValueVisitorImpl {
    public Object visitText(ExecutionContext ex, String text) {
        return text;
    }

    public Object visitECMAObject(ExecutionContext ex, Object so) {
        if(so == null){
            return null;
        }
        return so.toString();
    }
}
