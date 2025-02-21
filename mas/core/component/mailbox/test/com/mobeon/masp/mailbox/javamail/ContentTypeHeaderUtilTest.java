/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * ContentTypeHeaderUtil Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class ContentTypeHeaderUtilTest extends TestCase {
    public ContentTypeHeaderUtilTest(String name) {
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
        new ContentTypeHeaderUtil();
    }

    public static Test suite() {
        return new TestSuite(ContentTypeHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
