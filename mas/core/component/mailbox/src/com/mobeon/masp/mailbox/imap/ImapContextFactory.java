/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.imap;

import com.mobeon.masp.mailbox.BaseContext;
import com.mobeon.masp.mailbox.ContextFactory;

/**
 * @author Håkan Stolt
 */
public abstract class ImapContextFactory<C extends BaseContext> extends ContextFactory<C> {

    private ImapProperties imapProperties;

    protected ImapContextFactory() {
        imapProperties = new ImapProperties();
    }

    public ImapProperties getImapProperties() {
        return imapProperties;
    }

    public void setImapProperties(ImapProperties imapProperties) {
        this.imapProperties = imapProperties;
    }

}
