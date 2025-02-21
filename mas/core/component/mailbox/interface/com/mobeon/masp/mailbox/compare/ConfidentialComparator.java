/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.IStoredMessage;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author qhast
 */
public class ConfidentialComparator extends MessagePropertyComparator<Boolean>
        implements Comparator<IStoredMessage>, Serializable {

    public static final ConfidentialComparator CONFIDENTIAL_FIRST = new ConfidentialComparator(true);
    public static final ConfidentialComparator NON_CONFIDENTIAL_FIRST = new ConfidentialComparator(false);

    public ConfidentialComparator(boolean descending) {
        super(descending);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.isConfidential(),m2.isConfidential());
    }
}
