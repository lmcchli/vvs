/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelSmsEventRouter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.INotifierPhoneOnRouter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.INotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManagerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLoggerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierManagementCounter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierMdrGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;


/**
 * The INotifierNtfServicesManager interface defines the methods that the Notifier plug-in can invoke to obtain objects that
 * allow access to services provided by the NTF component.
 * <p>
 * The NTF component class that implements this interface consolidates access to all NTF component services and acts as single point of access.
 */
public interface INotifierNtfServicesManager {

    /**
     * Gets the {@link INotifierConfigManagerFactory} which instantiates configuration managers for configuration files.
     * @return the INotifierConfigManagerFactory which instantiates configuration managers
     */
    public INotifierConfigManagerFactory getConfigManagerFactory();
    
    
    /**
     * Gets the {@link IntfServiceTypePidLookup} which gives access to NTF's internal table of ServiceType
     * and PID depending on the position in the ReplaceNotifications.List (notification.conf)
     */
    public IntfServiceTypePidLookup getNtfServiceTypePidLookup();
        
    /**
     * Gets the {@link INotifierLoggerFactory} which instantiates loggers objects.
     * @return the INotifierLoggerFactory which instantiates loggers objects
     */
    public INotifierLoggerFactory getLoggerFactory();
    
    /**
     * Gets the {@link INotifierManagementCounter} which allows access to the MiO management counters.
     * @return the INotifierManagementCounter which allows access to the MiO management counters
     */
    public INotifierManagementCounter getManagementCounter();
    
    /**
     * Gets the {@link INotifierProfiler} which allows the generation of performance statistics.
     * @return the INotifierProfiler which allows the generation of performance statistics
     */
    public INotifierProfiler getProfiler();
    
    /**
     * Gets the {@link INotifierMdrGenerator} which allows the generation of message detail records (MDR).
     * @return the INotifierMdrGenerator which allows the generation of message detail records
     */
    public INotifierMdrGenerator getMdrGenerator();

    /**
     * Gets the {@link INotifierUtil} which allows access to NTF component utility methods.
     * @return the INotifierUtil which allows access to NTF component utility methods
     */
    public INotifierUtil getUtil ();
    
    /**
     * Gets the {@link INotifierMfsEventManager} which allows access to the MiO file system.
     * @return the INotifierMfsEventManager which allows access to the MiO file system
     */
    public INotifierMfsEventManager getMfsEventManager();

    /**
     * Gets the {@link INotifierDatabaseAccess} which allows access to the MiO database.
     * @return the INotifierDatabaseAccess which allows access to the MiO database
     */
    public INotifierDatabaseAccess getDatabaseAccess();
    
    /**
     * Gets the {@link INotifierEventScheduler} which allows the scheduling of events.
     * @return the INotifierEventScheduler which allows the scheduling of events
     */
    public INotifierEventScheduler getEventScheduler();
    
    /**
     * Gets the {@link INotifierPhoneOnRouter} which allows the registration of the Notifier plug-in as a phone on event handler.
     * @return the INotifierPhoneOnRouter which allows the registration of the Notifier plug-in as a phone on event handler
     */
    public INotifierPhoneOnRouter getPhoneOnRouter();
    
    
    /**
     * Gets the {@link ICancelSmsEventRouter} which allows the registration of the Notifier plug-in as a Cancel SMS event handler.
     * @return the INotifierPhoneOnRouter which allows the registration of the Notifier plug-in as a phone on event handler
     */
    public ICancelSmsEventRouter getCancelRouter();
    
    
    /**
     * Gets the {@link INotifierSenderSms} which allows the sending of SMS notifications.
     * @return the INotifierSenderSms which allows the sending of SMS notifications
     */
    public INotifierSenderSms getSender();

    /**
     * Gets the {@link INotifierMessageGenerator} which allows the generation of notification messages.
     * @return the INotifierMessageGenerator which allows the generation of notification messages
     */
    public INotifierMessageGenerator getMessageGenerator();

    /**
     * Gets an {@link INotifierNtfNotificationInfo} object for the given event properties.
     * @param eventProperties the properties associated with the event that is triggering the notification
     * @return INotifierNtfNotificationInfo object for the given event properties
     */
    public INotifierNtfNotificationInfo getNtfNotificationInfo(Properties eventProperties);
    
    /**
     * Get an (@link INotifierStateManagement) object which allows access to NTF's state information and shutdown control.
     * @return INotifierStateManagement object which allows access to NTF's state information and shutdown Management
     */
    public INotifierStateManagement getNotifierStateManagement(); 
    
}
