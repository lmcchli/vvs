/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs;

import java.util.Date;
import java.util.Map;
import java.util.Properties;


/**
 * The ANotifierSlamdownCallInfo abstract class defines the methods that can be invoked by the NTF component and
 * the Notifier plug-in to get information regarding the slam-down call.
 * <p>
 * The concrete class that implements this abstract class is a container for the slam-down call information.
 */
public abstract class ANotifierSlamdownCallInfo {

    /**
     * Gets the caller of the slam-down call.
     * @return the caller of the slam-down call
     */
    public String getCaller() {
        return null;
    }

    /**
     * Gets the Date object representing the date at which the slam-down call occurred.
     * @return the Date object representing the date at which the slam-down call occurred
     */
    public Date getDate() {
        return null;
    }

    /**
     * Sets the properties that associated to slam-down event when the slam-down call occurred.
     */
    public void setProperties(Properties properties) {
    }

    /**
     * Gets the properties that were associated to slam-down event when the slam-down call occurred.
     * @return the Map object containing the properties that were created when the slam-down call occurred
     */
    public Properties getProperties() {
        return null;
    }

}
