/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author QHAST
 */
public class BasicMessageContent implements IMessageContent {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BasicMessageContent.class);

    private IMediaObject mediaObject;
    private MessageContentProperties contentProperties;

    BasicMessageContent(IMediaObject mediaObject, MessageContentProperties contentProperties) {
        if(mediaObject == null)
            throw new IllegalArgumentException("mediaObject cannot be null!");
        if(mediaObject.getMediaProperties() == null)
            throw new IllegalArgumentException("mediaObject.mediaProperties cannot be null!");
        if(!mediaObject.isImmutable()) {
            throw new IllegalArgumentException("mediaObject must be immutable!");
        }
        this.mediaObject = mediaObject;
        this.contentProperties = contentProperties!=null?contentProperties:new MessageContentProperties();
    }

    /**
     * @return
     * @throws MailboxException
     *
     */
    public MediaProperties getMediaProperties() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaProperties() returns " + mediaObject.getMediaProperties());
        return mediaObject.getMediaProperties();
    }

    /**
     * @return
     * @throws MailboxException
     *
     */
    public IMediaObject getMediaObject() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaObject() returns " + mediaObject);
        return mediaObject;
    }

    /**
     * @return Map with
     * @throws MailboxException
     *
     */
    public MessageContentProperties getContentProperties() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getContentProperties() returns " + contentProperties);
        return contentProperties;
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("contentProperties=").append(contentProperties);
        sb.append(",mediaObject=").append(mediaObject);
        sb.append("}");
        return sb.toString();
    }
}
