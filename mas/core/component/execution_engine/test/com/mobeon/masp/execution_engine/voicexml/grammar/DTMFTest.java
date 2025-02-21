/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import junit.framework.*;


public class DTMFTest extends TestCase {


    public void testGetDTMFValue() throws Exception {
        assertTrue(DTMF.getDTMFValue("0") == DTMF.ZERO);
        assertTrue(DTMF.getDTMFValue("1") == DTMF.ONE);
        assertTrue(DTMF.getDTMFValue("2") == DTMF.TWO);
        assertTrue(DTMF.getDTMFValue("3") == DTMF.THREE);
        assertTrue(DTMF.getDTMFValue("4") == DTMF.FOUR);
        assertTrue(DTMF.getDTMFValue("5") == DTMF.FIVE);
        assertTrue(DTMF.getDTMFValue("6") == DTMF.SIX);
        assertTrue(DTMF.getDTMFValue("7") == DTMF.SEVEN);
        assertTrue(DTMF.getDTMFValue("8") == DTMF.EIGHT);
        assertTrue(DTMF.getDTMFValue("9") == DTMF.NINE);
        assertTrue(DTMF.getDTMFValue("A") == DTMF.A);
        assertTrue(DTMF.getDTMFValue("B") == DTMF.B);
        assertTrue(DTMF.getDTMFValue("C") == DTMF.C);
        assertTrue(DTMF.getDTMFValue("D") == DTMF.D);
        assertTrue(DTMF.getDTMFValue("#") == DTMF.HASH);
        assertTrue(DTMF.getDTMFValue("*") == DTMF.STAR);
        assertTrue(DTMF.getDTMFValue("K") == null);


    }

    public void testToString() throws Exception {
        assertTrue(DTMF.ZERO.toString().equals("0"));
         assertTrue(DTMF.ONE.toString().equals("1"));
         assertTrue(DTMF.TWO.toString().equals("2"));
         assertTrue(DTMF.THREE.toString().equals("3"));
         assertTrue(DTMF.FOUR.toString().equals("4"));
         assertTrue(DTMF.FIVE.toString().equals("5"));
        assertTrue(DTMF.SIX.toString().equals("6"));
         assertTrue(DTMF.SEVEN.toString().equals("7"));
         assertTrue(DTMF.EIGHT.toString().equals("8"));
         assertTrue(DTMF.NINE.toString().equals("9"));
         assertTrue(DTMF.A.toString().equals("A"));
         assertTrue(DTMF.B.toString().equals("B"));
         assertTrue(DTMF.C.toString().equals("C"));
         assertTrue(DTMF.D.toString().equals("D"));
         assertTrue(DTMF.HASH.toString().equals("#"));
         assertTrue(DTMF.STAR.toString().equals("*"));

    }
}