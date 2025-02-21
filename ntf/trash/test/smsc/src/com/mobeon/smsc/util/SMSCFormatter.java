/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.util;

import java.util.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;

/**
 * SMSCformatter is used to format teh log lines. It creates lines of the form
 * <I>2003-07-08 08:35:05.343 [HandlerThread-3/BindHandler.handleSMPP] Log message</I>
 */
public class SMSCFormatter extends java.util.logging.Formatter {
    private Calendar cal;
    private SimpleDateFormat df;

    /**
     * Constructor.
     */
    public SMSCFormatter() {
        super();
        cal = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        df.setTimeZone(cal.getTimeZone());
    }
    
    /**
     * Format the given log record and return the formatted string.
     *@param r - the log record to be formatted.
     *@return the formatted log record
     */
    public String format(LogRecord r) {
        String className = r.getSourceClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        StringBuffer sb = new StringBuffer();
        sb.append(df.format(new Date(r.getMillis())))
            .append(" ").append(r.getLevel().getName())
            .append(" [").append(Thread.currentThread().getName()).append("/")
            .append(className)
            .append(".")
            .append(r.getSourceMethodName())
            .append("] ")
            .append(r.getMessage())
            .append("\n");
        return new String(sb);
    }
}
