/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.ecma;

import com.mobeon.masp.util.Tools;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.*;

import java.net.URI;

public class WatchdogContextFactory extends ContextFactory {
    private int TIMEOUT = 1;
    private long LOG_INTERVAL = 1000; // millisecs

    private static ILogger log = ILoggerFactory.getILogger(WatchdogContextFactory.class);
    private static final int INTSRUCTION_THRESHOLD = 1000;

    public static void setLocation(Context context, URI uri, int lineNumber) {
        if(context instanceof WatchdogContext){
            WatchdogContext wc = (WatchdogContext) context;
            wc.setURILocation(uri, lineNumber);
        }
    }

    public static void setExpression(Context context, String expression) {
        if(context instanceof WatchdogContext){
            WatchdogContext wc = (WatchdogContext) context;
            wc.setExpression(expression);
        }
    }

    // Custom Context to store execution time.
    public static class WatchdogContext extends Context {
        long startTime;
        long timeOfLastLog;

        // Either uri and LineNumber are set, or expression is set
        private URI uri;
        private int lineNumber;
        String expression;

        public void setExpression(String expression) {
            this.expression = expression;
            uri = null;
            lineNumber = 0;
        }

        public void setURILocation(URI uri, int lineNumber){
            this.uri = uri;
            this.lineNumber = lineNumber;
            expression = null;
        }

        public String getLocation() {
            if(uri != null){
                return "<"+uri.getPath()+" line "+lineNumber+">";
            } else {
                return expression;
            }
        }
    }

    /**
     * @logs.warn "ECMAScript problem detected: <message>" - There was a problem with ECMA script execution, for example that the execution took unexpectedly long time. <message> should give more information about the problem.
     */
    public static class NastyErrorReporter implements ErrorReporter {
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            log.warn(
                    "ECMAScript problem detected:", ScriptRuntime.constructError(
                    "Warning", message, sourceName, line, lineSource, lineOffset));
        }

        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        }

        public EvaluatorException runtimeError(
                String message, String sourceName, int line, String lineSource, int lineOffset) {
            return new EvaluatorException(
                    message, sourceName, line, lineSource, lineOffset);
        }
    }

    // Override makeContext()
    protected Context makeContext() {
        WatchdogContext cx = new WatchdogContext();

        // Use pure interpreter mode to allow for
        // observeInstructionCount(Context, int) to work,
        // and use continuations and tail call elimination
        // too.
        cx.setOptimizationLevel(-2);
        // Make Rhino runtime to call observeInstructionCount
        // each 1000 bytecode instructions
        cx.setInstructionObserverThreshold(INTSRUCTION_THRESHOLD);
        cx.setErrorReporter(new NastyErrorReporter());
        return cx;
    }

    protected boolean hasFeature(Context cx, int featureIndex) {
        switch (featureIndex) {
            case Context.FEATURE_STRICT_VARS:
            case Context.FEATURE_STRICT_EVAL:
            case Context.FEATURE_DYNAMIC_SCOPE:
                return true;
        }
        return super.hasFeature(cx, featureIndex);

    }

    // Override observeInstructionCount(Context, int)
    /**
     * @param cx
     * @param instructionCount
     */
    protected void observeInstructionCount(Context cx, int instructionCount) {
        WatchdogContext mcx = (WatchdogContext) cx;
        if (Thread.currentThread().isInterrupted()) {
            throw new Error("Evaluator thread interrupted");
        }
        long currentTime = System.currentTimeMillis();
        if (mcx.startTime != 0) {
            long delta = currentTime - mcx.startTime;
            long l = Tools.secondsToMillis(TIMEOUT);
            if (delta > l) {
                // More then timeout seconds from Context creation time:
                // it is time to stop the script.
                // Throw Error instance to ensure that script will never
                // get control back through catch or finally.

                // Log first time and then max once per "log interval"
                if(mcx.timeOfLastLog == 0 || currentTime -mcx.timeOfLastLog > LOG_INTERVAL){
                    mcx.timeOfLastLog = currentTime;
                    if(log.isInfoEnabled())
                        log.info("ECMA Script "+ mcx.getLocation() + " has been running for " + delta + " milliseconds");
                }
            }
        }
    }

    private void checkInterrupted() throws Exception{

    }

    // Override doTopCall(Callable, Context, Scriptable scope, Scriptable thisObj, Object[] args)
    protected Object doTopCall(
            Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        WatchdogContext mcx = (WatchdogContext) cx;
        mcx.startTime = System.currentTimeMillis();
        mcx.timeOfLastLog = 0;

        return super.doTopCall(callable, cx, scope, thisObj, args);
    }

    protected void onContextCreated(Context context) {
        super.onContextCreated(context);
    }

    protected void onContextReleased(Context context) {
        super.onContextReleased(context);
    }
}

