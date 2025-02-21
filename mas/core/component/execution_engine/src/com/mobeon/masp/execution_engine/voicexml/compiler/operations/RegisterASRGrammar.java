/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
/*
 * User: MPAZE
 * Date: 2006-apr-11
 * Time: 15:59:35
 */

public class RegisterASRGrammar extends  VXMLOperationBase {
    private GrammarScopeNode grammar;
    public RegisterASRGrammar(GrammarScopeNode grammar) {
        this.grammar = grammar;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.setASRGrammar(this.grammar);
    }

    public String arguments() {
        return grammar.toString();
    }
}
