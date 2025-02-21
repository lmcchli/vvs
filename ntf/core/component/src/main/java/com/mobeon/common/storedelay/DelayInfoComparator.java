/*
 * DelayInfoComparator.java
 *
 * Created on den 2 september 2004, 13:27
 */

package com.mobeon.common.storedelay;

import java.util.Comparator;

/**
 * Compares DelayInfo based on wanttime, key and type.
 * Wanttime is the main comparision criteria, then key and
 * then type.
 * This comparision allows us to order DelayInfo objects
 * on time but still allow several objects in the same millisecond.
 * <p />
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * The inconcistency is that two DelayInfo objects that are equal
 * might not return zero when compared with equal. However, a comparision
 * that returns zero will always mean thet the objects are equal.
 */
public class DelayInfoComparator implements Comparator<Object>
{

    public int compare(Object o1, Object o2) {
        DelayInfo d1 = (DelayInfo)o1;
        DelayInfo d2 = (DelayInfo)o2;
        long diff = d1.getWantTime() - d2.getWantTime();
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            int result = d1.getKey().compareTo(d2.getKey());
            if (result != 0) return result;
            // Same time and same key, use the type to order even
            // if the ordering of types does not really matter.
            return d1.getType() - d2.getType();
        }

    }


}
