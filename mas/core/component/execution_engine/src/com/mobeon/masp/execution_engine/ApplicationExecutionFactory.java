/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.masp.execution_engine.components.ApplicationExecutionComponent;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.common.configuration.IConfigurationManager;

public interface ApplicationExecutionFactory {

    ApplicationExecutionComponent create(String serviceName, ApplicationExecutionFactory.Context ctx, ApplicationComponent application, Supervision supervision, MediaTranslationManager mediaTranslationManager, IServiceRequestManager serviceRequestManager);

    public static class Context {
        private IMediaObjectFactory mediaObjectFactory;
        private PlatformAccessFactory platformAccessFactory;
        private ISessionFactory sessionFactory;
        private CallManager callManager;
        private IConfigurationManager configurationManager;

        public Context(ApplicationManagmentImpl.Context ctx) {
            mediaObjectFactory = ctx.getMediaObjectFactory();
            platformAccessFactory = ctx.getPlatformAccessFactory();
            sessionFactory = ctx.getSessionFactory();
            callManager = ctx.getCallManager();
            configurationManager = ctx.getConfigurationManager();
        }

        public ApplicationExecutionImpl.Context toApplicationExecution() {
            return new ApplicationExecutionImpl.Context(this);
        }

        public IMediaObjectFactory getMediaObjectFactory() {
            return mediaObjectFactory;
        }

        public PlatformAccessFactory getPlatformAccessFactory() {
            return platformAccessFactory;
        }

        public ISessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public CallManager getCallManager() {
            return callManager;

        }

        public IConfigurationManager getConfigurationManager() {
            return configurationManager;
        }
    }

}
