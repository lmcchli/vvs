package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * Checks for an existing SpeechRecognizer, and starts it.
 * @author Mikael Andersson.
 */
public class StartRecognizer extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getMediaTranslator().recognize(ex.getCall().getInboundStream());
    }

    public String arguments() {
        return "";
    }
}
