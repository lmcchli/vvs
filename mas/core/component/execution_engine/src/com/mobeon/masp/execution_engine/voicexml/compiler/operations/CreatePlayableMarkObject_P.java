/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class CreatePlayableMarkObject_P extends VXMLOperationBase {
    private String value;
    private boolean doEval;

    public CreatePlayableMarkObject_P(String val, boolean doEval) {
        super();
        this.value = val;
        this.doEval = doEval;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        PlayableObjectImpl markObj = new PlayableObjectImpl();
        if (!doEval) {
            markObj.setMarkText(value);
        } else {
            String name = (String) ex.getCurrentScope().evaluate(value);
            markObj.setMarkText(name);
        }
        ex.getValueStack().pushScriptValue(markObj);
    }

    public String arguments() {
        return value;
    }
}
