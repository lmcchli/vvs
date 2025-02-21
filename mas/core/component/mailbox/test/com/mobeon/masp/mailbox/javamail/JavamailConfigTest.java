/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Properties;
import java.util.Arrays;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.masp.mailbox.MailboxException;
import org.jmock.Mock;

/**
 * JavamailConfig Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class JavamailConfigTest extends JavamailBaseTestCase {
    private JavamailConfig javamailConfig;

    public JavamailConfigTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailConfig = new JavamailConfig();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSessionProperties() throws Exception {
        javamailConfig.init(getMockConfigGroup());
        Properties sessionProperties = javamailConfig.getSessionProperties();
        assertEquals(2, sessionProperties.size());
        assertEquals("5000", sessionProperties.get(JavamailConfig.IMAP_COMMAND_TIMEOUT_KEY));
        assertEquals("5000", sessionProperties.get(JavamailConfig.IMAP_CONNECTION_TIMEOUT_KEY));

        javamailConfig.init(getMockConfigGroupNegativeValues());
        sessionProperties = javamailConfig.getSessionProperties();
        assertEquals(2, sessionProperties.size());
        assertEquals("0", sessionProperties.get(JavamailConfig.IMAP_COMMAND_TIMEOUT_KEY));
        assertEquals("0", sessionProperties.get(JavamailConfig.IMAP_CONNECTION_TIMEOUT_KEY));
    }

    public void testConfigurationException() throws Exception {
        try {
            javamailConfig.init(getMockConfigGroupConfigurationException());
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }

    }

    protected IGroup getMockConfigGroupNegativeValues() {
        Mock mockConfigGroup = mock(IGroup.class, "mockConfigGroup");
        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
        mockConfigGroup.stubs().method("getGroups").with(eq("message.additionalproperty")).
                will(returnValue(Arrays.asList(getAdditionalPropertyGroup())));
        mockConfigGroup.expects(once()).method("getGroup").with(eq("imap")).
                will(returnValue(getImapGroupNegativeValues()));
        return (IGroup)mockConfigGroup.proxy();
    }

    private IGroup getImapGroupNegativeValues() {
        Mock mockImapGroup = mock(IGroup.class, "mockImapGroup");
        mockImapGroup.expects(once()).method("getInteger").with(eq("connectiontimeout"), eq(5000)).
                will(returnValue(-5000));
        mockImapGroup.expects(once()).method("getInteger").with(eq("commandtimeout"), eq(5000)).
                will(returnValue(-5000));
        return (IGroup)mockImapGroup.proxy();
    }

    protected IGroup getMockConfigGroupConfigurationException() {
        Mock mockConfigGroup = mock(IGroup.class, "mockConfigGroup");
        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
        mockConfigGroup.stubs().method("getGroups").with(eq("message.additionalproperty")).
                will(returnValue(Arrays.asList(getAdditionalPropertyGroup())));
        mockConfigGroup.expects(once()).method("getFullName").will(returnValue(mockConfigGroup.toString()));
        mockConfigGroup.expects(once()).method("getGroup").with(eq("imap")).
                will(throwException(new UnknownGroupException("unknowngroupexception", (IGroup)mockConfigGroup.proxy())));
        return (IGroup)mockConfigGroup.proxy();
    }

    public static Test suite() {
        return new TestSuite(JavamailConfigTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
