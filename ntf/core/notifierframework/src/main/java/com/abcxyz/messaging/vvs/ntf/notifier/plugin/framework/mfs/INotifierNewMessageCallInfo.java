/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs;

import java.util.Date;
import java.util.Properties;


/**
 * The INotifierNewMessageCallInfo interface defines the methods that the Notifier plug-in can invoke to obtain
 * information regarding a new message deposit.
 * <p>
 * The concrete class that implements this interface class is a container for the new message deposit information.
 */
public interface INotifierNewMessageCallInfo {

    /**
     * Gets the Date object representing the date at which the new message deposit occurred.
     * @return the Date object representing the date at which the new message deposit occurred
     */
    public Date getDate();
    
    /**
     * Gets the telephone number of the caller who left the new message deposit.
     * @return the caller who left the new message deposit
     */
    public String getCaller();
    
    /**
     * Returns whether the telephone number of the caller is visible.
     * @return true if the telephone number of the caller is visible; false otherwise
     */
    public boolean getIsCallerVisible();
    
    /**
     * Returns the display name of the caller who left the new message deposit.
     * @return the display name of the caller
     */
    public String getCallerDisplayName();
    
    /**
     * Returns the additional properties that may have been set
     * @return additional properties or null if none have been set
     */
    public Properties getAdditionalProperties();
}
