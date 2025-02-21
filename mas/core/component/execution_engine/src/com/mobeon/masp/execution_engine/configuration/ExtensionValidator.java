/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtensionValidator implements Validator {
    Pattern p = Pattern.compile("[\\.\\\\/:;]");
    public boolean isValid(Object value) {
        if(value == null)
            return false;
        String ext = value.toString();
        return ! p.matcher(ext).find();
    }
}
