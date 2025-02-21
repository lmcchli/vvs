/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a SRGS DTMF rule
 */

public class Rule implements Matchable{
    private final ILogger logger = ILoggerFactory.getILogger(Rule.class);
    private String id;
    // remove items
    private List<DTMF[]> items = new ArrayList<DTMF[]>();


    private DTMFGrammar parent;
    private List<RuleExpansion> expansions = new ArrayList<RuleExpansion>();
    public Rule(Element element, DTMFGrammar parent) {

        // supports one one-of with a set of items only
        this.parent = parent;
        this.id = element.attributeValue(Constants.VoiceXML.ID);
        Node elem = null;
        Map<String, Rule> rule_map = parent.getRule_map();
        for (Object o : element.content()) {
            elem = (Node) o;
            if (elem.getNodeType() == Node.ELEMENT_NODE)
                if(Constants.SRGS.ONEOF.equals(elem.getName())) {
                    expansions.add(new OneOf((Element) elem, rule_map));
                }  else if(Constants.SRGS.ITEM.equals(elem.getName())) {
                    expansions.add(new Item((Element) elem, rule_map));
                } else if(Constants.SRGS.RULEREF.equals(elem.getName())) {
                    expansions.add(new RuleRef((Element) elem, rule_map));
                }
        }


    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public MatchType match(MatchState dtmf_arr) {

        
        // TODO: Is more then one expaneions even allowed, and if so ... what about backtracking
        MatchType ret = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        for (Matchable m : expansions) {
            MatchType mt = m.match(dtmf_arr);
            if((mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH || mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH) && !dtmf_arr.hasMoreItems())
                return mt;
            if(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH )
                ret = mt;

        }
        if(dtmf_arr.hasMoreItems()) {
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        }
        return ret;
    }

    /**
     * Special match method used by rule ref. This match method does not demand that all dtmf tokens are
     * consumed to be return match. This match works like the one in OneOf and Item and DTMFToken. If the
     * whole body is consumed by a match, match is returned
     * @param dtmf_arr
     * @return
     */
    public MatchType rulerefMatch(MatchState dtmf_arr) {

        // TODO: only one expansion child allowd
        for (Matchable m : expansions) {
            return m.match(dtmf_arr);
        }
        return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
    }


    private boolean equal(DTMF[] arr1, DTMF[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++)
            if (arr1[i] != arr2[i]) {
                return false;
            }
        return true;
    }

}
