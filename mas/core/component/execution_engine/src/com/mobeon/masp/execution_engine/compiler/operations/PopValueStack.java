package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jun 20, 2006
 * Time: 12:00:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PopValueStack extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getValueStack().pop();
    }

    public String arguments() {
        return "";
    }
}
