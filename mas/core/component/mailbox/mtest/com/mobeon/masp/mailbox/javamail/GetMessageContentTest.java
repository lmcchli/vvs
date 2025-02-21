/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.util.content.ContentSizePredicter;

import jakarta.mail.internet.MimePart;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author qhast
 */
public class GetMessageContentTest extends ConnectedMailboxTest {


    public GetMessageContentTest(String name) {
        super(name);
    }

    public void testGetMessageContent() throws Exception {

        mailboxProfile = new MailboxProfile("302102054","abcd","302102054@lab.mobeon.com");
        //mailboxProfile = new MailboxProfile("99910002","abcd","99910002@lab.mobeon.com");
        //imapServiceHostnameStubBuilder.will(returnValue("bishop.hurr9.lab.mobeon.com"));
        //imapServicePortStubBuilder.will(returnValue("143"));
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("messageContentTest");
        IStoredMessageList mlist = folder.getMessages();
        for(IStoredMessage message : mlist) {
            for(IMessageContent messageContent : message.getContent()) {
                MessageContentProperties contentProperties = messageContent.getContentProperties();
                MediaProperties mediaProperties = messageContent.getMediaProperties();
                long mediaPropertiesSize = mediaProperties.getSize();
                IMediaObject mediaObject = messageContent.getMediaObject();
                long mediaObjectSize = mediaObject.getSize();
                assertNotNull(mediaObject);
                assertEquals(mediaObject.getMediaProperties(),mediaProperties);
                long mediaObjectMediaPropertiesSize = mediaObject.getMediaProperties().getSize();
                assertEquals("MediaObject.mediaProperties.size differs from MediaObject.size" ,mediaObjectMediaPropertiesSize,mediaObjectSize);
            }
        }

        mbox.close();

    }

}
