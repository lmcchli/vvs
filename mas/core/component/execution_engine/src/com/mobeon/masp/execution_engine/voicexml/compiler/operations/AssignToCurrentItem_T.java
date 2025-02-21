/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class AssignToCurrentItem_T extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(AssignToCurrentItem_T.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        Object ecmaValue = stack.peek().toECMA(ex);
        if (ex.getFIAState().getNextItem() == null) {
            if (logger.isDebugEnabled()) logger.debug("No item selected by the FIA!");
            return;
        }
        String formItemName = ex.getFIAState().getNextItem().name;
        Scope scope = ex.getCurrentScope();
        if(! scope.isDeclaredInAnyScope(formItemName)) {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    formItemName + " is not declared", DebugInfo.getInstance());
            return;
        }
        Object wrappedEcmaObject = scope.javaToJS(ecmaValue);
        scope.setValue(formItemName, wrappedEcmaObject);
    }

    public String arguments() {
        return "";
    }
}
