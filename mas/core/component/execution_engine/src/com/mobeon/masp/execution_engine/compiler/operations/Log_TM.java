/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public class Log_TM extends OperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(Log_TM.class);

    public Log_TM() {
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        List<Value> values = ex.getValueStack().popToMark();
        StringBuffer accumulated = new StringBuffer();
        for (int i = values.size() - 1; i >= 0; i--) {
            Value v = values.get(i);
            accumulated.append(v.accept(ex, Visitors.getOnlyTextVisitor()));
        }
        if (log.isInfoEnabled()) log.info(accumulated);
    }

    public String arguments() {
        return "";
    }
}
