/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

/**
 * A {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier} that represents a Number.
 *
 * @author Mats Egland
 */
public class NumberQualifier extends AbstractMediaQualifier<Integer> {

    /**
     * Creates a <code>NumberQualifier</code> with the
     * vname, value and gender specified.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public NumberQualifier(String name, Integer value, IMediaQualifier.Gender gender) {
        super(name, value, gender);
    }

    public QualiferType getType() {
        return IMediaQualifier.QualiferType.Number;
    }

    public Class getValueType() {
        return Integer.class;
    }

}
