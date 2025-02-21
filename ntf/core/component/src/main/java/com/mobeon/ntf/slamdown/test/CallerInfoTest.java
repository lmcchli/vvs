/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.time.NtfTime;
import java.util.*;
import junit.framework.*;

/**
 * This class tests CallerInfo.
 */
public class CallerInfoTest extends NtfTestCase {

    CallerInfo to;

    public CallerInfoTest(String name) {
	super(name);
    }


    protected void setUp() {
    }

    public void testNumber() throws Exception {
	l("testNumber");
        to = CallerInfo.create("");
        assertEquals("", to.getNumber());
        to = CallerInfo.create("1");
        assertEquals("1", to.getNumber());
        to = CallerInfo.create("12");
        assertEquals("12", to.getNumber());
        to = CallerInfo.create("123");
        assertEquals("123", to.getNumber());
        to = CallerInfo.create("1234");
        assertEquals("1234", to.getNumber());
        to = CallerInfo.create("12345");
        assertEquals("12345", to.getNumber());
        to = CallerInfo.create("123456");
        assertEquals("123456", to.getNumber());
        to = CallerInfo.create("1234567");
        assertEquals("1234567", to.getNumber());
        to = CallerInfo.create("12345678");
        assertEquals("12345678", to.getNumber());
        to = CallerInfo.create("123456789");
        assertEquals("123456789", to.getNumber());
        to = CallerInfo.create("1234567890");
        assertEquals("1234567890", to.getNumber());
        to = CallerInfo.create("12345678901");
        assertEquals("12345678901", to.getNumber());
        to = CallerInfo.create("123456789012");
        assertEquals("123456789012", to.getNumber());
        to = CallerInfo.create("1234567890123");
        assertEquals("1234567890123", to.getNumber());
        to = CallerInfo.create("12345678901234");
        assertEquals("12345678901234", to.getNumber());
        to = CallerInfo.create("123456789012345");
        assertEquals("123456789012345", to.getNumber());
        to = CallerInfo.create("1234567890123456");
        assertEquals("1234567890123456", to.getNumber());
        to = CallerInfo.create("12345678901234567");
        assertEquals("12345678901234567", to.getNumber());
        to = CallerInfo.create("123456789012345678");
        assertEquals("123456789012345678", to.getNumber());
        to = CallerInfo.create("1234567890123456789");
        assertEquals("1234567890123456789", to.getNumber());
        to = CallerInfo.create("12345678901234567890");
        assertEquals("12345678901234567890", to.getNumber());
    }

    public void testLeadingZero() throws Exception {
	l("testLeadingZero");
        to = CallerInfo.create("0");
        assertEquals("0", to.getNumber());
        to = CallerInfo.create("00000");
        assertEquals("00000", to.getNumber());
        to = CallerInfo.create("000001");
        assertEquals("000001", to.getNumber());
        to = CallerInfo.create("00000100000");
        assertEquals("00000100000", to.getNumber());
    }

    public void testNonNumeric() throws Exception {
	l("testNonNumeric");
        to = CallerInfo.create("");
        assertEquals("", to.getNumber());
        to = CallerInfo.create("x");
        assertEquals("x", to.getNumber());
        to = CallerInfo.create("0x");
        assertEquals("0x", to.getNumber());
        to = CallerInfo.create("1x");
        assertEquals("1x", to.getNumber());
        to = CallerInfo.create("abcedfghijklmnopqrstuvwxyz@,./;'[]+-=");
        assertEquals("abcedfghijklmnopqrstuvwxyz@,./;'[]+-=", to.getNumber());
    }

    public void testPacking() throws Exception {
        l("testPacking");
        to = CallerInfo.create("1234567");
        assertEquals("com.mobeon.ntf.slamdown.CallerInfo$Numeric", to.getClass().getName());
        to = CallerInfo.create("12345678901234567");
        assertEquals("com.mobeon.ntf.slamdown.CallerInfo$Numeric", to.getClass().getName());
        to = CallerInfo.create("123456789012345678");
        assertEquals("com.mobeon.ntf.slamdown.CallerInfo$Other", to.getClass().getName());
        to = CallerInfo.create("xyz");
        assertEquals("com.mobeon.ntf.slamdown.CallerInfo$Other", to.getClass().getName());
    }

