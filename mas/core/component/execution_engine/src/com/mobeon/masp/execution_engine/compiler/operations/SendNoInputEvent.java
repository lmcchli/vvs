package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Aug 21, 2006
 * Time: 9:44:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class SendNoInputEvent extends VXMLOperationBase {
    private String message;
    private DebugInfo debugInfo;

    public SendNoInputEvent(String message, DebugInfo debugInfo){
        this.message = message;
        this.debugInfo = debugInfo;
    }
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getVoiceXMLEventHub().fireNoInputEvent(message, debugInfo);
    }

    public String arguments() {
        return message;
    }
}
