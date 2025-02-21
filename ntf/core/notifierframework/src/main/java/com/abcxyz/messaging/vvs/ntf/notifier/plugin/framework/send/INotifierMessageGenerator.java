/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.ANotifierSlamdownCallInfo;


/**
 * The INotifierMessageGenerator interface defines the methods that the Notifier plug-in can invoke to generate
 * the notification message to be sent.
 */
public interface INotifierMessageGenerator {

    
    /**
     * Generates the notification message as a string by inserting data that is specific to the current notification in the template string.
     * @param templateName the name of the template to use to generate the notification message
     * @param notificationInfo the information about the notification
     * @param subscriberProfile the subscriber-specific information
     * @param isFallbackMessageUsed indicates if a fall back message should be used when generation using the specified template fails.
     *                              The fall back message is generated from the "general" template; if this also fails, a hard-coded notification message is returned (e.g. "New message").
     * @return the notification message
     * @throws NotifierMessageGenerationException if text generation failed and isFallbackMessageUsed is false
     */
    public String generateNotificationMessageAsString(String templateName, ANotifierNotificationInfo notificationInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) throws NotifierMessageGenerationException;
    
    /**
     * Generates the notification message as a byte array by inserting data that is specific to the current notification in the template byte array.
     * @param charsetName the name of the char set to be used
     * @param templateName the name of the template to use to generate the notification message
     * @param notificationInfo the information about the notification
     * @param subscriberProfile the subscriber-specific information
     * @param isFallbackMessageUsed indicates if a fall back message should be used when generation using the specified template fails.
     *                              The fall back message is generated from the "general" template; if this also fails, a hard-coded notification message is returned (e.g. "New message").
     * @return the notification message
     * @throws NotifierMessageGenerationException if text generation failed and isFallbackMessageUsed is false
     */
    public byte[] generateNotificationMessageAsBytes(String charsetName, String templateName, ANotifierNotificationInfo notificationInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) throws NotifierMessageGenerationException;

    /**
     * Generates the slam-down notification message as a string by inserting data that is specific to the current notification in the template string.
     * @param templateName the name of the template to use to generate the notification message
     * @param slamdownCallInfo the information about the notification
     * @param subscriberProfile the subscriber-specific information
     * @param isFallbackMessageUsed indicates if a fall back message should be used when generation using the specified template fails.
     *                              The fall back message is generated from the "general" template; if this also fails, a hard-coded notification message is returned (e.g. "New message").
     * @return the notification message
     * @throws NotifierMessageGenerationException if text generation failed and isFallbackMessageUsed is false
     */
    public String generateSlamdownNotificationMessageAsString(String templateName, ANotifierSlamdownCallInfo slamdownCallInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) throws NotifierMessageGenerationException;
    
    /**
     * Generates the slam-down notification message as a byte array by inserting data that is specific to the current notification in the template byte array.
     * @param charsetName the name of the char set to be used
     * @param templateName the name of the template to use to generate the notification message
     * @param slamdownCallInfo the information about the notification
     * @param subscriberProfile the subscriber-specific information
     * @param isFallbackMessageUsed indicates if a fall back message should be used when generation using the specified template fails.
     *                              The fall back message is generated from the "general" template; if this also fails, a hard-coded notification message is returned (e.g. "New message").
     * @return the notification message
     * @throws NotifierMessageGenerationException if text generation failed and isFallbackMessageUsed is false
     */
    public byte[] generateSlamdownNotificationMessageAsBytes(String charsetName, String templateName, ANotifierSlamdownCallInfo slamdownCallInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) throws NotifierMessageGenerationException;
    
    /**
     * checks if a templateName exists in the /opt/moip/ntf/config/template folder
     * @param templateName - the template name to check.
     * @return true if the template exists.
     */
    public boolean doesCphrTemplateExist(String templateName);
    
    /**
     * checks for a given character set if a templateName exists in the /opt/moip/ntf/config/template folder
     * checks if a templateName exists in the /opt/moip/ntf/config/template folder
     * @param templateName - the template name to check.
     *  @param charsetName the name of the char set to be checked.
     * @return true if the template exists for the given character set.
     */
    public boolean doesCphrTemplateExistforCharSet(String templateName, String charsetName);
      
}
