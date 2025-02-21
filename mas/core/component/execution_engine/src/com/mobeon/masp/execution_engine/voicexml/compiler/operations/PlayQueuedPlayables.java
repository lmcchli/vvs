/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.PromptOperationBase;

/**
 * @author David Looberger
 */
public class PlayQueuedPlayables extends PromptOperationBase {

    public void execute(PromptQueue queue) throws InterruptedException {
        queue.playQueuedPlayableObjects();
    }

    public String arguments() {
        return "";
    }
}
