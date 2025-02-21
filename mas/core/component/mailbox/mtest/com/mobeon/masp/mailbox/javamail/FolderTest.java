/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.MailboxProfile;

/**
 * @author qhast
 */
public class FolderTest extends ConnectedMailboxTest {

    private IFolder folder;

    public FolderTest(String name) {
        super(name);
    }


    public void testAddFolder() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();
        folder = mbox.getFolder("folderTest");

        IFolder nisse = folder.addFolder("Nisse");
        nisse.addFolder("Olsson");

        mbox.close();

    }

    public void testDeleteFolder() throws Exception {
        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();
        folder = mbox.getFolder("folderTest");

        folder.deleteFolder("Nisse");

        mbox.close();

    }

    public void testAddMailboxFolder() throws Exception {

        IFolder nisse = mbox.addFolder("Nisse");
        nisse.addFolder("Olsson");

        mbox.close();

    }

    public void testDeleteMailboxFolder() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        mbox.deleteFolder("Nisse");

        mbox.close();

    }

    public void testAddAndDeleteFolder() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();
        folder = mbox.getFolder("folderTest");

        IFolder nisse = folder.addFolder("Nisse");
        nisse.addFolder("Olsson");
        nisse.deleteFolder("Olsson");

        mbox.close();

    }

}
