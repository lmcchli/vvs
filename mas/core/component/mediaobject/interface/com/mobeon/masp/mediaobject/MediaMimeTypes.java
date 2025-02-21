/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import jakarta.activation.MimeType;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a List of {@link MimeType}s.
 *
 * A media may consist of multiple parts, each with a
 * specific mime type (for example a {@link IMediaObject})
 * <p>
 * A media object may contain many different mime-types.
 * For example video that typically consist of audio and video.
 * <p>
 * This class is thread-safe, meaning that multiple threads may concurrently
 * use it.
 * <p>
 *
 * @see MimeType
 * @see IMediaObject
 *
 * @author Mats Egland
 */
public class MediaMimeTypes {

    /**
     * The synchronization-lock used.
     */
    private final Object LOCK = new Object();
    /**
     * The list of <code>MimeType</code>s.
     */
    private final List<MimeType> MIMETYPE_LIST = new ArrayList<MimeType>();

    /**
     * Empty constructor. Creates a MediaProperties object
     * with an non-null, but empty, list of mime-types.
     */
    public MediaMimeTypes() {}

    /**
     * Constructor that creates a MediaMimeTypes object with
     * the Mime Types passed in the mimeTypes parameter.
     * <p/>
     * No duplicates will be added, i.e. types that
     * return true when passed to the <code>hasMatchingMimeType</code>
     * will not be added.
     * <p/>
     * The {@link MediaMimeTypes#addMimeType(jakarta.activation.MimeType)}
     * method is used to add each type.
     * 
     * @param mimeTypes List of Mime Types that the object will
     *                  be initialized with.
     *                  The content of the List will be copied (shallow).
     *
     * @throws IllegalArgumentException If the passed list is null, or any of its
     *                                  enclosed mime-types is null.
     */
    public MediaMimeTypes(List<MimeType> mimeTypes) {
        if (mimeTypes == null) {
            throw new IllegalArgumentException(
                    "Passed mimeTypes parameter is null");
        }
        for (MimeType mimeType : mimeTypes) {
            addMimeType(mimeType);
        }
    }
    /**
     * Constructor that creates a <code>MimeType</code> object with
     * the types as specified in the mimeTypes parameter.
     * <p/>
     * No duplicates will be added, i.e. types that
     * return true when passed to the <code>hasMatchingMimeType</code>
     * will not be added.
     * <p/>
     * The {@link MediaMimeTypes#addMimeType(jakarta.activation.MimeType)}
     * method is used to add each type.
     *
     * @param mimeTypes The Mime Types that the object will
     *                  be initialized with.
     *
     * @throws IllegalArgumentException if passed mimeType parameter
     *                                  is null or any of the passed
     *                                  mime-types is null
     */
    public MediaMimeTypes(MimeType... mimeTypes) {
        if (mimeTypes == null) {
            throw new IllegalArgumentException(
                    "Passed mimeType parameter is null");
        }
        for (MimeType mimeType: mimeTypes) {
            addMimeType(mimeType);
        }
    }
    /**
     * Copy-constructor.
     *
     * @param mediaMimeTypes The <code>MediaMimeTypes</code> object that is copied
     *
     * @throws IllegalArgumentException if passed mimeType parameter is null.
     */
    public MediaMimeTypes(MediaMimeTypes mediaMimeTypes) {
        if (mediaMimeTypes == null) {
            throw new IllegalArgumentException(
                    "Passed mediaProperties parameter is null");
        }

        MIMETYPE_LIST.addAll(mediaMimeTypes.getAllMimeTypes());
    }

