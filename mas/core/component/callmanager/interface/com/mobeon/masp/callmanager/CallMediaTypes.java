/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.HashMap;

/**
 * Container of the outbound media types to use for a call.
 * Also contains the source responsible for assigning the outbound media types.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class CallMediaTypes {
    /** The primary MIME content type for audio */
    public static final String MIME_PRIMARY_TYPE_AUDIO = "audio";

    /** The primary MIME content type for video. */
    public static final String MIME_PRIMARY_TYPE_VIDEO = "video";

    private final MediaMimeTypes outboundMediaTypes;
    private final Object outboundMediaTypesSource;
    private final HashMap<CallMediaType, MimeType> mimeTypeMap =
            new HashMap<CallMediaType, MimeType>();

    public static enum CallMediaType {
        AUDIO, VIDEO
    }

    /**
     * Creates call media types to use when creating outbound calls
     * or when accepting inbound calls.
     * <p>
     * The outboundMediaTypes MUST contain exactly one audio mime type and zero
     * or one video mime type. IllegalArgumentException is thrown if this is
     * violated.
     *
     * @param outboundMediaTypes The media mime types to use for the outbound
     * stream of a call. MUST NOT be null.
     * @param outboundMediaTypesSource
     * @throws IllegalArgumentException if outboundMediaTypes is null or if the
     * contect of the outboundMediaTypes is not as described above, i.e. one
     * audio mime type and zero or one video mime type.
     */
    public CallMediaTypes(MediaMimeTypes outboundMediaTypes,
                          Object outboundMediaTypesSource)
    {
        if (outboundMediaTypes == null) {
            throw new IllegalArgumentException("Outbound Media Types is null.");
        }

        this.outboundMediaTypesSource = outboundMediaTypesSource;
        this.outboundMediaTypes = outboundMediaTypes;

        addMimeTypes(outboundMediaTypes);

        if (!mimeTypeMap.containsKey(CallMediaType.AUDIO)) {
            throw new IllegalArgumentException("No audio " +
                    "mime type found in outbound media types.");
        }
    }

    public MediaMimeTypes getOutboundMediaTypes() {
        return outboundMediaTypes;
    }

    public Object getOutboundMediaTypesSource() {
        return outboundMediaTypesSource;
    }

    public MimeType getMimeType(CallMediaType mediaType) {
        return mimeTypeMap.get(mediaType);
    }

    //=========================== Private Methods =========================

    private void addMimeTypes(MediaMimeTypes outboundMediaTypes) {
        List<MimeType> mimeTypes = outboundMediaTypes.getAllMimeTypes();
        for (MimeType mimeType : mimeTypes) {
            addMimeType(mimeType);
        }
    }

    private void addMimeType(MimeType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException(
                    "Mime type in outbound media is null.");
        }
        String primaryType = mimeType.getPrimaryType().toLowerCase();

        if (primaryType.equals(MIME_PRIMARY_TYPE_AUDIO)) {
            addMimeType(mimeType, CallMediaType.AUDIO);
        } else if (primaryType.equals(MIME_PRIMARY_TYPE_VIDEO)) {
            addMimeType(mimeType, CallMediaType.VIDEO);
        } else {
            throw new IllegalArgumentException("Unknown mime type <" +
                    mimeType + "> in outbound media types.");
        }
    }

    private void addMimeType(MimeType mimeType, CallMediaType type) {
        if (mimeTypeMap.containsKey(type)) {
            throw new IllegalArgumentException("More than one " + type +
                    " mime type in media types.");
        } else {
            mimeTypeMap.put(type, mimeType);
        }
    }

    public String toString() {
        return outboundMediaTypes.toString();
    }
}
