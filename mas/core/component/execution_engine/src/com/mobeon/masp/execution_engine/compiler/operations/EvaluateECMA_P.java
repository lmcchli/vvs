/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeImpl;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Script;

import java.net.URI;

/**
 * @author Mikael Andersson
 */
public class EvaluateECMA_P extends OperationBase {

    private final String scriptString;
    Script script;
    URI uri;
    int lineNumber;
    private CompilerElement element;

    static ILogger logger = ILoggerFactory.getILogger(EvaluateECMA_P.class);

    public EvaluateECMA_P(String scriptString, URI uri, int lineNumber) {
        this.scriptString = scriptString;
        this.script = ScopeImpl.compile(scriptString, uri, lineNumber);
        this.uri = uri;
        this.lineNumber = lineNumber;
    }

    public EvaluateECMA_P(CompilerElement element, String scriptString, URI uri, int lineNumber) {
        this(scriptString, uri, lineNumber);
        this.element = element;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {

        if(script == null){
            errorSemantic(ex, "Can not execute script: "+scriptString+", since it could not be compiled");
            return;
        }
        // Evaluate the script string and push the result onto the valuestack.
        Scope currentScope = ex.getCurrentScope();
        Object result = currentScope.exec(script, scriptString);        
        if (currentScope.lastEvaluationFailed()) {
            errorSemantic(ex,  "Evaluation of " + scriptString + " failed");
        } else {
            if (logger.isInfoEnabled()) {
                //TR: HU98608 Debug logs in VVS which contains pin numbers of subscribers
                //check if a number and if so obscure it so we do not display pin number or any other
                //number.  Unfortunately there is no way to know what this is for so we have to obscure all numbers
                //If the call flow needs to debug it, it will have to print the result directly.
            	if (result == null) {
                    // Do not remove the '' around "result" since some basic
                    // test cases parsing the log file will break then...
                    logger.info("Evaluation of '" + scriptString + "' resulted in 'null'");
            	} else {
                    String obscured_result = result.toString();
                    if (result instanceof String) {
                        String testString=(String)result;
                        Double test = null;
                        try {
                            test = Double.parseDouble(testString);
                        } catch (NumberFormatException nfe) {
                            //ignore
                        }
                        if (test != null) {
                            //This is a number so we need to obscure it.
                            obscured_result = "obscured:" + StringUtils.repeat("X", testString.length());
                        } 

                    }
                    // Do not remove the '' around "result" since some basic
                    // test cases parsing the log file will break then...
                    logger.info("Evaluation of '" + scriptString + "' resulted in '" + obscured_result  + "'");
            	}
            }
        	ex.getValueStack().push(new ECMAObjectValue(result));
        }
    }

    private void errorSemantic(ExecutionContext ex, String message) {
        if (logger.isDebugEnabled()) logger.debug(message);
        DebugInfo instance;
        if(element != null){
            instance = DebugInfo.getInstance(element);
        } else {
            instance = DebugInfo.getInstance();
        }
        ex.getEventHub().fireContextEventWithLocationInfo(Constants.Event.ERROR_SEMANTIC,
                message, instance);
    }

    public String arguments() {
        return textArgument(scriptString);
    }
}
