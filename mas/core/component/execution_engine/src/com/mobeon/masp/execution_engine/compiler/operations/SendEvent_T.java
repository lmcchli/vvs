/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.NotAValue;

/**
 * @author Mikael Andersson
 */
public class SendEvent_T extends OperationBase {

    public void execute(ExecutionContext ex) throws InterruptedException {
        Value v = ex.getValueStack().pop();
        if(v instanceof NotAValue){
            return;
        }
        Object o = v.getValue();
        ex.getEventHub().fireEvent((Event) o);
    }

    public String arguments() {
        return "";
    }
}
