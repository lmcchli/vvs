/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class RerunFIAIfUnfinishedItems extends VXMLOperationBase {
    FormPredicate form;
    public RerunFIAIfUnfinishedItems(FormPredicate form) {
        super();
        this.form = form;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState state = ex.getFIAState();
        if (!state.areAllFormItemsDone(ex)) {
            ex.getEngine().unwind(form, false);
        }
    }

    public String arguments() {
        return form.getName();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
