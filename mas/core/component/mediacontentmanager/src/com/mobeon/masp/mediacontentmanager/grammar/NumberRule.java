/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.MessageElement;
import com.mobeon.masp.mediacontentmanager.INumberRule;
import com.mobeon.masp.mediacontentmanager.INumberRuleCondition;

import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of {@link INumberRule}.
 * <p/>
 * Represents a rule. There is one rule for each divisor. Each rule may have
 * several conditions, {@link INumberRuleCondition}s, for different ranges of
 * the quotient and remainder.
 * The checked number is divided by the rule's divisor.
 *
 * @author mmawi
 */
public class NumberRule implements INumberRule {
    /**
     * The checked number will be divided by this number.
     */
    private long divisor;

    private List<INumberRuleCondition> conditionList =
            new ArrayList<INumberRuleCondition>();

    /**
     * Creates a new <code>NumberRule</code> with no conditions. Conditions
     * are added with {@link NumberRule#addCondition(INumberRuleCondition)}
     *
     * @param divisor   The rule's divisor. The checked number should be
     *                  divided by this to see which
     *                  <code>NumberRuleCondition</code> that is satisfied.
     */
    public NumberRule(long divisor) {
        this.divisor = divisor;
    }

    /**
     * Return the divisor for this rule.
     *
     * @return  The divisor for this rule.
     */
    public long getDivisor() {
        return divisor;
    }

    /**
     * Adds a new condition to this <code>NumberRule</code>.
     *
     * @param condition     The condition to add.
     */
    public void addCondition(INumberRuleCondition condition) {
        conditionList.add(condition);
    }

    /**
     * Return this rule's conditions.
     *
     * @return This rule's list of conditions.
     */
    public List<INumberRuleCondition> getConditionList() {
        return conditionList;
    }
}
