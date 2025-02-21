/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * If bargein is true, stop an (possibly) ongoing play, if false, remove the DTMF token from the token queue
 * @author David Looberger
 */
public class BargeinHandler extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(BargeinHandler.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        //Notify that we have received input
        ex.getFIAState().inputReceived(ex);

        if (! ex.getCall().getIsPlaying()){

            // We are no longer playing, but this may be due to  that we have cancelled the play.
            // Have we received play event for the cancellation?

            if(ex.getCall().hasReceivedPlayEvent()){
                if (logger.isDebugEnabled()) logger.debug("Bargein while not playing");
                return;
            } else {
                if (logger.isDebugEnabled()) logger.debug("Have not received play event yet, continue to wait");
                setEngineInWaitState(ex);
                return;
            }
        }

        if (ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("true")) {
            if (logger.isDebugEnabled()) logger.debug("Bargein. Stopping ongoing play");
            final FIAState state = ex.getFIAState();
            state.getMarkInfo(ex);
            if (!ex.getCall().stopPlay()) {
                if (logger.isInfoEnabled()) logger.info("Failed to stop play on outbound stream");
            } else {
                // wait for play finished
                setEngineInWaitState(ex);
            }
            PromptQueue(ex).setAbortPrompts(true);
        }
        else {
            if (logger.isDebugEnabled()) logger.debug("Bargein FALSE");
            if (InputAggregator(ex).clearControlTokenQ()) {
                if (logger.isDebugEnabled()) logger.debug("Removed all DTMF token from token queue");
            }
        }
    }

    private void setEngineInWaitState(VXMLExecutionContext ex){
        ExecutionResult state = ExecutionResult.EVENT_WAIT;

        // Verify that no events have arived that needs attention
        if (ex.getEventProcessor().hasEventsInQ()) {
            if (logger.isDebugEnabled())
                logger.debug("There are unhandled events in the event queue, setting state to " + ExecutionResult.DEFAULT);
            ex.setExecutionResult(ExecutionResult.DEFAULT);
        } else {
            if (logger.isDebugEnabled()) logger.debug("Setting state to " + state);
            ex.setExecutionResult(state);
        }
    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
