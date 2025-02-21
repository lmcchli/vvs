/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.imap;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.util.content.PageCounter;
import com.mobeon.common.configuration.IGroup;
import org.jmock.Mock;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * ImapQuotaUsageInventory Tester.
 *
 * @author MANDE
 * @since <pre>12/07/2006</pre>
 * @version 1.0
 */
public class ImapQuotaUsageInventoryTest extends MailboxBaseTestCase {
    private Mock mockMailbox;
    private ImapQuotaUsageInventory<IMailbox, ImapContext<BaseConfig>> imapQuotaUsageInventory;

    public ImapQuotaUsageInventoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpMockMailbox();
        setUpMockConfig();
        ImapContextFactory<ImapContext<BaseConfig>> imapContextFactory = getImapContextFactory();
        ImapContext<BaseConfig> imapContext = imapContextFactory.create(
                new MailboxProfile("accountid", "accountpassword", "emailaddress")
        );
        imapQuotaUsageInventory =
                new ImapQuotaUsageInventory<IMailbox, ImapContext<BaseConfig>>(getMockMailbox(), imapContext) {
                    protected void init() {
                    }
                };
    }

    private void setUpMockMailbox() {
        mockMailbox = mock(IMailbox.class);
    }

    private void setUpMockConfig() {
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testComposeQuotaName() throws Exception {
        assertEquals("user/accountid", imapQuotaUsageInventory.composeQuotaName(QuotaName.TOTAL));
    }

    private ImapContextFactory<ImapContext<BaseConfig>> getImapContextFactory() {
        ImapContextFactory<ImapContext<BaseConfig>> imapContextFactory = new ImapContextFactory() {
            protected BaseContext newContext() {
                return new ImapContext<BaseConfig>(new ImapProperties()) {
                    protected BaseConfig newConfig() {
                        return new MockConfig();
                    }
                };
            }
        };
        imapContextFactory.setImapProperties(new ImapProperties());
        imapContextFactory.setPageCounterMap(new HashMap<String, PageCounter>());
        imapContextFactory.setConfiguration(getMockConfiguration());
        imapContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        return imapContextFactory;
    }

    protected IGroup getMockConfigGroup() {
        Mock mockConfigGroup = mock(IGroup.class, "mockConfigGroup");
        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
        mockConfigGroup.stubs().method("getGroups").with(eq("message.additionalproperty")).
                will(returnValue(new ArrayList<IGroup>(0)));
//        mockConfigGroup.expects(once()).method("getGroup").with(eq("imap")).
//                will(returnValue(getImapGroup()));
        return (IGroup)mockConfigGroup.proxy();
    }

    private IMailbox getMockMailbox() {
        return (IMailbox)mockMailbox.proxy();
    }

    private static class MockConfig extends BaseConfig {

    }

    public static Test suite() {
        return new TestSuite(ImapQuotaUsageInventoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
