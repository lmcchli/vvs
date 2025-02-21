/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author QHAST
 */
public abstract class BaseMailboxAccountManager<C extends BaseContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseMailboxAccountManager.class);

    private ContextFactory<C> contextFactory;

    public ContextFactory<C> getContextFactory() {
        return contextFactory;
    }

    public void setContextFactory(ContextFactory<C> contextFactory) {
        this.contextFactory = contextFactory;
    }

    protected String getHost(IServiceInstance serviceInstance) throws MailboxException {
    	String host = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
        if (host == null) {
            MailboxException me = new MailboxException("Could not found service instance property \""+IServiceInstance.HOSTNAME+"\".");
            LOGGER.error(me.getMessage());
            throw me;
        }
        return host;
    }

    protected int getPort(IServiceInstance serviceInstance) throws MailboxException {

        String portString = null;
        try {
            portString = serviceInstance.getProperty(IServiceInstance.PORT);
            return Integer.valueOf(portString);
        } catch(NumberFormatException e) {
            MailboxException me = new MailboxException("Service instance ["+serviceInstance+"] returned port ["+portString+"] which is not a valid port number!");
            LOGGER.error(e.getMessage());
            e.printStackTrace(System.out);
            throw me;
        }
    }
}
