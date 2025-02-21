/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingActions;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingTypes;


/**
 * The ANotifierPlugin abstract class defines the Notifier plug-in methods that the NTF component will invoke.
 * This includes methods which initialise the Notifier plug-in and allow the Notifier plug-in to handle incoming notification signals.
 * <p>
 * Hence, the Notifier plug-in must have a concrete class that extends this abstract class and override the methods as needed.
 * <p>
 * In order for the Notifier plug-in to be loaded by the NTF component, the concrete class MUST have the following package and class name:<p>
 * <code>com.abcxyz.messaging.vvs.ntf.notifier.plugin.custom.NotifierPlugin</code>
 */
public abstract class ANotifierPlugin {

    /**
     * Initialises this plug-in.
     * This method is invoked by the NTF component upon loading of this plug-in.
     * @param notifierNtfServicesManager the INotifierNtfServicesManager which provides access to the NTF services available to this plug-in
     * @throws NotifierPluginException if initialisation fails
     */
    public void initialize(INotifierNtfServicesManager notifierNtfServicesManager) throws NotifierPluginException {
        return;
    }

    /**
     * Determines whether the given notification event is being handled by this plug-in.
     * @param eventType the event type of the specific notification event.
     *                  The event type is the same as the serviceType in the INotifierIncomingSignalInfo.
     *                  Examples of event types defined for NTF: notif, slmdw.
     * @param eventProperties the properties of the specific notification event
     * @return true if the Notifier plug-in is handling the event, false otherwise
     */
    public boolean isHandlingNotificationEvent(String eventType, Properties eventProperties) {
        return false;
    }

    /**
     * Handles an incoming notification signal if applicable.
     * <p>
     * This plug-in can indicate it is handling the notification signal completely, partially or not at all by setting the appropriate {@link NotifierHandlingTypes} value in the NotifierIncomingSignalResponse. 
     * <p>
     * This plug-in can also defer the decision to handle the notification signal by setting the appropriate {@link NotifierHandlingActions} value in the NotifierIncomingSignalResponse.
     *  
     * @param signalInfo the INotifierIncomingSignalInfo containing the information about the incoming notification signal, such as the event properties
     * @return NotifierIncomingSignalResponse indicating whether the plug-in is handling the notification signal completely, partially or not at all
     */
    public NotifierIncomingSignalResponse handleNotification(INotifierIncomingSignalInfo signalInfo) {
        NotifierIncomingSignalResponse incomingSignalResponse = new NotifierIncomingSignalResponse();
        incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
        incomingSignalResponse.setAction(NotifierHandlingActions.OK);
        return incomingSignalResponse;
    }
    
    /**
     * Refreshes the configuration used by this plug-in.
     * This method is invoked by the NTF component when a configuration refresh has been triggered.
     * <p>
     * This plug-in should retrieve new INotifierConfigManager objects and then update any configuration values stored in memory.  
     * @return true if this plug-in successfully refreshed its configuration, false otherwise
     */
    public boolean refreshConfig() {
        return true;
    }
    
    /**
     * Informs the plug-in of a change of state of NTF such as shutting down, lock etc..
     * This method is invoked by the NTF component when a change of NTF state has occurred.
     * <p>
     * This plug-in should take the appropriate action on state change i.e clean up before shutdown, wait for unlock etc.
     */
    public void stateChange(INotifierNtfAdminState state) {
        //do nothing - override this method if you wish to be informed of state changes <recommended>
    }
}
