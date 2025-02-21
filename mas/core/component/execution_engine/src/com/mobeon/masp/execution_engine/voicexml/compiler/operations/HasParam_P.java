/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

public class HasParam_P extends VXMLOperationBase {
    private String namePart;

    public HasParam_P(String namePart) {this.namePart = namePart;}

    public String arguments() {
        return textArgument(namePart);
    }

    public void execute(VXMLExecutionContext context) throws InterruptedException {
        if(context.getStatics().getParams().containsKey(namePart))
            context.getValueStack().pushScriptValue(Boolean.TRUE);
        else
            context.getValueStack().pushScriptValue(Boolean.FALSE);
    }


}
