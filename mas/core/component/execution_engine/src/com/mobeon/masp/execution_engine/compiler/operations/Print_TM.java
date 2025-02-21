/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author David Looberger
 */
public class Print_TM extends OperationBase {
    private static final ILogger logger = ILoggerFactory.getILoggerFromCategory("Application");
    private final String label;
    private final String prefix;

    public Print_TM(String document, int lineNo, String label) {
        if (document == null) {
            document = "";
        }

        if (label == null) {
            this.label = "info";
        } else {
            this.label = label;
        }
        this.prefix = document + ":" + lineNo + " ";
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        List<Value> values = stack.popToMark();
        String logstring = this.prefix;

        Collections.reverse(values);
        for (Value value : values) {
            Object theValue = value.getValue();
            Scope currentScope = ex.getCurrentScope();
            try {
                logstring += currentScope.toString(theValue);
            } catch (Throwable t){
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        value + " could not be converted to string", DebugInfo.getInstance());
                return;
            }
        }
        if (label.equals("debug")) {
            if (logger.isDebugEnabled())
                logger.debug(logstring);
        } else if (label.equals("info")) {
            if (logger.isInfoEnabled()) logger.info(logstring);
        }
        else if (label.equals("warn"))
            logger.warn(logstring);
        else if (label.equals("error"))
            logger.error(logstring);
        else if (label.equals("fatal"))
            logger.fatal(logstring);
        else if (logger.isInfoEnabled()) logger.info(label + ' ' + logstring);
    }

    public String arguments() {
        return "";
    }
}
