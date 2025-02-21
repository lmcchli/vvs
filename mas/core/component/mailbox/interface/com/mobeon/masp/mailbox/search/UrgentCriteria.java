/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the urgent message property.
 * @author qhast
 */
public class UrgentCriteria extends ValueCriteria<Boolean,MessagePropertyCriteriaVisitor> {

    public static final UrgentCriteria URGENT = new UrgentCriteria(true);
    public static final UrgentCriteria NON_URGENT = new UrgentCriteria(false);

    /**
     * Creates a UrgentCriteria with matched property value.
     *
     * @param propertyValue
     */
    public UrgentCriteria(boolean propertyValue) {
        super(propertyValue);
    }


    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitUrgentCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.isUrgent());
    }

    public UrgentCriteria clone() {
        return new UrgentCriteria(getValue());
    }
}
