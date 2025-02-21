/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import java.util.Properties;


/**
 * The INotifierNtfNotificationInfo interface defines the methods that the Notifier plug-in can invoke to access values 
 * stored under NTF-component-specific event property names.
 * <p>
 * This allows access to the event property values without needing to know the NTF-component-specific event property names.
 */
public interface INotifierNtfNotificationInfo {

    /**
     * Gets the event properties that was passed to create this INotifierNtfNotificationInfo object.
     * <p>
     * This is a convenience method.  It avoids the need to pass two containers: the event properties and this object;
     * this object can be passed alone to get the same information.
     * @return the event properties
     */
    public Properties getNotificationEventProperties();
    
    /**
     * Gets the telephone number of the sender of the message that triggered this notification.
     * @return the telephone number of the sender of the message that triggered this notification
     */
    public String getSenderTelephoneNumber();
    
    /**
     * Gets the telephone number of the receiver of the message that triggered this notification.
     * @return the telephone number of the receiver of the message that triggered this notification
     */
    public String getReceiverTelephoneNumber();
    
    /**
     * Gets the telephone number to which this slamdown notification should be sent.
     * @return the telephone number to which this slamdown notification should be sent
     */
    public String getSlamdownNotificationTelephoneNumber();
    
    /**
     * Gets the name of the file containing the slamdown events.
     * @return the name of the file containing the slamdown events
     */
    public String getSlamdownEventFileName();
    
    /**
     * Determines whether the state of the message that triggered this notification is new.
     * @return true if the state of the message is new; false otherwise
     */
    public boolean getIsMsgStateNew();

    /**
     * Determines whether the state of the message that triggered this notification is read.
     * @return true if the state of the message is read; false otherwise
     */
    public boolean getIsMsgStateRead();

    /**
     * Returns whether the state of the message that triggered this notification is saved.
     * @return true if the state of the message is saved; false otherwise
     */
    public boolean getIsMsgStateSaved();
    
    /**
     * Gets the originator message store address for the message that triggered this notification.
     * <p>
     * If the originator/sender of the message is a subscriber, the omsa is the msid of the sender of the message.
     * If the originator/sender of the message is not a subscriber, the omsa is the eid of the sender of the message.
     * @return the originator message store address for the message that triggered this notification
     */
    public String getOmsa();
    
    /**
     * Gets the recipient message store address for the message that triggered this notification.
     * <p>
     * If the recipient/receiver of the message is a subscriber, the rmsa is the msid of the sender of the message.
     * If the recipient/receiver of the message is not a subscriber, the rmsa is the eid of the sender of the message.
     * @return the recipient message store address for the message that triggered this notification
     */
    public String getRmsa();
    
}
