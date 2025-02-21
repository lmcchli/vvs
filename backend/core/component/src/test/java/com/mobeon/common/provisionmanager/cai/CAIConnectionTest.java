/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.IOException;
import java.net.*;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Testcase for the CAIConnection class
 *
 * @author ermmaha
 */
public class CAIConnectionTest extends MockObjectTestCase {
    static {
        ILoggerFactory.configureAndWatch("../log4jconf.xml");
    }
    protected String host = "ockelbo.lab.mobeon.com";
    protected int port = 2400;
    protected String uid = "cai";
    protected String pwd = "root";

    public CAIConnectionTest(String string) {
        super(string);
    }

    /**
     * Verifies that correct exceptions are thrown when failing to connect to the CAI server.
     *
     * @throws Exception if test case fails.
     */
    public void testFailedConnect() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        try {
            conn.connect("x" + host, port);
            fail("Expected IOException");
        } catch (IOException e) {
            assertFalse(conn.isConnected());
            assertTrue("Exception should be UnknownHostException", e instanceof UnknownHostException);
        }

        try {
            conn.connect(host, 8900);
            fail("Expected IOException");
        } catch (IOException e) {
            assertFalse(conn.isConnected());
            assertTrue("Exception should be ConnectException", e instanceof ConnectException);
        }
    }

    /**
     * Verifies the Login command
     *
     * @throws Exception if test case fails.
     */
    public void testLogin() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        try {
            conn.connect(host, port);
            assertTrue(conn.isConnected());
        } finally {
            conn.disconnect();
            assertFalse(conn.isConnected());
        }
    }

    /**
     * Verifies correct exceptions are thrown when failing to Login to the CAI server.
     *
     * @throws Exception
     */
    public void testFailedLogin() throws Exception {
        // test wrong uid
        try {
            CAIConnection conn = getCaiConnection("x" + uid, pwd);
            conn.connect(host, port);
            fail("Expected CAIException");
        } catch (CAIException e) {
            assertEquals("Error code should indicate invalid credentials", 5002, e.getErrorCode());
        }

        // test wrong pwd
        try {
            CAIConnection conn = getCaiConnection(uid, "x" + pwd);
            conn.connect(host, port);
            fail("Expected CAIException");
        } catch (CAIException e) {
            assertEquals("Error code should indicate invalid credentials", 5002, e.getErrorCode());
        }
    }

    /**
     * Verifies the Create command
     *
     * @throws Exception if test case fails.
     */
    public void testCreate() throws Exception {
        CreateCommand createCommand = new CreateCommand("161011");
        createCommand.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        createCommand.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");
        createCommand.addAttribute("CUSTOMERID", "987654321");

        CAIConnection conn = getCaiConnection(uid, pwd);
        conn.connect(host, port);
        conn.sendCommand(createCommand);

        conn.disconnect();
    }

    /**
     * Verifies correct exceptions are thrown when failing to Create a user.
     *
     * @throws Exception if test case fails.
     */
    public void testFailedCreate() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        conn.connect(host, port);

        // Test to add user with invalid attributes, should get exception
        CreateCommand createCommand = new CreateCommand("161012");
        createCommand.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        createCommand.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");
        createCommand.addAttribute("INVLAID", "value");

        try {
            conn.sendCommand(createCommand);
            fail("Expected CAIException");
        } catch (CAIException e) {
            assertEquals("Error code should indicate unknown parameter", 5041, e.getErrorCode());
        }

        // Test to add a user with invalid COS, should get exception
        createCommand = new CreateCommand("161013");
        createCommand.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        createCommand.addAttribute("COSDN", "cos=9999,ou=C22,o=mobeon.com");

        try {
            conn.sendCommand(createCommand);
            fail("Expected CAIException");
        } catch (CAIException e) {
            assertEquals("Error code should indicate unknown class of service", 5014, e.getErrorCode());
        }
    }

    /**
     * Verifies the Get command
     *
     * @throws Exception if test case fails.
     */
    public void testGet() throws Exception {
        GetCommand getCommand = new GetCommand("TELEPHONENUMBER", "161011");
        CAIConnection conn = getCaiConnection(uid, pwd);
        try {
            conn.connect(host, port);
            conn.sendCommand(getCommand);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Verifies the Delete command
     *
     * @throws Exception if test case fails.
     */
    public void testDelete() throws Exception {
        DeleteCommand deleteCommand = new DeleteCommand("161011");
        CAIConnection conn = getCaiConnection(uid, pwd);
        conn.connect(host, port);
        conn.sendCommand(deleteCommand);

        // Try delete a nonexisting subscriber
        try {
            conn.sendCommand(deleteCommand);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
            assertEquals("Error code should indicate undefined subscriber", 5008, e.getErrorCode());
        }

        conn.disconnect();
    }

    private CAIConnection getCaiConnection(String uid, String pwd) {
        return new CAIConnection(uid, pwd);
    }

    public static Test suite() {
        return new TestSuite(CAIConnectionTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
