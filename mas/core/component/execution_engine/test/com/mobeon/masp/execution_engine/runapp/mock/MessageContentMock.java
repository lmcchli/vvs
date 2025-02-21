package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObject;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-jan-26
 * Time: 12:19:43
 * To change this template use File | Settings | File Templates.
 */
public class MessageContentMock implements IMessageContent {
    private MediaProperties mediaProperties;
    private IMediaObject mediaObject;
    private MessageContentProperties messageContentProperties;

    public MessageContentMock(MediaProperties mediaProperties,
                              IMediaObject mediaObject, MessageContentProperties messageContentProperties){
        this.mediaProperties = mediaProperties;
        this.mediaObject = mediaObject;
        this.messageContentProperties = messageContentProperties;
    }

    public MediaProperties getMediaProperties() throws MailboxException {
        return mediaProperties;
    }

    public IMediaObject getMediaObject() throws MailboxException {
        return mediaObject;
    }

    public MessageContentProperties getContentProperties() throws MailboxException {
        return messageContentProperties;
    }
}
