/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
/*
 * User: MPAZE
 * Date: 2005-dec-01
 * Time: 11:34:59
 */

public class RegisterDTMFGrammar extends VXMLOperationBase {
    private GrammarScopeNode grammar;
    public RegisterDTMFGrammar(GrammarScopeNode grammar) {
        this.grammar = grammar;
    }

    public void execute(VXMLExecutionContext context) throws InterruptedException {
        if(grammar != null) {
         //((VXMLExecutionContext) context).setDTMFGrammar(predicate.getDTMFGrammar());
          context.setDTMFGrammar(grammar);
        }
    }

    public String arguments() {
        return grammar.toString();
    }


}
