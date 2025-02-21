/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.voicexml.grammar.OneOf;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.HashMap;

public class OneOfTest extends GrammarCase {
    OneOf oneOf;

    String test_string = "<one-of>\n" +
            "    <item repeat=\"0-\">\n" +
            "        <item>7</item>\n" +
            "    </item>\n" +
            "    <item>\n" +
            "        <item repeat=\"0-\">\n" +
            "            <item>7</item>\n" +
            "        </item>\n" +
            "        <one-of>\n" +
            "            <item>*</item>\n" +
            "            <item>* 0</item>\n" +
            "            <item>* *</item>\n" +
            "            <item>* 8</item>\n" +
            "        </one-of>\n" +
            "    </item>\n" +
            "</one-of>";

    public OneOfTest(String name) {
        super(name);
    }

    public void testPartialMatch() throws Exception {
        CompilerElement element = readDocument(test_string);

        oneOf = new OneOf(element, new HashMap<String, Rule>());
        DTMF [] hit1 = {DTMF.STAR, DTMF.EIGHT};
        DTMF [] miss1 = {DTMF.STAR, DTMF.NINE, DTMF.SEVEN};
        MatchType mt;

        MatchState ms = new MatchState(hit1);
        assertTrue("*8 does not match", oneOf.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        ms = new MatchState(miss1);
        mt = oneOf.match(ms);
        assertTrue("*97 does match", mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(ms.getCurrent() == 1);

        DTMF [] d2 = {DTMF.STAR, DTMF.EIGHT, DTMF.SEVEN};
        ms = new MatchState(d2);
        mt = oneOf.match(ms);
        assertTrue(ms.getCurrent() == 2);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.STAR, DTMF.SEVEN};
        ms = new MatchState(d3);
        mt = oneOf.match(ms);
        assertTrue(ms.getCurrent() == 1);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);


    }

    public void testSimpleOneOf() throws Exception {
        String test_str =
                "<one-of>\n" +
                        "   <item>*</item>\n" +
                        "   <item>* 0</item>\n" +
                        "   <item>* *</item>\n" +
                        "</one-of>";


        // a full match. Does the condition in Rule fix this?
        CompilerElement element = readDocument(test_str);

        oneOf = new OneOf(element, new HashMap<String, Rule>());

        DTMF [] d1 = {DTMF.STAR, DTMF.SEVEN};
        MatchState ms = new MatchState(d1);
        assertTrue(oneOf.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(ms.getCurrent() == 1);

        DTMF [] d2 = {DTMF.STAR};
        ms = new MatchState(d2);
        assertTrue(oneOf.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.STAR, DTMF.STAR, DTMF.SEVEN};
        ms = new MatchState(d3);
        MatchType mt = oneOf.match(ms);
        assertTrue(ms.getCurrent() == 2);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.ONE};
        ms = new MatchState(d4);
        assertTrue(oneOf.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d5 = {DTMF.SEVEN, DTMF.ONE};
        ms = new MatchState(d5);
        assertTrue(oneOf.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d6 = {DTMF.STAR, DTMF.SEVEN};
        ms = new MatchState(d6);
        mt = oneOf.match(ms);
        assertTrue(ms.getCurrent() == 1);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d7 = {DTMF.STAR, DTMF.STAR};
        ms = new MatchState(d7);
        mt = oneOf.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);


    }

    public void testFullMatch() throws Exception {
        String test_string = "<one-of>" +
                "   <item>A</item>" +
                "   <item>AB</item>" +
                "</one-of>";

        CompilerElement node = readDocument(test_string);
        OneOf oneof = new OneOf(node, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;

        DTMF [] d1 = {DTMF.A};
        ms = new MatchState(d1);
        mt = oneof.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.A, DTMF.B};
        ms = new MatchState(d2);
        mt = oneof.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.B};
        ms = new MatchState(d3);
        mt = oneof.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);


    }

    public void testTR27214() throws Exception {
        String test_str = "<one-of>" +
                "   <item repeat='0-'>7</item>" +
                "   <item>779</item>" +
                "</one-of>";

        CompilerElement node = readDocument(test_str);
        OneOf oneof = new OneOf(node, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;
        DTMF [] d = stringToDtmf("779");
        assertTrue(oneof.match(new MatchState(d)) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
    }
}