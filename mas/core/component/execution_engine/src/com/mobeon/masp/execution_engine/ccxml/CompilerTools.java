/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilerTools {
    public static boolean isValidStringAttribute(String value) {
        if (value == null || value.trim().length() == 0)
            return false;
        return true;
    }

    /**
     * Verify that the supplied value is one of the values
     * 'true' or 'false'.
     * @param value The Value to verify
     * @return true if the value has a correct value, false otherwise
     */
    public static boolean validateTrueOrFalse(String value) {
        if (value.equals("true") ||
            value.equals("false")) {
            return true;
        }
        else {
              return false;
        }
    }

    public static boolean validateTimeout(String timeout) {
        Pattern p = Pattern.compile("^[0-9]+(s|ms)$");
        Matcher m = p.matcher(timeout);
        return m.matches();
    }

    public static boolean isAtMostOneDefined(
        String ... values) {
        int moreThanOne = 0;
        for(String value:values) {
            if(value != null) {
                moreThanOne++;
                if(moreThanOne > 1)
                    break;
            }
        }
        return moreThanOne <= 1;
    }

    public static boolean isAllNull(String ... values) {
        boolean result = true;
        for(String value:values ) {
            if(value != null) {
                result = false;
                break;
            }
        }
        return result;
    }
}
