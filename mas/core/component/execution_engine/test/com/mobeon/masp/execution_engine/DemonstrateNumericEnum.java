/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.util.Tools;

public enum DemonstrateNumericEnum {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    NOTHING;


    public static DemonstrateNumericEnum add(DemonstrateNumericEnum a,DemonstrateNumericEnum b) {
        while(b != ZERO && b != NOTHING) {
            a = succ(a);
            b = pred(b);
        }
        return a;
    }

    public static DemonstrateNumericEnum multiply(DemonstrateNumericEnum a,DemonstrateNumericEnum b) {
        DemonstrateNumericEnum sum = ZERO;
        while(b != ZERO && b != NOTHING) {
            sum = add(sum,a);
            b = pred(b);
        }
        return sum;
    }


    public static DemonstrateNumericEnum succ(DemonstrateNumericEnum b) {
        if(b == NOTHING)
            return NOTHING;
        return DemonstrateNumericEnum.values()[b.ordinal()+1];
    }
    public static DemonstrateNumericEnum pred(DemonstrateNumericEnum b) {
        if(b == ZERO)
            return ZERO;
        if(b == NOTHING)
            return NOTHING;
        return DemonstrateNumericEnum.values()[b.ordinal()-1];
    }


    public static void main(String[] args) {
        Tools.println(add(THREE,FOUR));
        Tools.println(multiply(TWO,THREE));
    }
}
