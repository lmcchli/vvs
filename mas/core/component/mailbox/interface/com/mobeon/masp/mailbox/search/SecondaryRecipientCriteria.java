/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching a secondary recipient message property.
 */
public class SecondaryRecipientCriteria extends ValueCriteria<String,MessagePropertyCriteriaVisitor> {


    /**
     * Creates a SecondaryRecipientCriteria with matched property value.
     *
     * @param propertyValue
     */
    public SecondaryRecipientCriteria(String propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitSecondaryRecipientCriteria(this);
    }


    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        for(String s : message.getSecondaryRecipients()) {
            if(matchValue(s)) return true;
        }
        return false;
    }

    public SecondaryRecipientCriteria clone() {
        return new SecondaryRecipientCriteria(getValue());
    }
}
