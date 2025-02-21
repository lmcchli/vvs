/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager;

/**
 * Exception class that is thrown when some error occurred when calling the provisioning manager
 *
 * @author ermmaha
 */
public class ProvisioningException extends Exception {

    /**
     * Constructor
     *
     * @param msg detailed message about the error
     */
    public ProvisioningException(String msg) {
        super(msg);
    }

    /**
     * Constructor
     *
     * @param cause the cause of the exception
     */
    public ProvisioningException(Throwable cause) {
        super(cause);
    }
}
