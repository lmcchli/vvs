/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.xml.CompilerElement;

public class GrammarTest extends GrammarCase {
    private DTMFGrammar grammar;
    private String test_str = "<grammar mode='dtmf' root='thename'> " +
            "<rule id='thename'><one-of>" +
            "<item>1</item>" +
            "<item>22</item>" +
            "<item>1 2 3</item>" +
            "<item>#</item>" +
            "</one-of></rule></grammar>";

    private String test_str_ruleref = "<grammar mode=\"dtmf\" version=\"1.0\" root=\"pin\">\n" +
            "            <rule id=\"digit\">\n" +
            "                <one-of>\n" +
            "                    <item>0</item>\n" +
            "                    <item>1</item>\n" +
            "                    <item>2</item>\n" +
            "                    <item>3</item>\n" +
            "                    <item>4</item>\n" +
            "                    <item>5</item>\n" +
            "                    <item>6</item>\n" +
            "                    <item>7</item>\n" +
            "                    <item>8</item>\n" +
            "                    <item>9</item>\n" +
            "                </one-of>\n" +
            "            </rule>\n" +
            "            <rule id=\"pin\" scope=\"public\">\n" +
            "                <one-of>\n" +
            "                    <item>\n" +
            "                        <item repeat=\"4\">\n" +
            "                            <ruleref uri=\"#digit\"/>\n" +
            "                        </item>\n" +
            "                        #\n" +
            "                    </item>\n" +
            "                    <item>\n" +
            "                         * 9\n" +
            "                    </item>\n" +
            "                </one-of>\n" +
            "            </rule>\n" +
            "        </grammar>";


    private String test_str_getpin = " <grammar version=\"1.0\" mode=\"dtmf\" root=\"gd1_main_rule\">\n" +
            "<rule id=\"gd1_main_rule2\">\n" +
            "        <one-of>\n" +
            "            <item>0</item>\n" +
            "            <item>1</item>\n" +
            "            <item>2</item>\n" +
            "            <item>3</item>\n" +
            "            <item>4</item>\n" +
            "            <item>5</item>\n" +
            "            <item>6</item>\n" +
            "            <item>7</item>\n" +
            "            <item>8</item>\n" +
            "            <item>9</item>\n" +
            "        </one-of>\n" +
            "    </rule>\n" +
            "    <rule id=\"gd1_main_rule\" scope=\"public\">\n" +
            "        <one-of>\n" +
            "            <item repeat=\"0-\">\n" +
            "                <ruleref uri=\"#gd1_main_rule2\"/>\n" +
            "            </item>\n" +
            "            <item>\n" +
            "                <item repeat=\"0-\">\n" +
            "                    <ruleref uri=\"#gd1_main_rule2\"/>\n" +
            "                </item>\n" +
            "                <one-of>\n" +
            "                    <item>*</item>\n" +
            "                    <item>* 0</item>\n" +
            "                    <item>* *</item>\n" +
            "                    <item>* 8</item>\n" +
            "                </one-of>\n" +
            "            </item>\n" +
            "        </one-of>\n" +
            "    </rule>\n" +
            "</grammar>";


    public GrammarTest(String name) {
        super(name);
    }


    public void testHit() throws Exception {
        CompilerElement element = readDocument(test_str);
        grammar = new DTMFGrammar(element, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.LOCAL);

    }

    public void testRuleRef() throws Exception {

        CompilerElement element = readDocument(test_str_ruleref);
        grammar = new DTMFGrammar(element, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.LOCAL);



        MatchState ms;
        MatchType mt;


        DTMF [] tokens = {DTMF.FOUR, DTMF.SEVEN, DTMF.ONE, DTMF.ONE, DTMF.HASH};
        ms = new MatchState(tokens);
        mt = grammar.match(ms);
        log.debug("Match is:" + mt);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);


