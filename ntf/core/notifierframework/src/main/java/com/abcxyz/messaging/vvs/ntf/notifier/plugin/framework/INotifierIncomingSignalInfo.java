/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import java.util.Properties;


/**
 * The INotifierIncomingSignalInfo interface defines the methods that the Notifier plug-in can invoke
 * to obtain information regarding the incoming notification signal.
 * <p>
 * The Notifier plug-in can use this information to determine whether to handle the signal and the details of the notification to be sent.
 * <p>
 * The NTF component class that implements this interface is a container for the information associated with 
 * the notification signal that NTF received.
 */
public interface INotifierIncomingSignalInfo {

    /**
     * Gets the service type for this incoming notification signal.
     * <p>
     * Examples of service types defined for NTF: notif, slmdw.
     * <p>
     * Customized notification signals with customized service types can also be defined and sent in an inform event to the NTF component.
     * @return the service type for this incoming notification signal
     */
    public String getServiceType();

    /**
     * Returns whether this incoming notification signal has expired.
     * The signal is expired if all retries to send this signal to the NTF component have been exhausted.
     * <p>
     * An expiration of the signal is sent to trigger any closing tasks associated with this signal.  
     * For example, deletion of the notification payload file or MDR generation.
     * @return true if this signal has expired; false otherwise
     */
    public boolean isExpiry();

    /**
     * Gets the event properties associated with this incoming notification signal.
     * <p>
     * The event properties contain the information (in name-value pairs) that is needed to send out the notification.  
     * For example, the phone number of the subscriber to be notified can be stored in an event property.
     * <p>
     * For notification event properties already defined for NTF, a {@link INotifierNtfNotificationInfo} object can be used to retrieve the needed information.
     * The INotifierNtfNotificationInfo object can be obtained by calling {@link INotifierNtfServicesManager#getNtfNotificationInfo(Properties)}.
     * <p>
     * For customized event properties, the customized event properties names would have to be known by the Notifier plug-in in order to retrieve the needed information.
     * @return the event properties for this incoming notification signal
     */
    public Properties getNotificationEventProperties();
}
