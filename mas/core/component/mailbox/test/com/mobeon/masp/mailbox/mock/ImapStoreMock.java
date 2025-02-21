/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mock;

import org.jmock.Mock;

import jakarta.mail.*;
import jakarta.mail.event.ConnectionListener;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.eclipse.angus.mail.imap.IMAPStore;
import jakarta.mail.Quota;

import java.util.Set;
import java.util.HashSet;

/**
 * Store class for mocking purposes
 *
 * @author mande
 */
public class ImapStoreMock extends IMAPStore {
    private static final ILogger LOG = ILoggerFactory.getILogger(ImapStoreMock.class);

    private static Mock mockImapStore;
    private ImapStoreInterface imapStore;
    private static Set<FolderStub> folderStubSet = new HashSet<FolderStub>();

    public ImapStoreMock(Session session, URLName urlName) {
        super(session, urlName);
        imapStore = (ImapStoreInterface)mockImapStore.proxy();
        // A StoreMock is created when calling Store.getInstance, all mocked folders are registered
        // by the returnFolderStub method and receive their "owning" store here
        for (FolderStub folderStub : folderStubSet) {
            folderStub.setStore(this);
        }
        if (LOG.isDebugEnabled()) LOG.debug("Created ImapStoreMock");
    }

    public static void setMockImapStore(Mock mockImapStore) {
        ImapStoreMock.mockImapStore = mockImapStore;
    }

    public static void addStoreListener(FolderStub folderStub) {
        folderStubSet.add(folderStub);
    }

    public synchronized void close() throws MessagingException {
        imapStore.close();
    }

    public void connect() throws MessagingException {
        imapStore.connect();
    }

    public synchronized void addConnectionListener(ConnectionListener connectionListener) {
        imapStore.addConnectionListener(connectionListener);
    }

    public Folder getDefaultFolder() throws MessagingException {
        return imapStore.getDefaultFolder();
    }

    public Folder getFolder(String name) throws MessagingException {
        return imapStore.getFolder(name);
    }

    public Folder getFolder(URLName urlName) throws MessagingException {
        return imapStore.getFolder(urlName);
    }

    public Quota[] getQuota(String root) throws MessagingException {
        return imapStore.getQuota(root);
    }
}
