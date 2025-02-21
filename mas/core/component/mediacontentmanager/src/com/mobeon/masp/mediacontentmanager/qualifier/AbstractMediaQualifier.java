/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

/**
 * Abstract implementation of {@link IMediaQualifier} that
 * provides common functionality. Represents a Qualifer with
 * value-type of <code>E</code>.
 * <p/>
 *
 * A qualifier may have a gender that is used when
 * it is converted into a <code>IMediaObject</code>.
 * <p/>
 * For example: In spanish the number one (1) has three
 * different representations depending on
 * the gender: un, una and uno.
 *
 * @author Mats Egland
 */
public abstract class AbstractMediaQualifier<E> implements IMediaQualifier {
    /**
     * Optional. A Qualifier may have a name.
     */
    private String name;

    /**
     * The value of this qualifier.
     */
    protected E value;

    /**
     * The gender of the qualifier.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     */
    private IMediaQualifier.Gender gender;


    /**
     * Creates a <code>AbstractMediaQualifier</code> with the
     * specified name, value and gender.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public AbstractMediaQualifier(String name, E value, IMediaQualifier.Gender gender) {
        this.name = name;
        this.value = value;
        if (gender == null) {
            this.gender = IMediaQualifier.Gender.None;
        } else {
            this.gender = gender;
        }
    }

    /**
     * Returns the name of the qualifier.
     *
     * @return The name of the qualifier. Null if not
     *         specified.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this qualifier.
     *
     * @param name The name this qualifier will have.
     */
    public void setName(String name) {
        this.name = name;

    }

    // javadoc in interface
    public E getValue() {
        return this.value;
    }

    /**
     * Sets the value of this qualifier.
     *
     * @param value The value.
     */
    public void setValue(E value) {
        this.value = value;
    }

    /**
     * Returns the gender for this qualifier.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     *
     * @return The gender of the qualifier. <code>NONE</code> is
     *         default.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the gender for this qualifier.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     *
     * @param gender The gender of the qualifier.
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
