/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import org.jmock.MockObjectTestCase;
import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaTransport;
import com.mobeon.masp.callmanager.sdp.fields.SdpOrigin;
import com.mobeon.masp.callmanager.sdp.attributes.SdpFmtp;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPTime;
import com.mobeon.masp.callmanager.sdp.attributes.SdpRtpMap;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;

import java.util.HashMap;
import java.util.Vector;

/**
 * Base class used for class tests of SDP related classes.
 * @author Malin Flodin
 */
public abstract class SdpCase extends MockObjectTestCase {

    protected void assertConnectionField(SdpConnection connection, String host) {
        assertEquals(host, connection.getAddress());
        assertEquals("IP4", connection.getAddressType());
        assertEquals("IN", connection.getNetworkType());
    }

    protected void assertOrigin(SdpOrigin origin, String user) {
        assertEquals(user, origin.getUserName());
    }

    protected void assertFmtp(
            HashMap<Integer, SdpFmtp> fmtps, SdpFmtp expectedFmtp)
            throws Exception {
        if (expectedFmtp == null)
            assertTrue(fmtps.isEmpty());
        else {
            assertEquals(expectedFmtp.getFormat(),
                    fmtps.get(expectedFmtp.getFormat()).getFormat());
            assertEquals(expectedFmtp.getParameters(),
                    fmtps.get(expectedFmtp.getFormat()).getParameters());
        }
    }

    protected void assertMediaField(SdpMedia media, SdpMediaType type,
                                  int port, int portCount,
                                  Vector<Integer> formats) {
        assertEquals(type, media.getType());
        assertEquals(port, media.getPort());
        assertEquals(portCount, media.getPortCount());
        assertEquals(formats, media.getFormats().getFormats());
        assertEquals(SdpMediaTransport.RTP_AVP, media.getTransport());
    }

    protected void assertPTime(SdpPTime sdpPTime, Integer expectedPTime)
            throws Exception {
        if (expectedPTime == null)
            assertNull(sdpPTime);
        else
            assertEquals(expectedPTime.intValue(), sdpPTime.getpTime());
    }

    protected void assertRtpMap(
            HashMap<Integer, SdpRtpMap> rtpMaps, SdpRtpMap expectedMap)
            throws Exception {
        if (expectedMap == null)
            assertTrue(rtpMaps.isEmpty());
        else {
            assertEquals(expectedMap.getPayloadType(),
                    rtpMaps.get(expectedMap.getPayloadType()).getPayloadType());
            assertEquals(expectedMap.getChannels(),
                    rtpMaps.get(expectedMap.getPayloadType()).getChannels());
            assertEquals(expectedMap.getClockRate(),
                    rtpMaps.get(expectedMap.getPayloadType()).getClockRate());
            assertEquals(expectedMap.getEncodingName(),
                    rtpMaps.get(expectedMap.getPayloadType()).getEncodingName());
        }
    }

    protected void assertTransmissionMode(
            SdpTransmissionMode mode, SdpTransmissionMode expectedMode)
            throws Exception {
        if (expectedMode == null)
            assertNull(mode);
        else
            assertEquals(expectedMode, mode);
    }
}
