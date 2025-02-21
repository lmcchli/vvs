package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * @author Mikael Andersson
 */
public class SendFieldEvent_T2 extends VXMLOperationBase {
    private DebugInfo debugInfo;

    public SendFieldEvent_T2(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String message = stack.popAsString(ex);
        String event = stack.popAsString(ex);
        SimpleEvent ev = new SimpleEventImpl(event,message,debugInfo);
        ev.defineTarget(ex.getFieldTargetType(),ex.getFIAState().getFieldId());
        ex.getEventHub().fireEvent(ev);
    }

    public String arguments() {
        return "";
    }
}
