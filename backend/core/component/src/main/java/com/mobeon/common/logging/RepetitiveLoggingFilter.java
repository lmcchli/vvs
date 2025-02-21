/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.logging;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Håkan Stolt
 */
public class RepetitiveLoggingFilter extends Filter {

    /**
     *
     */
    private static Set<LogContext> logContextRegister = Collections.synchronizedSet(new HashSet<LogContext>());

    public int decide(LoggingEvent loggingEvent) {
        if (loggingEvent.getMessage() instanceof LogJustOnceMessage) {
            LogJustOnceMessage message = (LogJustOnceMessage) loggingEvent.getMessage();
            LogContext logContext = message.getLogContext();
            if (message.getTriggReset()) {
                boolean resetExecuted = logContextRegister.remove(logContext);
                for (LogContext c : logContextRegister) {
                    if (logContext.implies(c)) {
                        resetExecuted = true;
                        logContextRegister.remove(c);
                    }
                }
                if (resetExecuted) {
                    return ACCEPT;
                }
            } else if (!logContextRegister.contains(logContext)) {
                boolean alreadyImplied = false;
                for (LogContext c : logContextRegister) {
                    if (c.implies(logContext)) {
                        alreadyImplied = true;
                    }
                }
                logContextRegister.add(logContext);
                if (!alreadyImplied) {
                    return ACCEPT;
                }
            }
            return DENY;
        }
        return NEUTRAL;
    }


}
