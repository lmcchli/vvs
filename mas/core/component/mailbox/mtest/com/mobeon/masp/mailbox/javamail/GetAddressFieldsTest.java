/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.Arrays;

/**
 * @author qhast
 */
public class GetAddressFieldsTest extends ConnectedMailboxTest {

    public GetAddressFieldsTest(String name) {
        super(name);
    }

    
    public void testGetSpokenNames() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("addressTest");
        IStoredMessageList mlist = folder.getMessages();


        for(IStoredMessage m : mlist) {
            logger.debug("Getting addresses for "+m);
            assertNotNull(m.getSender());
            assertNotNull(m.getReplyToAddress());
            assertNotNull(m.getRecipients());
            assertNotNull(m.getSecondaryRecipients());
            logger.debug(m.getSender());
            logger.debug(m.getReplyToAddress());
            logger.debug(Arrays.asList(m.getRecipients()));
            logger.debug(Arrays.asList(m.getSecondaryRecipients()));

        }

        mbox.close();

    }

}
