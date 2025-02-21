package com.mobeon.masp.callmanager;
/**
 * Created by IntelliJ IDEA.
 * User: mmath
 * Date: 2007-feb-27
 * Time: 11:40:49
 * To change this template use File | Settings | File Templates.
 */

import junit.framework.*;
import java.util.HashSet;

public class SipUtilsTest extends TestCase {
    SipUtils sipUtils;

    String alphanum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "0123456789";
    String unreserved =  alphanum + "-_.!~*´()";
    String userUnreserved = unreserved + "&=+$,;?/";
    String passwordUnreserved = unreserved + "&=+$,";
    String paramUnreserved = unreserved + "[]/:&+$";
    String hnvUnreserved = unreserved + "[]/?:+$";

    public void testIsUnreserved() throws Exception {

        for (char c : unreserved.toCharArray()) {
            assertTrue(SipUtils.isUnreserved(c,SipUtils.UNRESERVED));
        }

        for (char c : userUnreserved.toCharArray()) {
            assertTrue(SipUtils.isUnreserved(c,SipUtils.USER_UNRESERVED));
        }

        for (char c : passwordUnreserved.toCharArray()) {
            assertTrue(SipUtils.isUnreserved(c,SipUtils.PASSWORD_UNRESERVED));
        }

        for (char c : paramUnreserved.toCharArray()) {
            assertTrue(SipUtils.isUnreserved(c,SipUtils.PARAM_UNRESERVED));
        }

        for (char c : hnvUnreserved.toCharArray()) {
            assertTrue(SipUtils.isUnreserved(c,SipUtils.HNV_UNRESERVED));
        }


        // Create strings with all characters NOT in xxx_unreserved
        StringBuffer inv_unreserved = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (unreserved.indexOf(c)<0)
                inv_unreserved.append(c);
        }

        StringBuffer inv_userUnreserved = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (userUnreserved.indexOf(c)<0)
                inv_userUnreserved.append(c);
        }

        StringBuffer inv_passwordUnreserved = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (passwordUnreserved.indexOf(c)<0)
                inv_passwordUnreserved.append(c);
        }

        StringBuffer inv_paramUnreserved = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (paramUnreserved.indexOf(c)<0)
                inv_paramUnreserved.append(c);
        }

        StringBuffer inv_hnvUnreserved = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (hnvUnreserved.indexOf(c)<0)
                inv_hnvUnreserved.append(c);
        }



        for (char c : inv_unreserved.toString().toCharArray()) {
            assertFalse(SipUtils.isUnreserved(c,SipUtils.UNRESERVED));
        }

        for (char c : inv_userUnreserved.toString().toCharArray()) {
            assertFalse(SipUtils.isUnreserved(c,SipUtils.USER_UNRESERVED));
        }

        for (char c : inv_passwordUnreserved.toString().toCharArray()) {
            assertFalse(SipUtils.isUnreserved(c,SipUtils.PASSWORD_UNRESERVED));
        }

        for (char c : inv_paramUnreserved.toString().toCharArray()) {
            assertFalse(SipUtils.isUnreserved(c,SipUtils.PARAM_UNRESERVED));
        }

        for (char c : inv_hnvUnreserved.toString().toCharArray()) {
            assertFalse(SipUtils.isUnreserved(c,SipUtils.HNV_UNRESERVED));
        }


    }


    public void testEscape() throws Exception {

        assertNull(SipUtils.escape(null,SipUtils.UNRESERVED));
        assertNull(SipUtils.escape("abc",null));
        assertEquals("",SipUtils.escape("",""));
        assertEquals("",SipUtils.escape("",SipUtils.UNRESERVED));

        assertEquals("abc%3f%e5%e4%f6",
                SipUtils.escape("abc?והצ",SipUtils.UNRESERVED));

        assertEquals(alphanum,SipUtils.escape(alphanum,""));

        StringBuffer inv_alphanum = new StringBuffer();
        StringBuffer inv_alphanum_escaped = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (alphanum.indexOf(c)<0) {
                inv_alphanum.append(c);
                inv_alphanum_escaped.append("%");
                inv_alphanum_escaped.append(String.format("%02x",(int)c));
            }
        }
        assertEquals(inv_alphanum_escaped.toString(),SipUtils.escape(inv_alphanum.toString(),""));

    }

    public void testUnescape() throws Exception {

        assertNull(SipUtils.unescape(null));
        assertEquals("",SipUtils.unescape(""));
        assertEquals("%",SipUtils.unescape("%"));
        assertEquals("%3",SipUtils.unescape("%3"));
        assertEquals("%1",SipUtils.unescape("%%31"));

        assertEquals(" ",SipUtils.unescape("%20"));
        assertEquals("abc?והצ",
                SipUtils.unescape("abc%3f%e5%e4%f6"));
        assertEquals("%g7=%1l%",SipUtils.unescape("%g7=%1l%"));


        StringBuffer inv_alphanum = new StringBuffer();
        StringBuffer inv_alphanum_escaped = new StringBuffer();
        for (char c=0; c<=255; c++) {
            if (alphanum.indexOf(c)<0) {
                inv_alphanum.append(c);
                inv_alphanum_escaped.append("%");
                inv_alphanum_escaped.append(String.format("%02x", (int) c));
            }
        }
        assertEquals(inv_alphanum.toString(),SipUtils.unescape(inv_alphanum_escaped.toString()));


    }

    /**
     * Test the uniqueness of the generated ICID value
     * @throws Exception
     */
    public void testGenerateICID() throws Exception {

        // Primitive assertion of the uniqueness of the generated ICID's
        // (Max 65536 new ICID's during one second is allowed)
        HashSet<String> set = new HashSet<String>();
        for (int i=0; i<65535; i++) {
            assertTrue(set.add(SipUtils.generateICID()));
        }
        Thread.sleep(1000);
        for (int i=0; i<65535; i++) {
            assertTrue(set.add(SipUtils.generateICID()));
        }
        Thread.sleep(1000);
        for (int i=0; i<65535; i++) {
            assertTrue(set.add(SipUtils.generateICID()));
        }
        set.clear();

    }
}