/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms;

/**
 * The ANotifierSendInfoSms defines the methods that the NTF component can invoke to obtain
 * information about sending the SMS notification.
 * <p>
 * The Notifier plug-in class that implements this abstract class is a container for the information regarding the sending of a notification.
 */
public abstract class ANotifierSendInfoSms {

    /**
     * The NotifierSmppPduType enum contains the possible SMPP PDU that can be used to send the notification.
     */
    public static enum NotifierSmppPduType {
        /**
         * The submit_sm PDU type should be used to send the notification.
         */
        SUBMIT_SM,
        
        /**
         * The data_sm PDU type should be used to send the notification.
         */
        DATA_SM
    }
    
    /**
     * The NotifierPhoneOnMethod enum contains the possible phone on verification methods.
     */
    public static enum NotifierPhoneOnMethod {
        /**
         * No phone on verification should be performed.
         */
        NONE,
        
        /**
         * SMS phone on verification should be performed.
         * <p>
         * Currently, the SMS phone on verification is supported only for data_sm.
         */
        SMS
    }
    
    /**
     * The default number of hours for which the notification will stay valid if stored in the SMSC.
     */
    public static final int NOTIFICATION_SMSC_VALIDITY_HOURS_DEFAULT = 6;

    
    /**
     * Gets the SMPP PDU type to use to send the notification.
     * @return the SMPP PDU type to use to send the notification
     */
    public NotifierSmppPduType getSmppPduType() {
        return null;
    }

    /**
     * Gets the service type to set in the SMPP PDU.
     * @return the service type to set in the SMPP PDU
     */
    public String getSmppServiceType() {
        return null;
    }

    /**
     * Gets the type of number for the source address.
     * @return the type of number for the source address
     */
    public int getSourceAddressTypeOfNumber() {
        return 0;
    }

    /**
     * Gets the numbering plan indicator for the source address.
     * @return the numbering plan indicator for the source address
     */
    public int getSourceAddressNumberingPlanIndicator() {
        return 0;
    }

    /**
     * Gets the number to use as the source address.
     * @return the number to use as the source address
     */
    public String getSourceAddressNumber() {
        return null;
    }

    /**
     * Gets the type of number for the destination address.
     * @return the type of number for the destination address
     */
    public int getDestinationAddressTypeOfNumber() {
        return 0;
    }

    /**
     * Gets the numbering plan indicator for the destination address.
     * @return the numbering plan indicator for the destination address
     */
    public int getDestinationAddressNumberingPlanIndicator() {
        return 0;
    }

    /**
     * Gets the number to use as the destination address.
     * @return the number to use as the destination address
     */
    public String getDestinationAddressNumber() {
        return null;
    }
    
    /**
     * Returns whether the number used as the destination address is normalized.
     * @return true if the number is normalized; false otherwise
     */
    public boolean getIsDestinationAddressNumberNormalized() {
        return false;
    }

    /**
     * Gets the phone on verification method to use.
     * <p>
     * Currently, the SMS phone on verification is supported only for data_sm.
     * @return the NotifierPhoneOnMethod enum constant indicating the phone on verification method to use
     */
    public NotifierPhoneOnMethod getPhoneOnMethod() {
        return null;
    }

    /**
     * Gets the integer number representing the replace position for the notification.
     * <p>
     * A replace position value less than 0 requests that this notification message does not replace any pending notification message in the SMSC.
     * <p>
     * A replace position value of 0 or greater requests that this notification message replace any pending notification message in the SMSC
     *  that has the same replace position.
     * @return the integer number representing the replace position for the notification
     */
    public int getNotificationReplacePosition() {
        return -1;
    }

    /**
     * Gets the number of hours for which this notification message will stay valid if stored in the SMSC.
     * @return the number of hours for which this notification message will stay valid in the SMSC
     */
    public int getNotificationValidity() {
        return NOTIFICATION_SMSC_VALIDITY_HOURS_DEFAULT;
    }
    
    /**
     * Gets the {@link ANotifierResultHandlerSms} object which will handle the send notification result.
     * @return the ANotifierResultHandlerSms object which will handle the send notification result
     */
    public ANotifierResultHandlerSms getNotificationResultHandler() {
        return null;
    }

    /**
     * Returns the string representation of this ANotifierSendInfoSms instance.
     * @return the string representation of this ANotifierSendInfoSms instance
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder("NotifierSendInfoSms:");
        buffer.append(" notificationReplacePosition=").append(getNotificationReplacePosition());
        buffer.append(" notificationValidity=").append(getNotificationValidity());
        buffer.append(" notifierSmppPduType=").append(getSmppPduType());
        buffer.append(" smppServiceType=").append(getSmppServiceType());
        buffer.append(" sourceTon=").append(getSourceAddressTypeOfNumber());
        buffer.append(" sourceNpi=").append(getSourceAddressNumberingPlanIndicator());
        buffer.append(" sourceNumber=").append(getSourceAddressNumber());
        buffer.append(" destinationTon=").append(getDestinationAddressTypeOfNumber());
        buffer.append(" destinationNpi=").append(getDestinationAddressNumberingPlanIndicator());
        buffer.append(" destinationNumber=").append(getDestinationAddressNumber());
        buffer.append(" isDestinationAddressNumberNormalized=").append(getIsDestinationAddressNumberNormalized());
        buffer.append(" notifierPhoneOnMethod=").append(getPhoneOnMethod());
        buffer.append(" notificationResultHandler=").append(getNotificationResultHandler());
        return buffer.toString();
    }
}
