package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 22, 2006
 * Time: 10:19:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResendBufferedDTMF extends VXMLOperationBase {

    private static ILogger logger = ILoggerFactory.getILogger(ResendBufferedDTMF.class);


    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if(ex.getInputAggregator().hasControlToken()){
            if(logger.isDebugEnabled())logger.debug("Resending DTMF");
            ex.getEventHub().fireContextEvent(Constants.VoiceXML.DTMFUTTERANCE_EVENT,
                    "Internally generated dtmf utterance, since unhandled DTMF is found",
                    DebugInfo.getInstance());
        }
    }

    public String arguments() {
        return "";
    }
}
