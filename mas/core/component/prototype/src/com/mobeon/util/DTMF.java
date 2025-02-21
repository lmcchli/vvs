/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.util;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-jan-13
 * Time: 16:45:30
 * To change this template use File | Settings | File Templates.
 */
public class DTMF {
    public static String getDTMF(Byte data) {
        int value = data.intValue();
        return getDTMF(value);
    }

    public static String getDTMF(String value) {
        return getDTMF(Integer.parseInt(value));       
    }

    public static String getDTMF(int value) {
        if (value >= 0 && value <= 9)
            return Integer.toString(value);
        if (value == 10)
            return "*";
        if (value == 11)
            return "#";
        if (value == 12)
            return "A";
        if (value == 13)
            return "B";
        if (value == 14)
            return "C";
        if (value == 15)
            return "D";
        if (value == 16)
            return "Flash";

        // Else return null;
        return null;
    }
}
