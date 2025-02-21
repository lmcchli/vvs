/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.IMediaObject;


/**
 * Factory that creates {@link IMediaQualifier}s.
 *
 * @author Mats Egland
 */
public interface IMediaQualifierFactory {

    /**
     * Creates a <code>IMediaQualifier</code> with the
     * specified type, name, value and gender.
     * <p/>
     * A qualifier may have a gender that is used when
     * it is converted into a <code>IMediaObject</code>.
     * <p/>
     * For example: In spanish the number one (1) has three
     * different representations depending on
     * the gender: un, una and uno.
     * <p/>
     * Note: This method does not support creation of qualifers
     *       of type <code>IMediaObject</code>. Use the method
     *       {@link IMediaQualifierFactory#create(String, com.mobeon.masp.mediaobject.IMediaObject, com.mobeon.masp.mediacontentmanager.IMediaQualifier.Gender)}
     *       to create such qualifiers.
     *
     * @param type   The type of qualifer to create.
     * @param name   Optional, may be null. Gives the name of the qualifier.
     * @param value  Optional, may be null. Gives the value of the qualifier.
     *               Represented as a String.
     * @param gender Optional, may be null.
     *               Gives the gender of the qualifier, for example "Male".
     *               If null is passed the gender will be set
     *               to <code>IMediaQualifier.Gender.NONE</code>.
     *
     * @return The created qualifer.
     * @throws MediaQualifierException
     *  If:
     *      <ul>
     *          <li>The passed value-string cannot be parsed to the type-value
     *              of the qualifer.
     *              Note: Null is legal though!</li>
     *          <li>The qualifier-type is of type <code>IMediaObject</code>.
     *              Use the specific create method that takes a
     *              <code>IMediaObject</code> as inparamter to create
     *              qualifiers of type <code>IMediaObject</code>.
     *
     *      </ul>
     */
    IMediaQualifier create(IMediaQualifier.QualiferType type,
                           String name,
                           String value,
                           IMediaQualifier.Gender gender)
        throws MediaQualifierException;

    /**
     * Specific method to create <code>IMediaQualifier</code>s
     * of qualifer-type <code>IMediaObject</code>, that takes an IMediaObject as value.
     *
     *
     * @param name          Optional, may be null. Gives the name of the qualifier.
     * @param mediaObject   Optional, may be null. Gives the value of the qualifier.
     *                      Represented as a String.
     * @param gender        Optional, may be null.
     *                      Gives the gender of the qualifier, for example "Male".
     *                      If null is passed the gender will be set
     *                      to <code>IMediaQualifier.Gender.NONE</code>.
     *
     * @return The created qualifer with qualifer-type <code>IMediaObject</code>
     *
     */
    IMediaQualifier create(String name,
                           IMediaObject mediaObject,
                           IMediaQualifier.Gender gender);
}
