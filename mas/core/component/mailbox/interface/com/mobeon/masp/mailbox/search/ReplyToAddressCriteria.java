/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the replyto address message property.
 * @author qhast
 */
public class ReplyToAddressCriteria extends ValueCriteria<String,MessagePropertyCriteriaVisitor> {



    /**
     * Creates a ReplyToAddressCriteria with matched property value.
     *
     * @param propertyValue
     */
    public ReplyToAddressCriteria(String propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitReplyToAddressCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getReplyToAddress());
    }

    public ReplyToAddressCriteria clone() {
        return new ReplyToAddressCriteria(getValue());
    }
}
