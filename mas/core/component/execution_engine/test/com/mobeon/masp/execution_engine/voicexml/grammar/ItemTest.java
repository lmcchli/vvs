/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.voicexml.grammar.Item;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.HashMap;

public class ItemTest extends GrammarCase {

    private String test_string = "<item>\n" +
            "    <item repeat='0-'>\n" +
            "        <item>7</item>\n" +
            "    </item>\n" +
            "    <one-of>\n" +
            "        <item>*</item>\n" +
            "        <item>* 0</item>\n" +
            "        <item>* *</item>\n" +
            "        <item>* 8</item>\n" +
            "    </one-of>\n" +
            "</item>";

    public ItemTest(String name) {
        super(name);
    }

    public void testRepeatAttrib() throws Exception {

        String test_string1 = " <item repeat='11-47'> 55 <item> 77 </item></item>";
        String test_string2 = " <item repeat='-11'> 55 </item>";
        String test_string3 = " <item repeat='47-'> 55 </item>";
        String test_string4 = " <item repeat='4711'> 55 </item>";
        String test_string5 = "<item repeat='0-'> 55 </item>";

        Item item1 = new Item(readDocument(test_string1), null);
        Item item2 = new Item(readDocument(test_string2), null);
        Item item3 = new Item(readDocument(test_string3), null);
        Item item4 = new Item(readDocument(test_string4), null);
        Item item5 = new Item(readDocument(test_string5), null);

        assertTrue(item1.getMin() == 11);
        assertTrue(item1.getMax() == 47);

        assertTrue(item2.getMin() == 0);
        assertTrue(item2.getMax() == 11);

        assertTrue(item3.getMin() == 47);
        assertTrue(item3.getMax() == Integer.MAX_VALUE);

        assertTrue(item4.getMin() == 4711);
        assertTrue(item4.getMax() == 4711);

        assertTrue(item5.getMin() == 0);
        assertTrue(item5.getMax() == Integer.MAX_VALUE);
    }

    public void testItemWithOneOf() throws Exception {
        CompilerElement element = readDocument(test_string);
        Item item = new Item(element, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;

        DTMF [] d1 = {DTMF.SEVEN};
        ms = new MatchState(d1);
        //assertTrue(item.match(ms) == Grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.SEVEN, DTMF.STAR};
        ms = new MatchState(d2);
        //mt = item.match(ms);
        //assertTrue(mt == Grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.SEVEN, DTMF.SEVEN};
        ms = new MatchState(d3);
        //assertTrue(item.match(ms) == Grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d4 = {DTMF.SEVEN, DTMF.SEVEN, DTMF.STAR};
        ms = new MatchState(d4);
        //assertTrue(item.match(ms) == Grammar.MatchType.MATCH);

        DTMF [] d5 = {DTMF.STAR, DTMF.NINE};
        ms = new MatchState(d5);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 1);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);  // TODO: returns full match can this break the parser?

