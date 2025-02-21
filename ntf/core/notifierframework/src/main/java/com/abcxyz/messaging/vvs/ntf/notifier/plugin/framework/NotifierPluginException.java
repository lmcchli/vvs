/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

/**
 * The NotifierPluginException class signals that an error has occurred in the Notifier plug-in. 
 */
public class NotifierPluginException extends Exception {

    /**
     * Constructs an NotifierPluginException with the specified detail message.
     * @param message the message containing the details of the error
     */
    public NotifierPluginException(String message) {
        super(message);
    }
}
