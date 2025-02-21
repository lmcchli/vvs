package com.mobeon.masp.util.executor;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Stub;
import org.jmock.core.Invocation;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TimeoutRetrier Tester.
 *
 * @author mande
 * @since <pre>04/21/2006</pre>
 * @version 1.0
 */
public class TimeoutRetrierTest extends MockObjectTestCase {
    static {
        ILoggerFactory.configureAndWatch("../log4jconf.xml");
    }

    private TimeoutRetrier timeoutRetrier;
    private static final int TRY_TIME_LIMIT = 500;
    private static final int TRY_LIMIT = 3;
    private static final int TIMEOUT = 1000;
    private Mock mockCallable;

    public TimeoutRetrierTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        mockCallable = mock(Callable.class);
        timeoutRetrier = new TimeoutRetrier<Object>((Callable<Object>)mockCallable.proxy(), TimeoutRetrierTest.TRY_LIMIT, TimeoutRetrierTest.TRY_TIME_LIMIT, TimeoutRetrierTest.TIMEOUT);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that ordinary exceptions are not caught by the TimeoutRetrier
     * @throws Exception
     */
    public void testException() throws Exception {
        Exception causeException = new Exception();
        mockCallable.expects(once()).method("call").
                will(throwException(causeException));
        try {
            timeoutRetrier.call();
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertSame("Exception should be same", causeException, e.getCause());
        }
    }

    public void testRetryLimit() throws Exception {
        Exception causeException = new Exception();
        for (int i = 0; i < TimeoutRetrierTest.TRY_LIMIT; i++) {
            mockCallable.expects(once()).method("call").
                    will(throwException(new RetryException(causeException)));
        }
        try {
            timeoutRetrier.call();
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertSame("Exception should be same", causeException, e.getCause());
        }
    }

    public void testTimeLimit() throws Exception {
        Exception causeException = new Exception();
        mockCallable.expects(once()).method("call").
                will(sleepAndThrowException(TimeoutRetrierTest.TRY_TIME_LIMIT + 10, new RetryException(causeException)));
        try {
            timeoutRetrier.call();
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertSame("Exception should be same", causeException, e.getCause());
        }
    }

    public void testTimeout() throws Exception {
        int threadCountStart = Thread.activeCount();
        mockCallable.expects(once()).method("call").
                will(sleep(TimeoutRetrierTest.TIMEOUT + 20));
        try {
            timeoutRetrier.call();
            fail("Expected TimeoutException");
        } catch (TimeoutException e) {
            assertTrue(true); // For statistical purposes
        }
        int threadCountEnd = Thread.activeCount();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.util.executor.TimeoutRetrier");
        int threadPoolSize = threadPoolExecutor.getPoolSize();
        assertTrue("Threads seems to be leaking", threadCountStart > threadCountEnd - threadPoolSize);
    }

    private Stub sleep(int millis) {
        return new TimeoutRetrierTest.SleepStub(millis);
    }

    private Stub sleepAndThrowException(long millis, Exception exception) {
        return new TimeoutRetrierTest.SleepAndThrowStub(millis, exception);
    }

    private class SleepStub implements Stub {
        protected long timeout;

        public SleepStub(long timeout) {
            this.timeout = timeout;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Thread.sleep(timeout);
            return null;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append("sleeps ").append(timeout).append(" ms");
            return buffer;
        }
    }

    private class SleepAndThrowStub extends TimeoutRetrierTest.SleepStub {
        private Exception exception;

        public SleepAndThrowStub(long timeout, Exception exception) {
            super(timeout);
            this.exception = exception;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            super.invoke(invocation);
            throw exception;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            super.describeTo(buffer);
            buffer.append(" and throws <").append(exception).append(">");
            return buffer;
        }
    }

    public static Test suite() {
        return new TestSuite(TimeoutRetrierTest.class);
    }
}
