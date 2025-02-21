/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.components;

import com.mobeon.masp.execution_engine.ApplicationExecutionFactory;
import com.mobeon.masp.execution_engine.SessionFactory;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.common.configuration.IConfigurationManager;

/**
 * Component interface for a class providing the {@link IApplicationManagment} service
 * within the com.mobeon.masp.execution_engine framework.
 * <p/>
 * <b>Note:</b> A class <em>must</em> implement this interface to be viable as a component
 * of the {@link IApplicationManagment} type.
 *
 * @author Mikael Andersson
 */
public interface ApplicationManagmentComponent extends IApplicationManagment {
    public static class Context {

        public Context() {}

        private ApplicationCompilerComponent applicationCompiler;
        private ApplicationExecutionFactory applicationExecutionFactory;
        private ISessionFactory sessionFactory;
        private IMediaObjectFactory mediaObjectFactory;
        private PlatformAccessFactory platformAccessFactory;
        private CallManager callManager;

        public void setConfigurationManager(IConfigurationManager configurationManager) {
            this.configurationManager = configurationManager;
        }

        public IConfigurationManager getConfigurationManager() {
            return configurationManager;
        }

        private IConfigurationManager configurationManager;


        public IMediaObjectFactory getMediaObjectFactory() {
            return mediaObjectFactory;
        }

        public ISessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public PlatformAccessFactory getPlatformAccessFactory() {
            return platformAccessFactory;
        }

        public ApplicationExecutionFactory getApplicationExecutionFactory() {
            return applicationExecutionFactory;
        }

        public ApplicationCompilerComponent getApplicationCompiler() {
            return applicationCompiler;
        }

        public ApplicationExecutionFactory.Context toApplicationExecutionFactory() {
            return new ApplicationExecutionFactory.Context(this);
        }

        public void setApplicationExecutionFactory(ApplicationExecutionFactory applicationExecutionFactory) {
            this.applicationExecutionFactory = applicationExecutionFactory;
        }

        public void setApplicationCompiler(ApplicationCompilerComponent applicationCompiler) {
            this.applicationCompiler = applicationCompiler;
        }

        public void setSessionFactory(ISessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
            this.mediaObjectFactory = mediaObjectFactory;
        }

        public void setPlatformAccessFactory(PlatformAccessFactory platformAccessFactory) {
            this.platformAccessFactory = platformAccessFactory;
        }

        public void setCallManager(CallManager callManager) {
            this.callManager = callManager;
        }

        public CallManager getCallManager() {
            return callManager;

        }
    }

}
