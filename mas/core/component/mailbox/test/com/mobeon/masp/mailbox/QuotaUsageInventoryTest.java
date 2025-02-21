/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import static com.mobeon.masp.mailbox.QuotaName.*;
import static com.mobeon.masp.mailbox.QuotaUsageInventory.ByteUsageUnit.*;
import com.mobeon.masp.mailbox.QuotaUsageInventory.ByteUsageUnit;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

/**
 * QuotaUsageInventory Tester.
 *
 * @author qhast
 */
public class QuotaUsageInventoryTest extends BaseMailboxTestCase
{

    private QuotaUsageInventory<IMailbox,BaseContext> quotaUsageInventory;
    private IMailbox mailbox ;
    private BaseContext context;

    public QuotaUsageInventoryTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        Mock mockMailbox = mock(IMailbox.class);
        Mock mockMoFactory = mock(IMediaObjectFactory.class);
        mailbox = (IMailbox) mockMailbox.proxy();

        ContextFactory cfactory = new ContextFactory(){

            protected BaseContext newContext() {
                return new BaseContext(){

                    protected BaseConfig newConfig() {
                        return new BaseConfig();
                    }
                };
            }
        };
        cfactory.setConfiguration((IConfiguration)configurationMock.proxy());
        cfactory.setMediaObjectFactory((IMediaObjectFactory)mockMoFactory.proxy());
        context = cfactory.create();
        quotaUsageInventory = new QuotaUsageInventory<IMailbox,BaseContext>(mailbox,context){
            protected void init() {}
        };

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetUnsupportedQuota() throws Exception
    {
        quotaUsageInventory.setMessageUsage(TOTAL,1000L);
        quotaUsageInventory.getQuota(TOTAL);
    }


    public void testGetSupportedQuota() throws Exception
    {
        assertNotNull(quotaUsageInventory.getQuota(TOTAL));
    }

    public void testGetQuotas() throws Exception
    {
        assertEquals("",QuotaName.values().length,quotaUsageInventory.getQuotas().length);
    }

    public void testGetMailbox() throws Exception
    {
        assertEquals("QuotaUSageInventory should return the same mailbox object used in constructor!",mailbox,quotaUsageInventory.getMailbox());
    }

    public void testGetContext() throws Exception
    {
        assertEquals("QuotaUSageInventory should return the same context object used in contsructor!",context,quotaUsageInventory.getContext());
    }

    public void testSetMessageUsage() throws Exception
    {
        quotaUsageInventory.setMessageUsage(TOTAL,1000L);
        assertTrue("getMessageUsage() should return a positiv integer!",quotaUsageInventory.getQuota(TOTAL).getMessageUsage()>=0);

        quotaUsageInventory.setMessageUsage(TOTAL,-1000L);
        assertTrue("Negative usage value parameter should make getMessageUsage() returning -1!",quotaUsageInventory.getQuota(TOTAL).getMessageUsage()==-1);

    }

    /**
     * Mostly to improve code coverage
     * @throws Exception
     */
    public void testToString() throws Exception {
        assertEquals(
                "{mailbox=mockIMailbox,quotaUsage=[QuotaUsage{Name=TOTAL,byteUsage=-1,messageUsage=-1}]}",
                quotaUsageInventory.toString()
        );
    }


    public static Test suite()
    {
        return new TestSuite(QuotaUsageInventoryTest.class);
    }


}
