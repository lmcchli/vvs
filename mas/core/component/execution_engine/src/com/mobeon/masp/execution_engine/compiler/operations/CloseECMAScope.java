package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 6:09:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CloseECMAScope extends OperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(CloseScope.class);

    /**
     * @logs.error "Failed to close ECMA scope." - The execution engine has encountered an internal error, either due to a looping/misbehaving application, or possibly a bug
     * @param ex
     * @throws InterruptedException
     */
    public void execute(ExecutionContext ex) throws InterruptedException {
        ScopeRegistry executor = ex.getScopeRegistry();
        if(!executor.deleteMostRecentECMAScope()){
            logger.error("Failed to close ECMA scope.");
        }
    }

    public String arguments() {
        return "";
    }
}
