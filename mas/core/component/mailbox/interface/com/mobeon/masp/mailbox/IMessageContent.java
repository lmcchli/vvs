/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 *
 */
public interface IMessageContent {

    /**
     *
     * @return
     * @throws MailboxException
     */
    public MediaProperties getMediaProperties() throws MailboxException;

    /**
     *
     * @return
     * @throws MailboxException
     */
    public IMediaObject getMediaObject() throws MailboxException;

    /**
     *
     * @return Map with
     * @throws MailboxException
     */
    public MessageContentProperties getContentProperties() throws MailboxException;
}
