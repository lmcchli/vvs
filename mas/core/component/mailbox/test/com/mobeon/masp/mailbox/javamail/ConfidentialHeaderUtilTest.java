/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.mail.Message;

/**
 * ConfidentialHeaderUtil Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class ConfidentialHeaderUtilTest extends JavamailBaseTestCase {
    public ConfidentialHeaderUtilTest(String name) {
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
        new ConfidentialHeaderUtil();
    }

    public void testIsConfidential() throws Exception {
        Message[] confidentialMessages = getConfidentialMessages();
        for (Message message : confidentialMessages) {
            assertTrue(ConfidentialHeaderUtil.isConfidential(message));
            ConfidentialHeaderUtil.unsetConfidentialHeader(message);
            assertFalse(ConfidentialHeaderUtil.isConfidential(message));
        }
        Message nonConfidentialMessage = getMessage();
        assertFalse(ConfidentialHeaderUtil.isConfidential(nonConfidentialMessage));
        ConfidentialHeaderUtil.setConfidentialHeader(nonConfidentialMessage);
        assertTrue(ConfidentialHeaderUtil.isConfidential(nonConfidentialMessage));
    }

    public static Test suite() {
        return new TestSuite(ConfidentialHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
