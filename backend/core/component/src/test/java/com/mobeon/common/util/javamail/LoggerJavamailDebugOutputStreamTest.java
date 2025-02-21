/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import com.mobeon.common.logging.ILogger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * LoggerJavamailDebugOutputStream Tester.
 *
 * @author qhast
 */
public class LoggerJavamailDebugOutputStreamTest extends MockObjectTestCase {

    protected static final String TESTDATA_BLOCK1 = "A587 OK Completed" + System.getProperty("line.separator");
    protected static final String TESTDATA_BLOCK2 = "SUkqAAgAAkqAAgAgAFNmoA=AgAFSUkqAAgAAkqAAgAgANmoA=" + System.getProperty("line.separator");
    protected static final String TESTDATA_BLOCK3 = "A587 OK Completed";

    protected Mock loggerMock;


    public LoggerJavamailDebugOutputStreamTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        loggerMock = mock(ILogger.class);

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructWithIllegalArguments() throws Exception {
        try {
            new LoggerJavamailDebugOutputStream(null);
            fail("Constructing with \"logger\" set to null should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    public void testFlushMessage() throws Exception {
        loggerMock.expects(atLeastOnce()).method("debug").with(isA(String.class));
        LoggerJavamailDebugOutputStream logOutputStream = new LoggerJavamailDebugOutputStream((ILogger) loggerMock.proxy());
        logOutputStream.write(TESTDATA_BLOCK1.getBytes());
        logOutputStream.write(TESTDATA_BLOCK2.getBytes());
        logOutputStream.write(TESTDATA_BLOCK3.getBytes());
        logOutputStream.close();

    }

    public static Test suite() {
        return new TestSuite(LoggerJavamailDebugOutputStreamTest.class);
    }
}
