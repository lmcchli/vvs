/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database;

import java.util.Iterator;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;

/**
 * The ANotifierDatabaseSubscriberProfile abstract class defines the methods that can be invoked to retrieve information
 * stored in a subscriber database profile.
 * <p>
 * Both the NTF component and Notifier plug-in can implement a concrete class extending this abstract class.
 * <p>
 * The Notifier plug-in can perform a MiO database subscriber lookup by invoking {@link INotifierDatabaseAccess#getSubscriberProfile(String)}.  
 * In this case, the ANotifierDatabaseSubscriberProfile object is an instance of the NTF component concrete class.
 * <p> 
 * Alternatively, the Notifier plug-in can create its own concrete class that extends ANotifierDatabaseSubscriberProfile.
 * Instances of this Notifier plug-in concrete class can be populated as needed and used wherever a ANotifierDatabaseSubscriberProfile object is needed.
 * This can be useful when processing notifications for users who are not provisioned in the MiO database.
 * <p>
 * Note that the getStringAttributes, getIntegerAttributes, and getBooleanAttributes methods specify that an array containing one default value is returned 
 * in the case that attribute values are not found.  This is because a Notifier plug-in concrete class can be passed to the NTF component and 
 * the NTF code might not expect null in these cases.
 * <p>
 * Similarly, the getCosProfile method specifies that an empty cos profile is returned in the case that there is no cos profile associated to 
 * this subscriber profile.
 */
public abstract class ANotifierDatabaseSubscriberProfile {
    
    /**
     * The default string array to return when no attribute values are found.
     */
    protected static final String[] STRING_ARRAY_ONE_EMPTY_STRING_VALUE = { "" };
    
    /**
     * The default int array to return when no attribute values are found.
     */
    protected static final int[] INT_ARRAY_ONE_ZERO_VALUE = { 0 }; 

    /**
     * The default boolean array to return when no attribute values are found.
     */
    protected static final boolean[] BOOLEAN_ARRAY_ONE_FALSE_VALUE = { false }; 

    /**
     * The NotificationType enum contains the possible notification types that a subscriber can be provisioned to receive.
     * <p>
     * Currently, the Notifier framework supports only the sending of SMS notifications so this enum has only the SMS NotifiicationType.
     */
    public static enum NotificationType {
        /**
         * The SMS notification type.
         */
        SMS
    }
    
    
    /**
     * Gets the scheme-specific-part of all the subscriber identities that start with the specified scheme.  
     * This method will return <code>null</code> if no identities are found.
     * <p>
     * For example, the scheme is "msid" and the scheme-specific-part is "9914b3e658dd74bc" in the following msid identity URI:
     * <PRE>msid:9914b3e658dd74bc</PRE>
     * 
     * @param scheme the scheme of the desired identities 
     * @return the scheme-specific-part of all the subscriber identities that start with the specified scheme, 
     *         or <code>null</code> if no identities are found.
     */
    public String[] getSubscriberIdentities(String scheme) {
        return null;
    }

    /**
     * Gets the string values for the specified attribute name.
     * This method will return an array containing one empty string if no attribute values are found.
     * <p>
     * The NTF component concrete class will retrieve the attribute values by following these steps:
     * <PRE>
     * 1. If there is a multiline profile associated with the subscriber telephone number 
     *    and the attribute is a multiline attribute, 
     *    get the attribute value from the multiline profile.
     *    Otherwise, get the attribute value from the subscriber's profile.
     *    
     * 2. If after the first step, no attribute values are found, 
     *    get the attribute value from the cos profile associated with the subscriber.
     *    
     * 3. If after the second step, no attribute values are found, 
     *    get the default string array containing one empty string.
     * </PRE>
     * 
     * @param attributeName the name of the attribute for which to retrieve the attribute string values
     * @return the attribute string values for the specified attribute name, or an array containing one empty string if no attribute values are found
     */
    public String[] getStringAttributes(String attributeName) {
        return STRING_ARRAY_ONE_EMPTY_STRING_VALUE;
    }