    public void testCount() throws Exception {
        l("testCount");
        to = CallerInfo.create("1234567");
        assertEquals(0, to.getVoiceCount());
        to.voiceSlamdown(null);
        assertEquals(1, to.getVoiceCount());
        to.voiceSlamdown(null);
        assertEquals(2, to.getVoiceCount());
        for (int i = 0; i < 1000; i++) {
            to.voiceSlamdown(null);
        }
        assertEquals(1002, to.getVoiceCount());
    }

    public void testDate() throws Exception {
        l("testDate");
        to = CallerInfo.create("1234567");
        Date before = new Date();
        to.voiceSlamdown(null);
        Date after = new Date();
        Date slam = to.getCallTime();
        assertNotNull(slam);
        //The slamdown time can be later than the before time, but due to
        //rounding, it can also be up to one second earlier than the before time
        assertTrue(slam.getTime() + 999 > before.getTime());
        assertTrue(slam.before(after));

        Date d = new GregorianCalendar(2004, 7, 1, 13, 6, 12).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d = new GregorianCalendar(2010, 8, 2, 14, 7, 13).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d = new GregorianCalendar(2020, 7, 1, 13, 6, 12).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d = new GregorianCalendar(2030, 12, 31, 23, 59, 59).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d = new GregorianCalendar(2040, 12, 31, 23, 59, 59).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d = new GregorianCalendar(2050, 12, 31, 23, 59, 59).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);
    }

    public void testFirstDate() throws Exception {
        l("testFirstDate");
        Config.setCfgVar("slamdowntimeoflastcall", "no");
        to = CallerInfo.create("1234567");
        Date before = new Date();
        to.voiceSlamdown(null);
        Date after = new Date();
        Date slam = to.getCallTime();
        assertNotNull(slam);
        //The slamdown time can be later than the before time, but due to
        //rounding, it can also be up to one second earlier than the before time
        assertTrue(slam.getTime() + 999 > before.getTime());
        assertTrue(slam.before(after));

        to = CallerInfo.create("1234567");
        Date d = new GregorianCalendar(2004, 7, 1, 13, 6, 12).getTime();
        to.voiceSlamdown(d);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        Date d2 = new GregorianCalendar(2010, 8, 2, 14, 7, 13).getTime();
        to.voiceSlamdown(d2);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);

        d2 = new GregorianCalendar(2000, 7, 1, 13, 6, 12).getTime();
        to.voiceSlamdown(d2);
        slam = to.getCallTime();
        log.logMessage("dateDiff: " + Math.abs(slam.getTime() - d.getTime()));
        assertEquals(d, slam);
    }

    public void testCompare() throws Exception {
	l("testCompare");
        Config.setCfgVar("slamdowntimeoflastcall", "yes");
        Date d = new Date();
        Date d2 = new Date(d.getTime() + 1100);
        to = CallerInfo.create("2345678");
        CallerInfo to2 = CallerInfo.create("1234567");
        to.voiceSlamdown(d);
        to2.voiceSlamdown(d);
        assertEquals(0, to.compareTo(to2));
        to2.voiceSlamdown(d2);
        log.logMessage("d=" + d + ", d2=" + d2);
        log.logMessage("d=" + d.getTime() + ", d2=" + d2.getTime());
        log.logMessage("to=" + to.getCallTime() + ", to2=" + to2.getCallTime());
        log.logMessage("to=" + to.getCallTime().getTime() + ", to2=" + to2.getCallTime().getTime());
        assertTrue(to.compareTo(to2) < 0);
        assertTrue(to2.compareTo(to) > 0);
    }
}
