/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

/**
 * DTMF Enumeration used by SRGS Grammar handeling
 */
public enum DTMF {
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX ,
        SEVEN,
        EIGHT,
        NINE,
        A,
        B,
        C,
        D,
        HASH,
        STAR;

    public String toString() {
        switch(this) {
            case ZERO :      return "0";
            case ONE :       return "1";
            case TWO :       return "2";
            case THREE :     return "3";
            case FOUR :      return "4";
            case FIVE :      return "5";
            case SIX :       return "6";
            case SEVEN :     return "7";
            case EIGHT :     return "8";
            case NINE :      return "9";
            case A :         return "A";
            case B :         return "B";
            case C :         return "C";
            case D :         return "D";
            case HASH :      return "#";
            case STAR :      return "*";
            default :         return "invalid";
        }
    }

    public static DTMF getDTMFValue(String dtmf) {
       if(dtmf == null) return null;
       if(dtmf.trim().equals("0")) return DTMF.ZERO;
       else if(dtmf.trim().equals("1")) return DTMF.ONE;
       else if(dtmf.trim().equals("2")) return DTMF.TWO ;
        else if(dtmf.trim().equals("3")) return DTMF.THREE;
        else if(dtmf.trim().equals("4")) return DTMF.FOUR;
        else if(dtmf.trim().equals("5")) return DTMF.FIVE;
        else if(dtmf.trim().equals("6")) return DTMF.SIX;
        else if(dtmf.trim().equals("7")) return DTMF.SEVEN;
        else if(dtmf.trim().equals("8")) return DTMF.EIGHT;
        else if(dtmf.trim().equals("9")) return DTMF.NINE;
        else if(dtmf.trim().equals("A")) return DTMF.A;
        else if(dtmf.trim().equals("B")) return DTMF.B;
        else if(dtmf.trim().equals("C")) return DTMF.C;
        else if(dtmf.trim().equals("D")) return DTMF.D;
        else if(dtmf.trim().equals("#")) return DTMF.HASH;
        else if(dtmf.trim().equals("*")) return DTMF.STAR;
        else return null;
    }
    
}

