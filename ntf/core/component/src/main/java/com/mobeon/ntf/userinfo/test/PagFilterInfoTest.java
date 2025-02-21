/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestUser;
import com.mobeon.ntf.userinfo.PagFilterInfo;
import java.util.*;
import junit.framework.*;

/**
 * This class tests SmsFilterInfo.
 */
public class PagFilterInfoTest extends NtfTestCase {

    public PagFilterInfoTest(String name) {
	super(name);
    }

    private TestUser user;

    protected void setUp() {
        user = new TestUser();
    }

    public void testPncString() {
        user.setPnc("123456789+++1111++33333+@#H");
        PagFilterInfo filter = new PagFilterInfo(user);
        assertEquals("123456789", filter.getNumber());
        assertEquals("+++1111++33333+@#", filter.getContent());
        assertTrue(filter.getHangup());

        user.setPnc("121212+abc++c+c+c++123abc+@#++h");
        PagFilterInfo filter2 = new PagFilterInfo(user);
        assertEquals("121212", filter2.getNumber());
        assertEquals("+abc++c+c+c++123abc+@#++", filter2.getContent());
        assertTrue(filter2.getHangup());

        user.setPnc("343434++++++++++@++");
        PagFilterInfo filter3 = new PagFilterInfo(user);
        assertEquals("343434", filter3.getNumber());
        assertEquals("++++++++++@++", filter3.getContent());
        assertTrue(!filter3.getHangup());
    }
}
