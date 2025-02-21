/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class CollectPhaseDeterminePromptsFIA extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(CollectPhaseDeterminePromptsFIA.class);


    private String formName;

    public CollectPhaseDeterminePromptsFIA(String id) {
        super();
        this.formName = id;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();
        fiaState.setPhase(FIAState.Phase.Collect);
        ex.setTransitioningState();

        FIAState.NextItem nextItem = fiaState.getNextItem();
        if (nextItem.name != null) {
            // Get/Queue the prompts for playing. The prompts will be played when
            // the context enters the STATE_WAITING state.
            fiaState.queuePrompts(ex);
            // Register grammars if such exist
            GrammarScopeNode grammar = nextItem.product.getGrammar();
            if (grammar != null) {
                if (logger.isDebugEnabled()) logger.debug("Setting grammar for " + nextItem.name);
                ex.setDTMFGrammar(grammar);
            }


        }        
    }

    public String arguments() {
        return formName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
