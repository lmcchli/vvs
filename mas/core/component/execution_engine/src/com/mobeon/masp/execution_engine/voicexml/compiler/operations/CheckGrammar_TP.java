/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.Value;

/**  The operations retrieves user input, in the form of a String, from the
 * value stack, and checks it against the registered grammars. If a hit is scored, the boolean
 * value TRUE is placed on the value stack, FALSE otherwise.
 *
 * @author David Looberger
 */
public class CheckGrammar_TP extends VXMLOperationBase   {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack value_stack = ex.getValueStack();
        Value value = value_stack.pop();
        String strValue = value.toString();

    }

    public String arguments() {
        return "";
    }
}
