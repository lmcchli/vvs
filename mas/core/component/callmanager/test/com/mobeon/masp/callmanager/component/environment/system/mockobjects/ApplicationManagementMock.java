/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.IApplicationExecution;

/**
 * A mock of IApplicationManagment used for testing.
 * This class is immutable.
 */
public class ApplicationManagementMock implements IApplicationManagment {
    private final IApplicationExecution applicationExecution;

    public ApplicationManagementMock(
            IApplicationExecution applicationExecution) {
        this.applicationExecution = applicationExecution;
    }

    public IApplicationExecution load(String service) {
        return applicationExecution;
    }
}
