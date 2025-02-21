package com.mobeon.masp.chargingaccountmanager;

/**
 * Date: 2007-nov-27
 *
 * @author emahagl
 */
public class ChargingAccountException extends Exception {
    /**
     * Constructor
     *
     * @param msg detailed message about the error
     */
    public ChargingAccountException(String msg) {
        super(msg);
    }

    /**
     * Constructor
     *
     * @param cause the cause of the exception
     */
    public ChargingAccountException(Throwable cause) {
        super(cause);
    }
}
