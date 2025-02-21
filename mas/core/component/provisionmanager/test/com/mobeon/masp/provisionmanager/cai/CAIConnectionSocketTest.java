package com.mobeon.masp.provisionmanager.cai;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.provisionmanager.mock.SocketMock;

import java.io.IOException;
import java.net.*;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CAIConnection tester that mocks the socket. Placed in separate test class since
 * DatagramSocket.setDatagramSocketImplFactory can only be called once
 *
 * @author mande
 */
public class CAIConnectionSocketTest extends MockObjectTestCase {
    static {
        ILoggerFactory.configureAndWatch("../log4jconf.xml");
    }
    protected String host = "host";
    protected int port = 2400;
    protected String uid = "uid";
    protected String pwd = "pwd";
    private static SocketMock mockSocket;

    public CAIConnectionSocketTest(String string) {
        super(string);
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpMockSocket();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void setUpLoginExpectation() {
        mockSocket.willReturn("Welcome message");
        mockSocket.willReturn("Enter command:");
        mockSocket.expects("LOGIN:" + uid + ":" + pwd + ";\n");
        mockSocket.willReturn("RESP:0:Successful;");
        mockSocket.willReturn("CAI>:");
    }

    /**
     * Test that SocketException when logging in does not result in reconnect
     * @throws Exception
     */
    public void testLoginFailure() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        mockSocket.willReturn("Welcome message");
        mockSocket.willReturn("Enter command:");
        mockSocket.expects("LOGIN:" + uid + ":" + pwd + ";\n");
        mockSocket.willThrow(new SocketException("socketexception"));
        try {
            conn.connect(host, port);
            fail("Expected IOException");
        } catch (Exception e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test handling of SocketException when connection has been reset (from server)
     * @throws Exception
     */
    public void testConnectionReset() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        setUpLoginExpectation();
        conn.connect(host, port);

        CAICommand command = new CAICommand() {
            public String toCommandString() {
                return "command;";
            }
        };
        mockSocket.expects("command;\n");
        mockSocket.willThrow(new SocketException("Connection reset"));
        setUpLoginExpectation();
        mockSocket.expects("command;\n");
        mockSocket.willReturn("RESP:0:Successful;");
        conn.sendCommand(command);

        // Test two consecutive exceptions
        mockSocket.expects("command;\n");
        mockSocket.willThrow(new SocketException("Connection reset"));
        mockSocket.willThrow(new SocketException("Connection reset"));
        try {
            conn.sendCommand(command);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue("Cause should be SocketException", e instanceof SocketException);
        }
        assertFalse("Connection should be down", conn.isConnected());
    }

    public void testLogoutSocketException() throws Exception {
        // Test socket failure at logout
        CAIConnection conn = getCaiConnection(uid, pwd);
        setUpLoginExpectation();
        conn.connect(host, port);
        mockSocket.expects("LOGOUT;\n");
        mockSocket.willThrow(new SocketException("Connection reset"));
        conn.disconnect();

        // Test socket failure at close
        conn = getCaiConnection(uid, pwd);
        setUpLoginExpectation();
        conn.connect(host, port);
        mockSocket.expects("LOGOUT;\n");
        mockSocket.willReturn(null);
        mockSocket.setThrowOnClose(new SocketException("Connection reset"));
        conn.disconnect();
    }

    /**
     * Tests that an empty response throws an exception
     * @throws Exception
     */
    public void testEmptyResponse() throws Exception {
        CAIConnection conn = getCaiConnection(uid, pwd);
        setUpLoginExpectation();
        conn.connect(host, port);
        mockSocket.expects("CREATE:MOIPSUB:TELEPHONENUMBER,phonenumber;\n");
        CreateCommand command = new CreateCommand("phonenumber");
        try {
            conn.sendCommand(command);
            fail("Expected CAIException");
        } catch (CAIException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    private void setUpMockSocket() throws IOException {
        // Can only set SocketImplFactory once.
        if (mockSocket == null) {
            mockSocket = new SocketMock();
            // Setup socket factory
            SocketImplFactory socketFactory = new SocketImplFactory() {
                public SocketImpl createSocketImpl() {
                    return mockSocket;
                }
            };
            Socket.setSocketImplFactory(socketFactory);
        }
        registerToVerify(mockSocket);
    }

    private CAIConnection getCaiConnection(String uid, String pwd) {
        return new CAIConnection(uid, pwd);
    }

    public static Test suite() {
        return new TestSuite(CAIConnectionSocketTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(CAIConnectionSocketTest.suite());
    }
}
