/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event.rule;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.AnalyzeClassRule;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRule;

public class CCXMLToParentRule extends AnalyzeClassRule {

    public CCXMLToParentRule() {
        super(
            new RulePair(SimpleEvent.class,EXIT_RULE),
            new RulePair(CCXMLEvent.class,EXIT_RULE)
        );
    }

    private static final EventRule EXIT_RULE = new EventRuleBase() {
        public boolean isValid(Event e) {
            SimpleEvent se = (SimpleEvent) e;
            return logIfValid(se.getEvent().equals(Constants.Event.CCXML_EXIT), e);
        }

        public String toString() {
            return "is(ccxml.exit)";
        }
    };

    public String toString() {
        return super.toString();
    }
}
