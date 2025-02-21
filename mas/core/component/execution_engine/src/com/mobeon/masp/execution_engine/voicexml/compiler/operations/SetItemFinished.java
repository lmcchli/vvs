/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * @author David Looberger
 */
public class SetItemFinished extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState state = ex.getFIAState();
        FIAState.NextItem nextItem = state.getNextItem();
        //Don't use this item for blocks, since it's clearly stated that they is to considerad as
        //filled at the entry of this block. If you set them filled after they exited, you can not
        //clear the block the clear operation resides in, as it will always be considered as filled
        if (nextItem.name != null) {

            Scope scope = ex.getCurrentScope();
            if(scope.evaluate(nextItem.name)!= scope.getUndefined()) {
                state.finishedExecutingItem(nextItem.name);
            }
        }
    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
