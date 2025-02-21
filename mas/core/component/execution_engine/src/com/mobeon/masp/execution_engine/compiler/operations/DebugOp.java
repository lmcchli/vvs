package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-08
 * Time: 18:16:24
 * To change this template use File | Settings | File Templates.
 */
public class DebugOp extends OperationBase {
    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        Thread.sleep(0);        
    }
}
