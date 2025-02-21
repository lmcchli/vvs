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
public class SubjectComparator extends StringValuePropertyComparator implements Comparator<IStoredMessage>, Serializable {

    public static final SubjectComparator ASCENDING = new SubjectComparator(false,false);
    public static final SubjectComparator DESCENDING = new SubjectComparator(true,false);
    public static final SubjectComparator ASCENDING_IGNORECASE = new SubjectComparator(false,true);
    public static final SubjectComparator DESCENDING_IGNORECASE = new SubjectComparator(true,true);

    public SubjectComparator(boolean descending, boolean ignoreCase) {
        super(descending, ignoreCase);
    }

    public int compare(IStoredMessage m1, IStoredMessage m2) {
        return compare(m1.getSubject(),m2.getSubject());
    }
}
