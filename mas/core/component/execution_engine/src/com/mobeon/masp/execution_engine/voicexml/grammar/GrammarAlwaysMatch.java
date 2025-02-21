/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.xml.CompilerElement;

public class GrammarAlwaysMatch extends DTMFGrammar {


    public GrammarAlwaysMatch(CompilerElement node, SCOPE scope) {
        super(node, scope);
    }

    public MatchType match(MatchState ms) {
         ms.consumeAll();
        return MatchType.FULL_MATCH;

    }
    public String toString() {
        return "GrammarAlwaysMatch";
    }

    public String getGrammar_id() {
        return "GrammarAlwaysMatch";
    }

}
