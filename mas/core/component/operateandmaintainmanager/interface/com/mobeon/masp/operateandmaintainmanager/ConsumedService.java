/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * This interface is used to increment sucessed/faild operations
 * and to set status for a consumed service.
 */
public interface ConsumedService {

     /**
     * Set's the status for the consumed service
     * @param status to be set.
     *Can be one of:
     *<dl>
     *<dt>0 Up</dt>
     *  <dd>The service is fully operational</dd>
     *<dt>1 Down</dt>
     *  <dd>The service is completely non-operational</dd>
     *<dt>2 Impaired </dt>
     *  <dd>The service is operational but not fully i.e. end-user might expect
     *          quality loss e.g. in terms of latency, sound quality etc.<dd>
     * </dl>
     */
    public void setStatus(Status status);

    /**
    * Increments number of success operations
    */
    public void incrementSuccessOperations();

    /**
    * Increments number of failed operations
    */
    public void incrementFailedOperations();


    /**
     * Returns success operations
    */
    public Integer getSuccessOperations();
    
    /**
     * Returns success operations
    */
    public Integer getFaildOperations();
    
}
