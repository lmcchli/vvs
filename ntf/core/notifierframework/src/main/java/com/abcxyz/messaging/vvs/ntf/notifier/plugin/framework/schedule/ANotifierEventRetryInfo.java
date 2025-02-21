/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule;


/**
 * The ANotifierEventRetryInfo abstract class defines the methods that the NTF Notifier scheduling mechanism will invoke to obtain
 * information regarding the event retry schema for an event service.
 * <p>
 * The Notifier plug-in concrete class that implements this abstract class should be a container for the event retry schema of an event service.
 */
public abstract class ANotifierEventRetryInfo {

    /**
     * Gets the name of the event service.
     * @return the name of the event service
     */
    public String getEventServiceName() {
        return null;
    }

    /**
     * Gets the event retry schema for the event service.
     * <p>
     * The expected format of the retry schema is described in the MiO VVS NTF CPI documentation.
     * @return the event retry schema for the event service
     */
    public String getEventRetrySchema() {
        return null;
    }

    /**
     * Gets the time period in minutes after which an event for this event service will expire.
     * @return the time period in minutes after which an event will expire
     */
    public long getExpireTimeInMinute() {
        return 0;
    }

    /**
     * Gets the time interval in minutes at which the signalling of an event expiration will be retried.
     * @return the time interval in minutes to use for event expiration retries
     */
    public long getExpireRetryTimerInMinute() {
        return 0;
    }

    /**
     * Gets the maximum times an event expiration signal will be retried.
     * @return the maximum times an event expiration signal will be retried
     */
    public int getMaxExpireTries() {
        return 0;
    }

}
