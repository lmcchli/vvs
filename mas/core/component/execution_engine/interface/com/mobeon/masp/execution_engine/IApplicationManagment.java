/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import java.net.URI;
/**
 * The ApplicationManagment interface present functions for storing and retrieving
 * compiled applications. The interface is implemented as a  singleton. If an application has been
 * compiled by one session it is available in compiled format to all sessions.
 */
public interface IApplicationManagment {

    /**
     * Locates an application related to the specified service.
     *
     * @param service
     * @return The application  or null if service is invalid
     */
    public IApplicationExecution load(String service);


}
