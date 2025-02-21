package com.mobeon.masp.callmanager;

import java.net.InetAddress;

/**
 * Contains protocol, IP address and port for a network connection.
 *
 * This class is immutable.
 *
 * @author mande
 */
public class Connection implements Comparable<Connection> {
    private final int port;
    private final InetAddress address;
    private final String protocol;

    private volatile int hashCode = 0;

    public Connection(String protocol, InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * Returns the IP address of the connection
     * @return the IP address of the connection
     */
    public String getIpAddress() {
        return address.getHostAddress();
    }

    /**
     * Returns the port number of the connection
     * @return the port number of the connection
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the protocol name of the connection
     * @return the protocol name of the connection
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Implements equals so that <code>Connection</code>s can be put in a <code>Set</code>.
     * @param obj the reference object with which to compare
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Connection)) {
            return false;
        }
        Connection connection = (Connection)obj;
        return connection.address.equals(address) &&
                connection.port == port &&
                connection.protocol.equals(protocol);
    }

    public int hashCode() {
        // Lazy initialized, cashed hashCode
        if (hashCode == 0) {
            int result = 17;
            result = 37*result + address.hashCode();
            result = 37*result + port;
            result = 37*result + protocol.hashCode();
            hashCode = result;
        }
        return hashCode;
    }


    public String toString() {
        StringBuffer buf = new StringBuffer(protocol);
        buf.append("://").append(getIpAddress()).append(":").append(port);
        return buf.toString();
    }

    /**
     * Implements compareTo so that <code>Connection</code>s can be put in a <code>SortedSet</code>.
     * The comparison is first made on protocol name, then on IP address and eventually on port number.
     * The protocol comparison is lexicographical. The IP address and port comparisons are numerical.
     * IPv4 addresses preceeds IPv6 addresses.
     * <p>For example:<p>
     * <ul><li>RTP://&lt;any address&gt;:&lt;any port&gt; is less than SIP://&lt;any address&gt;:&lt;any port&gt;</li>
     * <li>RTP://127.0.0.1:&lt;any port&gt; is less than RTP://127.0.0.2:&lt;any port&gt;</li>
     * <li>RTP://127.0.0.1:1111 is less than RTP://127.0.0.1:1112</li>
     * </ul>
     * @param connection the Connection to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public int compareTo(Connection connection) {
        int result;

        // Compare protocol
        result = protocol.compareTo(connection.protocol);
        if (result != 0) {
            return result;
        }

        // Compare addresses
        // IPv6 addresses are considered "greater than" IPv4 addresses
        result = address.getAddress().length - connection.address.getAddress().length;
        if (result != 0) {
            return result;
        }
        for (int i = 0; i < address.getAddress().length; i++) {
            result = address.getAddress()[i] - connection.address.getAddress()[i];
            if (result != 0) {
                return result;
            }
        }

        // Compare port
        return  port - connection.port;
    }
}
