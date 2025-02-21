package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 31, 2006
 * Time: 1:52:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class InhibitRecordingIfBufferedDTMF extends VXMLOperationBase {

    final ILogger logger = ILoggerFactory.getILogger(InhibitRecordingIfBufferedDTMF.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if(InputAggregator(ex).hasControlToken()){

            ex.getFIAState().collectDTMFUtterance(ex, false, true, false);

            if (ex.getUtterance() != null && ex.getFIAState().matchedGrammar()){
                String message = "There was buffered DTMF and a match. Recording inhibited. Throwing noinput event";
                if (logger.isDebugEnabled()) {
                    logger.debug(message);
                }
                ex.getFIAState().setInhibitRecording(true);
                ex.getVoiceXMLEventHub().fireNoInputEvent(message, DebugInfo.getInstance());
            } else {
                if (logger.isDebugEnabled()) logger.debug("There was buffered DTMF but no match");
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("No buffered DTMF");
        }
    }

    public String arguments() {
        return "";
    }
}
