/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the subject message property.
 * @author qhast
 */
public class SubjectCriteria extends ValueCriteria<String,MessagePropertyCriteriaVisitor> {

    /**
     * Creates a SubjectCriteria matched property value.
     *
     * @param propertyValue
     */
    public SubjectCriteria(String propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitSubjectCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getSubject());
    }

    public SubjectCriteria clone() {
        return new SubjectCriteria(getValue());
    }
}
