/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.util.content.PageCounter;

import java.util.Map;

/**
 * @author QHAST
 */
public abstract class ContextFactory<C extends BaseContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(ContextFactory.class);

    public static final String MAILBOX_CONFIG_GROUP_NAME = "mailbox";

    private IMediaObjectFactory mediaObjectFactory;
    private Map<String, PageCounter> pageCounterMap;
    private IConfiguration configuration;

    protected ContextFactory() {
    }

    public IMediaObjectFactory getMediaObjectFactory() {
        return mediaObjectFactory;
    }

    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    public Map<String, PageCounter> getPageCounterMap() {
        return pageCounterMap;
    }

    public void setPageCounterMap(Map<String, PageCounter> pageCounterMap) {
        this.pageCounterMap = pageCounterMap;
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }


    protected abstract C newContext();

    public final C create() throws MailboxException {
        return create(null);
    }

    public final C create(MailboxProfile mailboxProfile) throws MailboxException {
    	
    	// Read mailbox specific configuration if available
        IGroup mailboxConfigRoot = null;
        try {
        	mailboxConfigRoot = configuration.getConfiguration().getGroup(MAILBOX_CONFIG_GROUP_NAME);
        } catch (GroupCardinalityException e) {
        	LOGGER.warn("No specific configuration for " + MAILBOX_CONFIG_GROUP_NAME + " was found ");
        } catch (UnknownGroupException e) {
        	LOGGER.warn("No specific configuration for " + MAILBOX_CONFIG_GROUP_NAME + " was found ");
        }
        C context = newContext();
        context.setMediaObjectFactory(mediaObjectFactory);
        context.setPageCounterMap(pageCounterMap);        
        context.setMailboxProfile(mailboxProfile);
        if (mailboxConfigRoot != null) {
        	context.init(mailboxConfigRoot);
        }
        return context;
    }



}
