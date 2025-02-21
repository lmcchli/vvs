/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus;


/**
 * The INotifierPhoneOnRouter defines the method that the Notifier plug-in can invoke to obtain
 * access to phone on events.
 */
public interface INotifierPhoneOnRouter {

    /**
     * Registers a receiver for phone on events.
     * @param phoneOnReceiver the phone on event receiver 
     */
    public void register(ANotifierPhoneOnReceiver phoneOnReceiver);
    
}
