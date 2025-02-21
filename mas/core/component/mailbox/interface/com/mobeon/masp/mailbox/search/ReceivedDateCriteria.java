/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.QuantitativeValueCriteria;

import java.util.Date;

/**
 * Represents a criteria for matching the received date message property.
 * @author qhast
 */
public class ReceivedDateCriteria extends QuantitativeValueCriteria<Date,MessagePropertyCriteriaVisitor> {

    /**
     * Similar to {@link #ReceivedDateCriteria(java.util.Date,com.mobeon.masp.util.criteria.QuantitativeValueCriteria.Comparison)}
     * but comparision defaults
     * to {@link com.mobeon.masp.util.criteria.QuantitativeValueCriteria.Comparison#EQ}
     */
    ReceivedDateCriteria(Date propertyValue) {
        super(propertyValue);
    }

    /**
     * Creates a ReceivedDateCriteria with
     * matched property value and comparison.
     *
     * @param propertyValue
     * @param c             which comparsion that should be applied.
     */
    public ReceivedDateCriteria(Date propertyValue, Comparison c) {
        super(propertyValue, c);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see MessagePropertyCriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitReceivedDateCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getReceivedDate());
    }

    public ReceivedDateCriteria clone() {
        return new ReceivedDateCriteria(getValue(),getComparison());
    }
}
