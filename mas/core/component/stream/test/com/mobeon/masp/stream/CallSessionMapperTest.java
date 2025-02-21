package com.mobeon.masp.stream;

import org.jmock.MockObjectTestCase;
import org.apache.log4j.xml.DOMConfigurator;
import com.mobeon.masp.execution_engine.session.ISession;

/**
 * This class tests the functionality of the CallSessionMapper.
 */
public class CallSessionMapperTest extends MockObjectTestCase {
    CallSessionMapper theSingletonInstance = null;
    private static ISession sessionA = null;
    private static ISession sessionB = null;
    private static IMediaStream streamA = null;
    private static IMediaStream streamB = null;
    private static Object callA = new Object();
    private static Object callB = new Object();

    public CallSessionMapperTest() {
        if (sessionA ==null) sessionA = (ISession)mock(ISession.class).proxy();
        if (sessionB ==null) sessionB = (ISession)mock(ISession.class).proxy();
        if (streamA ==null) streamA = (IMediaStream)mock(IMediaStream.class).proxy();
        if (streamB ==null) streamB = (IMediaStream)mock(IMediaStream.class).proxy();
    }

    /**
     * Setting up the test cases ...
     */
    public void setUp() {
        theSingletonInstance = CallSessionMapper.getInstance();
    }

    /**
     * Verifying that the singleton getter operates as it should.
     */
    public void testSingleton() {
        assertNotNull(theSingletonInstance);
        assertEquals(theSingletonInstance, CallSessionMapper.getInstance());
    }

    /**
     * Verifying that putSession is ok.
     */
    public void testPutSession() {
        assertNotNull(sessionA);
        assertNotNull(sessionB);
        theSingletonInstance.putSession(callA, streamA, sessionA);
        theSingletonInstance.putSession(callB, streamB, sessionB);
    }

    /**
     * Verifying getSession.
     */
    public void testGetSession() {
        assertNotNull(theSingletonInstance.getSession(callA));
        assertNotNull(theSingletonInstance.getSession(callB));
        assertNotNull(theSingletonInstance.getSession(streamA));
        assertNotNull(theSingletonInstance.getSession(streamB));
        assertNotSame(theSingletonInstance.getSession(callA),
                theSingletonInstance.getSession(callB));
        assertEquals(theSingletonInstance.getSession(callA),
                theSingletonInstance.getSession(streamA));
        assertEquals(theSingletonInstance.getSession(callB),
                theSingletonInstance.getSession(streamB));
    }

    /**
     * Verifying popSession.
     */
    public void testPopSession() {
        assertNotNull(theSingletonInstance.popSession(streamA));
        assertNotNull(theSingletonInstance.getSession(callB));
        assertEquals(sessionB, theSingletonInstance.popSession(streamB));
        assertNull(theSingletonInstance.popSession(streamA));
        assertNull(theSingletonInstance.popSession(streamB));
        assertNull(theSingletonInstance.getSession(callA));
        assertNull(theSingletonInstance.getSession(callB));
    }
}
