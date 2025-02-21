/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.compare.ReceivedDateComparator;
import com.mobeon.masp.mailbox.search.StateCriteria;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * @author qhast
 */
public class ForwardMessageTest extends ConnectedMailboxTest {


    public ForwardMessageTest(String name) {
        super(name);
    }

    public void testGetForwardMessage() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("forwardTest");
        IStoredMessageList mlist = folder.getMessages();

        for(IStoredMessage m : mlist) {
            m.getContent();
            logger.debug("Message "+m+" forward="+m.isForward());
        }

        mbox.close();

    }

}
