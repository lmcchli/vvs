/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import jakarta.activation.MimeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class holds properties for a media.
 * <p/>
 * Note that there this class is totally passive, so it makes
 * no guarantees that the properties in it really applies to the
 * media it represent. It is up to the creator of an object of this
 * class to inject correct properties into it, or if the media itself
 * injects its properties into the MediaProperties object.
 * <p/>
 * The properties this class encapsulate are:
 * <ul>
 * <li> Content Type represented as a MIME Type (class {@link MimeType})</li>
 * <li> The size in bytes of the media.</li>
 * <li> The length of the media represented by the {@link MediaLength} class.
 * This class supports many lengths as a length may be expressed in many
 * units.</li>
 * <li> File extension. The file extension together with the Content Type denotes
 *      the format and type the media. Example "wav", "mov"... </li>
 * </ul>
 *
 *
 * @author Mats Egland
 */
public final class MediaProperties {
    /**
     * The synchronization-lock used to synchronize
     * access to the Map of lengths.
     */
    private final Object LOCK = new Object();
    /**
     * Default size.
     */
    private static final long DEFAULT_SIZE = 0;

    private final AtomicReference<MimeType> CONTENT_TYPE =
            new AtomicReference<MimeType>();

    private final AtomicReference<String> FILE_EXTENSION =
            new AtomicReference<String>();
    /**
     * Map of <{@link MediaLength.LengthUnit}, {@link MediaLength}> pairs.
     * Each entry holds the length in the unit given by the key for
     * the media.
     */
    private final Map<MediaLength.LengthUnit, MediaLength> MEDIA_LENGTHS_MAP =
            new HashMap<MediaLength.LengthUnit, MediaLength>();

    /**
     * The size in bytes of the media. Default is 0.
     */
    private final AtomicLong SIZE = new AtomicLong(0);

    /**
     * Empty constructor. Creates a MediaProperties object
     * with a empty <code>MediaMimeTypes</code> member,
     * no lenghts, size set to 0 and contentType = null.
     */
    public MediaProperties() {
        
    }

    /**
     * Contructs a <code>MediaProperties</code> object
     * with the given content-type.
     *
     * @param contentType The content type of the MediaProperties
     *                    represented as a MIME-type. Optional, can be null.
     *
     */
    public MediaProperties(MimeType contentType) {
        this.CONTENT_TYPE.set(contentType);
    }

    /**
     * Creates a <code>MediaProperties</code> object with the
     * specified content type and file extension.
     *
     * @param contentType   The content type of the media. Optional, can be null.
     * @param fileExtension The extension, i.e. "wav", "mov" etc. Optional, can be null.
     *
     */
    public MediaProperties(MimeType contentType, String fileExtension) {
        this(contentType);
        this.FILE_EXTENSION.set(fileExtension);
    }

    /**
     * Creates a <code>MediaProperties</code> object with the
     * specified content type, file extension and size.
     *
     * @param contentType   The content type of the media. Optional, can be null.
     * @param fileExtension The extension, i.e. "wav", "mov" etc. Optional, can be null.
     * @param size          The size in bytes of the media. Must be equal to
     *                      or greater than 0.
     *
     * @throws IllegalArgumentException If size is less than 0.
     */
    public MediaProperties(MimeType contentType, String fileExtension, long size) {
        this(contentType, fileExtension);
        if (size < 0) {
            throw new IllegalArgumentException(
                    "Size must be equal to or greater than 0");
        }
        this.SIZE.set(size);
    }

    /**
     * Creates a <code>MediaProperties</code> object with the
     * specified content type, file extension, size and lengths.
     *
     * @param contentType   The content type of the media.
     * @param fileExtension The extension, i.e. "wav", "mov" etc.
     * @param size          The size in bytes of the media.
     * @param mediaLengths  The lengths of the media (same length but in many units).
     *                      Optional, can be null.
     *
     * @throws IllegalArgumentException If size is less than 0.
     */
    public MediaProperties(MimeType contentType,
                           String fileExtension,
                           long size, MediaLength...mediaLengths) {

        this(contentType, fileExtension, size);
        if (mediaLengths != null) {
            for (MediaLength mediaLength : mediaLengths) {
                if (mediaLength != null) {
                    addLength(mediaLength);
                }
            }
        }
    }


    /**
     * Adds the specified length to this object.
     * If the this object previously contained a length for
     * the unit of the passed length, the old length is replaced
     * by the specified length.
     *
     * @param length The length to be added.
     * @throws IllegalArgumentException If argument is null.
     */
    public void addLength(MediaLength length) {
        if (length == null) {
            throw new IllegalArgumentException("The passed medialength object is null");
        }
        synchronized (LOCK) {
            MEDIA_LENGTHS_MAP.put(length.getUnit(), length);
        }
    }

