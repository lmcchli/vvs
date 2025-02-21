/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectPlayer;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class SetAndPlayObject extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(SetAndPlayObject.class);

    private PlayableObject playableObject;

    public SetAndPlayObject(PlayableObject playable) {
        this.playableObject = playable;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (Redirector.PromptQueue(ex).getAbortPrompts()) {
            if (log.isDebugEnabled()) log.debug("Prompt aborted");
        } else {

            if (! (playableObject instanceof com.mobeon.masp.execution_engine.runtime.StartNoInputTimer)) {
                playableObject.updateContextProperties(ex);
            } 
            PlayableObjectPlayer.play(playableObject, ex);
        }
    }

    public String arguments() {
        return "";
    }
}
