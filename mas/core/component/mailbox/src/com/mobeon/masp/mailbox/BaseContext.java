/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.util.content.PageCounter;
import static com.mobeon.masp.mailbox.QuotaName.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author QHAST
 */
public abstract class BaseContext<C extends BaseConfig> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseContext.class);

    private IMediaObjectFactory mediaObjectFactory;
    private C config;
    private Map<String, PageCounter> pageCounterMap;
    private MailboxProfile mailboxProfile;
    private Lock mailboxLock;

    protected BaseContext() {
        mailboxLock = new ReentrantLock();
    }

    public Lock getMailboxLock() {
        return mailboxLock;
    }

    public final IMediaObjectFactory getMediaObjectFactory() {
        return mediaObjectFactory;
    }

    final void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        if(mediaObjectFactory == null) throw new IllegalArgumentException("mediaObjectFactory cannot be null!");
        this.mediaObjectFactory = mediaObjectFactory;
    }

    public final C getConfig() {
            return config;
    }

    final public Map<String, PageCounter> getPageCounterMap() {
        return pageCounterMap;
    }

    final void setPageCounterMap(Map<String, PageCounter> pageCounterMap) {
        if(pageCounterMap == null) {
            this.pageCounterMap = new HashMap<String, PageCounter>();
        } else {
            this.pageCounterMap = pageCounterMap;
        }
    }

    public final MailboxProfile getMailboxProfile() {
        return mailboxProfile;
    }

    final void setMailboxProfile(MailboxProfile mailboxProfile) {
        this.mailboxProfile = mailboxProfile;
    }

    protected void init(IGroup configGroup) throws MailboxException {
        this.config = newConfig();
        this.config.init(configGroup);
    }

    protected abstract C newConfig();

}