    /**
     * Gets the integer values for the specified attribute name.
     * This method will return an array containing one zero integer value 
     * if no attribute values are found or at least one of the attribute values could not be parsed into an integer.
     * <p>
     * In the NTF component concrete class, this method is a convenience method; 
     * the result obtained from invoking {@link ANotifierDatabaseSubscriberProfile#getStringAttributes(String)} is parsed as integers and returned.
     * 
     * @param attributeName the name of the attribute for which to retrieve the attribute integer values
     * @return the attribute integer values for the specified attribute name, 
     *         or an array containing one zero integer if no attribute values are found or if at least one of the attribute values could not be parsed into an integer
     */
    public int[] getIntegerAttributes(String attributeName) {
        return INT_ARRAY_ONE_ZERO_VALUE;
    }

    /**
     * Gets the boolean values for the specified attribute name.
     * This method will return an array containing one false value if no attribute boolean values are found.
     * <p>
     * In the NTF component concrete class, this method is a convenience method; 
     * the result obtained from invoking {@link ANotifierDatabaseSubscriberProfile#getStringAttributes(String)} is converted into boolean values and returned.
     * The string values "yes" and "true" are converted to true; all other values are converted to false.
     * 
     * @param attributeName the name of the attribute for which to retrieve the attribute boolean values
     * @return the attribute boolean values for the specified attribute name, or an array containing one false value if no attribute values are found
     */
    public boolean[] getBooleanAttributes(String attributeName) {
        return BOOLEAN_ARRAY_ONE_FALSE_VALUE;
    }

    /**
     * Gets an iterator on all attribute names in this subscriber profile.
     * The order in which this iterator returns the attribute names is not guaranteed. 
     * @return an iterator on all attribute names in this profile
     */
    public Iterator<String> getAttributeNameIterator() {
        return null;
    }

    /**
     * Gets the class of service database profile associated with this subscriber profile.
     * This method will return an empty cos profile if no cos profile is associated with this subscriber profile.
     * 
     * @return the class of service database profile associated with this subscriber profile
     */
    public ANotifierDatabaseCosProfile getCosProfile() {
        return null;
    }

    /**
     * Retrieves the notification numbers for the given notification type.
     * <p>
     * In the NTF component concrete class, this method retrieves the notification numbers by following these steps:
     * <PRE>
     * 1. If there is a delivery profile for the notificationType (in the MOIPDeliveryProfile attribute) associated with the subscriber,
     *    get the notification numbers from the delivery profile.
     *    
     * 2. If after the first step, no notification numbers are found, 
     *    get the notification number from the MOIPNotifNumber attribute associated with the subscriber.
     *    
     * 3. If after the second step, no notification numbers are found, 
     *    get subscriber phone number and return it as the notification number.
     * </PRE>
     * @param notificationType the type of notification being sent; for example, SMS 
     * @param notificationInfo the container with all the event properties received by NTF
     * @return the notification numbers or an empty string array if the notificationType is not supported by the Notifier Framework
     */
    public String[] getSubscriberNotificationNumbers(NotificationType notificationType, ANotifierNotificationInfo notificationInfo) {
        return new String[0];
    }

    /**
     * Retrieves the new message deposit notification numbers according to the given notification type and the message deposit information.
     * <p>
     * In the NTF component concrete class, an empty string array is retrieved if:<BR>
     * - the notificationType is not supported by the Notifier Framework<BR>
     * - the notificationType is not provisioned for the subscriber<BR>
     * - all notifications are disabled for the subscriber<BR>
     * - the notificationType is disabled for the subscriber<BR>
     * - there is no enabled MOIPFilter for the notificationType associated with the subscriber<BR>
     * <p>
     * If the above conditions are false, the NTF concrete class retrieves the notification numbers by following these steps:
     * <PRE>
     * 1. If there is a delivery profile for the notificationType (in the MOIPDeliveryProfile attribute) associated with the subscriber,
     *    get the notification numbers from the delivery profile.
     *    
     * 2. If after the first step, no notification numbers are found, 
     *    get the notification number from the MOIPNotifNumber attribute associated with the subscriber.
     *    
     * 3. If after the second step, no notification numbers are found, 
     *    get subscriber phone number and return it as the notification number.
     * </PRE>
     * @param notificationType the type of notification being sent; for example, SMS 
     * @param notificationInfo the container with all the event properties received by NTF
     * @return the notification numbers or an empty string array if no notification for the given notificationType should be sent
     * @throws NotifierMfsException if information cannot be retrieved from the deposited message in the MFS
     */
    public String[] getSubscriberNotificationNumbersForNewMessageDeposit(NotificationType notificationType, ANotifierNotificationInfo notificationInfo) throws NotifierMfsException {
        return new String[0];
    }

}
