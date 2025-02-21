/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Testcase for the CAIConnectionPool class
 *
 * @author ermmaha
 */
public class CAIConnectionPoolTest extends TestCase {

    protected String host = "ockelbo.lab.mobeon.com";
    protected int port = 2400;
    protected String uid = "cai";
    protected String pwd = "root";

    private CAISpy spy = new CAISpy();
    private CAIConnectionPool pool;

    public CAIConnectionPoolTest(String string) {
        super(string);
    }

    protected void setUp() {
        pool = new CAIConnectionPool(host, port, uid, pwd);
        pool.setCommSpy(spy);
    }

    /**
     * Verifies the getConnection method
     *
     * @throws Exception if test case fails.
     */
    public void testPool() throws Exception {
        pool.setMaxSize(2);
        CAIConnection conn = pool.getConnection();
        CAIConnection conn2 = pool.getConnection();

        assertEquals(2, pool.getSize());

        pool.returnConnection(conn);
        pool.returnConnection(conn2);

        conn = pool.getConnection();
        conn2 = pool.getConnection();
        assertEquals(2, pool.getSize());

        try {
            //try to get a third connection (should timeout when waiting for a free one)
            pool.getConnection();
            fail("Expected CAIException ");
        } catch (CAIException e) {
            System.out.println("Exception " + e);
        }

        // Try returning a closed (invalid) connection
        conn.disconnect();
        pool.returnConnection(conn);
        assertEquals(1, pool.getSize());
        conn2.disconnect();
        pool.returnConnection(conn2);
        assertEquals(0, pool.getSize());
    }

    /**
     * Test a client that does not return its connection in time.
     *
     * @throws Exception if test case fails.
     */
    public void testUnbehavingClients() throws Exception {
        pool.setMaxSize(1);
        pool.setIdleTimeoutLimit(2000);
        pool.getConnection();

        sleep(5);

        CAIConnection conn2 = pool.getConnection();
        assertNotNull(conn2);
        assertEquals(1, pool.getSize());
    }

    /**
     * Make a thread that waits for the main thread to return it's CAIConnection
     *
     * @throws Exception if test case fails.
     */
    public void testPoolWait1() throws Exception {
        pool.setMaxSize(1);
        pool.setTimeoutLimit(10 * 1000);

        CAIConnection conn = pool.getConnection();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //will wait 10 seconds for a free connection
                    CAIConnection conn2 = pool.getConnection();
                    assertNotNull(conn2);
                } catch (Exception e) {
                    fail("Exception in run " + e);
                }
            }
        }).start();

        sleep(5);
        pool.returnConnection(conn);
    }

    /**
     * Verifies that correct exceptions are thrown when failing to connect to the CAI server.
     *
     * @throws Exception if test case fails.
     */
    public void testPoolErrors() throws Exception {
        pool = new CAIConnectionPool("x" + host, port, uid, pwd);
        pool.setCommSpy(spy);

        try {
            pool.getConnection();
            fail("Expected IOException ");
        } catch(IOException e) {
            assertEquals(0, pool.getSize());
        }

        pool = new CAIConnectionPool(host, port, "x" + uid, pwd);
        pool.setCommSpy(spy);

        try {
            pool.getConnection();
            fail("Expected CAIException ");
        } catch(CAIException e) {
            assertEquals(0, pool.getSize());
            assertEquals(5002, e.getErrorCode());
        }

        pool = new CAIConnectionPool(host, port, uid, "x" + pwd);
        pool.setCommSpy(spy);

        try {
            pool.getConnection();
            fail("Expected CAIException ");
        } catch(CAIException e) {
            assertEquals(0, pool.getSize());
            assertEquals(5002, e.getErrorCode());
        }

    }

    public void testChangedPassword() throws Exception {
        pool = new CAIConnectionPool(host, port, uid, "x" + pwd);
        pool.setCommSpy(spy);

        try {
            pool.getConnection();
            fail("Expected CAIException ");
        } catch(CAIException e) {
            assertEquals(0, pool.getSize());
            assertEquals(5002, e.getErrorCode());
        }
        
        pool.setPassword(pwd);
        pool.getConnection();
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }

    public static Test suite() {
        return new TestSuite(CAIConnectionPoolTest.class);
    }

    private static class CAISpy implements CAICommSpy {
        public void println(String line) {
            System.out.println("> " + line);
        }

        public void readLine(String line) {
            System.out.println("< " + line);
        }

        public void debug(String msg) {
            System.out.println(Thread.currentThread().getName() + " " + msg);
        }
    }
}

