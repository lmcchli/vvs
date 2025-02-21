/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms;

import java.util.Collection;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierMessageGenerationException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierSendException;


/**
 * The INotifierSenderSms interface defines the methods that the Notifier plug-in can invoke to send a SMS notification.
 */
public interface INotifierSenderSms {

    /**
     * Sends a new message deposit notification based on the information provided.
     * The notification message will be generated according the subscriber's preferences from the given profile.
     * @param notificationInfo the information about the notification
     * @param subscriberProfile the subscriber-specific information
     * @param sendInfoSms the information about the sending of the notification
     * @throws NotifierMessageGenerationException if the generation of the notification message fails and a default message is not generated.
     * @throws NotifierSendException if the notification cannot be sent.  The cause of the send failure can be retrieved by calling {@link NotifierSendException#getNotifierSendExceptionCause()}
     */
    public void sendNewMessageDepositNotificationSMS(ANotifierNotificationInfo notificationInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, ANotifierSendInfoSms sendInfoSms) 
            throws NotifierMessageGenerationException, NotifierSendException;
    
    /** 
     * Sends a notification based on the information provided.
     * @param sendInfoSms the information about the sending of the notification
     * @param notificationMessage the message to be sent in the notification
     * @throws NotifierSendException if the notification cannot be sent.  The cause of the send failure can be retrieved by calling {@link NotifierSendException#getNotifierSendExceptionCause()}
     */
    public void sendNotificationSms(ANotifierSendInfoSms sendInfoSms, String notificationMessage) 
            throws NotifierSendException;
    
    /**
     * Sends a notification based on the information provided.
     * @param sendInfoSms the information about the sending of the notification
     * @param notificationMessage the message to be sent in the notification
     * @throws NotifierSendException if the notification cannot be sent.  The cause of the send failure can be retrieved by calling {@link NotifierSendException#getNotifierSendExceptionCause()}
     */
    public void sendNotificationSms(ANotifierSendInfoSms sendInfoSms, byte[] notificationMessage) 
            throws NotifierSendException;
    
    
    /**
     * Sends a single cancelInfo based upon CancelInfo
     * 
     * This ultimately results in the sending of a cancel SMS
     * based upon the information in CancelInfo
     * see {@link com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo}
     * 
     * @param info CancelInfo
     */
    public void sendCancelSMS(CancelInfo info);
    
    /**
     * Sends a cancel SMS based upon the Collection of CancelInfo.
     * This will be filtered to not send duplicate cancel SMS.
     * 
     * This ultimately results in the sending of one or more cancel SMS
     * based upon the information in the CancelInfo
     * 
     * see {@link com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo}
     * 
     * @param infoSet A set of CancelInfo to send Cancel to.
     * 
     */
    public void sendCancelSMS(Collection<CancelInfo> infoSet);
}
