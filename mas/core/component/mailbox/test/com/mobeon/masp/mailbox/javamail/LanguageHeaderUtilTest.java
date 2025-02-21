/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * LanguageHeaderUtil Tester. Currently only instantiates the class for code coverage.
 *
 * @author MANDE
 * @since <pre>12/13/2006</pre>
 * @version 1.0
 */
public class LanguageHeaderUtilTest extends TestCase {
    public LanguageHeaderUtilTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Instantiate a LanguageHeaderUtil so that code is covered
     * @throws Exception
     */
    public void testCoverage() throws Exception {
        new LanguageHeaderUtil();
    }

    public static Test suite() {
        return new TestSuite(LanguageHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
