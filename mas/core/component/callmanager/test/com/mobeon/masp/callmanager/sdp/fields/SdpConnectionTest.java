package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.Connection;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

/**
 * SdpConnection Tester.
 *
 * @author Malin Flodin
 */
public class SdpConnectionTest extends MockObjectTestCase
{
    private SdpFactory sdpFactory = null;

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that null is returned when parsing an SDP connection that is null.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionWhenConnectionIsNull() throws Exception {
        SdpConnection sdpConnection = SdpConnection.parseConnection(null);
        assertNull(sdpConnection);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_NETWORK_PROTOCOL) is thrown if the connection contains an
     * unsupported network type.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForUnsupportedNetworkType() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IX IP4 224.2.1.1");
        try {
            SdpConnection.parseConnection(sd.getConnection());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_NETWORK_PROTOCOL,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_NETWORK_PROTOCOL) is thrown if the network type for the
     * connection is null.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForNullNetworkType() throws Exception {
        Mock connectionMock = mock(Connection.class);
        connectionMock.expects(once()).method("getNetworkType").
                will(returnValue(null));
        try {
            SdpConnection.parseConnection((Connection)connectionMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_NETWORK_PROTOCOL,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_NETWORK_ADDRESS_FORMAT) is thrown if the connection
     * contains an unsupported address type.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForUnsupportedAddressType() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IN IP6 224.2.1.1");
        try {
            SdpConnection.parseConnection(sd.getConnection());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_NETWORK_ADDRESS_FORMAT,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_NETWORK_ADDRESS_FORMAT) is thrown if the address type for
     * the connection is null.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForNullAddressType() throws Exception {
        Mock connectionMock = mock(Connection.class);
        connectionMock.expects(once()).method("getNetworkType").
                will(returnValue(SdpConstants.NETWORK_TYPE_IN));
        connectionMock.expects(once()).method("getAddressType").
                will(returnValue(null));
        try {
            SdpConnection.parseConnection((Connection)connectionMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_NETWORK_ADDRESS_FORMAT,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * MULTICAST_NOT_AVAILABLE) is thrown if the connection contains a
     * multicast address, i.e. if TTL is set.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForAddressWithTtl() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IN IP4 224.2.1.1/127/3");
        try {
            SdpConnection.parseConnection(sd.getConnection());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.MULTICAST_NOT_AVAILABLE, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * MULTICAST_NOT_AVAILABLE) is thrown if the address for
     * the connection contains multiple addresses although TTL is not set.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionForAddressWithMultipleAddresses() throws Exception {
        Mock connectionMock = mock(Connection.class);
        connectionMock.expects(once()).method("getNetworkType").
                will(returnValue(SdpConstants.NETWORK_TYPE_IN));
        connectionMock.expects(once()).method("getAddressType").
                will(returnValue(SdpConstants.ADDRESS_TYPE_IP4));
        connectionMock.expects(once()).method("getAddressTtl").
                will(returnValue(-1));
        connectionMock.expects(once()).method("getAddressCount").
                will(returnValue(3));
        try {
            SdpConnection.parseConnection((Connection)connectionMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.MULTICAST_NOT_AVAILABLE,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if an
     * {@link SdpParseException} was thrown while parsing the SDP.
     * @throws Exception if test case fails.
     */
    public void testParseConnectionWhenSdpExceptionIsThrown() throws Exception {
        Mock connectionMock = mock(Connection.class);
        connectionMock.expects(once()).method("getNetworkType").
                will(throwException(new SdpParseException(0, 0, "SDP Error")));
        try {
            SdpConnection.parseConnection((Connection)connectionMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that a correct address is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseConnection() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IN IP4 224.2.1.1");
        assertEquals(
                "224.2.1.1",
                SdpConnection.parseConnection(sd.getConnection()).getAddress());
    }

    /**
     * Verifies that the toString method returns the connection address as a
     * string.
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IN IP4 224.2.1.1");
        SdpConnection sdpConnection =
                SdpConnection.parseConnection(sd.getConnection());
        assertEquals("224.2.1.1", sdpConnection.getAddress());
        assertEquals("224.2.1.1", sdpConnection.toString());
    }

    /**
     * Verifies that the connection can be translated into an SDP stack
     * connection.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("c=IN IP4 224.2.1.1");
        SdpConnection sdpConnection =
                SdpConnection.parseConnection(sd.getConnection());
        Connection connection = sdpConnection.encodeToStackFormat(sdpFactory);
        assertEquals(connection.getAddress(), "224.2.1.1");
        assertEquals(connection.getAddressType(), "IP4");
        assertEquals(connection.getNetworkType(), "IN");
        assertEquals(connection.getAddressCount(), 1);
        assertEquals(connection.getAddressTtl(), -1);
    }

}
