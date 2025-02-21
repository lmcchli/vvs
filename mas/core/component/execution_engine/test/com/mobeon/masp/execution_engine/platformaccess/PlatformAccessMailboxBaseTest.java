/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import org.jmock.Mock;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IFolder;

/**
 * Date: 2005-nov-03
 *
 * @author ermmaha
 */
public abstract class PlatformAccessMailboxBaseTest extends PlatformAccessTest {

    protected String host = "johndeer.ipms";
    protected String uid0 = "id0";
    protected String uid1 = "id1";
    protected String pwd = "aaaa";
    protected String INBOX = "INBOX";
    protected String TRASH = "Trash";

    //mailbox
    protected Mock jmockMailboxId0;
    protected Mock jmockMailboxId1;
    //folders
    protected Mock jmockFolderId0Inbox;
    protected Mock jmockFolderId0Trash;
    protected Mock jmockFolderId1Inbox;
    protected Mock jmockFolderId1Trash;

    public PlatformAccessMailboxBaseTest(String name) {
        super(name);

        jmockMailboxId0 = mock(IMailbox.class);
        jmockMailboxId1 = mock(IMailbox.class);

        jmockProfileId0.stubs().method("getMailbox").withNoArguments().
                will(returnValue(jmockMailboxId0.proxy()));
        jmockProfileId1.stubs().method("getMailbox").withNoArguments().
                will(returnValue(jmockMailboxId1.proxy()));

        jmockFolderId0Inbox = mock(IFolder.class);
        jmockFolderId0Trash = mock(IFolder.class);
        jmockFolderId1Inbox = mock(IFolder.class);
        jmockFolderId1Trash = mock(IFolder.class);

        jmockFolderId0Inbox.stubs().method("getName").will(returnValue(INBOX));
        jmockMailboxId0.stubs().method("getFolder").with(eq(INBOX)).will(returnValue(jmockFolderId0Inbox.proxy()));
        jmockFolderId0Trash.stubs().method("getName").will(returnValue(TRASH));
        jmockMailboxId0.stubs().method("getFolder").with(eq(TRASH)).will(returnValue(jmockFolderId0Trash.proxy()));
        jmockFolderId1Inbox.stubs().method("getName").will(returnValue(INBOX));
        jmockMailboxId1.stubs().method("getFolder").with(eq(INBOX)).will(returnValue(jmockFolderId1Inbox.proxy()));
        jmockFolderId1Trash.stubs().method("getName").will(returnValue(TRASH));
        jmockMailboxId1.stubs().method("getFolder").with(eq(TRASH)).will(returnValue(jmockFolderId1Trash.proxy()));
    }
}
