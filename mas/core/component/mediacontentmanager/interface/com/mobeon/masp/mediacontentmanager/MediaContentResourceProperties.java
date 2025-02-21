/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import java.util.List;

/**
 * Default implementation of the <code>IMediaContentResourceProperties</code>
 * interface.
 *
 * @author Mats Egland
 */
public class MediaContentResourceProperties implements IMediaContentResourceProperties {
    /**
     * The language of the resource.
     * The language argument is a valid ISO Language Code. These codes are the lower-case, two-letter codes as defined by ISO-639. You can find a full list of these codes at a number of sites, such as:
     * http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt
     */
    private String language;
    /**
     * The type of the resource as a human readable tag.
     * For example "Prompt", "System", "Wide Announcement", "Fun Greeting" etc.
     */
    private String type;
    /**
     * Voice variant as a human readable tag.
     * For example "Female", "Male", "Bob"
     */
    private String voiceVariant;
    /**
     * Video variant as a human readable tag.
     * For example "Blue", "Green", "Flowers"
     */
    private String videoVariant;
    /**
     * The Media Codecs used in the media of this resource. Each codec
     * is described as a MIME Type, as described in RFC 3555.
     */
    private MediaMimeTypes mediaCodecs;

    /**
     * Empty constructor. Creates a
     * <code>MediaContentResourceProperties</code>
     * object with no properties set.
     */
    public MediaContentResourceProperties() {
        mediaCodecs = new MediaMimeTypes();
    }

    // Javadoc in interface
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language of the resource.
     * The language argument is a valid ISO Language Code. These codes are the lower-case, two-letter codes as defined by ISO-639. You can find a full list of these codes at a number of sites, such as:
     * http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt
     *
     * @param language The language of the resource.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    // javadoc in interface
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the content in the resource.
     * <p/>
     * <p/>
     * Example "Prompt", "System Wide Announcement"
     *
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    // javadoc in interface
    public String getVoiceVariant() {
        return voiceVariant;
    }
    /**
     * Sets the voice variant of the resource.
     * <p/>
     * For example "Female", "Male", "Bob".
     *
     * @param variant The variant to be set
     */
    public void setVoiceVariant(String variant) {
        this.voiceVariant = variant;
    }
    // javadoc in interface
    public String getVideoVariant() {
        return videoVariant;
    }
    /**
     * Sets the video variant of the resource.
     * <p/>
     * For example "Female", "Male", "Bob".
     *
     * @param variant The variant to be set.
     */
    public void setVideoVariant(String variant) {
        this.videoVariant = variant;
    }
    /**
     * Adds the specifed codec to the list of codecs
     * used in the resource. No duplicates will be added,
     * i.e. mime-types that matches with the
     * {@link MimeType#match(jakarta.activation.MimeType)}
     * method.
     *
     *
     * @param mimeType The MIME-type of the codec.
     */
    public void addCodec(MimeType mimeType) {
        mediaCodecs.addMimeType(mimeType);
    }
    // javadoc in interface
    public List<MimeType> getMediaCodecs() {
        return mediaCodecs.getAllMimeTypes();
    }

    // javadoc in interface
    public boolean hasMatchingCodec(MimeType mimeType) {
        return mediaCodecs.hasMatchingMimeType(mimeType);
    }

    public String toString() {

        StringBuffer codecs = new StringBuffer();
        List<MimeType> mimeTypes = mediaCodecs.getAllMimeTypes();
        for (MimeType mimeType : mimeTypes) {
            codecs.append(mimeType.toString());
            codecs.append(",");
        }
        return "Language=" + language + ", type=" + type + ", videoVariant=" + videoVariant
                + ", voiceVariant=" + voiceVariant +", codecs=[" + codecs.toString() + "]";
    }

    // javadoc in interface
    public boolean equals(Object other) {
        if (other instanceof IMediaContentResourceProperties) {
            IMediaContentResourceProperties otherProperties =
                    (IMediaContentResourceProperties)other;

            MediaMimeTypes otherMediaCodecs =
                    new MediaMimeTypes(otherProperties.getMediaCodecs());

            return language == otherProperties.getLanguage()
                    && videoVariant == otherProperties.getVideoVariant()
                    && voiceVariant == otherProperties.getVoiceVariant()
                    && type == otherProperties.getType()
                    && mediaCodecs.compareTo(otherMediaCodecs);
        } else {
            return false;
        }
    }
}
