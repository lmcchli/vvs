/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;

public class EventVar_P extends CCXMLOperationBase {
    private final String property;

    public EventVar_P() {
        property = null;
    }

    public EventVar_P(String property) {
        this.property = property;
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        CCXMLEvent event = ex.getEventVar();
        if(property == null) {
            ex.getValueStack().push(event);
        } else {
            ex.getValueStack().pushScriptValue(event.get(property,event));
        }
    }

    public String arguments() {
        return "";
    }
}
