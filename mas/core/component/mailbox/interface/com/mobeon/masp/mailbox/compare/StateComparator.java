/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Arrays;

/**
 * @author qhast
 */
public class StateComparator extends EnumValuePropertyComparator<StoredMessageState> implements Comparator<IStoredMessage> {

    public StateComparator(boolean descending, StoredMessageState... values) {
        super(descending, Arrays.asList(values));
    }

    public StateComparator(StoredMessageState... values) {
        this(false, values);
    }

    EnumMap<StoredMessageState, Integer> createEnumMap() {
        return new EnumMap<StoredMessageState, Integer>(StoredMessageState.class);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.getState(),m2.getState());
    }
}
