/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.Comparator;
import java.util.Date;
import java.io.Serializable;

/**
 * @author qhast
 */
public class ReceivedDateComparator extends MessagePropertyComparator<Date> implements Comparator<IStoredMessage>, Serializable {

    public final static ReceivedDateComparator OLDEST_FIRST = new ReceivedDateComparator(false);
    public final static ReceivedDateComparator NEWEST_FIRST = new ReceivedDateComparator(true);

    public ReceivedDateComparator(boolean descending) {
        super(descending);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.getReceivedDate(),m2.getReceivedDate());
    }
}
