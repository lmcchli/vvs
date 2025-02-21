/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;

public abstract class CCXMLOperationBase extends OperationBase{
    public void execute(ExecutionContext ex) throws InterruptedException {
        execute((CCXMLExecutionContext)ex);
    }
    public abstract void execute(CCXMLExecutionContext ex) throws InterruptedException;

}
