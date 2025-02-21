/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework;

/**
 * The PlatformAccessPluginException class signals that an error has occurred in the PlatformAccess plug-in. 
 */
public class PlatformAccessPluginException extends Exception {

    /**
     * Constructs a PlatformAccessPluginException with the specified detail message.
     * @param message the message containing the details of the error
     */
    public PlatformAccessPluginException(String message) {
        super(message);
    }

}
