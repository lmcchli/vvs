/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.common.configuration.IConfigurationManager;

/**
 * A factory for creating a runtime with matching {@link Engine},
 * {@link ExecutionContext}, and {@link Data} instances.
 */
public interface RuntimeFactory {

    public Engine createEngine(IConfigurationManager configurationManager);

    public ExecutionContext createExecutionContext(ISession session, PlatformAccessFactory platformAccessFactory, IServiceRequestManager serviceRequestManager, IConfigurationManager configurationManager);

    /**
     * Creates a prototype object for {@link Data} instances
     * used by this Runtime
     * @return The prototype instance
     */
    public Data createData();

    public RuntimeFacade createRuntime(IConfigurationManager configurationManager,
                                       PlatformAccessFactory platformAccessFactory,
                                       IMediaObjectFactory mediaObjectFactory,
                                       ISession session, Module entry,
                                       ModuleCollection moduleCollection,
                                       CallManager callManager,
                                       IEventDispatcher eventDispatcher,
                                       Supervision supervision,
                                       MediaTranslationManager mediaTranslationManager,
                                       IServiceRequestManager serviceRequestManager);
}
