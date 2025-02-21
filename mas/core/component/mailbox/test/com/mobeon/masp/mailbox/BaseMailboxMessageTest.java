/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.mediaobject.IMediaObject;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * BaseMailboxMessage Tester.
 *
 * @author qhast
 */
public class BaseMailboxMessageTest extends BaseMailboxTestCase
{
    BaseMailboxMessage<BaseContext> message;
    IMediaObject spokenNameOfSender;
    IMessageContent messageContent1;
    IMessageContent messageContent2;
    IMessageContent messageContent3;

    public BaseMailboxMessageTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        BaseContext context = new BaseContext(){

            protected BaseConfig newConfig() {
                return new BaseConfig();
            }
        };
        context.init((IGroup)mailboxConfigurationGroupMock.proxy());
        message = new BaseMailboxMessage<BaseContext>(context);
        spokenNameOfSender = (IMediaObject) mock(IMediaObject.class).proxy();
        messageContent1 = (IMessageContent) mock(IMessageContent.class).proxy();
        messageContent2 = (IMessageContent) mock(IMessageContent.class).proxy();
        messageContent3 = (IMessageContent) mock(IMessageContent.class).proxy();
    }

    /**
     * Tests that constructor not initializes SpokenNameOfSender.
     * @throws Exception
     */
    public void testGetSpokenNameOfSender() throws Exception
    {
        assertNull("SpokenNameOfSender should be null!",message.spokenNameOfSender);
    }


    /**
     * Tests that constructor not initializes Content.
     * @throws Exception
     */
    public void testGetContent() throws Exception
    {
        assertNull("Content should be null!",message.content);
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetMessageContent() throws Exception {

        message.addMessageContent(messageContent1);
        message.addMessageContent(messageContent3);
        message.addMessageContent(messageContent2);
        assertEquals("Number of MessageContent should be 3!",3,message.content.size());
        assertEquals("MessageContent[0] should be "+messageContent1+"!",
                messageContent1,message.content.get(0));
        assertEquals("MessageContent[1] should be "+messageContent3+"!",
                messageContent3,message.content.get(1));
        assertEquals("MessageContent[2] should be "+messageContent2+"!",
                messageContent2,message.content.get(2));
    }

    /**
     * Tests that add method really adds message content and keeps the order they was added.
     * @throws Exception
     */
    public void testAddMessageContent() throws Exception
    {
        message.addMessageContent(messageContent1);
        assertEquals("Number of Message Content should be 1!",1,message.content.size());
        message.addMessageContent(messageContent2);
        message.addMessageContent(messageContent3);
        assertEquals("Number of Message Content should be 3!",3,message.content.size());
        assertEquals("MessageContent[0] should be "+messageContent1+"!",
                messageContent1,message.content.get(0));
        assertEquals("MessageContent[1] should be "+messageContent2+"!",
                messageContent2,message.content.get(1));
        assertEquals("MessageContent[2] should be "+messageContent3+"!",
                messageContent3,message.content.get(2));
    }

    /**
     * Tests that constructor not initializes Sender.
     * @throws Exception
     */
    public void testGetSender() throws Exception
    {
        assertNull("Sender should be null!",message.getSender());
    }


    /**
     * Tests that constructor not initializes Recipients.
     * @throws Exception
     */
    public void testGetRecipients() throws Exception
    {
        assertNull("Recipients should be null!",message.getRecipients());
    }

    /**
     * Tests that constructor not initializes SecondaryRecipients.
     * @throws Exception
     */
    public void testGetSecondaryRecipients() throws Exception
    {
        assertNull("SecondaryRecipients should be null!",message.getSecondaryRecipients());
    }

    /**
     * Tests that constructor not initializes Subject.
     * @throws Exception
     */
    public void testGetSubject() throws Exception
    {
        assertNull("Subject should be null!",message.getSubject());
    }

    /**
     * Tests that constructor not initializes ReplyToAddress.
     * @throws Exception
     */
    public void testGetReplyToAddress() throws Exception
    {
        assertNull("ReplyToAddress should be null!",message.getReplyToAddress());
    }

    /**
     * Tests that constructor not initializes Language.
     * @throws Exception
     */
    public void testGetLanguage() throws Exception
    {
        assertNull("Language should be null!",message.getLanguage());
    }


    /**
     * Tests that constructor not initializes Type.
     * @throws Exception
     */
    public void testGetType() throws Exception
    {
        assertNull("Type should be null!",message.getType());
    }


    /**
     * Tests that constructor not initializes Urgent.
     * @throws Exception
     */
    public void testGetUrgent() throws Exception
    {
        assertNull("Urgent should be null!",message.urgent);
    }


    /**
     * Tests that constructor not initializes Confidential.
     * @throws Exception
     */
    public void testGetConfidential() throws Exception
    {
        assertNull("Confidential should be null!",message.urgent);
    }


    /**
     * Tests that constructor not initializes existing Additional Properties.
     * @throws Exception
     */
    public void testGetAdditionalProperty() throws Exception
    {
        assertNull("getAdditionalProperty(\"add1\") should be null!",message.getAdditionalProperty("add1"));

        try {
            message.getAdditionalProperty("add2");
            fail("getAdditionalProperty(\"add2\") should throw an IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            //OK
        }
    }

    public static Test suite()
    {
        return new TestSuite(BaseMailboxMessageTest.class);
    }
}
