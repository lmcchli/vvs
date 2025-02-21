/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Arrays;

/**
 * @author qhast
 */
public class TypeComparator extends EnumValuePropertyComparator<MailboxMessageType> implements Comparator<IStoredMessage> {

    public TypeComparator(boolean descending, MailboxMessageType... values) {
        super(descending, Arrays.asList(values));
    }

    public TypeComparator(MailboxMessageType... values) {
        this(false, values);
    }

    EnumMap<MailboxMessageType, Integer> createEnumMap() {
        return new EnumMap<MailboxMessageType, Integer>(MailboxMessageType.class);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.getType(),m2.getType());
    }
}
