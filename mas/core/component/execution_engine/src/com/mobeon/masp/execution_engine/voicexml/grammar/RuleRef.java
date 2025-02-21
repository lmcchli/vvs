/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;


import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Element;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleRef extends RuleExpansion {
    ILogger log = ILoggerFactory.getILogger(RuleRef.class);
    private Rule rule = null; // the rule that is referred

    public MatchType match(MatchState dtmf) {
        return this.rule.rulerefMatch(dtmf);
    }

    public RuleRef(Element node, Map<String, Rule> rule_map) {
        String rule_uri = node.attributeValue(Constants.SRGS.URI);
        /* TODO, for drop 4 only rulerefs within local grammar is supported */
        Pattern p = Pattern.compile("#(.*)");
        Matcher m = p.matcher(rule_uri);
        if (!m.matches()) {
            if (log.isDebugEnabled()) log.debug("SRGS ruleref with invalid uri, ignored");
            return;
        }
        rule_uri = m.group(1);
        rule_uri = rule_uri.trim();
        if (rule_uri == null || "".equals(rule_uri)) {
            if (log.isDebugEnabled()) log.debug("SRGS ruleref with invalid uri, ignored");
            return;
        }
        this.rule = rule_map.get(rule_uri);
        if (this.rule == null) {
            if (log.isDebugEnabled()) log.debug("SRGS ruleref with invalid uri, ignored");
            return;
        }


    }

    public Rule getRule() {
        return this.rule;
    }
}
