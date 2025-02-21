/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class OnPlayEvent extends VXMLOperationBase {

    private static final ILogger logger = ILoggerFactory.getILogger(OnPlayEvent.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getCall().setIsPlaying(false);
        ex.getCall().setHasReceivedPlayEvent(true);
    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
