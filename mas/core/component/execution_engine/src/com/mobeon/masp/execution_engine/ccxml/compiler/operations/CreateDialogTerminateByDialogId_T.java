/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogTerminateEvent;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

public class CreateDialogTerminateByDialogId_T extends CCXMLOperationBase {
    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String dialogId = stack.pop().toString(ex);
        DialogTerminateEvent dte = new DialogTerminateEvent(dialogId);
        stack.push(dte);

    }

    public String arguments() {
        return "";
    }
}
