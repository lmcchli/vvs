/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

/**
 * A container for SDP related constants.
 *
 * @author Malin Flodin
 */
public interface SdpConstants {

    // Connection constants
    public static final String NETWORK_TYPE_IN          = "IN";
    public static final String ADDRESS_TYPE_IP4         = "IP4";

    // Media types
    public static final String MEDIA_TYPE_AUDIO         = "audio";
    public static final String MEDIA_TYPE_VIDEO         = "video";

    // Transport types
    public static final String TRANSPORT_RTP_AVP        = "RTP/AVP";
    public static final String TRANSPORT_RTP_AVPF       = "RTP/AVPF";

    // Attributes
    public static final String ATTRIBUTE_CHARSET        = "charset";
    public static final String ATTRIBUTE_FMTP           = "fmtp";
    public static final String ATTRIBUTE_PTIME          = "ptime";
    public static final String ATTRIBUTE_MAXPTIME       = "maxptime";
    public static final String ATTRIBUTE_RTPMAP         = "rtpmap";
    public static final String ATTRIBUTE_RTCP_FB        = "rtcp-fb";

    // Attributes - unicast
    public static final String ATTRIBUTE_INACTIVE       = "inactive";
    public static final String ATTRIBUTE_SENDONLY       = "sendonly";
    public static final String ATTRIBUTE_RECVONLY       = "recvonly";
    public static final String ATTRIBUTE_SENDRECV       = "sendrecv";

    // Attributes - precondition
    public static final String ATTRIBUTE_PRECONDITION_CURRENT   = "curr";
    public static final String ATTRIBUTE_PRECONDITION_DESIRED   = "des";
    public static final String ATTRIBUTE_PRECONDITION_CONFIRM   = "conf";

    // RTCP feedback types
    public static final String RTCP_FB_FIR              = "ccm fir";

    // Fmtp's
    public static final String FMTP_DTMF                = "0-15";

    // Charset's
    public static final String UTF_8                    = "UTF-8";

    // Bandwidth types
    public static final String BW_TYPE_AS               = "AS";
    public static final String BW_TYPE_RS               = "RS";
    public static final String BW_TYPE_RR               = "RR";


}
