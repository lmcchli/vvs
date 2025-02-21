/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Control whether event processing is enabled or not.
 *
 * @author Mikael Andersson
 */
public class SetEventsEnabled extends OperationBase {
    private boolean enabled;
    private ILogger log = ILoggerFactory.getILogger(SetEventsEnabled.class);

    public SetEventsEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String arguments() {
        return String.valueOf(enabled);
    }

    public final void execute(ExecutionContext ex) {
        if(ex.getEventProcessor().isEnabled() == enabled) {
            if(log.isInfoEnabled())
                log.info("Attempt to set event processor state to value same as current value ("+enabled+"), ignored");
        } else {
            ex.getEventProcessor().setEnabled(enabled);
        }
    }
}
