/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.masp.execution_engine.components.ApplicationExecutionComponent;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

public class ApplicationExecutionFactoryImpl implements ApplicationExecutionFactory {

    public ApplicationExecutionComponent create(String serviceName, ApplicationExecutionFactory.Context ctx, ApplicationComponent application, Supervision supervision, MediaTranslationManager mediaTranslationManager, IServiceRequestManager serviceRequestManager) {
        return new ApplicationExecutionImpl(serviceName, ctx.toApplicationExecution(),(ApplicationComponent) application,supervision, mediaTranslationManager, serviceRequestManager);
    }

}
