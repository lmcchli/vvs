/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

import java.util.Date;

/**
 * A {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier} that represents a Date in
 * the form YYYY-MM-DD.
 *
 * @author Mats Egland
 */
public class WeekdayQualifier extends DateDMQualifier {

    /**
     * Creates a <code>WeekdayQualifier</code> with the
     * vname, value and gender specified.
     *
     * @param name   Optional, can be null. Sets the name of the qualifer.
     * @param value  Optional, can be null. Sets the value of the qualifier.
     * @param gender Optional, can be null. If so the gender will be set to
     *               <code>IMediaQualifier.Gender.NONE</code>.
     */
    public WeekdayQualifier(String name, Date value, IMediaQualifier.Gender gender) {
        super(name, value, gender);
    }

    public QualiferType getType() {
        return IMediaQualifier.QualiferType.WeekDay;
    }
   
}
