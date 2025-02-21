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
public class SenderComparator extends StringValuePropertyComparator implements Comparator<IStoredMessage>, Serializable {

    public static final SenderComparator ASCENDING = new SenderComparator(false,false);
    public static final SenderComparator DESCENDING = new SenderComparator(true,false);
    public static final SenderComparator ASCENDING_IGNORECASE = new SenderComparator(false,true);
    public static final SenderComparator DESCENDING_IGNORECASE = new SenderComparator(true,true);

    public SenderComparator(boolean descending, boolean ignoreCase) {
        super(descending, ignoreCase);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.getSender(),m2.getSender());
    }
}
