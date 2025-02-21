package com.mobeon.masp.callmanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Connection Tester.
 *
 * @author MANDE
 * @since <pre>10/31/2006</pre>
 * @version 1.0
 */
public class ConnectionTest extends TestCase {
    private Connection connection;

    public ConnectionTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        connection = new Connection("protocol", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1234);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetIpAddress() throws Exception {
        assertEquals("1.2.3.4", connection.getIpAddress());
    }

    public void testGetPort() throws Exception {
        assertEquals(1234, connection.getPort());
    }

    public void testGetProtocol() throws Exception {
        assertEquals("protocol", connection.getProtocol());
    }

    public void testToString() throws Exception {
        assertEquals("protocol://1.2.3.4:1234", connection.toString());
    }

    public void testCompareTo() throws Exception {
        Connection connection1 = new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1234);
        Connection connection2 = new Connection("protocol2", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1234);
        Connection connection3 = new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 5}), 1234);
        Connection connection4 = new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1235);

        Connection connection5 = new Connection("protocol1",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 1234);
        Connection connection6 = new Connection("protocol2",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 1234);
        Connection connection7 = new Connection("protocol1",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17}), 1234);
        Connection connection8 = new Connection("protocol1",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 1235);

        // Test IPv4 addresses
        assertTrue(connection1.compareTo(connection1) == 0);
        assertTrue(connection1.compareTo(connection2) < 0);
        assertTrue(connection1.compareTo(connection3) < 0);
        assertTrue(connection1.compareTo(connection4) < 0);
        assertTrue(connection2.compareTo(connection1) > 0);
        assertTrue(connection3.compareTo(connection1) > 0);
        assertTrue(connection4.compareTo(connection1) > 0);

        // Test IPv6 addresses
        assertTrue(connection5.compareTo(connection5) == 0);
        assertTrue(connection5.compareTo(connection6) < 0);
        assertTrue(connection5.compareTo(connection7) < 0);
        assertTrue(connection5.compareTo(connection8) < 0);
        assertTrue(connection6.compareTo(connection5) > 0);
        assertTrue(connection7.compareTo(connection5) > 0);
        assertTrue(connection8.compareTo(connection5) > 0);

        // Test combinations
        assertTrue(connection1.compareTo(connection5) < 0);
        assertTrue(connection5.compareTo(connection1) > 0);
    }

    /**
     * Test the hashCode method for some <code>Connection</code>s
     * @throws Exception
     */
    public void testHashCode() throws Exception {
        Connection[] connections = getConnections();
        for (int i = 0; i < connections.length; i++) {
            for (int j = i + 1; j < connections.length; j++) {
                assertTrue(connections[i].hashCode() != connections[j].hashCode());
            }
        }
        Connection[] connections2 = getConnections();
        for (int i = 0; i < connections.length; i++) {
            assertEquals(connections[i].hashCode(), connections2[i].hashCode());
        }
    }
    
    /**
     * Test the equals method for some <code>Connection</code>s
     * @throws Exception
     */
    public void testEquals() throws Exception {
        Connection[] connections = getConnections();
        for (int i = 0; i < connections.length; i++) {
            assertTrue("Should be equal", connections[i].equals(connections[i]));
            for (int j = i + 1; j < connections.length; j++) {
                assertFalse("Should not be equal", connections[i].equals(connections[j]));
            }
        }
        Connection[] connections2 = getConnections();
        for (int i = 0; i < connections.length; i++) {
            assertTrue(connections[i].equals(connections2[i]));
        }
        Connection connection = new Connection("protocol", InetAddress.getLocalHost(), 1234);
        assertFalse("Should not be equal", connection.equals(new Object()));
    }


    private Connection[] getConnections() throws UnknownHostException {
        Connection[] connections;
        connections = new Connection[] {
                new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1234),
                new Connection("protocol2", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1234),
                new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 5}), 1234),
                new Connection("protocol1", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 1235),
                new Connection("protocol1",
                        InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}),
                        1234
                ),
                new Connection("protocol2",
                        InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}),
                        1234
                ),
                new Connection("protocol1",
                        InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17}),
                        1234
                ),
                new Connection("protocol1",
                        InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}),
                        1235
                )
        };
        return connections;
    }

    public static Test suite() {
        return new TestSuite(ConnectionTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
