/* **********************************************************************
 * Copyright (c) ABCXYZ 2008. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the 
 * written consent of the copyright owner. 
 * 
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY 
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. 
 * 
 * **********************************************************************/

package com.abcxyz.services.moip.ntf.coremgmt;

/**
 * Class implements scheduling information to be used by scheduler's event handler.  
 * 
 * @author lmchuzh
 * @since 2009
 */
public class SchedulingInfo 
{

    private String typeKey;
    private String retrySchema;
    private long expireTimerInMin;
    //TODO delay time may not from here? should be set on runtime 
    private long delayTimerInMin = 0;
    private long adjustedTime;
    private long expireRetryTimerInMin = 60;
    private int maxExpireTries = 1;
    transient String backupEventId;
    private boolean isExpireEvent;
    private boolean isLastExpire;
    
    /**
     * default constructor 
     */
    public SchedulingInfo(String typeKey) {
        setEventTypeKey(typeKey);
    }
    /**
     * 
     * @param retrySchema
     * @param expireTimerInMin
     */
    public SchedulingInfo(String typeKey, String retrySchema, long expireTimerInMin) {
        setEventTypeKey(typeKey);
        this.retrySchema = retrySchema;
        this.expireTimerInMin = expireTimerInMin;
    }
    
    /**
     * constructor for supporting delay delivery
     */
    public SchedulingInfo(String typeKey, String retrySchema, long expireTimerInMin, long delayTimerInMin) {
        setEventTypeKey(typeKey);
        this.retrySchema = retrySchema;
        this.expireTimerInMin = expireTimerInMin;
        this.delayTimerInMin = delayTimerInMin;
    }
    
    public String getEventTypeKey()
    {
        return typeKey;
    }
    public void setEventTypeKey(String type)
    {
       
        type = type.replace("-", "");
        type = type.replace(";", "");
        type = type.replace("_", "");
        typeKey = type;
    }
    
    public String getEventRetrySchema()
    {
        return retrySchema;
    }

    public void setEventRetrySchema(String schema) {
        retrySchema = schema;
    }

    public long getExpireTimerInMin()
    {
        return expireTimerInMin;
    }

    public void setExpireTimerInMin(long timer) {
        expireTimerInMin = timer;
    }
    
    public long getDelayTimerInMin()
    {
        return this.delayTimerInMin;
    }
    
    public void setDelayTimerInMin(long timer)
    {
        this.delayTimerInMin = timer;
    }
    public void setExpireRetryTimerInMin(long expireRetryTimerInMin) {
        this.expireRetryTimerInMin = expireRetryTimerInMin;
    }
    
    public long getExpireRetryTimerInMin() {
        return expireRetryTimerInMin;
    }
    
    public void setMaxExpireTries(int maxExipreRetries) {
        this.maxExpireTries = maxExipreRetries;
    }

    public int getMaxExpireTries() {
        return this.maxExpireTries;
    }
    
    
    public void setAdjustedTime(long newTime) {
        adjustedTime = newTime;
    }
    
    public long getAdjustedTime() {
        return adjustedTime;
    }
    
    /**
     * from scheduler manager, after a backup event is scheduled, keep its reference
     * @param schedulerEventId
     */
    public void setBackupEventId(String schedulerEventId) {
        this.backupEventId = schedulerEventId;
    }
    
    public String getBackupEventId() {
        return backupEventId;
    }
    
    public void setExpireEvent() {
        isExpireEvent = true;
    }
    public boolean isExpireEvent() {
        return isExpireEvent;
    }
    public void setLastExpireEvent() {
        isLastExpire = true;
    }
    
    public boolean isLastExpireEvent() {
        return this.isLastExpire;
    }
    
}
