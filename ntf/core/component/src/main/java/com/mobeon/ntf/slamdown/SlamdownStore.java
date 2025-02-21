/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown;

import java.util.Date;

import com.mobeon.ntf.util.time.NtfTime;

/**
 * SlamdownStore stores information about slamdown calls to all notification phones.
 * Information about all pending slamdowns are stored in data structures in
 * SlamdownStore. hin the fields are escaped by the \ character.
 */

public class SlamdownStore
    implements com.mobeon.ntf.Constants {

    private static SlamdownStore _inst = new SlamdownStore();

    /**
     * Constructor.
     */
    protected SlamdownStore() {
        _inst = this;
    }

    public static SlamdownStore get() {
        return _inst;
    }

//    private static final long SEVENTEEN_DIGITS = 100000000000000000L;
    /**
     * packNumber attempts to pack a number from a String into a more compact
     * Object. "Number" has an extremely wide interpretation and can be a String
     * with any characters, but packing is only useful if all characters are
     * decimal digits.Currently there are two possible results: <UL>

     * <LI>The result is a Long if the number consists of 17 or fewer decimal
     * digits. The Long has a value where the 17 least significant digits are
     * the digits from the original number, and the 2 most significant digits
     * are the length of the original number: <PRE>
     *           LLNNNNNNNNNNNNNNNNN.
     * </PRE>

     * <LI>The result is the original String.

     * </UL>
     *@param n - the number to pack.
     *@return the packed Number
     */
/*    public static Object packNumber(String n) {
        if (n.length() < 18) {
            try {
                long l = Long.parseLong(n, 10) + n.length() * SEVENTEEN_DIGITS;
                if (l >= 0) {
                    return new Long(l);
                }
            } catch (NumberFormatException e) { ; }
        }
        return n;
    }*/

    /**
     * unpackNumber unpacks a number packed with packNumber to the original
     * String.
     *@param o - the number to unpack.
     *@return a String with the unpacked number.
     */
/*    public static String unpackNumber(Object o) {
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Long) {
            return unpackNumber(((Long) o).longValue());
        } else {
            return "0";
        }
    }*/

    /**
     * unpackNumber unpacks a number packed with packNumber to the original
     * String.
     *@param o - the number to unpack.
     *@return a String with the unpacked number.
     */
/*    public static String unpackNumber(long o) {
        long v = o;
        int l = (int) (v / SEVENTEEN_DIGITS);
        v %= SEVENTEEN_DIGITS;
        String fill = "0000000000000000" + v;
        return (fill).substring(fill.length() - l);
    }*/

    public static int packDate(Date d) {
        return (int) (d.getTime() / 1000L - NtfTime.START_TIME);
    }

    public static Date unpackDate(int i) {
        return new Date((i + (long)NtfTime.START_TIME) * 1000L);
    }

    public String toString() {
        return "";
    }

       
}
