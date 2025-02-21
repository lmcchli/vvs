/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Element;

import java.util.*;

/**
 * Grammar represents a inline xml SRGS DTMF grammar
 */
public class DTMFGrammar extends Grammar {
    private Rule root_rule;

    private Map<String, Rule> rule_map = new HashMap<String, Rule>();

    public DTMFGrammar(CompilerElement node, Grammar.SCOPE scope) {
        super(InputMode.DTMF, scope);
        parseSRGS(node);
    }


    private void parseSRGS(CompilerElement node) {
        if (node == null) {
            return;
        }
        String root = node.attributeValue(Constants.VoiceXML.ROOT);
        setGrammar_id(root);
        List list = node.content();
        for (Iterator i = list.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Element) {
                Element e = (Element) obj;
                if (e.getName().equals("rule")) {
                    String id = e.attributeValue(Constants.VoiceXML.ID);
                    if (id == null || "".equals(id)) {
                        if (logger.isDebugEnabled())
                            logger.debug("SRGS Rule errror - rule without id not allowed. Rule ignored");
                        continue;
                    }
                    Rule r = new Rule(e, this);

                    rule_map.put(id, r);
                    if (id != null && id.equals(root)) {
                        root_rule = r;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug(obj.getClass().toString());

            }
        }
    }


    public MatchType match(MatchState dtmf) {

        // all matching must start with the root rule
        MatchType mt = root_rule.match(dtmf);
        if (logger.isDebugEnabled()) logger.debug("Grammar returning " + mt);
        return mt;

    }




    public Rule getRootRule() {
        return this.root_rule;
    }

    public Map<String, Rule> getRule_map() {
        return rule_map;
    }
}
