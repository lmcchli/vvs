/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.PlatformAccessPluginLoader;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mediacontentmanager.IMediaContentManager;
import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.numberanalyzer.INumberAnalyzer;
import com.mobeon.masp.profilemanager.IProfileManager;
import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

public class PlatformAccessFactoryImpl implements PlatformAccessFactory {

    private INumberAnalyzer numberAnalyzer;
    private IProfileManager profileManager;
    private IConfiguration configuration;
    private IStorableMessageFactory storableMessageFactory;
    private IMediaContentManager mediacontentManager;
    private ITrafficEventSender trafficEventSender;
    private IMediaQualifierFactory mediaQualifierFactory;
    private IMediaObjectFactory mediaObjectFactory;
    private MediaTranslationManager mediaTranslationManager;
    private MediaHandlerFactory mediaHandlerFactory;
    private ContentTypeMapper contentTypeMapper;
    private ConfigManager authenticationBackendConfigurationManager;
    private IMailboxAccountManager mailboxAccountManager;

    public PlatformAccessFactoryImpl(
        INumberAnalyzer numberAnalyzer,
        IProfileManager profileManager,
        IConfiguration configuration,
        IStorableMessageFactory storableMessageFactory,
        IMediaContentManager mediacontentManager,
        ITrafficEventSender trafficEventSender,
        IMediaQualifierFactory mediaQualifierFactory,
        IMediaObjectFactory mediaObjectFactory,
        MediaTranslationManager mediaTranslationManager,
        MediaHandlerFactory mediaHandlerFactory,
        ContentTypeMapper contentTypeMapper,
        ConfigManager anAuthenticationBackendConfigurationManager) {

        this.numberAnalyzer = numberAnalyzer;
        this.profileManager = profileManager;
        this.configuration = configuration;
        this.storableMessageFactory = storableMessageFactory;
        this.mediacontentManager = mediacontentManager;
        this.trafficEventSender = trafficEventSender;
        this.mediaQualifierFactory = mediaQualifierFactory;
        this.mediaObjectFactory = mediaObjectFactory;
        this.mediaTranslationManager = mediaTranslationManager;
        this.mediaHandlerFactory = mediaHandlerFactory;
        this.contentTypeMapper = contentTypeMapper;
        this.authenticationBackendConfigurationManager = anAuthenticationBackendConfigurationManager;

        this.createPlugin(null);
    }

    /**
     * synchronized because one session may have more than one PlatformAccess objects. When they are created they
     * all check for data in the ISession object. See PlatformAccessImpl.
     *
     * @param executionContext
     * @return a new PlatformAccess object
     */
    public synchronized PlatformAccess create(ExecutionContext executionContext) {
    	PlatformAccessImpl p = new PlatformAccessImpl(
        		executionContext,
        		numberAnalyzer,
        		profileManager,
        		configuration,
        		storableMessageFactory,
        		mediacontentManager,
        		trafficEventSender,
        		mediaHandlerFactory,
        		contentTypeMapper,
        		authenticationBackendConfigurationManager);
    	p.setMailboxAccountManager(mailboxAccountManager);
    	return p;
    }

    public PlatformAccessUtil createUtil(ExecutionContext ex) {
        return new PlatformAccessUtilImpl(mediaQualifierFactory, mediaObjectFactory, mediaTranslationManager, mediaHandlerFactory);
    }

    public APlatformAccessPlugin createPlugin(ExecutionContext ex) {
        APlatformAccessPlugin platformAccessPlugin = PlatformAccessPluginLoader.get().getPlugin();
        return platformAccessPlugin;
    }

	public void setMailboxAccountManager(IMailboxAccountManager mailboxAccountManager) {
		this.mailboxAccountManager = mailboxAccountManager;
	}

	public IMailboxAccountManager getMailboxAccountManager() {
		return mailboxAccountManager;
	}
}
