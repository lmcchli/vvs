/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierNewMessageCallInfo;


public class NotifierNewMessageCallInfo implements INotifierNewMessageCallInfo{

    private String caller = null;
    private Date date = null;
    private boolean isCallerVisible = false;
    private String callerDisplayName = null;
    Properties additionalProperties = null;

    public NotifierNewMessageCallInfo(String caller, Date date) {
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
    
    public void setIsCallerVisible(boolean isVisible) {
        isCallerVisible = isVisible;
    }
    
    @Override
    public boolean getIsCallerVisible() {
        return isCallerVisible;
    }

    public void setCallerDisplayName(String displayName) {
        callerDisplayName = displayName;
    }
    
    @Override
    public String getCallerDisplayName() {
        return callerDisplayName;
    }

    public void setAdditionalProperties(Properties additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public Properties getAdditionalProperties() {
        return additionalProperties;
    }
    
    public String toString() {
        StringBuilder buffer = new StringBuilder("Caller:");
        buffer.append(caller).append(" ");
        buffer.append("Date:").append(date).append(" ");
        buffer.append("IsCallerVisible:").append(isCallerVisible).append(" ");
        buffer.append("CallerDisplayName:").append(callerDisplayName);
        return buffer.toString();
    }
}
