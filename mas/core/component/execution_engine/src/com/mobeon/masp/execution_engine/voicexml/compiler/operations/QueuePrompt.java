/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.compiler.PromptImpl;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.PromptOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.PromptQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Looberger
 */
public class QueuePrompt extends PromptOperationBase {
    private List<PromptImpl> promptList = new ArrayList<PromptImpl>();

    public QueuePrompt(PromptImpl prompt) {
        super();
        this.promptList.add(prompt);
    }

    public void execute(PromptQueue queue) throws InterruptedException {
        queue.addPromptsToQueue(promptList);
    }

    public String arguments() {
        return "";
    }
}