    /**
     * Adds a the given <code>MimeType</code> to the list of types
     * in this object. No duplicate type will be added, i.e. the
     * given type must return false when passed to the
     * <code>hasMatchingMimeType</code> method.
     *
     * @param mimeType The new mime type
     *
     * @throws IllegalArgumentException If the passed mimeType parameter is null.
     */
    public final void addMimeType(MimeType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("The passed mimeType parameter " +
                    "must not be null");
        }
        synchronized (LOCK) {
            if (!hasMatchingMimeType(mimeType)) {
                MIMETYPE_LIST.add(mimeType);
            }
        }
    }

    /**
     * Appends all the <code>MimeType</code>s in the given
     * <code>MediaMimeTypes</code> to this object. Will not
     * add duplicates, i.e. matching mime-types.
     *
     * @param mediaMimeTypes The mime-types to be added.
     *
     * @throws IllegalArgumentException If the passed parameter is null
     */
    public final void addAll(MediaMimeTypes mediaMimeTypes) {
        if (mediaMimeTypes == null) {
            throw new IllegalArgumentException("The passed mediaMimeTypes is null");
        }
        List<MimeType> newTypes = mediaMimeTypes.getAllMimeTypes();
        synchronized (LOCK) {
            for (MimeType mimeType : newTypes) {
                addMimeType(mimeType);
            }
        }
    }

    /**
     * Removes the specified mime type from the collection of mime types.
     * <p>
     * The passed object must be the same object to remove, i.e. this method
     * will not remove <code>MimeType</code>s that only matches the passed
     * type; so to remove a specific type, it should first be fetched with
     * the <code>getMimeType</code> method.
     *
     *
     * @param mimeType The mime type to remove
     *
     * @throws IllegalArgumentException If the passed mimeType parameter is null.
     */
    public final void removeMimeType(MimeType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("The passed mimeType is null");
        }
        synchronized (LOCK) {
            if (MIMETYPE_LIST.contains(mimeType)) {
                MIMETYPE_LIST.remove(mimeType);
            }
        }
    }
    /**
     * Removes all Mime-Types.
     */
    public final void clearMimeTypes() {
        synchronized (LOCK) {
            MIMETYPE_LIST.clear();
        }
    }
    /**
     * Returns the number of Mime-types this media contains.
     *
     * @return The number of mime-types in this property.
     */
    public final int getNumberOfMimeTypes() {
        synchronized (LOCK) {
            return MIMETYPE_LIST.size();
        }
    }
    /**
     * Returns the <code>MimeType</code>s in this object as a list.
     * <p>
     * Note: the returned List is a copy of the internal list, meaning
     * that modifications to it will not alter this object's list.
     *
     * @return The Mime Types of this object returned as a list
     *
     */
    public final List<MimeType> getAllMimeTypes() {
        synchronized (LOCK) {
            return new ArrayList<MimeType>(MIMETYPE_LIST);
        }
    }
    /**
     * This method returns whether the passed <code>MimeType</code>
     * matches any of the <code>MimeType</code>s in this object.
     * <p>
     * The {@link MimeType#match(String)} method is used to find
     * a matching MimeType, so this method will return true
     * if any of the contained <code>MimeType</code>s has the same
     * primary and sub type as what is in the given type.
     *
     *
     * @param mimeType  The mime type to match.
     *
     * @return true     If match MimeType exist in this object.
     *
     * @throws IllegalArgumentException If the passed mimeType parameter is null.
     */
    public final boolean hasMatchingMimeType(MimeType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("Passed mimeType parameter is null");
        }
        synchronized (LOCK) {
            // first check if the mime-type is in the list of mime-types
            if (MIMETYPE_LIST.contains(mimeType)) {
                return true;
            }
            // second, check if there is a matching MimeType
            boolean found = false;
            for (MimeType type : MIMETYPE_LIST) {
                if (type.match(mimeType)) {
                    found = true;
                    break;
                }
            }
            return found;
        }
    }

    /**
     * Returns the <code>MimeType</code> in this object that matches
     * the given type. If no match is found <code>null</code>
     * is returned.
     * <p/>
     * The {@link MimeType#match(jakarta.activation.MimeType)} method
     * is used to match two <code>MimeType</code>s.
     * <p/>
     * Method is thread-safe.
     *
     * @return  The mime-type in this object that matches the given type
     *          or null if no match.
     *
     * @throws IllegalArgumentException If the passed mimeType parameter is null.
     */
    public final MimeType getMatchingMimeType(MimeType mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("The passed mimeType parameter " +
                    "must not be null");
        }
        synchronized (LOCK) {
           for (MimeType type : MIMETYPE_LIST) {
                if (type.match(mimeType)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Compares a <code>MediaMimeTypes</code> to this media mime types. The
     * objects are equal if they contains the same <code>MimeType</code>s.
     *
     * @param mediaMimeTypes    The <code>MediaMimeTypes</code> object that
     *                          this object is compared to.
     * @return <code>true</code> if the two objects contain the same number of
     * <code>MimeType</code>s and all the <code>MimeType</code>s are equal. The
     * order of the <code>MimeType</code>s is not important.
     */
    public final boolean compareTo(MediaMimeTypes mediaMimeTypes) {
        if (mediaMimeTypes.getNumberOfMimeTypes() != this.getNumberOfMimeTypes()) {
            return false;
        }

        for (MimeType mt : mediaMimeTypes.getAllMimeTypes()) {
            if (!this.hasMatchingMimeType(mt)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "MediaMimeTypes: " + MIMETYPE_LIST;
    }
}
