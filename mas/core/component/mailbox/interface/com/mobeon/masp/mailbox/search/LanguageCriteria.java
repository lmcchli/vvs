package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.ValueCriteria;

public class LanguageCriteria extends ValueCriteria<String,MessagePropertyCriteriaVisitor> {

    /**
     * Creates a ReplyToAddressCriteria with matched property value.
     *
     * @param propertyValue
     */
    public LanguageCriteria(String propertyValue) {
        super(propertyValue);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(MessagePropertyCriteriaVisitor visitor) {
        visitor.visitLanguageCriteria(this);
    }

    /**
     * Matches a stored message against the criteria.
     * @param message
     * @return true if the message matches the criteria.
     */
    public boolean match(IStoredMessage message) {
        return matchValue(message.getBroadcastLanguage());
    }

    public LanguageCriteria clone() {
        return new LanguageCriteria(getValue());
    }
}