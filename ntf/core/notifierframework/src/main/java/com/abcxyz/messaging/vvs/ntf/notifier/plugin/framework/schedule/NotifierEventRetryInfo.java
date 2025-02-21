/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule;


/**
 * The NotifierEventRetryInfo class contains the event retry schema for a Notifier plug-in event service.
 * <p>
 * An instance of this class can be used to register a Notifier plug-in event service with the NTF Notifier scheduling mechanism. 
 */
public class NotifierEventRetryInfo extends ANotifierEventRetryInfo {
    
    private String serviceName = null;
    private String retrySchema = null;
    private boolean isRetryTimesFromINotifierEventHandler = false;
    private long expireInMinute = 0;
    private long expireRetryTimerInMinute = 60;
    private int maxExpireTries = 1;

 
    /**
     * Constructs a NotifierEventRetryInfo instance with the specified event service name.
     * @param serviceName the name of the event service
     */
    public NotifierEventRetryInfo(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Constructs a NotifierEventRetryInfo instance with the specified event service name, 
     * event retry schema and expiration time period.
     * @param serviceName the name of the event service
     * @param retrySchema the event retry schema. 
     *                    The expected format of the retry schema is described in the MiO VVS NTF CPI documentation.
     * @param expireInMinute the time period after which an event will expire
     */
    public NotifierEventRetryInfo(String serviceName, String retrySchema, long expireInMinute) {
        this.serviceName = serviceName;
        this.retrySchema = retrySchema;
        this.expireInMinute = expireInMinute;
    }

    /**
     * Sets the name of the event service.
     * @param serviceName the name of the event service
     */
    public void setEventServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getEventServiceName() {
        return serviceName;
    }

    /**
     * Sets the event retry schema for this event service.
     * @param retrySchema the event retry schema. 
     *                    The expected format of the retry schema is described in the MiO VVS NTF CPI documentation.
     */
    public void setEventRetrySchema(String retrySchema) {
        this.retrySchema = retrySchema;
    }

    @Override
    public String getEventRetrySchema() {
        return retrySchema;
    }

    /**
     * Sets the time period in minutes after which an event for this event service will expire.
     * @param expireInMinute the time period in minutes after which an event will expire
     */
    public void setExpireTimeInMinute(long expireInMinute) {
        this.expireInMinute = expireInMinute;
    }

    @Override
    public long getExpireTimeInMinute() {
        return expireInMinute;
    }

    /**
     * Sets the time interval in minutes at which the signalling of an event expiration will be retried.
     * @param expireRetryTimerInMinute the time interval in minutes to use for event expiration retries
     */
    public void setExpireRetryTimerInMinute(long expireRetryTimerInMinute) {
        this.expireRetryTimerInMinute = expireRetryTimerInMinute;
    }

    @Override
    public long getExpireRetryTimerInMinute() {
        return expireRetryTimerInMinute;
    }

    /**
     * Sets the maximum times an event expiration signal will be retried.
     * @param maxExipreRetries the maximum times an event expiration signal will be retried
     */
    public void setMaxExpireTries(int maxExipreRetries) {
        this.maxExpireTries = maxExipreRetries;
    }

    @Override
    public int getMaxExpireTries() {
        return maxExpireTries;
    }
    
    /**
     * Returns the string representation of this NotifierEventRetryInfo instance.
     * @return the string representation of this NotifierEventRetryInfo instance
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("serviceName=").append(serviceName);
        buffer.append(" retrySchema=").append(retrySchema);
        buffer.append(" isRetryTimeDynamic=").append(isRetryTimesFromINotifierEventHandler);
        buffer.append(" expireInMinute=").append(expireInMinute);
        buffer.append(" expireRetryTimerInMinute=").append(expireRetryTimerInMinute);
        buffer.append(" maxExpireTries=").append(maxExpireTries);
        return buffer.toString();
    }
}
