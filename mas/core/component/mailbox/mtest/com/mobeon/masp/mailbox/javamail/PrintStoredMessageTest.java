/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;

/**
 * @author qhast
 */
public class PrintStoredMessageTest extends ConnectedMailboxTest {


    public PrintStoredMessageTest(String name) {
        super(name);
    }

    public void testDoPrintMessage() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("printStoredMessageTest");
        IStoredMessageList mlist = folder.getMessages();

        for (IStoredMessage m : mlist) {
            m.print("123456","302102054");
        }

        mbox.close();

    }

}
