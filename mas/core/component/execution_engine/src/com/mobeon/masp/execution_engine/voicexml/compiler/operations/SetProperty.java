/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetProperty extends VXMLOperationBase {
    String prop;
    String value;
    public SetProperty(String prop, String value) {
        super();
        this.prop = prop;
        this.value = value;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.setProperty(prop, value);
    }

    public String arguments() {
        return prop;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
