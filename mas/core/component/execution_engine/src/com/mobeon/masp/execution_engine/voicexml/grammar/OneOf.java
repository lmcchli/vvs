/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;


import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;
import java.util.Map;

public class OneOf extends RuleExpansion {
    private ILogger log = ILoggerFactory.getILogger(OneOf.class);

    public OneOf(Element node, Map<String, Rule> rule_map) {
        List list = node.content();
        for (Object o : list) {
            Node n = (Node) o;
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = n.getName();
                if (Constants.SRGS.ITEM.equals(name)) {
                    content.add(new Item((Element) n, rule_map));

                } else {
                    if (log.isDebugEnabled()) log.debug("Invalid SRGS tag with name " + name);
                }
            }
        }
    }

    public MatchType match(MatchState dtmf) {
        // sanity check. If no children return no match
        if (this.content.size() == 0)
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        // save current dtmf state for back tracking

        int current = dtmf.getCurrent();
        int best_current = current;

        MatchType ret = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        MatchType bestSoFar = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        // iterate over children and save the most matchable reult in bestSoFar
        for (Matchable m : this.content) {
            ret = m.match(dtmf);

            // match fount, be done and return, do not know if it the longest though TODO: check if longest
            if (ret == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH) {
                // if the last mach was an empty match - do nothing 
                if (!dtmf.getLastMatchWasEmpty()) {   // TODO: test with oneof with only repeat "0-" items, will not work

                    bestSoFar = ret;
                    int c = dtmf.getCurrent();
                    if (c > best_current)
                        best_current = c;
                    //return ret;
                }

            } else if (ret == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH) {

                if (bestSoFar == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH) {
                    bestSoFar = ret;
                    best_current = dtmf.getCurrent();
                } else if (bestSoFar == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH)
                { // need to downgrade the FULL MATCH to a MATCH since partial match exist
                    bestSoFar = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
                }

            } else if (ret == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH) {
                // should only return FULL MATCH if we have no match or partial match, otherwise downgrade to a match
                if (bestSoFar == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH) {
                    bestSoFar = ret;

                } else if ((bestSoFar == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH || bestSoFar == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH) && best_current > dtmf.getCurrent()) {
                    bestSoFar = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;

                } else {
                    bestSoFar = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH;
                }
                int c = dtmf.getCurrent();
                if (c > best_current)
                    best_current = c;

            }
            dtmf.setCurrent(current);


        }

        dtmf.setCurrent(best_current);
        return bestSoFar;

    }
}
