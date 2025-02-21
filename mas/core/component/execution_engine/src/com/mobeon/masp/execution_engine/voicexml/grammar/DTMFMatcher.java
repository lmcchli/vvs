/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import org.apache.commons.lang.StringUtils;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class DTMFMatcher {

    private final static ILogger log = ILoggerFactory.getILogger(DTMFMatcher.class);

    public static MatchType match(GrammarScopeNode gnode, String dtmf) {

        
        //TR: HU98608 Debug logs in VVS which contains pin numbers of subscribers
        String dtmf_obscured = "";      
        if (log.isInfoEnabled()) {
        	if (dtmf != null) {
                //obscure with X's instead of actual DTMF digits as can display pin numbers etc.
                dtmf_obscured = "obscured:" + StringUtils.repeat("X", dtmf.length());
        	} else {
                dtmf_obscured = "null";
        	}
        }
              
        MatchState ms = new MatchState(dtmf);
        MatchType ret = matchNode(gnode, ms);
        Grammar grammar = gnode.getGrammar();
        if (grammar != null && log.isDebugEnabled())
            log.debug("Local grammar returning " + ret.toString() + " on input '" + dtmf_obscured + "' with grammar: " + grammar.getGrammar_id());

        MatchType p;
        GrammarScopeNode parent = gnode.getParent();
        while (parent != null) {

            p = matchNode(parent, ms);
            grammar = parent.getGrammar();
            if (log.isDebugEnabled()) {
                if (grammar != null)
                    log.debug("Parent grammar returning " + p.toString() + " on input '" + dtmf_obscured + "' with grammar: " + grammar
                            .getGrammar_id());
                else
                    log.debug("Parant grammar was a virutal grammar scope node");
            }
            ret = downgrade(ret, p);
            parent = parent.getParent();
        }

        String grammar_id = "NO_ID";
        try {
            grammar_id = gnode.getGrammar().getGrammar_id();
        } catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("No grammar id found!");
        }
        if (log.isInfoEnabled())
            log.info("DTMFMatcher returning " + ret.toString() + " on input '" + dtmf_obscured + "' with grammar: " + grammar_id);
        return ret;
    }


    private static MatchType downgrade(MatchType ret, MatchType p) {
        if (ret == MatchType.NO_MATCH) {
            if (p != MatchType.NO_MATCH)
                return p;
        }
        if (ret == MatchType.PARTIAL_MATCH || ret == MatchType.MATCH) {
            return ret;
        }
        if (ret == MatchType.FULL_MATCH) {
            if (p == MatchType.PARTIAL_MATCH || p == MatchType.MATCH)
                return MatchType.MATCH;
        }

        return ret;
    }

    /**
     * @param node
     * @param ms
     * @return The type of match that occured
     * @logs.error ""General error - found a ASR grammar in DTMFMatcher" - This is an internal error.
     */
    private static MatchType matchNode(GrammarScopeNode node, MatchState ms) {
        Grammar g = node.getGrammar();
        if (g == null)
            return MatchType.NO_MATCH; // we have i virtual root node which should be ignored - just ask its parent
        DTMFGrammar grammar = null;
        if (g instanceof DTMFGrammar) {
            grammar = (DTMFGrammar) g;
        } else {
            log.error("General error - found a ASR grammar in DTMFMatcher");
            return MatchType.NO_MATCH;
        }
        if (grammar != null) {
            MatchType ret = grammar.match(ms);

            if (ms.hasMoreItems()) // if the grammar did not consume all input we always have a no match
                return MatchType.NO_MATCH;
            return ret;
        }
        return MatchType.NO_MATCH;
    }

}
