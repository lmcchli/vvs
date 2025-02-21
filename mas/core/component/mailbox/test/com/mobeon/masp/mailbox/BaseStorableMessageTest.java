/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Date;

/**
 * BaseMailboxMessage Tester.
 *
 * @author qhast
 */
public class BaseStorableMessageTest extends BaseMailboxTestCase
{
    BaseStorableMessage<BaseContext> messageNoAdditionalProps;
    BaseStorableMessage<BaseContext> messageWithAdditionalProps;
    Mock spokenNameOfSenderMock;
    MediaProperties mediaProperties;
    IMediaObject spokenNameOfSender;
    IMessageContent messageContent1;
    IMessageContent messageContent2;
    IMessageContent messageContent3;

    public BaseStorableMessageTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        BaseContext context = new BaseContext<BaseConfig>(){
            protected BaseConfig newConfig() {
                return new BaseConfig();
            }
        };
        context.init((IGroup)mailboxConfigurationGroupMock.proxy());

        messageNoAdditionalProps = new BaseStorableMessage<BaseContext>(context){
            protected void storeWork(String host) {}
        };
        messageWithAdditionalProps = new BaseStorableMessage<BaseContext>(context){
            protected void storeWork(String host) {}
        };
        messageWithAdditionalProps.setAdditionalProperty("add1","Hejsan add1!");
        mediaProperties = new MediaProperties();
        spokenNameOfSenderMock = mock(IMediaObject.class);
        spokenNameOfSenderMock.stubs().method("isImmutable").withNoArguments().will(returnValue(true));
        spokenNameOfSenderMock.stubs().method("getMediaProperties").withNoArguments().will(returnValue(mediaProperties));
        spokenNameOfSender = (IMediaObject) spokenNameOfSenderMock.proxy();
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
        assertNull("SpokenNameOfSender should be null!",messageNoAdditionalProps.getSpokenNameOfSender());
        assertNull("SpokenNameOfSender should be null!",messageWithAdditionalProps.getSpokenNameOfSender());
    }

    /**
     * Tests that constructor not initializes SpokenNameOfSender.
     * @throws Exception
     */
    public void testSetSpokenNameOfSender() throws Exception
    {
        messageNoAdditionalProps.setSpokenNameOfSender(spokenNameOfSender,new MessageContentProperties());
        assertEquals("SpokenNameOfSender should be "+spokenNameOfSender+"!",
                spokenNameOfSender,messageNoAdditionalProps.getSpokenNameOfSender());

        messageWithAdditionalProps.setSpokenNameOfSender(spokenNameOfSender,new MessageContentProperties());
        assertEquals("SpokenNameOfSender should be "+spokenNameOfSender+"!",
                spokenNameOfSender,messageWithAdditionalProps.getSpokenNameOfSender());
    }

    /**
     * Tests that constructor initializes Content with an empty list.
     * @throws Exception
     */
    public void testGetContent() throws Exception
    {
        assertEquals("Content list size should be 0!",0,messageNoAdditionalProps.getContent().size());
        assertEquals("Content list size should be 0!",0,messageWithAdditionalProps.getContent().size());
    }

    /**
     * Tests that add method really adds message content and keeps the order they was added.
     * @throws Exception
     */
    public void testAddMessageContent() throws Exception
    {
        messageNoAdditionalProps.resetMessageContent();
        assertEquals("Number of Message Content should be 0!",0,messageNoAdditionalProps.getContent().size());
        messageWithAdditionalProps.addMessageContent(messageContent1);
        assertEquals("Number of Message Content should be 1!",1,messageWithAdditionalProps.getContent().size());
        messageWithAdditionalProps.addMessageContent(messageContent2);
        messageWithAdditionalProps.addMessageContent(messageContent3);
        assertEquals("Number of Message Content should be 3!",3,messageWithAdditionalProps.getContent().size());
        assertEquals("MessageContent[0] should be "+messageContent1+"!",
                messageContent1,messageWithAdditionalProps.getContent().get(0));
        assertEquals("MessageContent[1] should be "+messageContent2+"!",
                messageContent2,messageWithAdditionalProps.getContent().get(1));
        assertEquals("MessageContent[2] should be "+messageContent3+"!",
                messageContent3,messageWithAdditionalProps.getContent().get(2));
    }

    /**
     * Tests that constructor not initializes Sender.
     * @throws Exception
     */
    public void testGetSender() throws Exception
    {
        assertNull("Sender should be null!",messageNoAdditionalProps.getSender());
        assertNull("Sender should be null!",messageWithAdditionalProps.getSender());
    }


    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetSender() throws Exception
    {
        messageWithAdditionalProps.setSender("Nisse");
        assertEquals("getSender() should return \"Nisse\"!","Nisse",messageWithAdditionalProps.getSender());
    }

    /**
     * Tests that constructor initializes Recipients with an empty array.
     * @throws Exception
     */
    public void testGetRecipients() throws Exception
    {
        assertEquals("Recipients should have size 0!",0,messageNoAdditionalProps.getRecipients().length);
        assertEquals("Recipients should have size 0!",0,messageWithAdditionalProps.getRecipients().length);
    }

    /**
     * Tests that constructor initializes Secondary Recipients with an empty array.
     * @throws Exception
     */
    public void testGetSecondaryRecipients() throws Exception
    {
        assertEquals("Secondary Recipients should have size 0!",0,messageNoAdditionalProps.getSecondaryRecipients().length);
        assertEquals("Secondary Recipients should have size 0!",0,messageWithAdditionalProps.getSecondaryRecipients().length);
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetRecipients() throws Exception
    {
        messageNoAdditionalProps.setRecipients();
        assertEquals("Number of Recipients should be 0!",0,messageNoAdditionalProps.getRecipients().length);
        messageWithAdditionalProps.setRecipients("Hasse","Arne");
        assertEquals("Number of Recipients should be 2!",2,messageWithAdditionalProps.getRecipients().length);
        List<String> recipients = Arrays.asList(messageWithAdditionalProps.getRecipients());
        assertTrue("Recipients should contain Hasse!",recipients.contains("Hasse"));
        assertTrue("Recipients should contain Arne!",recipients.contains("Arne"));
    }

    /**
     * Tests that recipients is added to property value.
     * @throws Exception
     */
    public void testAddRecipient() throws Exception
    {
        messageNoAdditionalProps.setRecipients();
        assertEquals("Number of Recipients should be 0!",0,messageNoAdditionalProps.getRecipients().length);

        messageWithAdditionalProps.addRecipient("Hasse");
        assertEquals("Number of Recipients should be 1!",1,messageWithAdditionalProps.getRecipients().length);
        List<String> recipients = Arrays.asList(messageWithAdditionalProps.getRecipients());
        assertTrue("Recipients should contain Hasse!",recipients.contains("Hasse"));

        messageWithAdditionalProps.addRecipient("Arne");
        assertEquals("Number of Recipients should be 2!",2,messageWithAdditionalProps.getRecipients().length);
        List<String> recipients2 = Arrays.asList(messageWithAdditionalProps.getRecipients());
        assertTrue("Recipients should contain Hasse!",recipients2.contains("Hasse"));
        assertTrue("Recipients should contain Arne!",recipients2.contains("Arne"));

    }


    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetSecondaryRecipients() throws Exception
    {
        messageNoAdditionalProps.setSecondaryRecipients();
        assertEquals("Number of SecondaryRecipients should be 0!",0,messageNoAdditionalProps.getSecondaryRecipients().length);
        messageWithAdditionalProps.setSecondaryRecipients("Lisa","Stig","Monica");
        assertEquals("Number of SecondaryRecipients should be 3!",3,messageWithAdditionalProps.getSecondaryRecipients().length);
        List<String> recipients = Arrays.asList(messageWithAdditionalProps.getSecondaryRecipients());
        assertTrue("SecondaryRecipients should contain Lisa!",recipients.contains("Lisa"));
        assertTrue("SecondaryRecipients should contain Stig!",recipients.contains("Stig"));
        assertTrue("SecondaryRecipients should contain Monica!",recipients.contains("Monica"));
    }

    /**
     * Tests that secondary recipients is added to property value.
     * @throws Exception
     */
    public void testAddSecondaryRecipient() throws Exception
    {
        messageNoAdditionalProps.setSecondaryRecipients();
        assertEquals("Number of SecondaryRecipients should be 0!",0,messageNoAdditionalProps.getSecondaryRecipients().length);

        messageWithAdditionalProps.addSecondaryRecipient("Hasse");
        assertEquals("Number of SecondaryRecipients should be 1!",1,messageWithAdditionalProps.getSecondaryRecipients().length);
        List<String> recipients = Arrays.asList(messageWithAdditionalProps.getSecondaryRecipients());
        assertTrue("SecondaryRecipients should contain Hasse!",recipients.contains("Hasse"));

        messageWithAdditionalProps.addSecondaryRecipient("Arne");
        assertEquals("Number of SecondaryRecipients should be 2!",2,messageWithAdditionalProps.getSecondaryRecipients().length);
        List<String> recipients2 = Arrays.asList(messageWithAdditionalProps.getSecondaryRecipients());
        assertTrue("SecondaryRecipients should contain Hasse!",recipients2.contains("Hasse"));
        assertTrue("SecondaryRecipients should contain Arne!",recipients2.contains("Arne"));

    }

    /**
     * Tests that constructor initializes Subject with an empty string.
     * @throws Exception
     */
    public void testGetSubject() throws Exception
    {
        assertEquals("Subject should be an empty string!","",messageNoAdditionalProps.getSubject());
        assertEquals("Subject should be an empty string!","",messageWithAdditionalProps.getSubject());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetSubject() throws Exception
    {
        messageWithAdditionalProps.setSubject("Testing");
        assertEquals("getSubject() should return \"Testing\"!","Testing",messageWithAdditionalProps.getSubject());
    }

    /**
     * Tests that constructor not initializes ReplyToAddress.
     * @throws Exception
     */
    public void testGetReplyToAddress() throws Exception
    {
        assertNull("ReplyToAddress should be null!",messageNoAdditionalProps.getReplyToAddress());
        assertNull("ReplyToAddress should be null!",messageWithAdditionalProps.getReplyToAddress());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetReplyToAddress() throws Exception
    {
        messageWithAdditionalProps.setReplyToAddress("Kalle");
        assertEquals("getReplyToAddress() should return \"Kalle\"!","Kalle",messageWithAdditionalProps.getReplyToAddress());
    }

    /**
     * Tests that constructor not initializes DeliveryDate.
     * @throws Exception
     */
    public void testGetDeliveryDate() throws Exception
    {
        assertNull("DeliveryDate should be null!",messageNoAdditionalProps.getDeliveryDate());
        assertNull("DeliveryDate should be null!",messageWithAdditionalProps.getDeliveryDate());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetDeliveryDate() throws Exception
    {
        Date date = new Date();
        messageWithAdditionalProps.setDeliveryDate(date);
        assertEquals("getDeliveryDate() should return "+date,date,messageWithAdditionalProps.getDeliveryDate());
    }

    /**
     * Tests that constructor initializes Language to Loc.
     * @throws Exception
     */
    public void testGetLanguage() throws Exception
    {
        assertEquals("Language should be \""+Locale.ENGLISH.getLanguage()+"\"!", Locale.ENGLISH.getLanguage(),messageNoAdditionalProps.getLanguage());
        assertEquals("Language should be \""+Locale.ENGLISH.getLanguage()+"\"!", Locale.ENGLISH.getLanguage(),messageWithAdditionalProps.getLanguage());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetLanguage() throws Exception
    {
        messageWithAdditionalProps.setLanguage("en");
        assertEquals("getLanguage() should return \"en\"!","en",messageWithAdditionalProps.getLanguage());
    }

    /**
     * Tests that constructor not initializes Type.
     * @throws Exception
     */
    public void testGetType() throws Exception
    {
        assertEquals(MailboxMessageType.EMAIL,messageNoAdditionalProps.getType());
        assertEquals(MailboxMessageType.EMAIL,messageWithAdditionalProps.getType());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetType() throws Exception
    {
        messageWithAdditionalProps.setType(MailboxMessageType.VIDEO);
        assertEquals("getType() should return "+MailboxMessageType.VIDEO+"!",MailboxMessageType.VIDEO,messageWithAdditionalProps.getType());
    }

    /**
     * Tests that constructor initializes Urgent to false.
     * @throws Exception
     */
    public void testGetUrgent() throws Exception
    {
        assertEquals("Urgent should be false!",false,messageNoAdditionalProps.isUrgent());
        assertEquals("Urgent should be false!",false,messageWithAdditionalProps.isUrgent());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetUrgent() throws Exception
    {
        messageWithAdditionalProps.setUrgent(true);
        assertTrue("getUrgent() should return true!",messageWithAdditionalProps.isUrgent());
    }

    /**
     * Tests that constructor initializes Confidential to false.
     * @throws Exception
     */
    public void testGetConfidential() throws Exception
    {
        assertEquals("Confidential should be false!",false,messageNoAdditionalProps.isUrgent());
        assertEquals("Confidential should be false!",false,messageWithAdditionalProps.isUrgent());
    }

    /**
     * Tests that Setter initializes property.
     * @throws Exception
     */
    public void testSetConfidential() throws Exception
    {
        messageWithAdditionalProps.setConfidential(true);
        assertTrue("getConfidential() should return true!",messageWithAdditionalProps.isConfidential());
    }




    /**
     * Tests that getting additional properties with key==null throws an Exception.
     * @throws Exception
     */
    public void testFaultySettingAdditionalProperty() throws Exception
    {
        try {
            messageNoAdditionalProps.setAdditionalProperty(null,"0");
            fail("setAdditionalProperty(null,\"0\") should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            messageWithAdditionalProps.setAdditionalProperty(null,"0");
            fail("setAdditionalProperty(null,\"0\") should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    /**
     * Tests that getting existing additional properties returns initialized value.
     * @throws Exception
     */
    public void testSettingAdditionalProperty() throws Exception
    {
        messageWithAdditionalProps.setAdditionalProperty("add1","111");
        assertEquals("getAdditionalProperty(\"add1\") should return \"111\"","111",messageWithAdditionalProps.getAdditionalProperty("add1"));
        try {
            messageWithAdditionalProps.setAdditionalProperty("add2","Qwerty");
            fail("setAdditionalProperty(\"add2\",<value>) should throw an IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            //OK
        }

    }

    /**
     * Tests that storing message with valid value on property "sender" does NOT throw exception.
     * @throws Exception
     */
    public void testStoreValidPropertySender() throws Exception
    {
        messageNoAdditionalProps.setSender("Hakan Stolt <hakan.stolt@mobeon.com>");
        messageNoAdditionalProps.store();
        messageNoAdditionalProps.store("host");

        messageNoAdditionalProps.setSender("\"Hakan Stolt (123456789)\" <hakan.stolt@mobeon.com>");
        messageNoAdditionalProps.store();
        messageNoAdditionalProps.store("host");

        messageNoAdditionalProps.setSender("Unknown caller <>");
        messageNoAdditionalProps.store();
        messageNoAdditionalProps.store("host");
    }

    /**
     * Tests that storing message with invalid value on property "sender" does throw exception.
     * @throws Exception
     */
    public void testStoreInvalidPropertySender() throws Exception
    {
        messageNoAdditionalProps.setSender("Hakan Stolt (123456789) <hakan.stolt@mobeon.com>");
        try {
            messageNoAdditionalProps.store();
            fail("Storing message with invalid value on property \"sender\" should throw Exception");
        } catch(InvalidMessageException e) {
            assertEquals("Hakan Stolt (123456789) <hakan.stolt@mobeon.com>",e.getInvalidProperties().get("sender"));
        }

        try {
            messageNoAdditionalProps.store("host");
            fail("Storing message with invalid value on property \"sender\" should throw Exception");
        } catch(InvalidMessageException e) {
            assertEquals("Hakan Stolt (123456789) <hakan.stolt@mobeon.com>",e.getInvalidProperties().get("sender"));
        }

    }

    public static Test suite()
    {
        return new TestSuite(BaseStorableMessageTest.class);
    }
}
