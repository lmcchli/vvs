/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.pool;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ProfileManagerMockObjectBaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import java.util.Hashtable;

/**
 * Date: 2006-apr-27
 *
 * @author ermmaha
 */
public class DirContextPoolMTest extends ProfileManagerMockObjectBaseTestCase {

    private static final String LOG4J_CONFIGURATION = "../trafficeventsender/log4jconf.xml";

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected Hashtable<String, String> dirEnvironment;
    protected Hashtable<String, String> dirEnvironment2;
    private String providerUrl;
    private String providerUrl2;

    public DirContextPoolMTest(String string) {
        super(string);
    }

    public void setUp() {
        dirEnvironment = new Hashtable<String, String>();
        dirEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        dirEnvironment.put(Context.PROVIDER_URL, "ldap://polaris.ipms.su.erm.abcxyz.se:389/o=abcxyz.se");
        dirEnvironment.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
        dirEnvironment.put(Context.SECURITY_CREDENTIALS, "emmanager");

        // anonymous bind
        dirEnvironment2 = new Hashtable<String, String>();
        dirEnvironment2.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        dirEnvironment2.put(Context.PROVIDER_URL, "ldap://ockelbo.lab.mobeon.com:389/o=mobeon.com");

        providerUrl = dirEnvironment.get(Context.PROVIDER_URL);
        providerUrl2 = dirEnvironment2.get(Context.PROVIDER_URL);

        // Make sure that each test gets a new pool
        DirContextPoolManager.getInstance().removeDirContextPool(providerUrl);
        DirContextPoolManager.getInstance().removeDirContextPool(providerUrl2);
    }

    /**
     * Verifies the getContext method
     *
     * @throws Exception if test case fails.
     */
    public void testPool() throws Exception {
        DirContextPoolManager.getInstance().setMaxSize(2);
        try {
            DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContext context2 = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);

            DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(providerUrl);

            assertEquals(2, pool.getSize());

            DirContextPoolManager.getInstance().returnDirContext(context);
            DirContextPoolManager.getInstance().returnDirContext(context2);

            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            assertEquals(2, pool.getSize());

        } catch (NamingException e) {
            fail("Exception in testPool " + e);
        }

        try {
            // try to get a third connection (should timeout when waiting for a free one)
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            fail("Expected NamingException");
        } catch (NamingException e) {
            System.out.println("Exception " + e);
        }
    }

    /**
     * Verifies the getContext method and use nultiple pools
     *
     * @throws Exception if test case fails.
     */
    public void testMultiplePools() throws Exception {
        try {
            DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContext context2 = DirContextPoolManager.getInstance().getDirContext(dirEnvironment2);

            DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(providerUrl);
            assertEquals(1, pool.getSize());

            DirContextPool pool2 = DirContextPoolManager.getInstance().getDirContextPool(providerUrl2);
            assertEquals(1, pool2.getSize());

            DirContextPoolManager.getInstance().returnDirContext(context);
            DirContextPoolManager.getInstance().returnDirContext(context2);

            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            assertEquals(2, pool.getSize());

        } catch (NamingException e) {
            fail("Exception in testPool " + e);
        }
    }

    /**
     * Make some threads that wait for the other threads to return their context
     *
     * @throws Exception if test case fails.
     */
    public void testPoolWait1() throws Throwable {
        DirContextPoolManager.getInstance().setMaxSize(1);
        DirContextPoolManager.getInstance().setTimeoutLimit(1000);

        DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);

        int size = 5;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        // each thread will wait max 1 second for a free connection
                        DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
                        assertNotNull(context);
                        threadSleep(10);
                        DirContextPoolManager.getInstance().returnDirContext(context);
                    } catch (Exception e) {
                        fail("Exception in run " + e);
                    }
                }
            });
            threads[i].setUncaughtExceptionHandler(this);
            threads[i].start();
        }
        threadSleep(100);
        DirContextPoolManager.getInstance().returnDirContext(context);
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
    public void testPoolReleaseConn() throws Exception {
        DirContextPoolManager.getInstance().setForcedReleaseContextLimit(0);
        try {
            DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContextPoolManager.getInstance().returnDirContext(context);
            context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            DirContextPoolManager.getInstance().returnDirContext(context);
        } catch (NamingException e) {
            fail("Exception in testPool " + e);
        }
    }

    /**
     * Verifies that correct exceptions are thrown when failing to connect.
     *
     * @throws Exception if test case fails.
     */
    public void testPoolAuthErrors() throws Exception {
        // test wrong dn
        dirEnvironment.put(Context.SECURITY_PRINCIPAL, "cn=XDirectory Manager");
        try {
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            fail("Expected NamingException ");
        } catch (NamingException e) {
            if (e instanceof AuthenticationException) {
                AuthenticationException ax = (AuthenticationException) e;
                System.out.println(ax.getExplanation());
            }
        }

        dirEnvironment.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
        // test wrong pwd
        dirEnvironment.put(Context.SECURITY_CREDENTIALS, "Xemmanager");
        try {
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            fail("Expected NamingException ");
        } catch (NamingException e) {
            if (e instanceof AuthenticationException) {
                AuthenticationException ax = (AuthenticationException) e;
                System.out.println(ax.getExplanation());
            }
        }
    }

    /**
     * Verifies that correct exceptions are thrown when failing to connect.
     *
     * @throws Exception if test case fails.
     */
    public void testPoolErrors() throws Exception {
        dirEnvironment.put(Context.PROVIDER_URL, "ldap://Xpolaris.ipms.su.erm.abcxyz.se:389/o=abcxyz.se");
        try {
            DirContextPoolManager.getInstance().getDirContext(dirEnvironment);
            fail("Expected NamingException ");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception if test case fails.
     */
    public void testModify() throws Exception {
        try {
            DirContext context = DirContextPoolManager.getInstance().getDirContext(dirEnvironment);

            String dn = "uniqueidentifier=um5,ou=C1";
            ModificationItem[] mods = new ModificationItem[1];
            BasicAttribute attr = new BasicAttribute("badlogincount", "5");
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
            context.modifyAttributes(dn, mods);

            //System.out.println(context.getAttributes(dn));

            DirContextPoolManager.getInstance().returnDirContext(context);
        } catch (NamingException e) {
            fail("Exception in testPool " + e);
        }
    }

    public static void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }

    public static Test suite() {
        return new TestSuite(DirContextPoolMTest.class);
    }

    public static void main(String[] args) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.mobeon.masp.profilemanager.pool.PooledDirContextFactory");
        env.put(Context.PROVIDER_URL, "ldap://polaris.ipms.su.erm.abcxyz.se:389/o=abcxyz.se");
        env.put("com.sun.jndi.ldap.connect.pool", "false");

        try {
            // Create one initial context (Get connection from pool)
            DirContext ctx = new InitialDirContext(env);

            System.out.println(ctx.getAttributes("ou=C1"));

            ctx.close();   // Return connection to pool

            // Create another initial context (Get connection from pool)
            DirContext ctx2 = new InitialDirContext(env);

            System.out.println("\n");

            System.out.println(ctx2.getAttributes("ou=C2"));

            // Close the context when we're done
            ctx2.close();   // Return connection to pool

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
