/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author David Looberger
 */
public class Validate {
     public static boolean validateTime(String time, String regexp) {

        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(time);
        return m.matches();
    }

    public static boolean validateMimeType(String type) {
        // TODO: Not implemented yet
        // Should check that the attribute is a valid, supported mime type
        return true;
    }
}
