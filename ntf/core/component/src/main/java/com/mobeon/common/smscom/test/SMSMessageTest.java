/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.test;

import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.NtfUtil;
import java.io.StringBufferInputStream;
import java.util.*;
import junit.framework.*;

/**
 * Test of SMSMessage, NTFs default character converter
 */
public class SMSMessageTest extends NtfTestCase {
    byte[] bytes = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,};
    SMSMessage msg;

    public SMSMessageTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        msg = new SMSMessage(bytes, 0);
    }

    int posChar(byte b) {
        return b >= 0
            ? b
            : 256 + b;
    }

    /*
     * Test that find finds an entry if it exists, that it finds the correct
     * entry, and that it does not find non-existing entries.
     */
    public void test() throws Exception {
        l("test");

        assertEquals(bytes.length, msg.getText().length);
        assertEquals(bytes.length, msg.getLength());
        assertFalse(msg.isLargerThanOneFragment());
        assertTrue(msg.hasMoreFragments());
        msg.setFragmentSize(3);
        assertTrue(msg.isLargerThanOneFragment());

        assertTrue(msg.hasMoreFragments());
        msg.nextFragment();
        assertEquals(3, msg.getLength());
        assertEquals(0, msg.getFragmentNumber());
        assertEquals(0, msg.getPosition());

        assertTrue(msg.hasMoreFragments());
        msg.nextFragment();
        assertEquals(1, msg.getFragmentNumber());
        assertEquals(3, msg.getLength());
        assertEquals(3, msg.getPosition());

        assertTrue(msg.hasMoreFragments());
        msg.nextFragment();
        assertEquals(2, msg.getFragmentNumber());
        assertEquals(2, msg.getLength());
        assertEquals(6, msg.getPosition());

        assertFalse(msg.hasMoreFragments());
    }
}
