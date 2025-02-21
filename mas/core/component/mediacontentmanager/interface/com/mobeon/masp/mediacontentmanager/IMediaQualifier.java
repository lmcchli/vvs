/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

/**
 * A <code>IMediaQualifier</code> represents a qualifier for media.
 * The qualifier have two purposes:
 * <ul>
 * <li>To qualify which media to choose from multiple medias</li>
 * <li>To be itself </li>
 * </ul>
 * A qualifer has a specific type and a value. The types are
 * represented by the {@link QualiferType} enumeration. The types are
 * as follows with its associated value-type
 *
 * <p/>
 * The types are (From FD_MediaContentManager.14)
 * <ul>
 *  <li>Number A positive integer e.g. �101� and possibly a gender (female,
 *      male and neutral). Value-type = Integer</li>
 *  <li>CompleteDate A complete date in the form of �YYYY-MM-DD HH:MM:SS +-
 *      UTC� e.g. 2005-08-12 23:13:23 +0200�. Value-Type = todo.</li>
 *  <li>DateDM A date in the form of �YYYY-MM-DD� e.g. �2005-08-12�.
 *      Value-Type = todo</li>
 *  <li>Weekday A date in the form of �YYYY-MM-DD� e.g. �2005-08-12�.
 *      Value-Type = todo</li>
 *  <li>Time12 A time in the form �HH:MM:SS� e.g. 23:13:23�.
 *      Value-Type = todo</li>
 *  <li>Time24 A time in the form �HH:MM:SS� e.g. 23:13:23�.
 *      Value-Type = todo</li>
 *  <li>String String in UTF-8 e.g. �John Doe�.
 *      Value-Type = String</li>
 *  <li>IMediaObject An arbitrary IMediaObject e.g. the recorded name of a
 *      subscriber.Value-Type = todo.</li>
 * </ul>
 * <p/>
 * A qualifier may have a gender that is used when
 * it is converted into a <code>IMediaObject</code>.
 * <p/>
 * For example: In spanish the number one (1) has three
 * different representations depending on
 * the gender: un, una and uno.
 *
 * @author Mats Egland
 */
public interface IMediaQualifier {
    /**
     * Enumeration for genders.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     */
    public enum Gender {
        Male, Female, None
    }

    /**
     * Enumeration for the different types
     * of Qualifiers.
     * <p/>
     * The types are (From FD_MediaContentManager.14)
     * <ul>
     *  <li>Number A positive integer e.g. �101� and possibly a gender (female,
     *      male and neutral)</li>
     *  <li>CompleteDate A complete date in the form of �YYYY-MM-DD HH:MM:SS +-
     *      UTC� e.g. 2005-08-12 23:13:23 +0200�.</li>
     *  <li>DateDM A date in the form of �YYYY-MM-DD� e.g. �2005-08-12�</li>
     *  <li>Weekday A date in the form of �YYYY-MM-DD� e.g. �2005-08-12�</li>
     *  <li>Time12 A time in the form �HH:MM:SS� e.g. 23:13:23�</li>
     *  <li>Time24 A time in the form �HH:MM:SS� e.g. 23:13:23�</li>
     *  <li>String String in UTF-8 e.g. �John Doe�</li>
     *  <li>MediaObject An arbitrary IMediaObject e.g. the recorded name of a
     *      subscriber.</li>
     * </ul>
     */
    public enum QualiferType {
        Number,
        CompleteDate,
        DateDM,
        WeekDay,
        Time12,
        Time24,
        String,
        MediaObject}

    /**
     * Returns the name of this qualifer. Optional parameter, can be null.
     *
     * @return The name of the <code>IMediaQualifier</code> or <code>Null</code>
     *         if not specified.
     */
    String getName();

    /**
     * Sets the name of this qualifer.
     *
     * @param name The new name of the qualifier.
     */
    void setName(String name);

    /**
     * Returns the type of this qualifier. See the {@link QualiferType} enumeration
     * for the possible types.
     *
     * @return The type of qualifier.
     */
    QualiferType getType();

    /**
     * Returns the value of the qualifier. The value is of different
     * type depending of the type of qualifier. The <code>getValueType</code>
     * method returns the <code>Class</code> of the value object.
     *
     * @return The value of the qualifier.
     */
    Object getValue();

    
    /**
     * Returns the <code>Class</code> of the value. The value itself
     * is retrieved with the <code>getValue</code> method.
     *
     * @return The Class of the value in this qualifier.
     */
    Class getValueType();

    /**
     * Returns the gender of this qualifer. The {@link Gender} enumeration
     * specifies the different genders.
     *
     * @return The gender of this qualifer.
     */
    
    Gender getGender();
    
    /**
     * Sets the gender of this qualifer.
     *
     * @param gender The new gender of the qualifier.
     */

    public void setGender(Gender gender);
    
}
