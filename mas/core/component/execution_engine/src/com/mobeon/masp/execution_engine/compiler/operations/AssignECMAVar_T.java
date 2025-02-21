/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import org.apache.commons.lang.StringUtils;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Assigns a value from the stack to a name in the
 * current context.
 *
 * @author Patrick Zeits
 */
public class AssignECMAVar_T extends OperationBase {

    /**
     * what to assign, eg.e kalle or transition.kalle
     */
    private String name;

    /**
     * If not null, name is e.g. transition.kalle, and baseName is then kalle.
     * If null, name does not include dot.
     */
    private String baseName;
    /**
     * If not null, name is e.g. transition.kalle, and scopeName is then transition.
     * If null, name does not include dot.
     */
    private String scopeName;

    static ILogger logger = ILoggerFactory.getILogger(AssignECMAVar_T.class);


    public AssignECMAVar_T(String name) {
        this.name = name;
        int indexOfDot = name.lastIndexOf('.');
        if(indexOfDot != -1){
            this.baseName = name.substring(indexOfDot+1);
            this.scopeName = name.substring(0, indexOfDot);
        }
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        Object ecmaValue = stack.pop().toECMA(ex);
        Scope scope = ex.getCurrentScope();
        String varName = (baseName == null) ? name : baseName;
        if(! scope.isDeclaredInAnyScope(varName)){
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    name + " is not declared", DebugInfo.getInstance());
            return;
        }
        Object wrappedEcmaObject = scope.javaToJS(ecmaValue);
        if (wrappedEcmaObject != null) {
            if (logger.isInfoEnabled()) {
                //TR: HU98608 Debug logs in VVS which contains pin numbers of subscribers
                //obscure numbers like pin.  If needed for debug will need to print from VVA directly.
                String obscured_result = wrappedEcmaObject.toString();
                Double test = null;
                try {
                    test = Double.parseDouble(obscured_result);
                } catch (NumberFormatException nfe) {
                    //ignore
                }
                if (test != null) {
                    //This is a number so we need to obscure it.
                    obscured_result = "obscured:" + StringUtils.repeat("X", obscured_result.length());
                }  
                logger.info("Assigning the value " + obscured_result + " to the variable " + name);
            }
        } else {
            if (logger.isInfoEnabled()) logger.info("Assigning null to the variable " + name);
        }

        if(scopeName == null) {
            scope.setValue(name, wrappedEcmaObject);
        } else {
            scope.setValue(scopeName, baseName, wrappedEcmaObject);
            if(scope.lastEvaluationFailed()){
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        "Failure when trying to assign variable "+ name, DebugInfo.getInstance());
            }
        }
    }

    public String arguments() {
        return textArgument(name);
    }

}
