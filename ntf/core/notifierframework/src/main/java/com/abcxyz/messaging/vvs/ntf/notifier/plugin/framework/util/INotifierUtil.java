/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

import java.util.Properties;


/**
 * Interface for utility methods.
 */
public interface INotifierUtil {

    /**
     * Get telephone Number in normalized format
     * @param telephoneNumber The telephone number to normalize
     * @return the normalized version of the telephoneNumber
     */
    public String getNormalizedTelephoneNumber(String telephoneNumber);

    /**
     * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
     * @param telephoneNumber The telephone number of the user
     * @return true if storage is possible in the Geo Redundant system
     */
    public boolean isFileStorageOperationsAvailable(String telephoneNumber);


    /************************************
     * EVENT SCHEDULING UTILITY METHODS *
     ************************************/    

    /**
     * Generates a unique id to use when scheduling an event. 
     * @return unique id
     */
    public String getUniqueEventSchedulingId();

    /**
     * Extracts the event service name from the given scheduler event id string.
     * @param eventIdString string from which to extract
     * @return event service name
     */
    public String getEventServiceName(String eventIdString);

    /**
     * Extracts the event properties from the given scheduler event id string.
     * @param eventIdString eventIdString string from which to extract
     * @return event properties
     */
    public Properties getEventProperties(String eventIdString);


    /************************************
     * MESSAGE UTILITY METHODS *
     ************************************/    

    /**
     * Returns if the inbox contains new messages
     * @param msid subscriber's message store identity
     * @return boolean True if there is at least 1 new message in the inbox, false otherwise
     */
    public boolean isNewMessagesInInbox(String msid) throws NotifierUtilException;
}
