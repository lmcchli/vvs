/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.ReturnEvent;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

public class ReturnSubdialog extends VXMLOperationBase {
    private String name;
    private DebugInfo debugInfo;

    public ReturnSubdialog(DebugInfo debugInfo,String name) {
        this.debugInfo = debugInfo;
        this.name = name;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        EventProcessor.Entry entry= ex.getEventEntry();
        if(entry != null && entry.getEvent() != null) {
            if(entry.getEvent() instanceof ReturnEvent) {
                ReturnEvent event = ((ReturnEvent) entry.getEvent());

                if(event.getResult()!= null) {
                    ex.getCurrentScope().setValue(name,event.getResult());
                    return;
                }

                if(event.getResultEvent() != null) {
                    ex.getEventHub().fireContextEvent(event.getResultEvent());
                    return;
                }

                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH,"Subdialog returned invalid/no data !", debugInfo);
            }
        }
    }

    public String arguments() {
        return textArgument(name);
    }

}
