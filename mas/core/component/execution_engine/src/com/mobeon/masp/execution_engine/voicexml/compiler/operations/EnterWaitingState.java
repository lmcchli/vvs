package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-16
 * Time: 21:59:15
 * To change this template use File | Settings | File Templates.
 */
public class EnterWaitingState extends  VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.setWaitingState();
    }

    public String arguments() {
        return "";
    }
}
