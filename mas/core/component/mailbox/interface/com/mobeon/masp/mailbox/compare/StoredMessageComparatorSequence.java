/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.*;
import java.io.Serializable;

/**
 * Composite
 * @author qhast
 */
public class StoredMessageComparatorSequence implements Comparator<IStoredMessage>, Serializable {

    private List<Comparator<IStoredMessage>> comparators;

    public StoredMessageComparatorSequence(Comparator<IStoredMessage>... comparators) {
        this.comparators = new ArrayList<Comparator<IStoredMessage>>(comparators.length);
        for(Comparator<IStoredMessage> c : comparators) {
            this.comparators.add(c);
        }
    }

    public void add(Comparator<IStoredMessage> comparator) {
        this.comparators.add(comparator);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        for(Comparator<IStoredMessage> c : comparators) {
            int r = c.compare(m1,m2);
            if(r != 0) return r;
        }
        return 0;
    }

}
