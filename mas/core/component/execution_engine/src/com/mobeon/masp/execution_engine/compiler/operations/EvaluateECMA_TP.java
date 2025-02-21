/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/** Retrieve an ECMA expression from the ValueStack, evaluate it and place the result on the ValueStack
 *
 * @author David Looberger
 */
public class EvaluateECMA_TP extends OperationBase {

    static ILogger logger = ILoggerFactory.getILogger(EvaluateECMA_TP.class);

    public void execute(ExecutionContext ex) throws InterruptedException {
        Value value = ex.getValueStack().pop();
        String scriptString = value.toString();
        // Evaluate the script string and push the result onto the valuestack.
        Object result = ex.getCurrentScope().evaluate(scriptString);
        ex.getValueStack().push(new ECMAObjectValue(result));
    }

    public String arguments() {
        return classToMnemonic(getClass());
    }
}
