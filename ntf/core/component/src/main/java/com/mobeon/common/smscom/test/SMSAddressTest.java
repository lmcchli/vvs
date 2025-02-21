/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.test;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComDataException;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.NtfUtil;
import java.util.*;
import junit.framework.*;

/**
 * Test of SMSAddress
 */
public class SMSAddressTest extends NtfTestCase {
    SMSAddress to;

    public SMSAddressTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    /*
     */
    public void test() throws Exception {
        l("test");
        boolean ok = false;
        ok = false; try { to = new SMSAddress(""); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress(",,"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress(",12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress(",,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("1,,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("1,1,1,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("a,b,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("1,b,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        ok = false; try { to = new SMSAddress("a,1,12345"); } catch (SMSComDataException e) {ok = true; }
        if (!ok) { fail("Expected SMSComDataException"); }

        to = new SMSAddress("1,2,33333");
        assertEquals(1, to.getTON());
        assertEquals(2, to.getNPI());
        assertEquals("33333", to.getNumber());
    }
}
