/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database;

/**
 * The INotifierDatabaseAccess interface defines the methods that the Notifier Plug-in can invoke to perform actions
 * on the MiO database.  An example of an action is retrieving a subscriber profile.
 */
public interface INotifierDatabaseAccess {

    /**
     * Gets the MiO database profile for the specified subscriber telephone number.  
     * This method returns null if the subscriber is not found in database.
     * @param subscriberNumber the subcriber's telephone number
     * @return the MiO database profile for the specified subscriber telephone number; null if the subscriber is not found in the database
     * @throws NotifierDatabaseException if an error occurs while retrieving the subscriber's profile
     */
    public ANotifierDatabaseSubscriberProfile getSubscriberProfile(String subscriberNumber) throws NotifierDatabaseException;
        
}
