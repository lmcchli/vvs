/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.slamdown.CallerInfo;
import java.util.*;
import junit.framework.*;

/**
 * This class tests SlamdownList.
 */
public class SlamdownListTest extends NtfTestCase {

    SlamdownList tc;
    SlamdownList info;

    public SlamdownListTest(String name) {
        super(name);
    }


    protected void setUp() {
    }

    public void test() throws Exception {
        l("test");
        info = new SlamdownList("123456", "u1@host.domain.tld", "lang", "smsc", 24, "GoldCos");
        assertEquals("123456", info.getNumber());
        assertEquals("u1@host.domain.tld", info.getMail());
        assertEquals("lang", info.getPreferredLanguage());
        assertEquals("smsc", info.getSmsc());
        assertEquals(0, info.getState());
        assertEquals("GoldCos", info.getCosName());
        for (byte i = 0; i < 7; i++) {
            info.setState(i);
            assertEquals(i, info.getState());
        }
        // should stay in DONE==6
        info.setState((byte)7);
        assertEquals(6, info.getState() );

        assertEquals(0, info.getRetryCount());
        for (byte i = 0; i < 10; i++) {
            info.incrRetryCount();
        }
        assertEquals(10, info.getRetryCount());
        info.resetRetryCount();
        assertEquals(0, info.getRetryCount());


        SlamdownList sl2 = new SlamdownList("234567", "u2@host.domain.tld", "en", "cimd2", 24, "GoldCos");
        assertEquals("234567", sl2.getNumber());
        assertEquals("u2@host.domain.tld", sl2.getMail());
        assertEquals("en", sl2.getPreferredLanguage());
        assertEquals("cimd2", sl2.getSmsc());
        assertEquals("en", sl2.getPreferredLanguage());
        assertEquals("cimd2", sl2.getSmsc());
        assertEquals("GoldCos", sl2.getCosName());

        SlamdownList sl3 = new SlamdownList("345678", "u3@host.domain.tld", "lang", "smsc", 24, "GoldCos");
        assertEquals("345678", sl3.getNumber());
        assertEquals("u3@host.domain.tld", sl3.getMail());
        assertEquals("lang", sl3.getPreferredLanguage());
        assertEquals("smsc", sl3.getSmsc());
        assertTrue(info.getPreferredLanguage() == sl3.getPreferredLanguage());
        assertTrue(info.getSmsc() == sl3.getSmsc());
        assertTrue(info.getCosName() == sl3.getCosName());

    }

    private int getCount( CallerInfo[] callers ) {
        int count = 0;
        for( int i=0;i<callers.length;i++ ) {
            if( callers[i] != null )
                count++;
        }
        return count;
    }

    public void testCaller() throws Exception {
        l("testCaller");
        info = new SlamdownList("123456", "u1@host.domain.tld", "lang", "smsc", 24, "GoldCos");
        CallerInfo[] res = info.getCallers();
        assertNotNull(res);

        assertEquals(0, getCount( res ));

        info.voiceSlamdown("1111", null);
        res = info.getCallers();
        assertEquals(1, getCount(res));
        info.voiceSlamdown("1111", null);
        res = info.getCallers();
        assertEquals(1, getCount(res));
        info.voiceSlamdown("2222", null);
        res = info.getCallers();
        assertEquals(2, getCount(res));
        info.voiceSlamdown("1111", new Date(new Date().getTime() + 1010L));
        res = info.getCallers();
        assertEquals(2, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());

        info.voiceSlamdown("3333", new Date(new Date().getTime() + 2010L));
        res = info.getCallers();
        assertEquals(3, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());
        assertEquals("3333", res[2].getNumber());

        info.voiceSlamdown("4444", new Date(new Date().getTime() + 3010L));
        res = info.getCallers();
        assertEquals(4, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());
        assertEquals("3333", res[2].getNumber());
        assertEquals("4444", res[3].getNumber());

        info.voiceSlamdown("5555", new Date(new Date().getTime() + 4010L));
        res = info.getCallers();
        assertEquals(5, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());
        assertEquals("3333", res[2].getNumber());
        assertEquals("4444", res[3].getNumber());
        assertEquals("5555", res[4].getNumber());

        info.voiceSlamdown("6666", new Date(new Date().getTime() + 5010L));
        res = info.getCallers();
        assertEquals(6, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());
        assertEquals("3333", res[2].getNumber());
        assertEquals("4444", res[3].getNumber());
        assertEquals("5555", res[4].getNumber());
        assertEquals("6666", res[5].getNumber());

        info.voiceSlamdown("7777", new Date(new Date().getTime() + 6010L));
        res = info.getCallers();
        assertEquals(7, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("2222", res[1].getNumber());
        assertEquals("3333", res[2].getNumber());
        assertEquals("4444", res[3].getNumber());
        assertEquals("5555", res[4].getNumber());
        assertEquals("6666", res[5].getNumber());
        assertEquals("7777", res[6].getNumber());

        assertEquals(3, res[0].getVoiceCount());
        assertEquals(1, res[1].getVoiceCount());
        assertEquals(1, res[6].getVoiceCount());

        info.removeCallerAt(6); //7777
        res = info.getCallers();
        assertEquals(6, getCount(res));
        assertEquals("1111", res[0].getNumber());
        assertEquals("6666", res[5].getNumber());

        info.removeCallerAt(0); //1111
        res = info.getCallers();
        assertEquals(5, getCount(res));
        assertEquals("2222", res[0].getNumber());
        assertEquals("6666", res[4].getNumber());

        info.removeCallerAt(2); //4444
        res = info.getCallers();
        assertEquals(4, getCount(res));
        assertEquals("2222", res[0].getNumber());
        assertEquals("3333", res[1].getNumber());
        assertEquals("5555", res[2].getNumber());
        assertEquals("6666", res[3].getNumber());

        info.removeCallerAt(2); //5555
        info.removeCallerAt(0); //1111
        info.removeCallerAt(1); //6666
        res = info.getCallers();
        assertEquals(1, getCount(res));
        assertEquals("3333", res[0].getNumber());
        info.removeCallerAt(0); //3333
        res = info.getCallers();
        assertEquals(0, getCount(res));
    }
}
