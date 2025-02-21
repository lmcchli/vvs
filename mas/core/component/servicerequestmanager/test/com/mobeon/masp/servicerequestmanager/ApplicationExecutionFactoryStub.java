/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author mmawi
 */
public class ApplicationExecutionFactoryStub {
    private static ILogger log = ILoggerFactory.getILogger(ApplicationExecutionFactoryStub.class);
    private static IServiceRequestManager serviceRequestManager;

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    private static IEventDispatcher eventDispatcher;


    public ApplicationExecutionFactoryStub() {
    }

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public IServiceRequestManager getServiceRequestManager() {
        return serviceRequestManager;
    }

    public static IApplicationExecution create() {
        return new ApplicationExecutionStub(serviceRequestManager);
    }
}
