/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.compiler.OperationBase;

/**
 * @author Mikael Andersson
 */
public class ReadEvent_P extends OperationBase {

    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        SimpleEvent ev = (SimpleEvent) ex.getEventProcessor().poll().getEvent();
        ex.getValueStack().push(ev);
    }
}
