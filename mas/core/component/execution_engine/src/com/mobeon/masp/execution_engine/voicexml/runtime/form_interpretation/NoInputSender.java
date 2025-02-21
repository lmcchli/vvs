package com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.TimeValue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-18
 * Time: 11:16:21
 * To change this template use File | Settings | File Templates.
 */
public class NoInputSender implements Runnable {
    private ScheduledFuture<?> ongoingTimeout;
    private final static ScheduledExecutorService timeOutscheduler = Executors.newScheduledThreadPool(1);
    private ILogger log = ILoggerFactory.getILogger(getClass());
    private boolean isLocked = false;
    private String fieldId;

    public NoInputSender(ExecutionContextImpl context) {
        this.context = context;
    }

    private final ExecutionContextImpl context;

    public void run() {
        context.getSession().registerSessionInLogger();  // to get sessionID correct in log file
        context.getVoiceXMLEventHub().fireNoInputEvent("No input event generated",fieldId, DebugInfo.getInstance());
    }

    public void cancelAndLock() {
        if (ongoingTimeout != null) {
            // Allow ongoing tasks to complete
            ongoingTimeout.cancel(false);
        }
        fieldId = null;
        isLocked = true;
    }

    public void start(TimeValue timeout, String fieldId) {
        this.fieldId = fieldId;
        if (isLocked) {
            log.info("Attempted to start no-input, but no-input timer has already been cancelled !");
            return;
        }
        if (TestEventGenerator.isActive()) {
            TestEventGenerator.generateEvent(TestEvent.NOINPUT_STARTING, timeout);
        }
        if (ongoingTimeout != null) {
            ongoingTimeout.cancel(false);
        }
        if (fieldId != null) {
            ongoingTimeout = timeOutscheduler.schedule(this, timeout.getTime(), timeout.getUnit());
        } else {
            log.error("<CHECKOUT>Undefined form interpretation state when starting no-input timer");
        }
    }

    public void unlock() {
        isLocked = false;
    }
}
