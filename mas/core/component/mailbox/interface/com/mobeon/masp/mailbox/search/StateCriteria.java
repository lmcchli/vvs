/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the stored message state message property.
 * @author qhast
 */
public class StateCriteria extends ValueCriteria<StoredMessageState,MessagePropertyCriteriaVisitor> {

    public static final StateCriteria NEW;
    public static final NotCriteria NOT_NEW;

    public static final StateCriteria SAVED;
    public static final NotCriteria NOT_SAVED;

    public static final StateCriteria READ;
    public static final NotCriteria NOT_READ;

    public static final StateCriteria DELETED;
    public static final NotCriteria NOT_DELETED;


    static {
        NEW = new StateCriteria(StoredMessageState.NEW);
        SAVED = new StateCriteria(StoredMessageState.SAVED);
        READ = new StateCriteria(StoredMessageState.READ);
        DELETED = new StateCriteria(StoredMessageState.DELETED);
        NOT_NEW = new NotCriteria(NEW);
        NOT_SAVED = new NotCriteria(SAVED);
        NOT_READ = new NotCriteria(READ);
        NOT_DELETED = new NotCriteria(DELETED);
    }

    /**
     * Creates a StoredMessageStateCriteria with matched property value.
     *
     * @param propertyValue
     */
    public StateCriteria(StoredMessageState propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitStateCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getState());
    }

    public StateCriteria clone() {
        return new StateCriteria(getValue());
    }
}
