/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SmtpOptions Tester.
 *
 * @author qhast
 */
public class SmtpOptionsTest extends TestCase {
    private SmtpOptions smtpOptions;

    public SmtpOptionsTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        smtpOptions = new SmtpOptions();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSetEnvelopeFrom() throws Exception {
        assertNull("getEnvelopeFrom() should return null o a non-initilized SmtpOptions object", smtpOptions.getEnvelopeFrom());

        smtpOptions.setEnvelopeFrom("hakan.stolt@mobeon.com");
        assertEquals("hakan.stolt@mobeon.com", smtpOptions.getEnvelopeFrom());
    }

    public void testToString() throws Exception {
        assertNotNull(smtpOptions.toString());
    }

    public static Test suite() {
        return new TestSuite(SmtpOptionsTest.class);
    }
}
