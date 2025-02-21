package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * HostedServiceLogger Tester.
 *
 * @author qhast
 */
public class HostedServiceLoggerTest extends MockObjectTestCase
{
    Mock decoratedLogger;
    HostedServiceLogger hostedServiceLogger;

    public HostedServiceLoggerTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        decoratedLogger = mock(ILogger.class);
        hostedServiceLogger = new HostedServiceLogger((ILogger)decoratedLogger.proxy());
    }

    /**
     * Tests that decorated logger error method is called with a {@link LogJustOnceMessage}.
     * @throws Exception
     */
    public void testLogNotAvailable() throws Exception
    {
        decoratedLogger.expects(once()).method("error").with(isA(LogJustOnceMessage.class));
        decoratedLogger.expects(once()).method("error").with(isA(LogJustOnceMessage.class));
        hostedServiceLogger.notAvailable("imap","bighost",143);
        hostedServiceLogger.notAvailable("imap","bighost",143,"testing");
    }

    /**
     * Tests that decorated logger info method is called with a {@link LogJustOnceMessage}.
     * @throws Exception
     */
    public void testLogAvailable() throws Exception {
        decoratedLogger.expects(once()).method("isInfoEnabled").will(returnValue(true));
        decoratedLogger.expects(once()).method("info").with(isA(LogJustOnceMessage.class));
        hostedServiceLogger.available("imap", "bighost", 143);
        decoratedLogger.expects(once()).method("isInfoEnabled").will(returnValue(true));
        decoratedLogger.expects(once()).method("info").with(isA(LogJustOnceMessage.class));
        hostedServiceLogger.available("radius-ma", "lamholt.capacity1.lab.mobeon.com", 143);
    }

    /**
     * Tests that decorated logger debug methods is called when calling
     * the debug methods on the decorator.
     * @throws Exception
     */
    public void testDebug() throws Exception
    {
        Exception e = new Exception();
        decoratedLogger.stubs().method("isDebugEnabled").will(returnValue(true));
        decoratedLogger.expects(once()).method("debug").with(eq("m"));
        decoratedLogger.expects(once()).method("debug").with(eq("m"),eq(e));
        hostedServiceLogger.debug("m");
        hostedServiceLogger.debug("m",e);
    }

    /**
     * Tests that decorated logger info methods is called when calling
     * the info methods on the decorator.
     * @throws Exception
     */
    public void testInfo() throws Exception
    {
        Exception e = new Exception();
        decoratedLogger.expects(once()).method("isInfoEnabled").will(returnValue(true));
        decoratedLogger.expects(once()).method("info").with(eq("m"));
        decoratedLogger.expects(once()).method("isInfoEnabled").will(returnValue(true));
        decoratedLogger.expects(once()).method("info").with(eq("m"),eq(e));
        hostedServiceLogger.info("m");
        hostedServiceLogger.info("m",e);
    }

    /**
     * Tests that decorated logger warn methods is called when calling
     * the warn methods on the decorator.
     * @throws Exception
     */
    public void testWarn() throws Exception
    {
        Exception e = new Exception();
        decoratedLogger.expects(once()).method("warn").with(eq("m"));
        decoratedLogger.expects(once()).method("warn").with(eq("m"),eq(e));
        hostedServiceLogger.warn("m");
        hostedServiceLogger.warn("m",e);
    }

    /**
     * Tests that decorated logger error methods is called when calling
     * the error methods on the decorator.
     * @throws Exception
     */
    public void testError() throws Exception
    {
        Exception e = new Exception();
        decoratedLogger.expects(once()).method("error").with(eq("m"));
        decoratedLogger.expects(once()).method("error").with(eq("m"),eq(e));
        hostedServiceLogger.error("m");
        hostedServiceLogger.error("m",e);
    }

    /**
     * Tests that decorated logger fatal methods is called when calling
     * the fatal methods on the decorator.
     * @throws Exception
     */
    public void testFatal() throws Exception
    {
        Exception e = new Exception();
        decoratedLogger.expects(once()).method("fatal").with(eq("m"));
        decoratedLogger.expects(once()).method("fatal").with(eq("m"),eq(e));
        hostedServiceLogger.fatal("m");
        hostedServiceLogger.fatal("m",e);
    }

    /**
     * Tests that decorated logger registerSessionInfo method is called when calling
     * the registerSessionInfo method on the decorator.
     * @throws Exception
     */
    public void testRegisterSessionInfo() throws Exception
    {
        decoratedLogger.expects(once()).method("registerSessionInfo").with(eq("n"),eq("s"));
        hostedServiceLogger.registerSessionInfo("n","s");
    }

    /**
     * Tests that decorated logger isDebugEnabled method is called when calling
     * the isDebugEnabled method on the decorator.
     * @throws Exception
     */
    public void testIsDebugEnabled() throws Exception
    {
        decoratedLogger.expects(once()).method("isDebugEnabled").withNoArguments().will(returnValue(true));
        assertTrue("Should return true!",hostedServiceLogger.isDebugEnabled());
    }

    public static Test suite()
    {
        return new TestSuite(HostedServiceLoggerTest.class);
    }
}
