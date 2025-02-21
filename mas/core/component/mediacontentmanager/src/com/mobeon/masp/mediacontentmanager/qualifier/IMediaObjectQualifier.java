/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * A {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier}
 * that represents a IMediaObject.
 *
 * @author Mats Egland
 */
public class IMediaObjectQualifier extends AbstractMediaQualifier<IMediaObject> {

    /**
     * Creates a <code>IMediaObjectQualifier</code> with the
     * name, value and gender specified.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public IMediaObjectQualifier(String name, IMediaObject value, IMediaQualifier.Gender gender) {
        super(name, value, gender);
    }

    // Javadoc in IMediaQualifier
    public QualiferType getType() {
        return IMediaQualifier.QualiferType.MediaObject;
    }

    // Javadoc in IMediaQualifier
    public Class getValueType() {
        return IMediaObject.class;
    }

}
