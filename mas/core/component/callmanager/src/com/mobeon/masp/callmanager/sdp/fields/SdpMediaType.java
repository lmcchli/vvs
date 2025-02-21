/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.masp.callmanager.sdp.SdpConstants;

/**
 * The media type present in the Media field of a Media Description.
 * <p>
 * Currently only audio and video are supported in Call Manager.
 * Therefore, this enumerate only contains values for audio and video.
 *
 * @author Malin Flodin
 */
public enum SdpMediaType {
    AUDIO (SdpConstants.MEDIA_TYPE_AUDIO),
    VIDEO (SdpConstants.MEDIA_TYPE_VIDEO);

    private final String name;

    SdpMediaType(String name) {
       this.name = name;
   }

    public String toString() {
        return name;
    }
    
    /**
     * Parses a string and converts it into an {@link SdpMediaType}.
     * "audio" => {@link SdpMediaType.AUDIO}
     * "video" => {@link SdpMediaType.VIDEO}
     * For other strings, null is returned.
     * @param   mediaType   The media type as a string.
     * @return  An {@link SdpMediaType}.
     *          Null is returned if <param>mediaType</param> is something other
     *          than audio or video.
     */
    public static SdpMediaType parseMediaType(String mediaType) {

        SdpMediaType sdpMediaType = null;

        if (mediaType != null) {
            String mt = mediaType.toLowerCase();

            if (mt.equals(SdpConstants.MEDIA_TYPE_AUDIO))
                sdpMediaType = SdpMediaType.AUDIO;
            else if (mt.equals(SdpConstants.MEDIA_TYPE_VIDEO))
                sdpMediaType = SdpMediaType.VIDEO;
        }

        return sdpMediaType;
    }

}
