/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * BaseMailbox Tester.
 *
 * @author qhast
 */
public class BaseMailboxTest extends TestCase
{
    private BaseMailbox<BaseContext<BaseConfig>> mailbox;
    private BaseContext<BaseConfig> ctx;

    public BaseMailboxTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        ctx = new BaseContext<BaseConfig>() {
            protected BaseConfig newConfig() {
                return new BaseConfig();
            }
        };

        mailbox = new BaseMailbox<BaseContext<BaseConfig>>(ctx) {

            protected QuotaUsageInventory createQuotaUsageInventory() {
                return new QuotaUsageInventory<IMailbox,BaseContext<BaseConfig>>(null,ctx) {
                    protected void init() throws MailboxException {
                        for(QuotaName name : QuotaName.values()) {
                            setMessageUsage(name,name.ordinal());
                        }
                    }
                };
            }
        };
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetContext() throws Exception
    {
        assertEquals(ctx,mailbox.getContext());
    }

    public void testCreateQuotaUsageInventory() throws Exception
    {
        IQuotaUsageInventory inventory = mailbox.createQuotaUsageInventory();
        QuotaUsage[] quotas = inventory.getQuotas();
        assertEquals(QuotaName.values().length,quotas.length);
        for(QuotaUsage usage : quotas) {
            assertEquals(-1,usage.getMessageUsage());
            assertEquals(-1,usage.getByteUsage());
        }
    }

    public void testGetQuotaUsageInventory() throws Exception
    {
        IQuotaUsageInventory inventory = mailbox.getQuotaUsageInventory();
        QuotaUsage[] quotas = inventory.getQuotas();
        assertEquals(QuotaName.values().length,quotas.length);
        for(QuotaUsage usage : quotas) {
            assertEquals(usage.getName().ordinal(),usage.getMessageUsage());
            assertEquals(usage.getName().ordinal()*10,usage.getByteUsage());
        }
    }

    public static Test suite()
    {
        return new TestSuite(BaseMailboxTest.class);
    }
}
