/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.util;

import com.mobeon.masp.util.TimeValue;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Looberger
 */
public class TimeValueParser {
    private static final String regexp = "([0-9]*\\.?[0-9]+)(s|ms)";
    private static Pattern pattern;
    static {
       pattern = Pattern.compile(regexp);
    }

    public static TimeValue getTime(String strValue) {
        TimeValue ret = null;

        Matcher m = pattern.matcher(strValue);
        if (!m.matches()) {
            return null;
        }
        String group = m.group(1);
        if (group != null && group.length() > 0) {
            Float time = new Float(group);
            TimeUnit tu = getTimeUnit(strValue);
            if (time.floatValue() < 1) {
                time = time * 1000;
                if (tu == TimeUnit.SECONDS) {
                    tu = TimeUnit.MILLISECONDS;
                } else if (tu == TimeUnit.MILLISECONDS) {
                    tu = TimeUnit.MICROSECONDS;
                } else if (tu == TimeUnit.MICROSECONDS) {
                    tu = TimeUnit.NANOSECONDS;
                }
            }
            ret = new TimeValue(time.intValue(), tu);
        }
        return ret;
    }

    private static TimeUnit getTimeUnit(String strValue) {
        Matcher m = pattern.matcher(strValue);
         if (!m.matches()) {
            return TimeUnit.SECONDS;
        }
        String group = m.group(2);
        if (group.equals("ms")) {
            return TimeUnit.MILLISECONDS;
        } else if (group.equals("s")) {
            return TimeUnit.SECONDS;
        } else {
          // Default to second for now
          return TimeUnit.SECONDS;
        }
    }
}
