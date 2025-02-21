/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send;

import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierConstants;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;

/**
 * The NotifierNotificationInfo class is a sample container for the information related to a notification.
 */
public class NotifierNotificationInfo extends ANotifierNotificationInfo {
        
    protected static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierNotificationInfo.class);
    protected INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();
    INotifierMfsEventManager notifierMfsEventManager = TemplateSmsPlugin.getMfsEventManager();
    
    protected Properties properties = null;
    
    
    public NotifierNotificationInfo(Properties properties) {
        this.properties = properties;
        log.debug("Intialized with properties: " + this.properties);
    }


    @Override
    public String getNotificationType() {
        String notificationType = properties.getProperty(NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY);
        log.debug("Got notification type from property " + NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY + ": " + notificationType);
        return notificationType;
    }


    @Override
    public boolean getSenderVisibility() {
        boolean isVisible = false;
        String visibility = properties.getProperty(NotifierConstants.SENDER_VISIBILITY_PROPERTY);
        if(visibility == null || "1".equals(visibility) || "true".equalsIgnoreCase(visibility)) {
            isVisible = true;
        }
        log.debug("Got sender visibility from property " + NotifierConstants.SENDER_VISIBILITY_PROPERTY + ": " + isVisible);
        return isVisible;
    }


    @Override
    public String getSenderPhoneNumber() {
        String senderNumber = properties.getProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY);
        senderNumber = notifierUtil.getNormalizedTelephoneNumber(senderNumber);
        log.debug("Got sender phone number from property " + NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY + ": " + senderNumber);
        return senderNumber;
    }


    @Override
    public String getSenderDisplayName() {
        String senderDisplayName = properties.getProperty(NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY);
        log.debug("Got senderDisplayName from property " + NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY + ": " + senderDisplayName);
        return senderDisplayName;
    }


    @Override
    public String getReceiverPhoneNumber() {
        String receiverNumber = properties.getProperty(NotifierConstants.RECEIVER_PHONE_NUMBER_PROPERTY);
        log.debug("Got receiver phone number from property " + NotifierConstants.RECEIVER_PHONE_NUMBER_PROPERTY + ": " + receiverNumber);
        return receiverNumber;
    }


    @Override
    public String getNotificationPhoneNumber() {
        String notificationNumber = properties.getProperty(NotifierConstants.NOTIFICATION_PHONE_NUMBER_PROPERTY);
        notificationNumber = notifierUtil.getNormalizedTelephoneNumber(notificationNumber);
        log.debug("Got notification phone number from property " + NotifierConstants.NOTIFICATION_PHONE_NUMBER_PROPERTY + ": " + notificationNumber);
        return notificationNumber;
    }


    @Override
    public boolean getIsUrgent() {
        boolean isUrgent = false;
        String urgency = properties.getProperty(NotifierConstants.URGENT_PROPERTY);
        if("1".equals(urgency) || "true".equalsIgnoreCase(urgency)) {
            isUrgent = true;
        }
        log.debug("Got message urgency from property " + NotifierConstants.URGENT_PROPERTY + ": " + isUrgent);
        return isUrgent;
    }


    @Override
    public Date getDate() {
        Date date = null;
        String milliSecondsString = properties.getProperty(NotifierConstants.DATE_PROPERTY);
        try {
            long millisSeconds = Long.parseLong(milliSecondsString);
            date = new Date(millisSeconds);
            log.debug("Got message date from property " + NotifierConstants.DATE_PROPERTY + ": " + date);
        } catch (NumberFormatException e) {
            date = new Date();
            log.warn("Returning current time as the message receive date because value for property " + NotifierConstants.DATE_PROPERTY + " is not a number: " + milliSecondsString);
        }
        return date;
    }


    @Override
    public String getMessagePayloadAsString() throws NotifierMfsException {
        String fileName = properties.getProperty(NotifierConstants.MESSAGE_PAYLOAD_FILE_PROPERTY);
        String notificationNumber = getNotificationPhoneNumber();
        log.debug("Getting message text with notification number=" + notificationNumber + " filename="+ fileName);          
        return notifierMfsEventManager.getFileContentAsString(notificationNumber, fileName);
    }

    
    @Override
    public byte[] getMessagePayloadAsBytes() throws NotifierMfsException {
        String fileName = properties.getProperty(NotifierConstants.MESSAGE_PAYLOAD_FILE_PROPERTY);
        String notificationNumber = getNotificationPhoneNumber();
        log.debug("Getting message text with notification number=" + notificationNumber + " filename="+ fileName);
        return notifierMfsEventManager.getFileContentAsBytes(notificationNumber, fileName);
    }


    @Override
    public String getProperty(String propertyName) {
        String propertyValue = properties.getProperty(propertyName);
        log.debug("Got value for property " + propertyName + ": " + propertyValue);
        return propertyValue;
    }


    @Override
    public Properties getProperties() {
        return properties;
    }

    
}
