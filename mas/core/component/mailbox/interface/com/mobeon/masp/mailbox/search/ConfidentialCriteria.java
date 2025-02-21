/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the confidential message property.
 * @author qhast
 */
public class ConfidentialCriteria extends ValueCriteria<Boolean,MessagePropertyCriteriaVisitor> {

    public static final ConfidentialCriteria CONFIDENTIAL = new ConfidentialCriteria(true);
    public static final ConfidentialCriteria NON_CONFIDENTIAL = new ConfidentialCriteria(false);

    /**
     * Creates a BooleanPropertyValueCriteria with
     * a matched property value.
     *
     * @param propertyValue
     */
    public ConfidentialCriteria(boolean propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitConfidentialCriteria(this);
    }

    /**
     * Matches an object against the criteria.
     *
     * @param o object
     * @return true if the object matches the criteria.
     */
    public boolean match(IStoredMessage o) {
        return matchValue(o.isConfidential());
    }

    public ConfidentialCriteria clone() {
        return new ConfidentialCriteria(getValue());
    }
}
