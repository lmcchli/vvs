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
public class GetProperty_P extends VXMLOperationBase {
    private String propName ;
    private static final ILogger log = ILoggerFactory.getILogger(GetProperty_P.class);

    public GetProperty_P(String prop) {
        super();
        this.propName = prop;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        String value = ex.getProperties().getProperty(propName);
        if (log.isDebugEnabled()) log.debug("Retrieved the value " + value + " for property " + propName);
        ValueStack vs = ex.getValueStack();
        vs.push(value);
    }

    public String arguments() {
        return propName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
