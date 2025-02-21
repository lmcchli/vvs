/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

/**
 * Contains all SIP warning codes supported by Call Manager.
 * <p>
 * This enum is immutable.
 *
 * @author Malin Flodin
 */
//TODO: Add warning messages for the component!
public enum SipWarning {
    // Warning codes from SIP specification RFC 3261

    // Note that the warning texts must only contain qdtext or quoted-pair according to
    // RFC3261. 
    INCOMPATIBLE_NETWORK_PROTOCOL
            (300, "Only network protocol IN is supported."),

    INCOMPATIBLE_NETWORK_ADDRESS_FORMAT
            (301, "Only network address format IP4 is supported."),

    INCOMPATIBLE_TRANSPORT_PROTOCOL
            (302, "Only transport protocols RTP/AVP or RTP/AVPF are supported."),

    // TODO: Phase 2! Add support for bandwidth checks!
    INCOMPATIBLE_BANDWIDTH_UNIT
            (303, "Only bandwidth unit TODO is supported."),

    MEDIA_TYPE_NOT_AVAILABLE
            (304, "Only media types \\\"audio\\\" and \\\"video\\\" are supported."),

    INCOMPATIBLE_MEDIA_FORMAT
            (305, "Required media formats not supported by session description."),

    ATTRIBUTE_NOT_UNDERSTOOD
            (306, "Media attribute is not understood."),

    SD_PARAMETER_NOT_UNDERSTOOD
            (307, "Parameter is not understood."),

    MULTICAST_NOT_AVAILABLE
            (330, "Multicast is not supported."),

    UNICAST_NOT_AVAILABLE
            (331, "Unicast is not supported"),

    INSUFFICIENT_BANDWIDTH
            (370, "Required bandwidth is not available."),

    // Own warning codes
    RENEGOTIATION_NOT_SUPPORTED
            (399, "Modifying an ongoing session is not supported."),

    ENCRYPTION_NOT_SUPPORTED
            (399, "Encrypted SDP is not supported."),

    CHARSET_NOT_SUPPORTED
            (399, "Charset specified in SDP is not supported. " +
                    "Only UTF-8 is supported."),

    PORT_COUNT_NOT_ALLOWED
            (399, "Port count in media field is not supported. " +
                    "Only port count one is supported."),

    VERSION_NOT_SUPPORTED
            (399, "Only version 0 is supported.");


    private final int code;
    private final String text;

    SipWarning(int code, String text) {
       this.code = code;
       this.text = text;
   }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
