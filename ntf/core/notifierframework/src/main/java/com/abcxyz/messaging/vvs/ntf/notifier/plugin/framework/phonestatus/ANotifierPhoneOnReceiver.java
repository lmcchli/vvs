/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus;


/**
 * The ANotifierPhoneOnReceiver abstract class defines the methods that the NTF component can invoke
 * to communicate phone on events to the Notifier plug-in.
 * <p>
 * The Notifier plug-in concrete class that implements this abstract class should handle the phone on event.
 */
public abstract class ANotifierPhoneOnReceiver {

    /**
     * Handles a phone on event.
     * @param phoneOnEvent - the INotifierPhoneOnEvent object containing the phone on information
     */
    public void phoneOn(INotifierPhoneOnEvent phoneOnEvent) {
        return;
    }

}
