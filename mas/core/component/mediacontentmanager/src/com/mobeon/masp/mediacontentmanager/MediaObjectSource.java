/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.MediaLength;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a the source of an <code>IMediaObject</code>.
 * A source have the content of an media object in some way.
 * Either as a reference to a file that contains the content
 * or as text. A <code>MediaObjectSource</code> may also have
 * the length of the media. The length can be given in
 * the units, each length is represented by the class
 * {@link MediaLength}.
 *
 * This class is not synchronized as its content should have
 * been filled before any requests is done.
 *
 * @author Mats Egland
 */
public class MediaObjectSource {
    /**
     * The types of sources. A MediaFile type
     * has a reference to a mediafile which holds
     * the content. A Text type holds the content in
     * this class as text.
     */
    public enum Type {MEDIAFILE, TEXT}
    /**
     * The source reference.
     */
    private String src;

    /**
     * The sourcetext. If the type of this source is
     * <code>Text</code>, this is the actual content.
     */
    private String sourceText;
    /**
     * The type of source.
     */
    private Type type;
    /**
     * URI to the file that holds the definition of this object.
     */
    private URI definitionFile;

    /**
     * Map of <{@link com.mobeon.masp.mediaobject.MediaLength.LengthUnit},
     * {@link com.mobeon.masp.mediaobject.MediaLength}> pairs.
     * Each entry holds the length in the unit given by the key for
     * the media.
     */
    private final Map<MediaLength.LengthUnit, MediaLength> MEDIA_LENGTHS_MAP =
            new HashMap<MediaLength.LengthUnit, MediaLength>();

    /**
     * Creates a <code>MediaObjectSource</code> with the specified
     * type and source.
     *
     * @param type  The {@link Type} of the created source.
     *
     * @param src   The source-reference of the created source.
     *
     * @param definitionFile The file that holds the definition of this
     *                       object source.
     *
     * @throws IllegalArgumentException If: type is null, src is null or empty
     *                                      or definitionFile is null.
     */
    public MediaObjectSource(Type type, String src, URI definitionFile) {
        if (type == null) {
            throw new IllegalArgumentException("Argument type is null");
        } else if (src == null) {
            throw new IllegalArgumentException("Argument src is null");
        } else if (src.trim().length() == 0) {
            throw new IllegalArgumentException("Argument src is empty");
        } else if (definitionFile == null) {
            throw new IllegalArgumentException("Argument definitionFile is null");
        }
        this.type = type;
        this.src = src;
        this.definitionFile = definitionFile;
    }
    /**
     * Returns the source text of this source. If the type of this source is
     * <code>Text</code>, this is the actual content. If the type
     * of this source is <code>MediaFile</code> this is the textual
     * representation of the content in the referenced file.
     *
     * @return Textual representation of the source.
     */
    public String getSourceText() {
        return sourceText;
    }

    /**
     * Returns the file extension for this media object source. Returns null
     * if the type is not media file.
     * @return the file extension. For example, "wav"
     */
    public String getFileExtension() {
        if(type == Type.MEDIAFILE)
            return src.substring(getSrc().lastIndexOf(".")+1);
        else
            return null;
    }

    /**
     * Sets the source text of this source. If the type of this source is
     * <code>Text</code>, this is the actual content. If the type
     * of this source is <code>MediaFile</code> this is the textual
     * representation of the content in the referenced file.
     *
     * @param sourceText Textual representation of the source.
     */
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    /**
     * Returns the <code>Type</code> for this source.
     *
     * @return The type of source. See {@link Type} enumeration.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the <code>Type</code> of this source.
     *
     * @param type The type of source. See {@link Type} enumeration.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Sets the source-reference of this source.
     * If the type of this source is
     * <code>Text</code>, this is only a label. If the type
     * of this source is <code>MediaFile</code> this is a
     * reference to a file on the file system.
     *
     * @param src The source-reference.
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * Returns the source-reference of this source.
     * If the type of this source is
     * <code>Text</code>, this is only a label. If the type
     * of this source is <code>MediaFile</code> this is a
     * reference to a file on the file system.
     *
     * @return the source-reference of this media object source.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Returns URI to the definition file, i.e. the
     * file that contains the definition for this object source.
     *
     * @return The file that holds the
     *         definition of this content.
     */
    public URI getDefinitionFile() {
        return definitionFile;
    }

    /**
     * Adds the specified length to this object.
     * If the this object previously contained a length for
     * the unit of the passed length, the old length is replaced
     * by the specified length.
     *
     * Not thread-safe.
     *
     * @param length The length to be added.
     * @throws IllegalArgumentException If argument is null.
     */
    public void addLength(MediaLength length) {
        if (length == null) {
            throw new IllegalArgumentException("The passed medialength object is null");
        }
        MEDIA_LENGTHS_MAP.put(length.getUnit(), length);
    }
    /**
     * Returns all medialengths in this object as
     * an <code>List</code> of <code>MediaLength</code>s.
     * <p/>
     * The returned List is a new created list, i.e. changes
     * to it will not be reflected in this MediaProperties.
     *
     * @return All lengths in this properties object as a list.
     */
    public List<MediaLength> getAllMediaLengths() {
        return new ArrayList<MediaLength>(MEDIA_LENGTHS_MAP.values());
    }

}
