/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class SetFIAState extends VXMLOperationBase {
    private FIAObjects fia ;
    private static ILogger logger = ILoggerFactory.getILogger(SetFIAState.class);
    public SetFIAState(FIAObjects fia) {
        super();
        this.fia = fia;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (logger.isDebugEnabled()) logger.debug("Setting FIA Object");
        ex.setFIAState(new FIAState(fia));
    }

    public String arguments() {
        return fia.getId();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
