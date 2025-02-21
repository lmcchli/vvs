/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.pool;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory;
import com.mobeon.masp.profilemanager.ProfileManagerMockObjectBaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InterruptedNamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * Date: 2006-maj-03
 *
 * @author ermmaha
 */
public class DirContextPoolTest extends ProfileManagerMockObjectBaseTestCase {
    private static final String LOG4J_CONFIGURATION = "../log4jconf.xml";

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected Hashtable<String, String> dirContextEnv = new Hashtable<String, String>();

    protected Mock mockDirContext;

    public DirContextPoolTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        dirContextEnv.put(Context.PROVIDER_URL, "ldap://ldap.url");
        dirContextEnv.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
        dirContextEnv.put(Context.SECURITY_CREDENTIALS, "emmanager");
        dirContextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory");
        mockDirContext = mock(DirContext.class);
        MockLdapCtxFactory.setDirContext((DirContext)mockDirContext.proxy());
        MockLdapCtxFactory.setNumCreatedContexts(0);

        // to reset the tests (singletons are impossible to test...)
        String url = dirContextEnv.get(Context.PROVIDER_URL);
        DirContextPoolManager.getInstance().removeDirContextPool(url);
    }

    /**
     * @throws Exception if testcase fails.
     */
    public void testPool() throws Exception {
        DirContextPoolManager.getInstance().setMaxSize(2);
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));
        DirContext context = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        DirContext context2 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        assertEquals(2, pool.getSize());
        DirContextPoolManager.getInstance().returnDirContext(context);
        // It is also possible to just close a context, but cannot be used for reporting error
        context2.close();
        assertEquals(2, pool.getSize());
        context = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        mockDirContext.expects(once()).method("close");
        DirContextPoolManager.getInstance().returnDirContext(context, true);
        assertEquals(1, pool.getSize());
        // Try return the same context twice
        DirContextPoolManager.getInstance().returnDirContext(context, true);
        assertEquals(1, pool.getSize());
    }

    /**
     * @throws Exception if testcase fails.
     */
    public void testPoolTimeout() throws Exception {
        DirContextPoolManager.getInstance().setMaxSize(1);
        DirContextPoolManager.getInstance().setTimeoutLimit(100);
        DirContextPoolManager.getInstance().getDirContext(dirContextEnv);

        try {
            // try to get a second connection (should timeout when waiting for a free one)
            DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test pool when interrupted
     * @throws Exception
     */
    public void testPoolInterrupted() throws Throwable {
        DirContextPoolManager.getInstance().setMaxSize(1);
        DirContextPoolManager.getInstance().setTimeoutLimit(3000);
        DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    // try to get a second connection (should wait for a free one)
                    DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
                    fail("Expected NamingException");
                } catch (NamingException e) {
                    assertTrue("Expected InterruptedNamingException", e instanceof InterruptedNamingException);
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        sleep(500);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }

    /**
     * Make some threads that waits for the other threads to return their context
     *
     * @throws Exception if test case fails.
     */
    public void testPoolWait() throws Throwable {
        DirContextPoolManager.getInstance().setMaxSize(1);
        DirContextPoolManager.getInstance().setTimeoutLimit(1000);

        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));
        Context context = pool.getDirContext(dirContextEnv);
        int size = 5;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        // each thread will wait max 1 seconds for a free connection
                        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));
                        Context context = pool.getDirContext(dirContextEnv);
                        threadSleep(1);
                        pool.returnDirContext(context);
                    } catch (Exception e) {
                        fail("Exception in run " + e);
                    }
                }
            });
            threads[i].setUncaughtExceptionHandler(this);
            threads[i].start();
        }
        threadSleep(100);
        pool.returnDirContext(context);
        for (Thread thread : threads) {
            thread.join();
            if (exceptionMap.containsKey(thread)) {
                throw exceptionMap.get(thread);
            }
        }
    }

    /**
     * @throws Exception if test case fails.
     */
    public void testPoolRelease() throws Exception {
        DirContextPoolManager.getInstance().setForcedReleaseContextLimit(0);
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));

        mockDirContext.expects(once()).method("close");

        Context context = pool.getDirContext(dirContextEnv);
        pool.returnDirContext(context);

        assertEquals(0, pool.getSize());
    }

    /**
     * Tests get/returnDirContext in threaded environment. Unknown DirContexts should not be returned,
     * indicated by usedConnections not zero at program end.
     * @throws Exception if test case fails.
     */
    public void testPoolMT() throws Throwable {

        int iterations = 20;
        final int times = 10;
        mockDirContext.stubs().method("close");
        DirContextPoolManager.getInstance().setForcedReleaseContextLimit(5000);
        DirContextPoolManager.getInstance().setMaxSize(80);
        DirContextPoolManager.getInstance().setTimeoutLimit(100000);
        int size = 100;
        for (int i = 0; i < iterations; i++) {
            Thread[] threads = new Thread[size];
            for (int ii = 0; ii < size; ii++) {
                threads[ii] = new Thread(new Runnable() {
                    public void run() {
                        for (int i = 0; i < times; i++) {
                            try {
                                DirContext context = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
                                DirContextPoolManager.getInstance().returnDirContext(context);
                            } catch (Exception e) {
                                fail("Exception in run: " + e + ", " + e.getStackTrace());
                            }
                        }
                    }
                });
                threads[ii].setUncaughtExceptionHandler(this);
                threads[ii].start();
            }
            for (Thread thread : threads) {
                thread.join();
                if (exceptionMap.containsKey(thread)) {
                    throw exceptionMap.get(thread);
                }
            }
        }
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool("ldap://ldap.url");
        assertEquals("All contexts should have been returned", 0, pool.getUsedContexts());
    }

    /**
     * Verifies that correct exceptions are thrown.
     *
     * @throws Exception if test case fails.
     */
    public void testPoolErrors() throws Exception {
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));

        try {
            // tell the InitialContextFactory to throw a NamingException
            MockLdapCtxFactory.throwInitialException(new NamingException("namingexception"));
            pool.getDirContext(dirContextEnv);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertTrue(true); // For statistical purposes
        }

        try {
            // set the mocked context to null so that the getInitialContext returns null
            MockLdapCtxFactory.setDirContext(null);
            pool.getDirContext(dirContextEnv);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testContextReuse() throws Exception {
        DirContextPoolManager.getInstance().setMaxSize(2);
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(dirContextEnv.get(Context.PROVIDER_URL));
        DirContext context = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        DirContext context2 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);

        assertEquals(2, pool.getSize());
        DirContextPoolManager.getInstance().returnDirContext(context);
        // It is also possible to just close a context, but cannot be used for reporting error
        context2.close();
        assertEquals(2, pool.getSize());
        assertEquals(2, MockLdapCtxFactory.getNumCreatedContexts());
        DirContext context3 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        assertTrue(context3 == context || context3 == context2);
        DirContext context4 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        assertTrue(context4 == context || context4 == context2);
        assertEquals(2, MockLdapCtxFactory.getNumCreatedContexts());

        mockDirContext.stubs().method("close");
        DirContextPoolManager.getInstance().returnDirContext(context4, true);  // release this context
        DirContextPoolManager.getInstance().returnDirContext(context3, true);  // release this context

        assertEquals(0, pool.getSize());

        // since context3 and context4 were released, we should now get new contexts
        DirContext context5 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        DirContext context6 = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
        assertEquals(4, MockLdapCtxFactory.getNumCreatedContexts());

        assertTrue(context5 != context3 && context5 != context4);
        assertTrue(context6 != context3 && context5 != context6);
    }

    public static void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }

    public static Test suite() {
        return new TestSuite(DirContextPoolTest.class);
    }
}
