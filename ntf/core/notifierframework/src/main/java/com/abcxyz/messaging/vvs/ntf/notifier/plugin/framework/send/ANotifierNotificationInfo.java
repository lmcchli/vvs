/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send;

import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;

/**
 * The ANotifierNotificationInfo abstract class defines the methods that NTF component can invoke to obtain
 * information regarding a notification.
 * <p>
 * The Notifier plug-in class that implements this abstract class is a container for the information regarding a notification.
 */
public abstract class ANotifierNotificationInfo {

    /**
     * Gets the notification type for this notification.
     * @return the notification type for this notification
     */
    public String getNotificationType() {
        return null;
    }

    /**
     * Returns whether the phone number of the sender of the message that trigger this notification is visible.
     * @return true if the sender of the message that trigger this notification is visible; false otherwise
     */
    public boolean getSenderVisibility() {
        return false;
    }

    /**
     * Gets the phone number of the sender of the message that trigger this notification.
     * @return the phone number of the sender of the message that trigger this notification
     */
    public String getSenderPhoneNumber() {
        return null;
    }

    /**
     * Gets the display name of the sender of the message that trigger this notification.
     * @return the display name of the sender of the message that trigger this notification
     */
    public String getSenderDisplayName() {
        return null;
    }

    /**
     * Gets the phone number of the receiver of the message that trigger this notification.
     * @return the phone number of the receiver of the message that trigger this notification
     */
    public String getReceiverPhoneNumber() {
        return null;
    }

    /**
     * Gets the phone number to which this notification should be sent.
     * @return the phone number to which this notification should be sent
     */
    public String getNotificationPhoneNumber() {
        return null;
    }

    /**
     * Returns whether the message that trigger this notification is urgent.
     * @return true if the message that trigger this notification is urgent; false otherwise
     */
    public boolean getIsUrgent() {
        return false;
    }

    /**
     * Gets the date at which the message that trigger this notification was left.
     * @return the date at which the message that trigger this notification was left
     */
    public Date getDate() {
        return null;
    }

    /**
     * Gets the string value to use for the notification template PAYLOAD tag.
     * <p>
     * Please see the MiO VVS NTF CPI documentation for more information about the notification template PAYLOAD tag.
     * @return the string value to use for the notification template PAYLOAD tag
     * @throws NotifierMfsException if an error occurs while trying to get the pay-load content
     */
    public String getMessagePayloadAsString() throws NotifierMfsException {
        return null;
    }

   /**
     * Gets the byte array to use for the notification template PAYLOAD tag.
     * <p>
     * Please see the MiO VVS NTF CPI documentation for more information about the notification template PAYLOAD tag.
     * <p>
     * This method is called instead of {@link ANotifierNotificationInfo#getMessagePayloadAsString} when NTF is configured
     * to send the notification in bytes.
     * @return the byte array to use for the notification template PAYLOAD tag
     * @throws NotifierMfsException if an error occurs while trying to get the pay-load content
    */
    public byte[] getMessagePayloadAsBytes() throws NotifierMfsException {
        return null;
    }

    /**
     * Gets the property value for the specified property name.
     * @param propertyName the name of the property for which to get the value
     * @return the property value for the specified property name
     */
    public String getProperty(String propertyName) {
        return null;
    }

    /**
     * Gets the properties associated with this notification.
     * @return the properties associated with this notification
     */
    public Properties getProperties() {
        return null;
    }

}
