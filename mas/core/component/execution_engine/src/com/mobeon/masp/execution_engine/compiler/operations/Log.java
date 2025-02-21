/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author Patrick Zeits
 */
public class Log extends OperationBase {
    private final ILogger log = ILoggerFactory.getILogger(Log.class);
    private final String log_message;

    public Log(String str) {
        if (str == null)
            str = "";
        log_message = str;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        if (log.isInfoEnabled()) log.info(log_message);
    }

    public String arguments() {
        return textArgument(log_message);
    }
}
