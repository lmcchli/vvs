/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.contact;

import java.util.Comparator;

/**
 * This class implements a {@link Comparator} for compairing two {@link Contact}.
 *
 * @author Malin Flodin
 */
public class ContactComparator implements Comparator {

    /**
     * Compaires two objects of class {@link Contact}.
     * @param o1
     * @param o2
     * @return  0 if the two objects are equal
     *          -1 if <param>o1</param> has a higher Q value than <param>o2</param>
     *          1 otherwise.
     * @throws  ClassCastException
     *          - ClassCastException if the objects are not instances of class
     *          {@link Contact}.
     */
    public int compare(Object o1, Object o2) throws ClassCastException {
        if (!(o1 instanceof Contact) || !(o2 instanceof Contact))
            throw new ClassCastException("Can only compare objects of class Contact.");

        Contact c1 = (Contact)o1;
        Contact c2 = (Contact)o2;

        if (c1 == c2) return 0;

        if (c1.getQ() > c2.getQ())
            return -1;
        else
            return 1;
    }

}
