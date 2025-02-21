/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import org.jmock.Mock;

/**
 * BasicMessageContent Tester.
 *
 * @author qhast
 */
public class BasicMessageContentTest extends BaseMailboxTestCase
{
    private Mock immutableMediaObject;
    private Mock notImmutableMediaObject;
    private Mock immutableNullMediaPropertiesMediaObject;
    private Mock notImmutableNullMediaPropertiesMediaObject;

    private static final MediaProperties MEDIA_PROPERTIES = new MediaProperties();
    private static final MessageContentProperties MESSAGE_CONTENT_PROPERTIES = new MessageContentProperties();


    public BasicMessageContentTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        immutableMediaObject = mock(IMediaObject.class);
        immutableMediaObject.stubs().method("getMediaProperties").withNoArguments().will(returnValue(MEDIA_PROPERTIES));
        immutableMediaObject.stubs().method("isImmutable").withNoArguments().will(returnValue(true));

        notImmutableMediaObject = mock(IMediaObject.class);
        notImmutableMediaObject.stubs().method("getMediaProperties").withNoArguments().will(returnValue(MEDIA_PROPERTIES));
        notImmutableMediaObject.stubs().method("isImmutable").withNoArguments().will(returnValue(false));

        immutableNullMediaPropertiesMediaObject = mock(IMediaObject.class);
        immutableNullMediaPropertiesMediaObject.stubs().method("getMediaProperties").withNoArguments().will(returnValue(null));
        immutableNullMediaPropertiesMediaObject.stubs().method("isImmutable").withNoArguments().will(returnValue(true));

        notImmutableNullMediaPropertiesMediaObject = mock(IMediaObject.class);
        notImmutableNullMediaPropertiesMediaObject.stubs().method("getMediaProperties").withNoArguments().will(returnValue(null));
        notImmutableNullMediaPropertiesMediaObject.stubs().method("isImmutable").withNoArguments().will(returnValue(false));

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetMediaProperties() throws Exception
    {
        IMessageContent messageContent = new BasicMessageContent((IMediaObject)immutableMediaObject.proxy(),MESSAGE_CONTENT_PROPERTIES);
        assertEquals("messageContent.mediaProperties is not equal to mediaProperties passed to construtor.",MEDIA_PROPERTIES,messageContent.getMediaProperties());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetMediaObject() throws Exception
    {
        IMediaObject mo = (IMediaObject)immutableMediaObject.proxy();
        IMessageContent messageContent = new BasicMessageContent(mo,MESSAGE_CONTENT_PROPERTIES);
        assertEquals("messageContent.mediaObject is not equal to mediaObject passed to construtor.",mo,messageContent.getMediaObject());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetContentProperties() throws Exception
    {
        IMessageContent messageContent = new BasicMessageContent((IMediaObject)immutableMediaObject.proxy(),MESSAGE_CONTENT_PROPERTIES);
        assertEquals("messageContent.contentProperties is not equal to contentProperties passed to construtor.",MESSAGE_CONTENT_PROPERTIES,messageContent.getContentProperties());
    }

    /**
     * Tests that faulty arguments supplied to constructor throws an IllegalArgumentException.
     * @throws Exception
     */
    public void testIllegalArgumentConstruct() throws Exception
    {
        try {
            new BasicMessageContent((IMediaObject)notImmutableMediaObject.proxy(),MESSAGE_CONTENT_PROPERTIES);
            fail("Constructing with a non immutable mediaObject should had thrown an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new BasicMessageContent((IMediaObject)notImmutableNullMediaPropertiesMediaObject.proxy(),MESSAGE_CONTENT_PROPERTIES);
            fail("Constructing with a non immutable mediaObject with a null mediaProperties reference should had thrown an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new BasicMessageContent((IMediaObject)immutableNullMediaPropertiesMediaObject.proxy(),MESSAGE_CONTENT_PROPERTIES);
            fail("Constructing with a mediaObject with a null mediaProperties reference should had thrown an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new BasicMessageContent(null,MESSAGE_CONTENT_PROPERTIES);
            fail("Constructing with a null mediaObject reference should had thrown an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

    }

    /**
     * Tests that toString not crashes or returns null.
     * @throws Exception
     */
    public void testToString() throws Exception {
        MessageContentProperties mcp = new MessageContentProperties();

        BasicMessageContent bmc = new BasicMessageContent((IMediaObject) immutableMediaObject.proxy(), mcp);
        assertNotNull("toString() shoud not return null!",bmc.toString());

        mcp.setFilename("abc");
        assertNotNull("toString() shoud not return null!",bmc.toString());

        mcp.setDescription("xyz");
        assertNotNull("toString() shoud not return null!",bmc.toString());

        mcp.setLanguage("en");
        assertNotNull("toString() shoud not return null!",bmc.toString());
    }

    public static Test suite()
    {
        return new TestSuite(BasicMessageContentTest.class);
    }
}
