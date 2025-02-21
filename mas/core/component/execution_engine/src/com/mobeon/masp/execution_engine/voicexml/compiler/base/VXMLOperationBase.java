/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.base;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public abstract class VXMLOperationBase extends OperationBase {

    public final void execute(ExecutionContext ex) throws InterruptedException {
        execute((VXMLExecutionContext)ex);
    }
    public abstract void execute(VXMLExecutionContext ex) throws InterruptedException;
}
