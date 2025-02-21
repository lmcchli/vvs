/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.configuration.ParameterBlock;
import com.mobeon.masp.execution_engine.configuration.ConfigurationParameters;
import com.mobeon.masp.execution_engine.configuration.ParameterId;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.runtime.*;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

@ConfigurationParameters({ParameterId.RuntimeFactoryBase_VxmlEnginePoolSize})
public class Factory extends RuntimeFactoryBase {

    private static ParameterBlock parameterBlock = new ParameterBlock();

    public VXMLExecutionContext createExecutionContext(ISession session, PlatformAccessFactory platformAccessFactory, IServiceRequestManager serviceRequestManager, IConfigurationManager configurationManager) {
        return new ExecutionContextImpl(session, platformAccessFactory, serviceRequestManager, configurationManager);
    }

    public Data createData() {
        return new VoiceXMLData();
    }

    public RuntimeFacade createRuntime(IConfigurationManager configurationManager,
                                       PlatformAccessFactory platformAccessFactory, IMediaObjectFactory mediaObjectFactory, ISession session, Module entry, ModuleCollection moduleCollection, CallManager callManager, IEventDispatcher eventDispatcher, Supervision supervision, MediaTranslationManager mediaTranslationManager, IServiceRequestManager serviceRequestManager) {
        Engine e = createEngine(configurationManager);
        VXMLExecutionContext ex = createExecutionContext(session, platformAccessFactory, serviceRequestManager, configurationManager);
        ex.setSupervision(supervision);
        e.setExecutionContext(ex);
        ex.setEngine(e);
        ex.setMediaObjectFactory(mediaObjectFactory);
        ex.setMediaTranslationManager(mediaTranslationManager);
        return new VoiceXMLRuntime(e, ex, entry, moduleCollection);
    }

    public ParameterBlock getParameterBlock() {
        return parameterBlock;
    }

    public ParameterId getIdForConfiguredPoolSize() {
        return ParameterId.RuntimeFactoryBase_VxmlEnginePoolSize;
    }

    public String getNameForConfiguredPoolSize() {
        return RuntimeConstants.CONFIG.ENGINE_VXML_POOL_SIZE;
    }
}
