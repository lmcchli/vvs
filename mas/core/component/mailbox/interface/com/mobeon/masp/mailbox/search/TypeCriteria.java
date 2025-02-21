/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Represents a criteria for matching the mailbox message type message property.
 * @author qhast
 */
public class TypeCriteria extends ValueCriteria<MailboxMessageType,MessagePropertyCriteriaVisitor> {

    public static final TypeCriteria VOICE = new TypeCriteria(MailboxMessageType.VOICE);
    public static final NotCriteria NOT_VOICE = new NotCriteria(VOICE);

    public static final TypeCriteria VIDEO = new TypeCriteria(MailboxMessageType.VIDEO);
    public static final NotCriteria NOT_VIDEO = new NotCriteria(VIDEO);

    public static final TypeCriteria FAX = new TypeCriteria(MailboxMessageType.FAX);
    public static final NotCriteria NOT_FAX = new NotCriteria(FAX);

    public static final TypeCriteria EMAIL = new TypeCriteria(MailboxMessageType.EMAIL);
    public static final NotCriteria NOT_EMAIL = new NotCriteria(EMAIL);


    /**
     * Creates a StringPropertyValueCriteria with matched property value.
     *
     * @param propertyValue
     */
    public TypeCriteria(MailboxMessageType propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitTypeCriteria(this);
    }

    public boolean match(IStoredMessage message) {
        return matchValue(message.getType());
    }

    public TypeCriteria clone() {
        return new TypeCriteria(getValue());
    }
}
