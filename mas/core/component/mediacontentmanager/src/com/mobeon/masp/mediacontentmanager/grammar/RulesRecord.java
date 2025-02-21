/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IRulesRecord;
import com.mobeon.masp.mediacontentmanager.INumberRule;

import java.util.*;

/**
 * Default implementation of the interface
 * {@link com.mobeon.masp.mediacontentmanager.IRulesRecord}.
 * <p/>
 * A <code>RulesRecord</code> holds a mapping of divisors to
 * {@link INumberRule}s for one
 * {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier.QualiferType}
 * and one or many
 * {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier.Gender}s.
 *
 * @author mmawi
 */
public class RulesRecord implements IRulesRecord {

    /**
     * The list of <code>Gender</code>s that this <code>RulesRecord</code>
     * contains <code>INumberRule</code>s for.
     */
    private List<IMediaQualifier.Gender> genders;
    /**
     * The <code>QualifierType</code> that this <code>RulesRecord</code>
     * contains <code>INumberRule</code>s for.
     */
    private IMediaQualifier.QualiferType type;

    /**
     * The collection of <code>INumberRule</code>s associated with this
     * <code>RulesRecord</code>.
     */
    private SortedMap<Long, INumberRule> numberRules =
            new TreeMap<Long, INumberRule>(Collections.reverseOrder());


    /**
     * Creates a new <code>RulesRecord</code> of the specified type with
     * undefined genders.
     * @param type The type.
     */
    public RulesRecord(IMediaQualifier.QualiferType type) {
        this.type = type;
        genders = new ArrayList<IMediaQualifier.Gender>();
    }

    /**
     * Add a gender for the <code>RulesRecord</code>.
     * @param gender The gender.
     */
    public void addGender(IMediaQualifier.Gender gender) {
        genders.add(gender);
    }

    /**
     * Used to match a rule with a gender and type.
     * @param gender    The <code>Gender</code> that the rule
     *                  is compared to.
     * @param ruleType  The <code>QualiferType</code> that the
     *                  rule is compared to.
     * @return <code>true</code> if the rule matches the specified <code>gender</code> and <code>ruleType</code>,
     * <code>false</code> otherwise.
     */
    public boolean compareRule(IMediaQualifier.Gender gender, IMediaQualifier.QualiferType ruleType) {
        return (genders.contains(gender)) && (ruleType == this.type);
    }

    /**
     * Return this <code>RulesRecord<code>'s <code>INumberRule</code>s sorted
     * on divisor, descending.
     * @return The list of <code>INumberRule</code>s associated with this
     * <code>RulesRecord</code>
     */
    public SortedMap<Long, INumberRule> getNumberRules() {
        return numberRules;
    }

    /**
     * Return the NumberRule for a divisor.
     * @param divisor   The divisor to look for.
     * @return Returns the <code>INumberRule</code> for the divisor.
     * If the divisor is not found, <code>null</code> is returned.
     */
    public INumberRule getNumberRule(Long divisor) {
        return numberRules.get(divisor);
    }

    /**
     * Add a <code>INumberRule</code> to the <code>RulesRecord</code>.
     * @param numberRule The NumberRule to add.
     */
    public void addNumberRule(INumberRule numberRule) {
        numberRules.put(numberRule.getDivisor(), numberRule);
    }
}
