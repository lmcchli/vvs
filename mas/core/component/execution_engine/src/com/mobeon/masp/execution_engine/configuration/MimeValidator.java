/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import java.util.regex.Pattern;

public class MimeValidator implements Validator {
    Pattern p = Pattern.compile("[^ \\(\\)<>@,;:\\\"/\\[\\]\\?=]+/[^ \\(\\)<>@,;:\\\"/\\[\\]\\?=]+");
    public boolean isValid(Object value) {
        if(value == null)
            return false;
        String mime = value.toString();
        return p.matcher(mime).matches();
    }
}