        DTMF [] d6 = {DTMF.STAR, DTMF.EIGHT, DTMF.HASH};
        ms = new MatchState(d6);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 2);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

    }

    public void testSimpleMatch() throws Exception {

        CompilerElement element = readDocument("<item>* 0</item>");
        Item item = new Item(element, new HashMap<String, Rule>());
        MatchState ms;

        DTMF [] d1 = {DTMF.STAR};
        ms = new MatchState(d1);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.STAR, DTMF.ZERO};
        ms = new MatchState(d2);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.STAR, DTMF.ZERO, DTMF.SEVEN};
        ms = new MatchState(d3);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(ms.getCurrent() == 2);

        DTMF [] d4 = {DTMF.ZERO};
        ms = new MatchState(d4);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d5 = {DTMF.SIX};
        ms = new MatchState(d5);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);


    }

    public void testRepeat() throws Exception {
        String test_str = "<item repeat='1-3'>" +
                " <one-of>" +
                "  <item> 1</item>" +
                "  <item> 2</item>" +
                "  <item> 3</item>" +
                " </one-of>" +
                "</item>";
        CompilerElement element = readDocument(test_str);
        Item item = new Item(element, new HashMap<String, Rule>());
        MatchState ms;

        DTMF [] d1 = {DTMF.ONE};
        ms = new MatchState(d1);
       assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.ONE, DTMF.TWO};
        ms = new MatchState(d2);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.ONE, DTMF.TWO, DTMF.ONE};
        ms = new MatchState(d3);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.ONE, DTMF.TWO, DTMF.FOUR};
        ms = new MatchState(d4);
        MatchType mt = item.match(ms);
        assertTrue(ms.getCurrent() == 2);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d5 = {DTMF.ONE, DTMF.TWO, DTMF.ONE, DTMF.THREE, DTMF.ONE};
        ms = new MatchState(d5);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 3);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
    }

    public void testInfRepeat() throws Exception {
        String test_str = "<item repeat='0-'>" +
                "1" +
                "</item>";
        CompilerElement element = readDocument(test_str);
        Item item = new Item(element, new HashMap<String, Rule>());
        MatchState ms;

        DTMF [] d1 = {DTMF.ONE};
        ms = new MatchState(d1);
        MatchType mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.ONE, DTMF.ONE};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.ONE, DTMF.ONE, DTMF.ONE};
        ms = new MatchState(d3);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d4 = {DTMF.TWO};
        ms = new MatchState(d4);
        mt = item.match(ms);
        assertTrue(ms.getLastMatchWasEmpty());
        assertTrue(ms.getCurrent() == 0);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d5 = {DTMF.ONE, DTMF.ONE, DTMF.ONE, DTMF.TWO};
        ms = new MatchState(d5);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 3);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);


    }

    public void testMixedItem() throws Exception {
        String test_str =
                "<item>\n" +
                        "  <item repeat=\"4\">\n" +
                        "        <one-of>" +
                        "            <item> 1</item>" +
                        "            <item> 2</item>" +
                        "            <item> 3</item>" +
                        "        </one-of>" +
                        "   </item>" +
                        "   #\n" +
                        "</item>";

        CompilerElement element = readDocument(test_str);
        Item item = new Item(element, new HashMap<String, Rule>());
        DTMF [] tokens = {DTMF.ONE, DTMF.TWO, DTMF.THREE, DTMF.ONE, DTMF.HASH};
        MatchState ms = new MatchState(tokens);

        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d1 = {DTMF.ONE};
        ms = new MatchState(d1);
        MatchType mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.ONE, DTMF.TWO, DTMF.THREE, DTMF.ONE, DTMF.HASH, DTMF.ONE};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 5);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);


    }

    public void testBackTrack() throws Exception {
        String test_str = "<item repeat='2-3'>" +
                "<item>A</item>" +
                "<item>B</item>" +
                "</item>";
        CompilerElement element = readDocument(test_str);
        Item item = new Item(element, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;

        DTMF [] d1 = {DTMF.A, DTMF.B, DTMF.A, DTMF.B};
        uniqeDTMFTest(d1, item);

        DTMF [] d2 = {DTMF.A, DTMF.B, DTMF.A, DTMF.B, DTMF.A};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d3 = {DTMF.A, DTMF.B, DTMF.A, DTMF.B, DTMF.A, DTMF.B, DTMF.SEVEN};
        ms = new MatchState(d3);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 6);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.A};
        ms = new MatchState(d4);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);


    }

    public void testInfItemAndOneOf() throws Exception {
        String test_str = "    <item>\n" +
                "        <item repeat=\"0-\">\n" +
                "            <item>7</item>\n" +
                "        </item>\n" +
                "        <one-of>\n" +
                "            <item>*</item>\n" +
                "            <item>* 0</item>\n" +
                "            <item>* *</item>\n" +
                "            <item>* 8</item>\n" +
                "        </one-of>\n" +
                "    </item>\n";

        CompilerElement node = readDocument(test_str);
        MatchState ms;
        MatchType mt;

        Item item = new Item(node, new HashMap<String, Rule>());

        DTMF [] d1 = {DTMF.STAR, DTMF.THREE};
        ms = new MatchState(d1);
        //    mt = item.match(ms);
        //      assertTrue(ms.getCurrent() == 1);
//        assertTrue(mt == Grammar.MatchType.FULL_MATCH);

        DTMF [] d2 = {DTMF.SEVEN, DTMF.SEVEN, DTMF.SEVEN, DTMF.STAR, DTMF.STAR};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.SEVEN, DTMF.SEVEN, DTMF.SEVEN, DTMF.STAR};
        ms = new MatchState(d3);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

    }


    public void testSimplePartialMatch() throws Exception {
        String test_str = "<item>" +
                "<item>A</item>" +
                "<item>B</item>" +
                "</item>";

        CompilerElement node = readDocument(test_str);
        MatchState ms;
        MatchType mt;
        Item item = new Item(node, new HashMap<String, Rule>());

        DTMF [] d1 = {DTMF.A};
        ms = new MatchState(d1);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.A, DTMF.B};
        ms = new MatchState(d2);
        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.A, DTMF.B, DTMF.ONE};
        ms = new MatchState(d3);

        assertTrue(item.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(ms.getCurrent() == 2);

    }

    public void testFullMatch() throws Exception {
        String test_str = "<item repeat='2-3'> A </item>";


        CompilerElement node = readDocument(test_str);
        MatchState ms;
        MatchType mt;
        Item item = new Item(node, new HashMap<String, Rule>());

        DTMF [] d1 = {DTMF.A};
        ms = new MatchState(d1);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.A, DTMF.A};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.A, DTMF.A, DTMF.A};
        ms = new MatchState(d3);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d4 = {DTMF.A, DTMF.A, DTMF.A, DTMF.B};
        ms = new MatchState(d4);
        mt = item.match(ms);
        assertTrue(ms.getCurrent() == 3);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

    }

    public void testFullMatch2() throws Exception {
        String test_str = "<item>A B</item>";
        CompilerElement node = readDocument(test_str);
        Item item = new Item(node, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;

        DTMF [] d1 = {DTMF.A};
        ms = new MatchState(d1);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);
        assertTrue(ms.getCurrent() == 1);

        DTMF [] d2 = {DTMF.A, DTMF.B};
        ms = new MatchState(d2);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);


    }

    public void testComplexFullMatch() throws Exception {
        String test_str = "<item>\n" +
                "   <one-of>\n" +
                "       <item>1</item>\n" +
                "       <item>11</item>\n" +
                "   </one-of>\n" +
                "   <one-of>\n" +
                "       <item>2</item>\n" +
                "       <item>22</item>\n" +
                "   </one-of>\n" +
                "   <one-of>\n" +
                "       <item>3</item>\n" +
                "       <item>33</item>\n" +
                "   </one-of>\n" +
                "</item>";

        CompilerElement node = readDocument(test_str);
        Item item = new Item(node, new HashMap<String, Rule>());
        MatchState ms;
        MatchType mt;
        DTMF [] d;


        d = stringToDtmf("123");
        ms = new MatchState(d);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        d = stringToDtmf("1233");
        ms = new MatchState(d);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        d = stringToDtmf("11223");
        ms = new MatchState(d);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        d = stringToDtmf("112");
        ms = new MatchState(d);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        d = stringToDtmf("12");
        ms = new MatchState(d);
        mt = item.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);
    }

    

}