/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/** Unwind the engine stackt to the repromptPoint maintained in the ExecutionContext.
 * The repromptPoint is set by the execute method of the {@link com.mobeon.masp.execution_engine.compiler.InputItemImpl}
 * which is implemenation class for input item nodes containing prompts.
 *
 * @author David Looberger
 */
public class UnwindToRepromtPoint extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Product repromptPoint = ex.getFIAState().getForm();
        if (repromptPoint != null) {
            ex.getEngine().unwind(repromptPoint, false);
        }
        else {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    "repromptPoint was null should never happen...", DebugInfo.getInstance());
        }
    }

    public String arguments() {
        return "";
    }
}
