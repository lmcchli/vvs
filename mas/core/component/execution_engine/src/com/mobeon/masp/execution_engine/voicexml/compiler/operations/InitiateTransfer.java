package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.TransferState.CallState.INITIATING;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 6, 2006
 * Time: 3:14:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class InitiateTransfer extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getTransferState().resetAll();
        ex.getTransferState().setCallState(INITIATING);
        TestEventGenerator.generateEvent(TestEvent.TRANSFER_INITIATED);
    }

    public String arguments() {
        return "";
    }
}
