/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.text.Phrases;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.slamdown.SlamdownFormatter;
import com.mobeon.ntf.slamdown.SlamdownList;
import java.util.*;
import junit.framework.*;

/**
 * This class tests SlamdownFormatter.
 */
public class SlamdownFormatterTest extends NtfTestCase {

    SlamdownFormatter tc;
    SlamdownList info;
    Properties phr = null;
    long whenl = (2004 - 1970) * 365L * 24 * 3600 * 1000;
    Date when = new Date(whenl);

    public SlamdownFormatterTest(String name) {
	super(name);
    }


    protected void setUp() {
        phr = new Properties();
        TextCreator.get();
    }

    public void testNoConfiguration() throws Exception {
	l("testNoConfiguration");
        //Strange language
        //SlamdownFormatter.setTestPhrases(phr);
        Phrases.clearPhrases();
        TextCreator.get().reset();
        info = new SlamdownList("123456", "um1", "nosuchlanguage", "smsc", 24, "GoldCos");
        info.voiceSlamdown("987654", when);
        assertEquals("Callers:\n", SlamdownFormatter.formatHeader(info));
        assertEquals("", SlamdownFormatter.formatFooter(info));
        String[] body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(1, body.length);
        assertEquals("987654\n", body[0]);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[0]
                       + SlamdownFormatter.formatFooter(info));
    }

    public void testNormal() throws Exception {
	l("testNormal");
            /*String templates = "slamdownheader = { " +
                    "\"Calls with no message left:\" UNICODE=0041 }\n " +
                    "slamdownfooter = { \"--------\" \\u000a }\n" +
                    "slamdownbody = {" +
                    "\"[\" COUNT \"] \" FROM \", \" DATE=yyyy_dMMM_hh:mm \\u000A" +
                    "}\n";
                    */

        String templates = "slamdownheader = { " +
            "\"Calls \" \\u0644 \" with no 123 message \" UNICODE=0b1 UNICODE=0644 \" left:\" \\u00ab \\u00a3 }\n " +
            "slamdownfooter = { \"--------\" \\u00ab }\n" +
                "slamdownbody = {" +
                "\"[\" COUNT \"] \" FROM \", \" DATE=yyyy_dMMM_hh:mm \\u000A" +
                "}\n";



        Phrases.clearPhrases();
        TextCreator.get().reset();
        Phrases.addPhraseString(templates, "en", null);

        info = new SlamdownList("123456", "um1", "en", "smsc", 24, "GoldCos");
        info.voiceSlamdown("987654", when);
        info.voiceSlamdown("987654", when);
        info.voiceSlamdown("876543", when);


            assertEquals("Calls \u0644 with no 123 message \u00b1\u0644 left:\u00ab\u00a3", SlamdownFormatter.formatHeader(info));
            assertEquals("--------\u00ab", SlamdownFormatter.formatFooter(info));



        String[] body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(2, body.length);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[0]
                       + body[1]
                       + SlamdownFormatter.formatFooter(info));
            assertEquals("[2] 987654, 2003 23Dec 07:00\n", body[0]);
            assertEquals("[1] 876543, 2003 23Dec 07:00\n", body[1]);
        Phrases.clearPhrases();
        TextCreator.get().reset();
    }

    public void testLang() throws Exception {
	l("testLang");
        String templates = "slamdownheader = { " +
                "\"Missade samtal:\" UNICODE=000A }\n " +
                "slamdownfooter = { \"\" }\n" +
                "slamdownbody = {" +
                "\"[\" COUNT \"] \" FROM \", \" DATE=dd_MMM_yy_hh:mm \\u000A" +
                "}\n";

        Phrases.clearPhrases();
        TextCreator.get().reset();
        Phrases.addPhraseString(templates, "sv", "goldcos");

        //phr.setProperty("slamdownheader", "Missade samtal:\n");
        //phr.setProperty("slamdownfooter", "");
        //phr.setProperty("slamdownbody", "[__COUNT__] __FROM__, __TIME__\n");
        //phr.setProperty("slamdowntimeformat", "dd MMM yy hh:mm");
        //SlamdownFormatter.setTestPhrases(phr);

        info = new SlamdownList("123456", "um1", "sv", "smsc", 24, "goldcos");
        info.voiceSlamdown("121212", when);
        info.voiceSlamdown("111111", new Date(whenl - 1L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("101010", new Date(whenl - 2L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("090909", new Date(whenl - 3L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("080808", new Date(whenl - 4L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("070707", new Date(whenl - 5L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("060606", new Date(whenl - 6L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("050505", new Date(whenl - 7L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("040404", new Date(whenl - 8L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("030303", new Date(whenl - 9L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("020202", new Date(whenl - 10L * 30 * 24 * 3600 * 1000));
        info.voiceSlamdown("010101", new Date(whenl - 11L * 30 * 24 * 3600 * 1000));
        assertEquals("Missade samtal:\n", SlamdownFormatter.formatHeader(info));
        assertEquals("", SlamdownFormatter.formatFooter(info));
        String[] body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(12, body.length);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[0]
                       + body[1]
                       + body[2]
                       + body[3]
                       + body[4]
                       + body[5]
                       + body[6]
                       + body[7]
                       + body[8]
                       + body[9]
                       + body[10]
                       + body[11]
                       + SlamdownFormatter.formatFooter(info));
        assertEquals("[1] 121212, 24 dec 03 01:00\n", body[11]);
        assertEquals("[1] 111111, 24 nov 03 01:00\n", body[10]);
        assertEquals("[1] 101010, 25 okt 03 02:00\n", body[9]);
        assertEquals("[1] 090909, 25 sep 03 02:00\n", body[8]);
        assertEquals("[1] 080808, 26 aug 03 02:00\n", body[7]);
        assertEquals("[1] 070707, 27 jul 03 02:00\n", body[6]);
        assertEquals("[1] 060606, 27 jun 03 02:00\n", body[5]);
        assertEquals("[1] 050505, 28 maj 03 02:00\n", body[4]);
        assertEquals("[1] 040404, 28 apr 03 02:00\n", body[3]);
        assertEquals("[1] 030303, 29 mar 03 01:00\n", body[2]);
        assertEquals("[1] 020202, 27 feb 03 01:00\n", body[1]);
        assertEquals("[1] 010101, 28 jan 03 01:00\n", body[0]);
        Phrases.clearPhrases();
        TextCreator.get().reset();
    }

    public void testLimits() throws Exception {
	l("testLimits");
        String templates = "slamdownheader = { " +
                "\"Callers:\" UNICODE=000A }\n " +
                "slamdownfooter = { \"\" }\n" +
                "slamdownbody = {" +
                "\"[\" COUNT \"] \" FROM \\u000A" +
                "}\n";

        Phrases.clearPhrases();
        TextCreator.get().reset();
        Phrases.addPhraseString(templates, "en", null);

        //phr.setProperty("slamdownheader", "Callers:\n");
        //phr.setProperty("slamdownfooter", "");
        //phr.setProperty("slamdownbody", "[__COUNT__] __FROM__\n");
        //phr.setProperty("slamdowntimeformat", "dMMM hh:mm");
        //SlamdownFormatter.setTestPhrases(phr);

        info = new SlamdownList("123456", "um1", "en", "smsc", 24, "goldcos");
        for (int i = 0; i < 20; i++) {
            info.voiceSlamdown("55555", when);
        }
        info.voiceSlamdown("666666", new Date(whenl + 1000));
        info.voiceSlamdown("7777777", new Date(whenl + 2000));
        info.voiceSlamdown("999999999", new Date(whenl + 3000));
        assertEquals("Callers:\n", SlamdownFormatter.formatHeader(info));
        assertEquals("", SlamdownFormatter.formatFooter(info));

        Config.setCfgVar("slamdownmaxcallspercaller", "0");
        String[] body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(4, body.length);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[0]
                       + body[1]
                       + body[2]
                       + body[3]
                       + SlamdownFormatter.formatFooter(info));
        assertEquals("[20] 55555\n", body[0]);
        assertEquals("[1] 666666\n", body[1]);
        assertEquals("[1] 7777777\n", body[2]);
        assertEquals("[1] 999999999\n", body[3]);

        Config.setCfgVar("slamdownmaxcallspercaller", "9");
        Config.setCfgVar("slamdownmaxdigitsinnumber", "6");
        body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(4, body.length);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[0]
                       + body[1]
                       + body[2]
                       + body[3]
                       + SlamdownFormatter.formatFooter(info));
        assertEquals("[9] 55555\n", body[0]);
        assertEquals("[1] 666666\n", body[1]);
        assertEquals("[1] *77777\n", body[2]);
        assertEquals("[1] *99999\n", body[3]);

        Config.setCfgVar("slamdownmaxcallspercaller", "12");
        Config.setCfgVar("slamdownmaxdigitsinnumber", "5");
        Config.setCfgVar("slamdowntruncatednumberindication", "");
        body = SlamdownFormatter.formatBody(info);
        assertNotNull(body);
        assertEquals(4, body.length);
        log.logMessage(SlamdownFormatter.formatHeader(info)
                       + body[3]
                       + body[2]
                       + body[1]
                       + body[0]
                       + SlamdownFormatter.formatFooter(info));
        assertEquals("[12] 55555\n", body[0]);
        assertEquals("[1] 66666\n", body[1]);
        assertEquals("[1] 77777\n", body[2]);
        assertEquals("[1] 99999\n", body[3]);
        Phrases.clearPhrases();
        TextCreator.get().reset();
    }
}
