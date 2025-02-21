/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;

/**
 * @author Mikael Andersson
 */
public class CreateDialogStartByDialogId_T extends CCXMLOperationBase {
    public String arguments() {
        return "";
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        Value v = ex.getValueStack().pop();
        String s = v.toString(ex);
        DialogStartEvent de = ex.fetchDialogEvent(s);
        ex.getValueStack().push(de);
    }
}
