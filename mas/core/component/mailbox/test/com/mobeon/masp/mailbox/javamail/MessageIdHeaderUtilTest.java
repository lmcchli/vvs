/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MessageIdHeaderUtil Tester. Currently only instantiates the class for code coverage.
 *
 * @author MANDE
 * @since <pre>12/13/2006</pre>
 * @version 1.0
 */
public class MessageIdHeaderUtilTest extends TestCase {
    public MessageIdHeaderUtilTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Instantiate a MessageIdHeaderUtil so that code is covered
     * @throws Exception
     */
    public void testCoverage() throws Exception {
        new MessageIdHeaderUtil();
    }

    public static Test suite() {
        return new TestSuite(MessageIdHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
