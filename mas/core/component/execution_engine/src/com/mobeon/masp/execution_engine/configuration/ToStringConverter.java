/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

public class ToStringConverter implements Converter {
    public Object convert(Object value) {
        if(value != null)
            return value.toString();
        return null;
    }
}
