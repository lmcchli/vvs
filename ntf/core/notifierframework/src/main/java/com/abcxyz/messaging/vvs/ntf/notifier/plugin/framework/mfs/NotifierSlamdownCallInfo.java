/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs;

import java.util.Date;
import java.util.Map;
import java.util.Properties;


/**
 * The NotifierSlamdownCallInfo class is a container for the information about a slam-down call.
 */
public class NotifierSlamdownCallInfo extends ANotifierSlamdownCallInfo {

    private String caller = null;
    private Date date = null;
    Properties properties = null;
    
    /**
     * Constructs a NotifierSlamdownCallInfo instance with the specified caller and date.
     * @param caller the caller of the slam-down call
     * @param date the Date object representing the date at which the slam-down call occurred
     */
    public NotifierSlamdownCallInfo(String caller, Date date) {
        this.caller = caller;
        this.date = date;                    
    }

    @Override
    public String getCaller() {
        return caller;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns the string representation of this slam-down.
     * This consists of the caller and date of this slam-down call.
     * @return the string representation of this slam-down
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder("Caller:");
        buffer.append(caller).append(" ");
        buffer.append("Date:").append(date);
        return buffer.toString();
    }
}
