/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.PromptOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class SetPlayObjectToAlternative extends PromptOperationBase {
    private static ILogger log = ILoggerFactory.getILogger(SetPlayObjectToAlternative.class);

    public void execute(PromptQueue queue) throws InterruptedException {
        PlayableObject playable = queue.getPlayableObject();
        PlayableObject alternative = null;
        if (playable != null) {
            if (log.isDebugEnabled()) log.debug("Setting current playable object to its alternative playing object");
            alternative = playable.getAlternative();
        }
        queue.setPlayingObject(alternative);
    }

    public String arguments() {
        return "";
    }
}
