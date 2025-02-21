package com.mobeon.masp.execution_engine.voicexml.compiler.base;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.compiler.OperationBase;

/**
 * @author Mikael Andersson
 */
public abstract class PromptOperationBase extends OperationBase {

    public final void execute(ExecutionContext ex) throws InterruptedException {
        execute(PromptQueue((VXMLExecutionContext)ex));
    }

    public abstract void execute(PromptQueue queue) throws InterruptedException;
}
