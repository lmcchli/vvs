package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILoggerFactory;

public class EventReporter {
    public void reportEvent(ExecutionContext context,String event) {
       // context.getEventHub().fireContextEvent(event, DebugInfo.getInstance());
        throw new PlatformAccessException(event,null);
    }


    public void reportEvent(ExecutionContext context,String event, String message) {
        throw new PlatformAccessException(event,message);
    }

    public void reportEventWithMultiLineMessage(ExecutionContext context,String event) {
        throw new PlatformAccessException(event,"first line\nsecond line");
    }

    public void log(String logMsg) {
        ILoggerFactory.getILogger(EventReporter.class).debug(logMsg);
    }
}
