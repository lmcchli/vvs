/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * MessageUtil Tester.
 *
 * @author qhast
 */
public class MessageUtilTest extends TestCase {
    public MessageUtilTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClassCall() throws Exception {
        new MessageUtil();
    }

    public void testEnableStorageFlagKeeping() throws Exception {
        MessageUtil.enableStorageFlagKeeping(new MimeMessage(Session.getInstance(new Properties())));
    }

    public static Test suite() {
        return new TestSuite(MessageUtilTest.class);
    }
}
