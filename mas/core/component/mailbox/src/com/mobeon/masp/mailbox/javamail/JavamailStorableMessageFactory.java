/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.IStorableMessage;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.BaseStoreableMessageFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author QHAST
 */
public class JavamailStorableMessageFactory extends BaseStoreableMessageFactory<JavamailContext> implements IStorableMessageFactory {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailStorableMessageFactory.class);

    /**
     * Creates a new storable messsage.
     *
     * @return
     * @throws com.mobeon.masp.mailbox.MailboxException
     *          if a storeable message not could be created of some reason.
     */
    public IStorableMessage create() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("create()");
        JavamailStorableMessage message = new JavamailStorableMessage(getContextFactory().create());
        if (LOGGER.isInfoEnabled()) LOGGER.info("create() returns " + message);
        return message;
    }
}
