/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier} that represents a Date in
 * the form YYYY-MM-DD.
 *
 * @author Mats Egland
 */
public class DateDMQualifier extends AbstractMediaQualifier<Date> {
    protected DateFormat dateFormat;

    /**
     * Creates a <code>DateDMQualifier</code> with the
     * vname, value and gender specified.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public DateDMQualifier(String name, Date value, IMediaQualifier.Gender gender) {
        super(name, value, gender);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public QualiferType getType() {
        return IMediaQualifier.QualiferType.DateDM;
    }

    public Class getValueType() {
        return Date.class;
    }

}
