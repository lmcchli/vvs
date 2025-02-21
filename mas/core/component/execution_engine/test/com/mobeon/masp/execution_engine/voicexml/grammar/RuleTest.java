/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.xml.CompilerElement;

public class RuleTest extends GrammarCase {
    private Rule rule;


    public RuleTest(String name) {
        super(name);
    }

    private void init(String test_str) {

        CompilerElement element = readDocument(test_str);
        CompilerElement g = readDocument("<grammar />");
        rule = new Rule(element, new DTMFGrammar(g, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT));
    }

    public void testSimpleOneOf() throws Exception {
        String test_src = "<rule id='thename'><one-of>" +
                "<item>1</item>" +
                "<item>2 2</item>" +
                "<item>#</item>" +
                "<item>123</item>" +
                "</one-of></rule>";

        init(test_src);

        DTMF [] d1 = {DTMF.ONE};
        MatchState ms = new MatchState(d1);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.TWO};
        ms = new MatchState(d2);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d3 = {DTMF.ONE, DTMF.TWO, DTMF.THREE};
        ms = new MatchState(d3);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.HASH};
        ms = new MatchState(d4);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d5 = {DTMF.TWO, DTMF.TWO};
        ms = new MatchState(d5);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d6 = {DTMF.SEVEN, DTMF.TWO};
        ms = new MatchState(d6);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d7 = {DTMF.SEVEN};
        ms = new MatchState(d7);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
    }

    public void testRepeat() throws Exception {
        String test_str = "<rule><item repeat='1-3'>" +
                " <one-of>" +
                "  <item> 1</item>" +
                "  <item> 2</item>" +
                "  <item> 3</item>" +
                " </one-of>" +
                "</item>" +
                "</rule>";
        init(test_str);
        MatchState ms;
        DTMF [] d1 = {DTMF.ONE};
        ms = new MatchState(d1);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.ONE, DTMF.TWO};
        ms = new MatchState(d2);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.ONE, DTMF.TWO, DTMF.ONE};
        ms = new MatchState(d3);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.ONE, DTMF.TWO, DTMF.FOUR};
        ms = new MatchState(d4);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d5 = {DTMF.ONE, DTMF.TWO, DTMF.ONE, DTMF.THREE};
        ms = new MatchState(d5);
        assertTrue(rule.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
    }

    public void testOneof() throws Exception {
        String test_str =
                "<rule id='tada'>\n" +
                        "   <item>\n" +
                        "       <one-of>\n" +
                        "           <item>*</item>\n" +
                        "           <item>* 0</item>\n" +
                        "           <item>* *</item>\n" +
                        "           <item>* 8</item>\n" +
                        "      </one-of>\n" +
                        "      9\n" +
                        "   </item>\n" +
                        "</rule>";
        MatchState ms;
        MatchType mt;
        DTMF [] d;
        init(test_str);

        d = stringToDtmf("*9");
        ms = new MatchState(d);
        mt = rule.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        d = stringToDtmf("*8");
        ms = new MatchState(d);
        mt = rule.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        d = stringToDtmf("*86");
        ms = new MatchState(d);
        mt = rule.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        d = stringToDtmf("*96");
        ms = new MatchState(d);
        mt = rule.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);


    }
}