/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus;


/**
 * The INotifierPhoneOnEvent interface defines the methods that the Notifier plug-in can invoke to obtain
 * information regarding the phone on event.
 */
public interface INotifierPhoneOnEvent {

    /**
     * The NotifierPhoneOnResult contains the possible phone statuses as a result of the phone on check.
     */
    public static enum NotifierPhoneOnResult {
        /** The phone is on. */
        OK,
        
        /** The phone is busy. */
        BUSY,

        /** Temporary failure in determining the phone's status. */
        FAILED_TEMPORARY,
        
        /** Permanent failure in determining the phone's status. */
        FAILED
        
    }
    
    /**
     * Retrieves the phone number for which this phone on event was sent.
     * @return the phone number for which this phone on event was sent
     */
    public String getPhoneNumber();
    
    /**
     * Retrieves the phone's status as a result of the phone on check.
     * @return the {@link NotifierPhoneOnResult} indicating the phone's status as a result of the phone on check
     */
    public NotifierPhoneOnResult getResult();
    
}