    /**
     * Adds the specified length in the specified unit.
     * If the this object previously contained a length for
     * the specififed unit the old length is replaced
     * by the specified length.
     *
     * @param unit   The unit of the length.
     * @param length The length to be added.
     *
     * @throws IllegalArgumentException If unit is null
     *                                  or length is negative.
     */
    public void addLengthInUnit(MediaLength.LengthUnit unit, long length) {
        addLength(new MediaLength(unit, length));
    }

    /**
     * Removes the length with the specified unit
     * from this object. If no length in specified unit
     * exist, nothing is removed.
     *
     * @param unit The unit of the length to remove.
     * @throws IllegalArgumentException If argument is null.
     */
    public void removeLengthInUnit(MediaLength.LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("The passed unit object is null");
        }
        synchronized (LOCK) {
            MEDIA_LENGTHS_MAP.remove(unit);
        }
    }

    /**
     * Returns wheter this object has the length represented in the
     * given unit.
     *
     * @param unit The unit of the length.
     * @return true If this object has length in given unit, otherwise false.
     * @throws IllegalArgumentException If argument is null.
     */
    public boolean hasLengthInUnit(MediaLength.LengthUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("The passed unit object is null");
        }
        synchronized (LOCK) {
            return MEDIA_LENGTHS_MAP.containsKey(unit);
        }
    }

    /**
     * Returns the length of the media represented by this object, in
     * represented as the actual <code>MediaLength</code> object that
     * holds the length in this object.
     * <p/>
     * This method will throw <code>IllegalArgumentException</code>
     * if the unit is not supported, so best is to precede with a
     * call to the <code>hasLengthInUnit</code> method.
     *
     * @param unit The unit that the length is to be expressed in.
     * @return The length of of the media represented, in the unit
     *         specified in the parameter.
     * @throws IllegalArgumentException If the length in the unit given is not
     *                                  represented by this object or if
     *                                  argument is null.
     */
    public long getLengthInUnit(MediaLength.LengthUnit unit) {
        synchronized (LOCK) {
            if (hasLengthInUnit(unit)) {
                return MEDIA_LENGTHS_MAP.get(unit).getValue();
            } else {
                throw new IllegalArgumentException("Unit:" + unit +
                        " is not supported by this MediaProperties");
            }
        }
    }

    /**
     * Returns the value of the size-property of the media in bytes.
     *
     *
     * @return Size in bytes of media represented by this object.
     */
    public long getSize() {
        return SIZE.get();
    }

    /**
     * Sets size-property of the media.
     *
     * @param newSize The new value for the size-property (in bytes)
     *
     * @throws IllegalArgumentException If size is less than zero.
     */
    public void setSize(long newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException(
                    "Size must be equal to or greater than 0");
        }
        SIZE.set(newSize);
    }

    /**
     * Returns all medialengths in this object as
     * an <code>List</code> of <code>MediaLength</code>s.
     * The returned List is a new created list, i.e. changes
     * to it will not be reflected in this MediaProperties.
     *
     * @return All lengths in this properties object as a list.
     */
    public List<MediaLength> getAllMediaLengths() {
        return new ArrayList<MediaLength>(MEDIA_LENGTHS_MAP.values());
    }

    /**
     * Returns the content-type the media represented
     * as {@link MimeType}.
     *
     * @return The content-type of the media.
     */
    public MimeType getContentType() {
        return CONTENT_TYPE.get();
    }

    /**
     * Sets the content-type of the media.
     * The content-type is represented as
     * a {@link MimeType}.
     */
    public void setContentType(MimeType contentType) {
        this.CONTENT_TYPE.set(contentType);
    }

    /**
     * Returns the fileextension, ("WAV" etc)
     * for the media represented
     * as a String.
     *
     * @return The fileextension of the media or null if not specified.
     */
    public String getFileExtension() {
        return FILE_EXTENSION.get();
    }

    /**
     * Sets the fileextension, ("WAV" etc)
     * for the media represented
     * as a String.
     *
     * @param ext fileextension of the media.
     */
    public void setFileExtension(String ext) {
        this.FILE_EXTENSION.set(ext);
    }


    /**
     * Returns a string representation of the object.
     * E.g. {content-type=plain/text,fileExtension=txt,size=123B,mediaLengths=[{unit=MILLISECONDS,value=9000}]}
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("content-type=").append(getContentType());
        sb.append(",fileExtension=").append(getFileExtension());
        sb.append(",size=").append(getSize()).append("B");
        sb.append(",mediaLengths=").append(getAllMediaLengths());
        sb.append("}");
        return sb.toString();
    }

}

