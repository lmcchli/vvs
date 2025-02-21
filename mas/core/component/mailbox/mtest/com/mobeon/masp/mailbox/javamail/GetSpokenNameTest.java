/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * @author qhast
 */
public class GetSpokenNameTest extends ConnectedMailboxTest {

    public GetSpokenNameTest(String name) {
        super(name);
    }


    public void testGetSpokenNames() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("spokenNameTest");
        IStoredMessageList mlist = folder.getMessages();


        for(IStoredMessage m : mlist) {
            logger.debug("Getting Spoken name for "+m);
            IMediaObject spokenNameOfSender = m.getSpokenNameOfSender();
            if(m.getSubject().matches(".*[Ff][Ww][Dd].*")) {
                assertNull(spokenNameOfSender);
            } else {
                assertNotNull(spokenNameOfSender);
            }
        }

        mbox.close();

    }

}
