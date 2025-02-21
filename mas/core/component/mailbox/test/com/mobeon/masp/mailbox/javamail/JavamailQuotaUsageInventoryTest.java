/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.QuotaUsage;
import com.mobeon.masp.mailbox.QuotaName;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.mock.ImapStoreInterface;
import com.mobeon.masp.mailbox.mock.ImapStoreMock;
import com.mobeon.masp.mailbox.mock.FolderInterface;
import com.mobeon.masp.mailbox.mock.FolderMock;
import org.eclipse.angus.mail.imap.IMAPStore;
import jakarta.mail.Quota;

import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.FolderNotFoundException;

import org.jmock.Mock;

/**
 * JavamailQuotaUsageInventory Tester.
 *
 * @author MANDE
 * @since <pre>12/14/2006</pre>
 * @version 1.0
 */
public class JavamailQuotaUsageInventoryTest extends JavamailBaseTestCase {
    Mock mockImapStore;
    static final int MESSAGE_COUNT = 5;
    private Mock mockImapFolder;
    private FolderMock imapFolderMock;
    private JavamailStoreAdapter javamailImapStoreAdapter;

    public JavamailQuotaUsageInventoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpMockImapStore();
        setUpMockImapFolder();
        setUpJavamailImapStoreAdapter();
    }

    private void setUpMockImapStore() throws Exception {
        mockImapStore = mock(ImapStoreInterface.class);
        ImapStoreMock.setMockImapStore(mockImapStore);
    }

    private void setUpMockImapFolder() throws Exception {
        mockImapFolder = mock(FolderInterface.class);
        mockImapFolder.stubs().method("getFullName").will(returnValue("mockFolder"));
        imapFolderMock = new FolderMock(getImapStore());
        imapFolderMock.setMockFolder(mockImapFolder);
    }

    protected void setUpJavamailImapStoreAdapter() throws NoSuchProviderException {
        javamailImapStoreAdapter = new JavamailStoreAdapter(getImapStore(), javamailContext);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test getting quotas when the interim fallback solution is used for message count
     * @throws Exception
     */
    public void testGetQuotasFallback() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailImapStoreAdapter, javamailContext);
        Quota quota = new Quota("user/accountid");
        quota.setResourceLimit("STORAGE", 1000);
        quota.resources[0].usage = 1;
        mockImapStore.expects(once()).method("getQuota").with(eq("inbox")).will(returnValue(new Quota[] {
                quota
        }));
        mockImapStore.expects(once()).method("getFolder").with(eq("inbox")).will(returnValue(imapFolderMock));
        mockImapFolder.expects(once()).method("isOpen").will(returnValue(true));
        mockImapFolder.expects(once()).method("getMessageCount").will(returnValue(MESSAGE_COUNT));
        mockImapFolder.expects(atLeastOnce()).method("list").
                will(onConsecutiveCalls(
                        returnValue(new Folder[] { imapFolderMock }),
                        returnValue(new Folder[0]))
                );
        mockImapFolder.expects(once()).method("getMessageCount").will(returnValue(MESSAGE_COUNT));
        javamailQuotaUsageInventory.init();
        QuotaUsage[] quotas = javamailQuotaUsageInventory.getQuotas();
        assertEquals(1, quotas.length);
        assertEquals(MESSAGE_COUNT * 2, quotas[0].getMessageUsage());
        assertEquals(1024, quotas[0].getByteUsage());
        assertEquals(QuotaName.TOTAL, quotas[0].getName());
    }

    /**
     * Test getting quotas when the interim fallback solution fails
     * @throws Exception
     */
    public void testGetQuotasFallbackMessagingException() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailImapStoreAdapter, javamailContext);
        Quota quota = new Quota("user/accountid");
        quota.setResourceLimit("STORAGE", 1000);
        quota.resources[0].usage = 1;
        mockImapStore.expects(once()).method("getQuota").with(eq("inbox")).will(returnValue(new Quota[] {
                quota
        }));
        mockImapStore.expects(once()).method("getFolder").with(eq("inbox")).will(returnValue(imapFolderMock));
        mockImapFolder.expects(once()).method("isOpen").will(returnValue(true));
        mockImapFolder.expects(once()).method("getMessageCount").
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailQuotaUsageInventory.init();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting quotas when the interim fallback solution cannot find the folder
     * @throws Exception
     */
    public void testGetQuotasFallbackFolderNotFoundException() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailImapStoreAdapter, javamailContext);
        Quota quota = new Quota("user/accountid");
        quota.setResourceLimit("STORAGE", 1000);
        quota.resources[0].usage = 1;
        mockImapStore.expects(once()).method("getQuota").with(eq("inbox")).will(returnValue(new Quota[] {
                quota
        }));
        mockImapStore.expects(once()).method("getFolder").with(eq("inbox")).
                will(throwException(new FolderNotFoundException("foldernotfoundexception", null)));
        javamailQuotaUsageInventory.init();
        QuotaUsage[] quotas = javamailQuotaUsageInventory.getQuotas();
        assertEquals(1, quotas.length);
        assertEquals(0, quotas[0].getMessageUsage());
        assertEquals(1024, quotas[0].getByteUsage());
        assertEquals(QuotaName.TOTAL, quotas[0].getName());
    }

    /**
     * Test getting quotas when MS supports "MessageUsage" in IMAP QuotaRoot command
     * @throws Exception
     */
    public void testGetQuotas() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailImapStoreAdapter, javamailContext);
        Quota quota = new Quota("user/accountid");
        quota.setResourceLimit("STORAGE", 1000);
        quota.resources[0].usage = 1;
        quota.setResourceLimit("MESSAGE", 10);
        quota.resources[1].usage = 5;
        mockImapStore.expects(once()).method("getQuota").with(eq("inbox")).will(returnValue(new Quota[] {
                quota
        }));
        javamailQuotaUsageInventory.init();
        QuotaUsage[] quotas = javamailQuotaUsageInventory.getQuotas();
        assertEquals(1, quotas.length);
        assertEquals(5, quotas[0].getMessageUsage());
        assertEquals(1024, quotas[0].getByteUsage());
        assertEquals(QuotaName.TOTAL, quotas[0].getName());
    }

    public void testGetQuotasNoImapStore() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailStoreAdapter, javamailContext);
        try {
            javamailQuotaUsageInventory.init();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetQuotasMessagingException() throws Exception {
        JavamailQuotaUsageInventory javamailQuotaUsageInventory =
                new JavamailQuotaUsageInventory(javamailImapStoreAdapter, javamailContext);
        mockImapStore.expects(once()).method("getQuota").with(eq("inbox")).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailQuotaUsageInventory.init();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    protected IMAPStore getImapStore() throws NoSuchProviderException {
        return (IMAPStore)SESSION.getStore("mockimapstore");
    }

    public static Test suite() {
        return new TestSuite(JavamailQuotaUsageInventoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
