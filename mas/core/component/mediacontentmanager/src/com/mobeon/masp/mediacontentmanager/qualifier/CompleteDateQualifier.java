/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier} that represents a Date in
 * the form YYYY-MM-DD hh:mm:ss +UTC.
 *
 * @author Mats Egland
 */
public class CompleteDateQualifier extends DateDMQualifier {

    /**
     * Creates a <code>CompleteDateQualifier</code> with the
     * vname, value and gender specified.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public CompleteDateQualifier(String name, Date value, IMediaQualifier.Gender gender) {
        super(name, value, gender);
        dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    }

    public QualiferType getType() {
        return IMediaQualifier.QualiferType.CompleteDate;
    }

}
