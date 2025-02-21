/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;
import com.mobeon.masp.execution_engine.voicexml.grammar.RuleRef;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.Map;
import java.util.HashMap;

public class RuleRefTest extends GrammarCase {

    public RuleRefTest(String name) {
        super(name);
    }

    public void testRuleRef() throws Exception {
        String test_string = "<ruleref uri='#test_uri' />";
        CompilerElement elem = readDocument(test_string);
        CompilerElement rule_elem = readDocument("<rule id='bb'> <item> 12 </item> </rule>");
        CompilerElement grammar_elem = readDocument("<grammar />");
        Map<String, Rule>  map = new HashMap<String, Rule>();
        Rule  r = new Rule(rule_elem, new DTMFGrammar(grammar_elem, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT));
        map.put("test_uri", r);

        RuleRef ref = new RuleRef(elem, map);

        assert(r == ref.getRule());


    }
}