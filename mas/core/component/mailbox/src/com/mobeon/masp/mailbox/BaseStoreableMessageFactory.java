/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mailbox.javamail.JavamailContext;

/**
 * @author QHAST
 */
public abstract class BaseStoreableMessageFactory<C extends BaseContext>{

    private ContextFactory<C> contextFactory;

    public ContextFactory<C> getContextFactory() {
        return contextFactory;
    }

    public void setContextFactory(ContextFactory<C> contextFactory) {
        this.contextFactory = contextFactory;
    }

    public abstract IStorableMessage create() throws MailboxException;
}
