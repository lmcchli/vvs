/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import jakarta.activation.MimeType;
import java.util.List;

/**
 * Properties for a {@link IMediaContentResource}.
 * Contains the following combination of characteristics:
 * <p/>
 * <ul>
 * <li> Language as a string. For example "en", "sv", "en_UK".</li>
 * <p/>
 * <li> Type as a human readable tag.
 * For example "Prompt", "System", "Wide Announcement",
 * "Fun Greeting" etc. </li>
 * <p/>
 * <li> Voice Variant as a human readable tag.
 * For example "Female", "Male" </li>
 * <p/>
 * <li> Video Variant as a human readable tag.
 * For example "Blue", "Red" etc</li>
 * <p/>
 * <li> Media Codecs in the resource.
 * I.e. a list of all codecs of the media in the resource.
 * Each codec is represented as a MIME-type with the
 * {@link MimeType} class.</li>
 * </ul>
 * <p/>
 * The language argument is a valid ISO Language Code.
 * These codes are the lower-case, two-letter codes as defined by ISO-639. You can find a full list of these codes at a number of sites, such as:
 * http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt
 *
 * @author Mats Egland
 */
public interface IMediaContentResourceProperties {
    /**
     * The language is a valid ISO Language Code.
     * These codes are the lower-case, two-letter codes as defined by ISO-639.
     * <p/>
     * You can find a full list of these codes at a number of sites, such as:
     * http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt
     *
     * @return The language in the resource.
     */
    public String getLanguage();

    /**
     * Returns the type of the media content in the
     * resource.
     * <p/>
     * <p/>
     * Example "Prompt", "System Wide Announcement"
     *
     * @return The type of the content in the resource.
     *         Null if not specified.
     */
    public String getType();

    /**
     * Returns the voice variant of the resource.
     * <p/>
     * For example "Female", "Male", "Bob".
     *
     * @return The voice variant as a human
     *         readable tag. Null if not specified.
     */
    public String getVoiceVariant();

    /**
     * Returns the video variant of the resource.
     * <p/>
     * For example "Female", "Male", "Bob".
     *
     * @return The video variant as a human
     *         readable tag. Null if not specified.
     */
    public String getVideoVariant();

    /**
     * Returns the codecs of the media in the resource.
     * Each codec is represented as a MIME Type, as
     * described in RFC 3555.
     * <p/>
     * The list returned is a new list. So it is safe
     * to iterate over it. 
     *
     * @return List of all codecs used in the resource.
     */
    public List<MimeType> getMediaCodecs();

    /**
     * Returns whether this resource has a codec that
     * matches the specified codec.
     *
     * @param mimeType The codec as a MimeType.
     *
     * @return true if resource has matching codec.
     */
    public boolean hasMatchingCodec(MimeType mimeType);

    /**
     * Compares this properties object with another.
     * @param other
     * @return true if the objects has the same language, voice variant,
     * video variant and codecs. False otherwise.
     */
    public boolean equals(Object other);   
}
