/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import java.util.regex.Pattern;

public class NameValidator implements Validator {
    Pattern p = Pattern.compile("[a-zA-Z0-9_-]+");

    public boolean isValid(Object value) {
        if(value == null)
            return false;
        String name = value.toString();
        return p.matcher(name).matches();
    }
}
