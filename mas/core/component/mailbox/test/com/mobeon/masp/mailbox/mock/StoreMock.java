/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mock;

import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.builder.NameMatchBuilder;

import jakarta.mail.*;
import jakarta.mail.event.ConnectionListener;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Set;
import java.util.HashSet;

/**
 * Store class for mocking purposes. Javamail uses abstract base classes instead of interfaces which makes use of
 * jmock difficult. This class extends the javamail Store class and uses an internal Mock object which implements
 * the StoreInterface interface. Method calls on the StoreMock class are redirected to the mock object so that
 * expectations can be set on the mock object as usual. To be able to set expectations, the mock object to use must
 * be set with the static setMockStore method.
 *
 * @author mande
 */
public class StoreMock extends Store {
    private static final ILogger LOG = ILoggerFactory.getILogger(StoreMock.class);

    private static Mock mockStore;
    private StoreInterface store;
    private static Set<FolderStub> folderStubSet = new HashSet<FolderStub>();

    public StoreMock(Session session, URLName urlName) {
        super(session, urlName);
        store = (StoreInterface)mockStore.proxy();
        // A StoreMock is created when calling Store.getInstance, all mocked folders are registered
        // by the returnFolderStub method and receive their "owning" store here
        for (FolderStub folderStub : folderStubSet) {
            folderStub.setStore(this);
        }
        if (LOG.isDebugEnabled()) LOG.debug("Created StoreMock");
    }

    /**
     * Sets the StoreInterface Mock object, used for expectations, constraints and stub behaviour.
     * @param mockStore
     */
    public static void setMockStore(Mock mockStore) {
        StoreMock.mockStore = mockStore;
    }

    /**
     * Todo: Explain how this works. Is currently not used in mailbox (profilemanager uses this somehow)
     * @param folderStub
     */
    public static void addStoreListener(FolderStub folderStub) {
        folderStubSet.add(folderStub);
    }

    public NameMatchBuilder expects(InvocationMatcher expectation) {
        return mockStore.expects(expectation);
    }

    public synchronized void close() throws MessagingException {
        store.close();
    }

    public void connect() throws MessagingException {
        store.connect();
    }

    public synchronized void addConnectionListener(ConnectionListener connectionListener) {
        store.addConnectionListener(connectionListener);
    }

    public Folder getDefaultFolder() throws MessagingException {
        return store.getDefaultFolder();
    }

    public Folder getFolder(String name) throws MessagingException {
        return store.getFolder(name);
    }

    public Folder getFolder(URLName urlName) throws MessagingException {
        return store.getFolder(urlName);
    }

    // Change notifiers from protected to public to be able to test listeners
    public synchronized void notifyConnectionListeners(int i) {
        super.notifyConnectionListeners(i);
    }
}
