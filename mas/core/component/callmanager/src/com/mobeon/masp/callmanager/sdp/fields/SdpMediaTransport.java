/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.masp.callmanager.sdp.SdpConstants;

/**
 * The media transport present in the Media field of a Media Description.
 * <p>
 * Currently only "RTP/AVP" and (part of) "RTP/AVPF" is supported in Call Manager.
 * Therefore, this enumerate only contains values for RTP/AVP and RTP/AVPF.
 *
 * @author Malin Flodin
 */
public enum SdpMediaTransport {
    RTP_AVP (SdpConstants.TRANSPORT_RTP_AVP),
    RTP_AVPF (SdpConstants.TRANSPORT_RTP_AVPF);

    private final String name;

    SdpMediaTransport(String name) {
       this.name = name;
   }

    public String toString() {
        return name;
    }

    /**
     * Parses a string and converts it into an {@link SdpMediaTransport}.
     * "RTP/AVP" => {@link SdpMediaTransport.RTP_AVP}
     * For other strings, null is returned.
     * @param   transport   The media transport as a string.
     * @return  An {@link SdpMediaTransport}.
     *          Null is returned if <param>transport</param> is something other
     *          than "RTP/AVP" or "RTP/AVPF".
     */
    public static SdpMediaTransport parseMediaTranport(String transport) {
        SdpMediaTransport sdpMediaTransport = null;

        if (transport != null) {
            if (transport.equalsIgnoreCase(SdpConstants.TRANSPORT_RTP_AVP)) {
                sdpMediaTransport = SdpMediaTransport.RTP_AVP;
            }
            else if (transport.equalsIgnoreCase(SdpConstants.TRANSPORT_RTP_AVPF)) {
                sdpMediaTransport = SdpMediaTransport.RTP_AVPF;
            }
        }

        return sdpMediaTransport;
    }
}
