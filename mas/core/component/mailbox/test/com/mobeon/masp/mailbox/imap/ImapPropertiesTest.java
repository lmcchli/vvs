package com.mobeon.masp.mailbox.imap;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.mobeon.masp.mailbox.QuotaUsageInventory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * ImapProperties Tester.
 *
 * @author MANDE
 * @since <pre>12/07/2006</pre>
 * @version 1.0
 */
public class ImapPropertiesTest extends TestCase {
    private ImapProperties imapProperties;

    public ImapPropertiesTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        imapProperties = new ImapProperties();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetQuotaRootMailboxName() throws Exception {
        // Test default value
        assertEquals("inbox", imapProperties.getQuotaRootMailboxName());
        // Test set
        String quotaRootMailboxName = "quotaRootMailboxName";
        imapProperties.setQuotaRootMailboxName(quotaRootMailboxName);
        assertEquals(quotaRootMailboxName, imapProperties.getQuotaRootMailboxName());
    }

    public void testSetGetTotalQuotaNameTemplate() throws Exception {
        // Test default value
        assertEquals("user/${accountid}", imapProperties.getTotalQuotaNameTemplate());
        // Test set
        String totalQuotaNameTemplate = "totalQuotaNameTemplate";
        imapProperties.setTotalQuotaNameTemplate(totalQuotaNameTemplate);
        assertEquals(totalQuotaNameTemplate, imapProperties.getTotalQuotaNameTemplate());
    }

    public void testSetGetByteUsageQuotaRootResourceName() throws Exception {
        // Test default value
        assertEquals("STORAGE", imapProperties.getByteUsageQuotaRootResourceName());
        // Test set
        String byteUsageQuotaRootResourceName = "byteUsageQuotaRootResourceName";
        imapProperties.setByteUsageQuotaRootResourceName(byteUsageQuotaRootResourceName);
        assertEquals(byteUsageQuotaRootResourceName, imapProperties.getByteUsageQuotaRootResourceName());
    }

    public void testSetGetByteUsageQuotaRootResourceUnit() throws Exception {
        // Test default value
        assertEquals(QuotaUsageInventory.ByteUsageUnit.KILOBYTES, imapProperties.getByteUsageQuotaRootResourceUnit());
        // Test set
        imapProperties.setByteUsageQuotaRootResourceUnit(QuotaUsageInventory.ByteUsageUnit.BYTES);
        assertEquals(QuotaUsageInventory.ByteUsageUnit.BYTES, imapProperties.getByteUsageQuotaRootResourceUnit());
    }

    public void testSetGetMessageUsageQuotaRootResourceName() throws Exception {
        // Test default value
        assertEquals("MESSAGE", imapProperties.getMessageUsageQuotaRootResourceName());
        // Test set
        String messageUsageQuotaRootResourceName = "messageUsageQuotaRootResourceName";
        imapProperties.setMessageUsageQuotaRootResourceName(messageUsageQuotaRootResourceName);
        assertEquals(messageUsageQuotaRootResourceName, imapProperties.getMessageUsageQuotaRootResourceName());
    }

    public void testSetGetMessageUsageFolderNamesArray() throws Exception {
        // Test default value
        assertEquals(new String[]{"inbox"}, imapProperties.getMessageUsageFolderNames());
        // Test set
        String[] messageUsageFolderNames = new String[]{
                "messageUsageFolderNamesArrayItem1",
                "messageUsageFolderNamesArrayItem2"
        };
        imapProperties.setMessageUsageFolderNames(messageUsageFolderNames);
        assertEquals(messageUsageFolderNames, imapProperties.getMessageUsageFolderNames());
    }

    public void testSetGetMessageUsageFolderNamesSet() throws Exception {
        // Test default value
        assertEquals(new String[]{"inbox"}, imapProperties.getMessageUsageFolderNames());
        // Test set
        String[] messageUsageFolderNames = new String[]{
                "messageUsageFolderNamesSetItem1",
                "messageUsageFolderNamesSetItem2"
        };
        imapProperties.setMessageUsageFolderNames(new HashSet<String>(Arrays.asList(messageUsageFolderNames)));
        assertEquals(messageUsageFolderNames, imapProperties.getMessageUsageFolderNames());
    }

    public void testToString() throws Exception {
        assertEquals(
                "ImapProperties:{quotaRootMailboxName=inbox,totalQuotaNameTemplate=user/${accountid}," +
                        "messageUsageQuotaRootResourceName=MESSAGE,byteUsageQuotaRootResourceName=STORAGE," + 
                        "byteUsageQuotaRootResourceUnit=KILOBYTES,messageUsageFolderNames=[inbox]}",
                imapProperties.toString()
        );
    }

    public static <T> void assertEquals(T[] expected, T[] actual) {
        assertEquals("", expected, actual);
    }

    public static <T> void assertEquals(String applicationName, T[] expected, T[] actual) {
        assertTrue(applicationName + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                Arrays.equals(expected, actual));
    }

    public static Test suite() {
        return new TestSuite(ImapPropertiesTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
