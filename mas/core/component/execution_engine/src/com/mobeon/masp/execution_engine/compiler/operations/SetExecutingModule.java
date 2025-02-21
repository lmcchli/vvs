package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 8, 2006
 * Time: 4:04:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetExecutingModule extends OperationBase {

    private Module module;

    public SetExecutingModule(Module module) {
        this.module = module;
    }

    public String arguments() {
        return "" + module.getDocumentURI();
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        context.setExecutingModule(module);
        if (TestEventGenerator.isActive()) {
            if (context instanceof VXMLExecutionContext) {
                TestEventGenerator.generateEvent(TestEvent.VXML_MODULE_STARTED, context);
            } else {
                TestEventGenerator.generateEvent(TestEvent.CCXML_MODULE_STARTED, context);
            }
        }
    }
}

