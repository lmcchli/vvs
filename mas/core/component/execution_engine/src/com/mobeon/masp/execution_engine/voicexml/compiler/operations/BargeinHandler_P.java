package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.PromptQueue;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.InputAggregator;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Handle barge in for promt. Pushes TRUE on value stack if the the engine should set state to
 * WAITING at the end of the event handler where the operation is used, FALSE otherwise
 *
 * @author David Looberger
 */
public class BargeinHandler_P extends VXMLOperationBase {
    private static ILogger log = ILoggerFactory.getILogger(BargeinHandler_P.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        final boolean bargeIn = ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("true");
        final FIAState state = ex.getFIAState();

        if(bargeIn)
            state.inputReceived(ex);

        if (!ex.getCall().getIsPlaying()) {
            ex.getValueStack().push(new ECMAObjectValue(Boolean.FALSE));
            return;
        }

        if (bargeIn) {
            if (log.isDebugEnabled()) log.debug("Bargein. Stopping ongoing play");
            state.getMarkInfo(ex);
            if (!ex.getCall().stopPlay()) {
                if (log.isInfoEnabled()) log.info("Failed to stop play on outbound stream");

            }
            ex.getValueStack().push(new ECMAObjectValue(Boolean.TRUE));
            PromptQueue(ex).setAbortPrompts(true);
        } else {
            if (log.isDebugEnabled()) log.debug("Bargein FALSE");
            ex.getValueStack().push(new ECMAObjectValue(Boolean.FALSE));
            InputAggregator inputAggregator = ex.getInputAggregator();
            if (inputAggregator.clearControlTokenQ()) {
                if (log.isDebugEnabled()) log.debug("Removed all DTMF token from token queue");
            }
        }
    }

    public String arguments() {
        return "";
    }
}
