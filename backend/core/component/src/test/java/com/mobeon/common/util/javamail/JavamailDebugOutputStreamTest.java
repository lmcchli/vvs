/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JavamailDebugOutputStream Tester.
 *
 * @author qhast
 */
public class JavamailDebugOutputStreamTest extends TestCase {
    private static final String TESTDATA_BLOCK1 = "A587 OK Completed";
    private static final String TESTDATA_BLOCK2 = "SUkqAAgAAkqAAgAgAFNmoA=AgAFSUkqAAgAAkqAAgAgANmoA=" + System.getProperty("line.separator");
    private static final String TESTDATA_BLOCK3 = "A587 OK Completed";

    private StringBuilder buffer = new StringBuilder();
    private JavamailDebugOutputStream os;

    public JavamailDebugOutputStreamTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        os = new JavamailDebugOutputStream() {

            protected void flushMessage(String message) {
                buffer.append(message);
            }
        };
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWritingToStream() throws Exception {

        os.write(TESTDATA_BLOCK1.getBytes());
        os.write(TESTDATA_BLOCK3.getBytes());
        os.close();

        assertEquals(
                JavamailDebugOutputStream.MESSAGE_HEADER + TESTDATA_BLOCK1 + TESTDATA_BLOCK1
                , buffer.toString());

    }

    public void testWritingToStream2() throws Exception {

        os.write(TESTDATA_BLOCK1.getBytes());
        os.write(TESTDATA_BLOCK2.getBytes());
        os.write(TESTDATA_BLOCK3.getBytes());
        os.close();

        assertEquals(
                (JavamailDebugOutputStream.MESSAGE_HEADER + TESTDATA_BLOCK1).substring(0, 20),
                buffer.substring(0, 20)
        );
        assertEquals(TESTDATA_BLOCK3, buffer.substring(buffer.length() - TESTDATA_BLOCK3.length()));

    }

    public static Test suite() {
        return new TestSuite(JavamailDebugOutputStreamTest.class);
    }
}
