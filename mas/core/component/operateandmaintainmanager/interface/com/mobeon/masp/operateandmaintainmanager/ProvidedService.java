/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * Interface for ProvidedServiceEntry.
 */
public interface ProvidedService {
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
    void setStatus(Status status);

    /**
     *  Sets the application for the provided service
     *
     * @param applicationName
     * @param applicationVersion
     */
    void setApplication(String applicationName, String applicationVersion);

}
