/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * An interface providing access to management counter 
 */
public interface INotifierManagementCounter {

    /**
     * Increment a counter of the given service successful operation
     *@param counterServiceName - the name of the counter
     */
    public void incrementSuccessCounter(String counterServiceName);
    
    /**
     * Increment a counter of the given service failed operation
     *@param counterServiceName - the name of the counter
     */
    public void incrementFailCounter(String counterServiceName);
    
}
