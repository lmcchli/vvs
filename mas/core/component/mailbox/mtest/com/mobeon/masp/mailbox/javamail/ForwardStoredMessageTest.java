/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;

import jakarta.activation.MimeType;

/**
 * @author qhast
 */
public class ForwardStoredMessageTest extends ConnectedMailboxTest {


    public ForwardStoredMessageTest(String name) {
        super(name);
    }


    public void testDoForwardMessage() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IMediaObject spokenName = createMediaObject("SpokenNameFakeWavData", new MimeType("audio/wav"));
        spokenName.getMediaProperties().setFileExtension("wav");
        spokenName.getMediaProperties().addLengthInUnit(MILLISECONDS, 2567);

        IMediaObject comment = createMediaObject("CommentFakeWavData", new MimeType("audio/wav"));
        spokenName.getMediaProperties().setFileExtension("wav");
        spokenName.getMediaProperties().addLengthInUnit(MILLISECONDS, 1333);

        IFolder folder = mbox.getFolder("forwardStoredMessageTest");
        IStoredMessageList mlist = folder.getMessages();

        for (IStoredMessage m : mlist) {
            IStorableMessage forward = m.forward();
            forward.setType(MailboxMessageType.VOICE);
            forward.setSender("<hakan.stolt@mobeon.com>");
            forward.setSubject("Testing Forwarding VOICE Message");
            forward.setRecipients("302102054@lab.mobeon.com");
            forward.addContent(comment,new MessageContentProperties("comment", "Forwarder's comment", "en"));
            forward.setSpokenNameOfSender(spokenName, new MessageContentProperties("spokenname", "Originator's spoken name", "en"));
            forward.store();

        }

        mbox.close();

    }

}
