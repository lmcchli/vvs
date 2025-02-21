/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectPlayer;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.wrapper.Call;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.File;
import java.net.URI;

public class PlayAudio_T extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(PlayAudio_T.class);
    private static final int BUFFER_SIZE = 1024;

    private final boolean setEngineInWaitState;
    private final boolean considerTransferTerminationFlag;

    public PlayAudio_T(boolean setEngineInWaitState, boolean considerTransferTerminationFlag) {
        this.setEngineInWaitState = setEngineInWaitState;
        this.considerTransferTerminationFlag = considerTransferTerminationFlag;
    }

    public String arguments() {
        return classToMnemonic(getClass());
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        Object src;
        ValueStack value_stack = ex.getValueStack();
        src = value_stack.pop().toObject(ex);

        if (considerTransferTerminationFlag && ex.getTransferState().getCallState() != TransferState.CallState.INITIATING) {
            logger.debug("Transfer is terminated, will not play");
            return;
        }

        Call call = ex.getCall();
        if (src == null) {
            // TODO: Handle the case when we receive a null object.
            if (logger.isDebugEnabled()) logger.debug("Playing a NULL media object!");
            Long offset = Tools.parseCSS2Time(ex.getProperties().getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET));
            if (offset == null) {
                offset = 0L;
            }
            call.play(ex.getMediaObjectFactory().create(), offset);
            if (setEngineInWaitState) {
                setEngineInWaitState(ex);
            }
            return;
        }

        try {
            URI documentURI = ex.getExecutingModule().getDocumentURI();
            if (logger.isDebugEnabled()) logger.debug("Source is of type " + src.getClass());
            Long offset = Tools.parseCSS2Time(ex.getProperties().getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET));
            if (offset == null) {
                offset = 0L;
            }
            if (src instanceof PlayableObject) {
                PlayableObjectPlayer.playAndForget((PlayableObject) src, ex);


            } else {

                if (logger.isInfoEnabled()) logger.info("Unable to play this object in transfer");
                // ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, DebugInfo.getInstance());
            }

            if (logger.isDebugEnabled()) logger.debug("Done issuing play job");
        } catch (IllegalStateException e) {
            if (logger.isDebugEnabled()) logger.debug(e);

        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled()) logger.debug(e);
        }
    }

    private static void logFileNotExistError(String path) {
        if (logger.isInfoEnabled()) logger.info("Requested to play file '" + path +
                "', but it does not exist");
    }

    private static boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }

    private static void setEngineInWaitState(VXMLExecutionContext ex) {
        if (logger.isDebugEnabled()) logger.debug("Setting engine state to " + ExecutionResult.EVENT_WAIT);
        // Verify that no events have arived that needs attention
        if (ex.getEventProcessor().hasEventsInQ()) {
            if (logger.isDebugEnabled())
                logger.debug("There are unhandled events in the event queue, setting state to " + ExecutionResult.DEFAULT);
            ex.setExecutionResult(ExecutionResult.DEFAULT);
        } else {
            if (logger.isDebugEnabled()) logger.debug("Setting VXML state to " + Constants.VoiceXMLState.STATE_WAITING);
            ex.setWaitingState();
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
        }
    }
}
