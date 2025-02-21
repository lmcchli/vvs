/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class QueuePlayableObject_T extends VXMLOperationBase {
    
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Value value = ex.getValueStack().pop();
        Object obj = value.toObject(ex);
        if (obj instanceof PlayableObjectImpl) {
            PlayableObjectImpl playableObject = (PlayableObjectImpl) obj;
            PromptQueue(ex).addPlayableToQueue(playableObject);
        }


    }

    public String arguments() {
        return "";
    }
}
