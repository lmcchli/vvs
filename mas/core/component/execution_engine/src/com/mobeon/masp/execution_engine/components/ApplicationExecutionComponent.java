/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.components;

import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.SessionFactory;
import com.mobeon.masp.execution_engine.ApplicationExecutionFactoryImpl;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.common.configuration.IConfigurationManager;

/**
 * Component interface for a class providing the {@link IApplicationExecution} service
 * within the com.mobeon.masp.execution_engine framework.
 * <p/>
 * <b>Note:</b> A class <em>must</em> implement this interface to be viable as a component
 * of the {@link IApplicationExecution} type.
 *
 * @author Mikael Andersson
 */
public interface ApplicationExecutionComponent extends IApplicationExecution {
    public static class Context {
        private IMediaObjectFactory mediaObjectFactory;
        private PlatformAccessFactory platformAccessFactory;
        private ISessionFactory sessionFactory;
        private CallManager callManager;
        private IConfigurationManager configurationManager;


        private Context() {

        }

        public static Context testInstance() {
            return new Context();
        }

        public Context(ApplicationExecutionFactoryImpl.Context ctx) {
            mediaObjectFactory = ctx.getMediaObjectFactory();
            sessionFactory = ctx.getSessionFactory();
            platformAccessFactory = ctx.getPlatformAccessFactory();
            callManager = ctx.getCallManager();
            configurationManager = ctx.getConfigurationManager();
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


        public void setSessionFactory(ISessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
            this.mediaObjectFactory = mediaObjectFactory;
        }

        public CallManager getCallManager() {
            return callManager;

        }
        public IConfigurationManager getConfigurationManager() {
            return configurationManager;

        }

    }

}
