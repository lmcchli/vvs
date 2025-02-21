package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.PromptOperationBase;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jun 5, 2006
 * Time: 4:40:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetAbortPrompts extends PromptOperationBase {

    public void execute(PromptQueue queue) throws InterruptedException {
        queue.setAbortPrompts(true);
    }

    public String arguments() {
        return "";
    }
}
