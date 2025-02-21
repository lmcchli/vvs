/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class ClearFormItems extends VXMLOperationBase {
    String namelist;
    public ClearFormItems(String namelist) {
        super();
        this.namelist = namelist;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getFIAState().clearFormItems(ex, namelist);
    }

    public String arguments() {

        return namelist != null ? namelist : "";
    }
}
