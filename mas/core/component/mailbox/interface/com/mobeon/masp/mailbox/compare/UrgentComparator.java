/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.Comparator;
import java.io.Serializable;

/**
 * @author qhast
 */
public class UrgentComparator extends MessagePropertyComparator<Boolean> implements Comparator<IStoredMessage>, Serializable {

    public static final UrgentComparator URGENT_FIRST = new UrgentComparator(true);
    public static final UrgentComparator NON_URGENT_FIRST = new UrgentComparator(false);

    public UrgentComparator(boolean descending) {
        super(descending);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.isUrgent(),m2.isUrgent());
    }
}
