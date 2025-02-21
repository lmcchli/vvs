/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;
import junit.framework.*;
import com.mobeon.masp.execution_engine.voicexml.grammar.DTMFToken;

public class DTMFTokenTest extends TestCase {


    public void testPartialMatch() throws Exception {
        DTMFToken dtmfToken = new DTMFToken("*9");

        DTMF [] d = {DTMF.STAR};

        MatchState ms = new MatchState(d);

        assertTrue(dtmfToken.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);

        DTMF [] d2 = {DTMF.STAR, DTMF.NINE, DTMF.SEVEN};
        MatchState dtmf = new MatchState(d2);
        MatchType matchType = dtmfToken.match(dtmf);
        assertTrue(matchType == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

        DTMF [] d3 = {DTMF.STAR, DTMF.SEVEN};
        assertTrue(dtmfToken.match(new MatchState(d3)) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

        DTMF [] d4 = {DTMF.SEVEN};
        assertTrue(dtmfToken.match(new MatchState(d4)) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH);

    }
}