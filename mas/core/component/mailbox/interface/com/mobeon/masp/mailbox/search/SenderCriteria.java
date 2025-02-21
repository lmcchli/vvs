/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the sender message property.
 * @author qhast
 */
public class SenderCriteria extends ValueCriteria<String,MessagePropertyCriteriaVisitor> {

    /**
     * Creates a SenderCriteria with matched property value.
     *
     * @param propertyValue
     */
    public SenderCriteria(String propertyValue) {
        super(propertyValue);
    }


    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see MessagePropertyCriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitSenderCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getSender());
    }


    public SenderCriteria clone() {
        return new SenderCriteria(getValue());
    }

}