        DTMF [] bad_tokens = {DTMF.SEVEN, DTMF.EIGHT, DTMF.ONE, DTMF.THREE};
        ms = new MatchState(bad_tokens);
        mt = grammar.match(ms);
        log.debug("Match is:" + mt);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);


        DTMF [] star_nine = {DTMF.STAR, DTMF.NINE};
        ms = new MatchState(star_nine);
        mt = grammar.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        log.debug("Match is:" + mt);

        DTMF [] to_many_tokens = {DTMF.FOUR, DTMF.SEVEN, DTMF.ONE, DTMF.ONE, DTMF.ONE, DTMF.HASH};
        ms = new MatchState(to_many_tokens);
        mt = grammar.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
        ;

        DTMF [] only_two = {DTMF.ONE, DTMF.NINE};
        ms = new MatchState(only_two);
        mt = grammar.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);
        log.debug("Match is:" + mt);

        DTMF [] bad_star_nine = {DTMF.STAR, DTMF.ONE};
        ms = new MatchState(bad_star_nine);
        mt = grammar.match(ms);
        log.debug("Match is:" + mt);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
        log.debug("Match is:" + mt);

        DTMF [] partof_star_nine = {DTMF.STAR};
        ms = new MatchState(partof_star_nine);
        mt = grammar.match(ms);
        log.debug("Match is :" + mt);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] bad_hash = {DTMF.HASH};
        ms = new MatchState(bad_hash);
        mt = grammar.match(ms);
        log.debug("Match is :" + mt);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
    }

    public void testGetPin() throws Exception {
        CompilerElement element = readDocument(test_str_getpin);
        grammar = new DTMFGrammar(element, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.LOCAL);

        DTMF [] tokens1 = {DTMF.FOUR, DTMF.SEVEN, DTMF.ONE, DTMF.ONE};
        DTMF [] tokens2 = {DTMF.FOUR, DTMF.SEVEN};
        DTMF [] tokens3 = {DTMF.STAR, DTMF.EIGHT};
        DTMF [] tokens4 = {DTMF.STAR, DTMF.STAR};
        DTMF [] tokens5 = {DTMF.STAR};

        DTMF [] tokenss6 = {DTMF.EIGHT, DTMF.STAR};
        DTMF [] bad_tokens2 = {DTMF.STAR, DTMF.SIX, DTMF.SEVEN};

        MatchState match1 = new MatchState(tokens1);
        MatchState match2 = new MatchState(tokens2);
        MatchState match3 = new MatchState(tokens3);
        MatchState match4 = new MatchState(tokens4);
        MatchState match5 = new MatchState(tokens5);
        MatchState match6 = new MatchState(tokenss6);
        MatchState miss2 = new MatchState(bad_tokens2);


        assertTrue(grammar.match(match1) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);
        assertTrue(grammar.match(match2) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);
        assertTrue(grammar.match(match3) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(grammar.match(match4) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
        assertTrue(grammar.match(match5) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        assertTrue(grammar.match(match6) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        assertTrue(grammar.match(miss2) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d1 = {DTMF.FOUR, DTMF.SEVEN, DTMF.ONE, DTMF.ONE, DTMF.STAR, DTMF.STAR};
        MatchState ms = new MatchState(d1);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d2 = {DTMF.FOUR, DTMF.SEVEN, DTMF.ONE, DTMF.ONE, DTMF.STAR, DTMF.STAR, DTMF.NINE};
        ms = new MatchState(d2);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);


    }

    public void testSimpleVVAGrammar() throws Exception {
        String test_str = "<grammar version=\"1.0\" root=\"cmnu1_main_rule\" mode=\"dtmf\">\n" +
                "   <rule id=\"cmnu1_main_rule\">\n" +
                "      <one-of>\n" +
                "         <item>#</item>\n" +
                "         <item>1</item>\n" +
                "         <item>2</item>\n" +
                "         <item>3</item>\n" +
                "         <item>4</item>\n" +
                "         <item>5</item>\n" +
                "         <item>6</item>\n" +
                "         <item>7</item>\n" +
                "         <item>9</item>\n" +
                "         <item>1 0</item>\n" +
                "         <item>1 1</item>\n" +
                "         <item>*</item>\n" +
                "         <item>* 4</item>\n" +
                "         <item>* 0</item>\n" +
                "         <item>0</item>\n" +
                "         <item>* *</item>\n" +
                "         <item>* 8</item>\n" +
                "      </one-of>\n" +
                "   </rule>\n" +
                "</grammar>";

        CompilerElement node = readDocument(test_str);
        DTMFGrammar grammar = new DTMFGrammar(node, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT);
        MatchState ms;
        MatchType mt;

        DTMF [] d1 = {DTMF.ONE};
        ms = new MatchState(d1);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d2 = {DTMF.ONE, DTMF.ONE};
        ms = new MatchState(d2);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        DTMF [] d3 = {DTMF.ONE, DTMF.THREE};
        ms = new MatchState(d3);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

    }


    public void testEmptyStringMatch() throws Exception {
        String test_str =
                "<grammar root='rule1' mode='dtmf'>\n" +
               " <rule id='rule1'>" +
                "       <item repeat='0-'>5</item>\n" +
                " </rule>\n"+
                "</grammar>";

        CompilerElement node = readDocument(test_str);
        DTMFGrammar grammar = new DTMFGrammar(node, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT);
        MatchState  ms;
        MatchType mt;

        DTMF  [] d = stringToDtmf("");
        assertTrue(d != null && d.length == 0);
        ms = new MatchState(d);
        mt = grammar.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

    }

    public void testEmptyStringMatchComplex() throws Exception {
        String test_str =" <grammar version=\"1.0\" mode=\"dtmf\" root=\"gd1_main_rule\">\n" +
                "   <rule id=\"gd1_main_rule2\">\n" +
                "      <one-of>\n" +
                "         <item>0</item>\n" +
                "         <item>1</item>\n" +
                "         <item>2</item>\n" +
                "         <item>3</item>\n" +
                "         <item>4</item>\n" +
                "         <item>5</item>\n" +
                "         <item>6</item>\n" +
                "         <item>7</item>\n" +
                "         <item>8</item>\n" +
                "         <item>9</item>\n" +
                "      </one-of>\n" +
                "   </rule>\n" +
                "   <rule id=\"gd1_main_rule\" scope=\"public\">\n" +
                "      <one-of>\n" +
                "         <item repeat=\"0-\">\n" +
                "            <ruleref uri=\"#gd1_main_rule2\" />\n" +
                "         </item>\n" +
                "         <item>\n" +
                "            <item repeat=\"0-\">\n" +
                "               <ruleref uri=\"#gd1_main_rule2\" />\n" +
                "            </item>\n" +
                "            <one-of>\n" +
                "               <item>*</item>\n" +
                "               <item>*</item>\n" +
                "               <item>* 0</item>\n" +
                "               <item>* *</item>\n" +
                "               <item>* 8</item>\n" +
                "            </one-of>\n" +
                "         </item>\n" +
                "      </one-of>\n" +
                "   </rule>\n" +
                "</grammar>";

        CompilerElement node = readDocument(test_str);
        DTMFGrammar grammar = new DTMFGrammar(node, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT);
        MatchState  ms;
        MatchType mt;

        DTMF  [] d = stringToDtmf("");
        assertTrue(d != null && d.length == 0);
        ms = new MatchState(d);
        mt = grammar.match(ms);
        assertTrue(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);
    }

    public void testTR29971() throws Exception {
        String test_str = "<grammar mode=\"dtmf\" version=\"1.0\" root=\"rec1_main_rule\">\n" +
                "   <rule id=\"rec1_main_rule\">\n" +
                "      <one-of>\n" +
                "         <item>#</item>\n" +
                "      </one-of>\n" +
                "   </rule>\n" +
                "</grammar>";
        CompilerElement node = readDocument(test_str);
        DTMFGrammar grammar = new DTMFGrammar(node, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT);
        MatchState ms;
        MatchType mt;

        DTMF [] d = stringToDtmf("#");
        ms = new MatchState(d);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        d = stringToDtmf("*");
        ms = new MatchState(d);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);
    }


    public void testTR27214() throws Exception {
        String test_str = "<grammar version=\"1.0\" mode=\"dtmf\" root=\"gd1_main_rule\">\n" +
            "   <rule id=\"gd1_main_rule2\">\n" +
            "      <one-of>\n" +
            "         <item>0</item>\n" +
            "         <item>1</item>\n" +
            "         <item>2</item>\n" +
            "         <item>3</item>\n" +
            "         <item>4</item>\n" +
            "         <item>5</item>\n" +
            "         <item>6</item>\n" +
            "         <item>7</item>\n" +
            "         <item>8</item>\n" +
            "         <item>9</item>\n" +
            "      </one-of>\n" +
            "   </rule>\n" +
            "   <rule id=\"gd1_main_rule\" scope=\"public\">\n" +
            "      <one-of>\n" +
            "         <item repeat=\"0-\">\n" +
            "            <ruleref uri=\"#gd1_main_rule2\" />\n" +
            "         </item>\n" +
            "         <item>\n" +
            "            <item repeat=\"0-\">\n" +
            "               <ruleref uri=\"#gd1_main_rule2\" />\n" +
            "            </item>\n" +
            "            *" +
            "         </item>\n" +
            "      </one-of>\n" +
            "   </rule>\n" +
            "</grammar>";
        CompilerElement node = readDocument(test_str);

        DTMFGrammar grammar = new DTMFGrammar(node, com.mobeon.masp.execution_engine.voicexml.grammar.DTMFGrammar.SCOPE.DOCUMENT);
        MatchState ms;
        MatchType mt;

        DTMF [] d = stringToDtmf("77*");
        ms = new MatchState(d);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);

        d = stringToDtmf("*");
        ms = new MatchState(d);
        assertTrue(grammar.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH);
    }
}