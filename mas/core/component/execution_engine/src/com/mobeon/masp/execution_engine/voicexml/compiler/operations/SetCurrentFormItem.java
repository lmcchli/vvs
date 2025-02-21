/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetCurrentFormItem extends VXMLOperationBase {
    private String localName        ;

    public SetCurrentFormItem(String localName) {
        super();
        this.localName = localName;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.setCurrentFormItem(localName);
    }

    public String arguments() {
        return localName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
