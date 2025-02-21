/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.runtime.ecma.ECMAExecutorException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author Mikael Andersson
 */
public class CloseScope extends OperationBase {

    private static final ILogger logger = ILoggerFactory.getILogger(CloseScope.class);

    /**
     * @logs.error "Failed to close ECMA scope." - The execution engine has encountered an internal error, either due to a looping/misbehaving application, or possibly a bug
     * @param ex
     * @throws InterruptedException
     */
    public void execute(ExecutionContext ex) throws InterruptedException {
        ScopeRegistry executor = ex.getScopeRegistry();
            if(!executor.deleteMostRecentScope()){
                logger.error("Failed to close ECMA scope.");
            }
    }

    public String arguments() {
        return "";
    }
}
