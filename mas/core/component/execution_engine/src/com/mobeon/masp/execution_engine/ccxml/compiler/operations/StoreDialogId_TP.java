/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.values.NotAValue;

public class StoreDialogId_TP extends CCXMLOperationBase {
     final private String dialogId;

    public StoreDialogId_TP(String dialogId) {
        super();
        this.dialogId = dialogId;
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        Object value = ex.getValueStack().peek().getValue();
        if(value instanceof NotAValue){
            return;
        }
        if(value instanceof DialogStartEvent) {
            DialogStartEvent dse = (DialogStartEvent)value;
            Id<BridgeParty> id = dse.getDialogId();
            ex.getCurrentScope().evaluateAndDeclareVariable(dialogId,id.toString(),true,false);
        } else {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    "internal error: event was of class " + value.getClass() + " in " + getClass(), DebugInfo.getInstance());
        }
    }

    public String arguments() {
        return textArgument(dialogId);
    }
}
