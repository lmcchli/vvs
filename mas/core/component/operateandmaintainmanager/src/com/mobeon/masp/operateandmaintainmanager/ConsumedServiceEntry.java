/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;

/**
 * This class holds ServiceName, Host, Port, No of sucess/faild operations for a consumed service.
 *
 */

public class ConsumedServiceEntry implements ConsumedService, Serializable {
    private Status status;
    private int consumedServiceSucessOper;
    private int consumedServiceFaildOper;
    private Date statusChangeTime;
    private Integer port;
    private String host;
    private String serviceName;
    private static ILogger log;

    public ConsumedServiceEntry(String serviceName, String host, Integer port) {
        log = ILoggerFactory.getILogger(ConsumedServiceEntry.class);
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.status = Status.DOWN;

        Calendar cal = Calendar.getInstance();
        this.statusChangeTime = cal.getTime();

    }

    /**
     * Sets status for a consumed service.
     * @param status
     */
    public synchronized void setStatus(Status status) {
        this.status = status;
        Calendar cal = Calendar.getInstance();
        this.statusChangeTime = cal.getTime();

    }

    public synchronized Status getStatus() {
        return this.status;
    }

    public String getServiceName(){
        return this.serviceName;
    }

    public synchronized Date getStatusChangeTime() {
        return this.statusChangeTime;
    }


    /**
     * Increment success operations for cunsumed service.
     */
    public synchronized void incrementSuccessOperations() {
        consumedServiceSucessOper++;
    }

    /**
     * Increment failed operations for cunsumed service.
     */
    public synchronized void incrementFailedOperations() {
        consumedServiceFaildOper++;
    }


    public Integer getSuccessOperations() {
        return consumedServiceSucessOper;
    }

    public Integer getFaildOperations() {
        return consumedServiceFaildOper;
    }


}
