/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class RetrieveProperty_P extends VXMLOperationBase {
    String prop = null;
    private static final ILogger log = ILoggerFactory.getILogger(RetrieveProperty_P.class);

    public RetrieveProperty_P(String prop) {
        super();
        this.prop = prop;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        String value = ex.getProperties().getProperty(prop);
        ValueStack vs = ex.getValueStack();
        if (log.isDebugEnabled()) log.debug("Assigning " + value + " to " + prop);
        vs.push(value);
    }

    public String arguments() {
        return prop != null ? prop : "";
    }
}
