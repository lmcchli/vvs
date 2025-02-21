/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.util.content.PageCounter;
import com.mobeon.common.configuration.*;

import java.util.Map;
import java.util.HashMap;

/**
 * ContextFactory Tester.
 *
 * @author qhast
 */
public class ContextFactoryTest extends BaseMailboxTestCase
{
    private ContextFactory<BaseContext<BaseConfig>> contextFactory;
    private IMediaObjectFactory mediaObjectFactory;
    private Map<String,PageCounter> pcMap;

    public ContextFactoryTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        mediaObjectFactory = (IMediaObjectFactory) mock(IMediaObjectFactory.class).proxy();
        pcMap = new HashMap<String,PageCounter>();

        contextFactory = new ContextFactory<BaseContext<BaseConfig>>(){

            protected BaseContext<BaseConfig> newContext() {
                return new BaseContext<BaseConfig>(){

                    protected BaseConfig newConfig() {
                        return new BaseConfig();
                    }
                };
            }
        };
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSetMediaObjectFactory() throws Exception
    {
        assertNull(contextFactory.getMediaObjectFactory());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        assertEquals(mediaObjectFactory,contextFactory.getMediaObjectFactory());
    }

    public void testGetSetPageCounterMap() throws Exception
    {
        assertNull(contextFactory.getPageCounterMap());
        contextFactory.setPageCounterMap(pcMap);
        assertEquals(pcMap,contextFactory.getPageCounterMap());
    }

    public void testGetSetConfiguration() throws Exception
    {
        assertNull(contextFactory.getConfiguration());
        IConfiguration config = (IConfiguration) configurationMock.proxy();
        contextFactory.setConfiguration(config);
        assertEquals(config,contextFactory.getConfiguration());
    }

    public void testNewContext() throws Exception
    {
        assertNotNull(contextFactory.newContext());
    }

    public void testCreateWithMissingMailboxConfigurationGroup() throws Exception
    {
        latestConfigurationGetMailboxGroupStubBuilder.will(throwException(new UnknownGroupException("mailbox",null)));
        contextFactory.setConfiguration((IConfiguration)configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);
        try {
            contextFactory.create();
            fail("Should throw MailboxException if contextFactory is injected with a bad configuration");
        } catch(MailboxException e) {
            //OK
        }
    }

    public void testCreateMissingAdditionalPropertyNameParameter() throws Exception
    {
        additionalProperty1GetNameParameterStubBuilder.will(throwException(new UnknownParameterException("name",(IGroup)addtionalPropertyAdd1Mock.proxy())));
        contextFactory.setConfiguration((IConfiguration)configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);
        try {
            contextFactory.create();
            fail("Should throw MailboxException if contextFactory is injected with a errornous configuration");
        } catch(MailboxException e) {
            //OK
        }
    }

    public void testCreateWithAdditionalProperties() throws Exception
    {
        contextFactory.setConfiguration((IConfiguration) configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);

        BaseContext<BaseConfig> baseContext = contextFactory.create();

        assertEquals(mediaObjectFactory,baseContext.getMediaObjectFactory());
        assertEquals(pcMap,baseContext.getPageCounterMap());
        assertNull(baseContext.getMailboxProfile());
    }

    public void testCreateWithoutAdditionalProperties() throws Exception
    {
        mailboxConfigurationGetAdditionalPropertiesGroupsStubBuilder.will(throwException(new UnknownGroupException("additionalproperty",(IGroup)mailboxConfigurationGroupMock.proxy())));
        contextFactory.setConfiguration((IConfiguration) configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);

        BaseContext<BaseConfig> baseContext = contextFactory.create();

        assertEquals(mediaObjectFactory,baseContext.getMediaObjectFactory());
        assertEquals(pcMap,baseContext.getPageCounterMap());
        assertNull(baseContext.getMailboxProfile());
    }

    public void testCreateWithMailboxProfile() throws Exception
    {
        contextFactory.setConfiguration((IConfiguration) configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);

        MailboxProfile profile = new MailboxProfile();
        BaseContext<BaseConfig> baseContext = contextFactory.create(profile);

        assertEquals(mediaObjectFactory,baseContext.getMediaObjectFactory());
        assertEquals(pcMap,baseContext.getPageCounterMap());
        assertEquals(profile,baseContext.getMailboxProfile());
    }

    public void testGetMailboxLock() throws Exception
    {
        contextFactory.setConfiguration((IConfiguration) configurationMock.proxy());
        contextFactory.setMediaObjectFactory(mediaObjectFactory);
        contextFactory.setPageCounterMap(pcMap);
        BaseContext<BaseConfig> baseContext = contextFactory.create();
        assertNotNull(baseContext.getMailboxLock());
    }

    public static Test suite()
    {
        return new TestSuite(ContextFactoryTest.class);
    }
}
