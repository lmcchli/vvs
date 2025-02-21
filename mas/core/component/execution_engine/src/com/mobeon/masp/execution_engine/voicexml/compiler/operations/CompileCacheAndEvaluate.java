/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeImpl;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.Script;

import java.net.URI;

/**
 * @author David Looberger
 */
public class CompileCacheAndEvaluate extends OperationBase {
    private String script = null;
    private String cacheKey = null;
    private static final ILogger logger = ILoggerFactory.getILogger(CompileCacheAndEvaluate.class);
    private URI uri;
    private int lineNumber;

    public CompileCacheAndEvaluate(String inlineScript, String cacheKey, URI uri, int lineNumber) {
        super();
        this.script = inlineScript;
        this.cacheKey = cacheKey;
        this.uri = uri;
        this.lineNumber = lineNumber;
    }

    /**
     * Failed to compile and execute the script - The ECMA script could not be compiled, probably due to a syntax error.
     * @param ex
     * @throws InterruptedException
     */
    public void execute(ExecutionContext ex) throws InterruptedException {
        Script cachedScript = ScopeImpl.getScriptFromCache(cacheKey);
        Scope currentScope = ex.getCurrentScope();

        if (cachedScript != null) {
            evaluate(ex, currentScope, cachedScript);
            return;
        }

        Script compiledScript = ex.getCurrentScope().compileAndCache(script, cacheKey);
        // Descard any object returned from the execution of the script
        if (compiledScript != null) {
            evaluate(ex, currentScope, compiledScript);
        } else {
            logger.error("Failed to compile and execute the script :" + script);
            errorSemantic(ex);
        }
    }

    private void evaluate(ExecutionContext ex, Scope currentScope, Script compiledScript) {

        // Descard any object returned from the execution of the compiledScript
            Object result = currentScope.exec(compiledScript, uri, lineNumber);
            if (currentScope.lastEvaluationFailed()) {
                errorSemantic(ex);
            } else {
                if (logger.isDebugEnabled()) {
                    // Do not remove the '' around "result" since some basic
                    // test cases parsing the log file will break then...
                    if (logger.isDebugEnabled()) logger.debug("Evaluation of '" + script + "' resulted in '" + result
                                                              + "'");
                }
                ex.getValueStack().push(new ECMAObjectValue(result));
            }
    }

     private void errorSemantic(ExecutionContext ex) {
         String msg = "Evaluation of " + script + " failed";
         if (logger.isDebugEnabled()) logger.debug(msg);
         ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                 msg, DebugInfo.getInstance());
     }

    public String arguments() {
        return cacheKey;
    }
}
