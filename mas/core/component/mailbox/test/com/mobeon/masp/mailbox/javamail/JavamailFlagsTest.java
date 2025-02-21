/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * JavamailFlags Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class JavamailFlagsTest extends TestCase {
    public JavamailFlagsTest(String name) {
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
        new JavamailFlags();
    }

    public static Test suite() {
        return new TestSuite(JavamailFlagsTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
