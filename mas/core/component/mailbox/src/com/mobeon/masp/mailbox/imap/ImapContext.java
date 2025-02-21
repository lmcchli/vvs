/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.imap;

import com.mobeon.masp.mailbox.BaseConfig;
import com.mobeon.masp.mailbox.BaseContext;

/**
 * @author Håkan Stolt
 */
public abstract class ImapContext<C extends BaseConfig> extends BaseContext<C> {

    private ImapProperties imapProperties;

    protected ImapContext(ImapProperties imapProperties) {
        this.imapProperties = imapProperties;
    }

    public ImapProperties getImapProperties() {
        return imapProperties;
    }
}
