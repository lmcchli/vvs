/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.mail.Message;

/**
 * UrgentHeaderUtil Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class UrgentHeaderUtilTest extends JavamailBaseTestCase {
    public UrgentHeaderUtilTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCoverage() throws Exception {
        // Only to get full coverage of util class with static methods
        new UrgentHeaderUtil();
    }

    public void testIsUrgent() throws Exception {
        Message[] urgentMessages = getUrgentMessages();
        for (Message message : urgentMessages) {
            assertTrue(UrgentHeaderUtil.isUrgent(message));
            UrgentHeaderUtil.unsetUrgentHeader(message);
            assertFalse(UrgentHeaderUtil.isUrgent(message));
        }
        Message nonUrgentMessage = getMessage();
        assertFalse(UrgentHeaderUtil.isUrgent(nonUrgentMessage));
        UrgentHeaderUtil.setUrgentHeader(nonUrgentMessage);
        assertTrue(UrgentHeaderUtil.isUrgent(nonUrgentMessage));
    }

    public static Test suite() {
        return new TestSuite(UrgentHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
