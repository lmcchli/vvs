/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * BaseMailboxAccountManager Tester.
 *
 * @author qhast
 */
public class BaseMailboxAccountManagerTest extends MockObjectTestCase
{
    private BaseMailboxAccountManager<BaseContext<BaseConfig>> accountManager;
    private Mock serviceInstance;
    private Mock missingHostServiceInstance;
    private Mock missingPortServiceInstance;
    private Mock malformedPortServiceInstance;

    public BaseMailboxAccountManagerTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        accountManager = new BaseMailboxAccountManager<BaseContext<BaseConfig>>() {

        };

        serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("localhost"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("123"));

        missingHostServiceInstance = mock(IServiceInstance.class);
        missingHostServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("123"));

        missingPortServiceInstance = mock(IServiceInstance.class);
        missingPortServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("localhost"));

        malformedPortServiceInstance = mock(IServiceInstance.class);
        malformedPortServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("localhost"));
        malformedPortServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("123abc"));

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSetContextFactory() throws Exception
    {
        assertEquals(null,accountManager.getContextFactory());
        ContextFactory<BaseContext<BaseConfig>> ctxFactory = new ContextFactory<BaseContext<BaseConfig>>(){
            protected BaseContext<BaseConfig> newContext() {
                return new BaseContext<BaseConfig>(){
                    protected BaseConfig newConfig() {
                        return new BaseConfig();
                    }
                };
            }
        };
        accountManager.setContextFactory(ctxFactory);
        assertEquals(ctxFactory,accountManager.getContextFactory());
    }

    public void testGetHost() throws Exception
    {
        assertEquals("localhost",accountManager.getHost((IServiceInstance)serviceInstance.proxy()));
        assertEquals("localhost",accountManager.getHost((IServiceInstance)missingPortServiceInstance.proxy()));
        assertEquals("localhost",accountManager.getHost((IServiceInstance)malformedPortServiceInstance.proxy()));
    }

    public void testGetMissingHost() throws Exception
    {
        try {
            accountManager.getHost((IServiceInstance)missingHostServiceInstance.proxy());
            fail("Getting host from a service instance missing host property should throw an Exception!");
        } catch(MailboxException e) {
            //OK
        }

    }

    public void testGetPort() throws Exception
    {
        assertEquals(123,accountManager.getPort((IServiceInstance)serviceInstance.proxy()));
        assertEquals(123,accountManager.getPort((IServiceInstance)missingHostServiceInstance.proxy()));
    }

    public void testGetMissingPort() throws Exception
    {
        try {
            accountManager.getPort((IServiceInstance)missingPortServiceInstance.proxy());
            fail("Getting port from a service instance missing port property should throw an Exception!");
        } catch(MailboxException e) {
            //OK
        }

    }

    public void testGetMalformedPort() throws Exception
    {
        try {
            accountManager.getPort((IServiceInstance)malformedPortServiceInstance.proxy());
            fail("Getting port from a service instance having a malformed port property should throw an Exception!");
        } catch(MailboxException e) {
            //OK
        }

    }

    public static Test suite()
    {
        return new TestSuite(BaseMailboxAccountManagerTest.class);
    }
}
