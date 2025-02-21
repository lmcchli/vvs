package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Aug 23, 2006
 * Time: 12:03:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddBargeinHandler extends VXMLOperationBase {
    
    private final static List<Executable> asrBargeinHandler;
    private final static List<Executable> dtmfBargeinHandler;
    private final static List<Executable> noAsrBargeinHandler;
    private final static List<Executable> noDtmfBargeinHandler;

    static {
        Predicate nopEventHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        nopEventHandler.add(Ops.nop());

        Predicate terminateEventHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        terminateEventHandler.add(
                new AtomicExecutable(
                        Ops.bargeinHandler()));

        dtmfBargeinHandler = new ArrayList<Executable>();
        dtmfBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.DTMFUTTERANCE_EVENT, terminateEventHandler, true));

        asrBargeinHandler = new ArrayList<Executable>();
        //asrBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT,terminateEventHandler, true));
        asrBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT,nopEventHandler, true));


        Predicate noTerminateEventHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        noTerminateEventHandler.add(
                new AtomicExecutable(
                        Ops.bargeinHandler(), Ops.changeExecutionResult(ExecutionResult.EVENT_WAIT)));

        noDtmfBargeinHandler = new ArrayList<Executable>();
        noDtmfBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT, noTerminateEventHandler, true));

        noAsrBargeinHandler = new ArrayList<Executable>();
        noAsrBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT, nopEventHandler, true));
        //noAsrBargeinHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT, noTerminateEventHandler, true));
        noAsrBargeinHandler.add(Ops.nop());

    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("true")) {
            ex.anonymousCall(dtmfBargeinHandler);
            ex.anonymousCall(asrBargeinHandler);
        } else {
            ex.anonymousCall(noDtmfBargeinHandler);
            ex.anonymousCall(noAsrBargeinHandler);
        }
    }

    public String arguments() {
        return "";
    }
}
