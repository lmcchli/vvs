/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.mail.*;
import jakarta.mail.FolderNotFoundException;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.mock.StoreMock;
import com.mobeon.masp.mailbox.mock.FolderMock;

/**
 * JavamailStoreAdapter Tester.
 *
 * @author MANDE
 * @since <pre>12/19/2006</pre>
 * @version 1.0
 */
public class JavamailStoreAdapterTest extends JavamailBaseTestCase {
    Store store;
    StoreMock storeMock;

    public JavamailStoreAdapterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Overridden so that the used Store is saved
     * @throws NoSuchProviderException
     */
    @Override
    protected void setUpJavamailStoreAdapter() throws NoSuchProviderException {
        store = getStore();
        storeMock = (StoreMock)store;
        javamailStoreAdapter = new JavamailStoreAdapter(store, javamailContext);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetStore() throws Exception {
        assertSame(javamailStoreAdapter.getStore(), store);
    }

    public void testCreateQuotaUsageInventory() throws Exception {
        QuotaUsageInventory quotaUsageInventory = javamailStoreAdapter.createQuotaUsageInventory();
        assertTrue("Expected instance of JavamailQuotaUsageInventory", quotaUsageInventory instanceof JavamailQuotaUsageInventory);
    }

    public void testGetFolder() throws Exception {
        String folderName = "folder";
        // Test get folder when not found
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new FolderNotFoundException(folderMock, "foldernotfoundexception")));
        try {
            javamailStoreAdapter.getFolder(folderName);
            fail("Expected FolderNotFoundException");
        } catch (com.mobeon.masp.mailbox.FolderNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test get folder when getting folder fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.getFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test get folder when open fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("isOpen").will(returnValue(false));
        folderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.getFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test get folder when successful
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        JavamailFolderAdapter folder = javamailStoreAdapter.getFolder(folderName);
        assertNotNull("Folder should not be null", folder);
        assertSame(folderMock, folder.folder);
        // Folder should be cached
        JavamailFolderAdapter cachedFolder = javamailStoreAdapter.getFolder(folderName);
        assertSame(folder, cachedFolder);
    }

    public void testAddFolder() throws Exception {
        String folderName = "folder";
        // Test add folder when getting folder fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.addFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test add folder when folder exists
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("getName").will(returnValue(folderName));
        try {
            javamailStoreAdapter.addFolder(folderName);
            fail("Expected FolderAlreadyExistsException");
        } catch (FolderAlreadyExistsException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test add folder when create fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(false));
        folderMock.expects(once()).method("create").with(eq(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)).
                will(throwException(new MessagingException("messagingexception")));
        folderMock.expects(once()).method("getName").will(returnValue(folderName));
        try {
            javamailStoreAdapter.addFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test add folder when successful
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(false));
        folderMock.expects(once()).method("create").with(eq(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)).
                will(returnValue(true));
        JavamailFolderAdapter folder = javamailStoreAdapter.addFolder(folderName);
        assertNotNull("Folder should not be null", folder);
        assertSame(folderMock, folder.folder);
    }

    public void testDeleteFolder() throws Exception {
        String folderName = "folder";
        // Test delete folder when getting folder fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.deleteFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test delete folder when folder does not exist
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(false));
        try {
            javamailStoreAdapter.deleteFolder(folderName);
            fail("Expected FolderNotFoundException");
        } catch (com.mobeon.masp.mailbox.FolderNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test delete folder when delete fails
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("delete").with(eq(true)).
                will(throwException(new MessagingException("messagingexception")));
        folderMock.expects(once()).method("getName").will(returnValue(folderName));
        try {
            javamailStoreAdapter.deleteFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test delete folder when successful
        storeMock.expects(once()).method("getFolder").with(eq(folderName)).will(returnValue(folderMock));
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("delete").with(eq(true)).will(returnValue(true));
        javamailStoreAdapter.deleteFolder(folderName);
    }

    public void testClose() throws Exception {
        String openFolderName = "openFolder";
        String closedFolderName = "closedFolder";
        // Open folders so we can check that they will be closed
        FolderMock openFolderMock = getFolderMock(getMockFolder(openFolderName));
        FolderMock closedFolderMock = getFolderMock(getMockFolder(closedFolderName));
        storeMock.expects(once()).method("getFolder").with(eq(openFolderName)).will(returnValue(openFolderMock));
        storeMock.expects(once()).method("getFolder").with(eq(closedFolderName)).will(returnValue(closedFolderMock));
        openFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        openFolderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE));
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        closedFolderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE));
        JavamailFolderAdapter closedFolder = javamailStoreAdapter.getFolder(closedFolderName);
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        closedFolderMock.expects(once()).method("close").with(eq(false));
        JavamailFolderAdapter openFolder = javamailStoreAdapter.getFolder(openFolderName);
        assertNotNull("Folder should not be null", openFolder);
        assertSame(openFolderMock, openFolder.folder);
        assertNotNull("Folder should not be null", closedFolder);
        assertSame(closedFolderMock, closedFolder.folder);
        // Test close when folder close fails
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        openFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        openFolderMock.expects(once()).method("close").will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.close();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test close when folder open fails
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        openFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        openFolderMock.expects(once()).method("close").with(eq(true));
        closedFolderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailStoreAdapter.close();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test close when returning store fails
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        closedFolderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE));
        closedFolderMock.expects(once()).method("close").with(eq(true));
        storeMock.expects(once()).method("close").will(throwException(new MessagingException("messagingexception")));
        javamailStoreAdapter.close();
        // Test close when successful
        closedFolderMock.expects(once()).method("isOpen").will(returnValue(false));
        closedFolderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE));
        closedFolderMock.expects(once()).method("close").with(eq(true));
        storeMock.expects(once()).method("close");
        javamailStoreAdapter.close();
    }

    public static Test suite() {
        return new TestSuite(JavamailStoreAdapterTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
