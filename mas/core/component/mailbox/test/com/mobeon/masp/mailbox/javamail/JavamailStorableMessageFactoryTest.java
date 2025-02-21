/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.IStorableMessage;

/**
 * JavamailStorableMessageFactory Tester.
 *
 * @author MANDE
 * @since <pre>12/12/2006</pre>
 * @version 1.0
 */
public class JavamailStorableMessageFactoryTest extends JavamailBaseTestCase {
    private JavamailStorableMessageFactory javamailStorableMessageFactory;

    public JavamailStorableMessageFactoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailStorableMessageFactory = new JavamailStorableMessageFactory();
        javamailStorableMessageFactory.setContextFactory(getJavamailContextFactory());
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreate() throws Exception {
        setUpMockAppender();
        mockAppender.expects(once()).method("doAppend").with(anInfoLog());
        mockAppender.expects(once()).method("doAppend").with(anInfoLog());
        mockAppender.stubs().method("doAppend").with(aDebugLog());
        IStorableMessage storableMessage = javamailStorableMessageFactory.create();
        assertNotNull("Storable message should not be null", storableMessage);
        tearDownMockAppender();
    }

    public static Test suite() {
        return new TestSuite(JavamailStorableMessageFactoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
