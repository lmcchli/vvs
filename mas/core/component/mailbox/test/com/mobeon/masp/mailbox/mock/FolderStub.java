/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.mock;

import org.jmock.core.Stub;
import org.jmock.core.Invocation;
import org.jmock.Mock;

import jakarta.mail.Store;

/**
 * Stub used for returning a mocked folder from a mocked store's getFolder method
 *
 * @author mande
 */
public class FolderStub implements Stub, StoreListenerMock {

    private Mock mockFolder;
    private Store store;

    public FolderStub(Mock mockFolder) {
        this.mockFolder = mockFolder;
    }

    public Object invoke(Invocation invocation) throws Throwable {
        // Check that invoked object is of StoreInterface
        if (invocation.invokedObject instanceof StoreInterface) {
            FolderMock folder = new FolderMock(store);
            folder.setMockFolder(mockFolder);
            return folder;
        }
        throw new RuntimeException("Incorrect use of FolderStub");
    }

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("returns a mocked folder");
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
