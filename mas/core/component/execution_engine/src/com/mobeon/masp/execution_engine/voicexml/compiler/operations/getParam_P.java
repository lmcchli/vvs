/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

public class getParam_P extends VXMLOperationBase {
    private String namePart;

    public getParam_P(String namePart) {this.namePart = namePart;}

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Object o = ex.getStatics().getParams().get(namePart);
        ex.getValueStack().pushScriptValue(o);
    }

    public String arguments() {
        return textArgument(namePart);
    }
}
