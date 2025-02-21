package com.mobeon.masp.mailbox.mfs;


import java.text.ParseException;
import java.util.Date;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import junit.framework.TestCase;

import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

public class MfsStorableMessageTest extends TestCase {
	
	static {
		try {
			CommonTestingSetup.setup();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (ConfigurationDataException e) {
			e.printStackTrace();
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStoreWork() {
		MfsContext context = new MfsContext(null);
		MfsMock mfs = new MfsMock();
		MfsStorableMessage message = new MfsStorableMessage(context, mfs);
		
		String sender = "sender <testStoreWork@unittest.org>";
		String recipient = "recipient <unknown@unittest.org>";
		String secondaryRecipient = "secondary recipient <unknown2@unnittest.org>";
		String subject = "testStoreWork unit test";
		String replyTo = "unknown@unittest.org";
		String lang = "en";
		String contentDescription = "contentdescription";
		String contentDescription2 = "spokennamedescription";
		String contentLanguage = "messagelanguage";
		String spokenLanguage = "spokennamelanguage";
		
		// The following fields are mapped to Container 1
		message.setType(MailboxMessageType.VOICE);
		message.setSender(sender);
		message.addRecipient(recipient);
		message.addSecondaryRecipient(secondaryRecipient);
		message.setSubject(subject);
		message.setUrgent(false);
		
		// The following fields are mapped to Container 2
		message.setReplyToAddress(replyTo);
		message.setLanguage(lang);
		message.setConfidential(false);
		
		// The following fields are mapped to Container 3
		try {
	        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
	        IMediaObject messageContent = mediaObjectFactory.create(
	                "messagecontent",
	                new MediaProperties(
	                        new MimeType("audio", "wav"),
	                        "extension",
	                        10,
	                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 3000)
	                )
	        );
	        message.addContent(messageContent, 
	        		new MessageContentProperties(
	        				"contentfilename", 
	        				contentDescription,
	        				contentLanguage,null));

	        IMediaObject spokenName = mediaObjectFactory.create(
	                "spokennamecontent",
	                new MediaProperties(
	                        new MimeType("audio", "wav"),
	                        "extension",
	                        10,
	                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 1000)
	                )
	        );
	        message.setSpokenNameOfSender(spokenName, 
	        		new MessageContentProperties(
	        				"spokennamefilename", 
	        				contentDescription2, 
	        				spokenLanguage,null));
	        
		} catch (MediaObjectException e) {
			fail("Fail to create media object");
		} catch (MimeTypeParseException e) {
			fail("Fail to create mime object");
		}


		// Call the tested method
		try {
			message.store();
		} catch (MailboxException ex) {
			fail("Unexpected Exception Caught while storing message");
		}
		
		// Validate container 1
		try {
			Container1 c1 = mfs.getContainer1();
			assertEquals(ServiceName.VOICE, c1.getMsgClass());
			assertEquals(sender, c1.getFrom());
			assertEquals(recipient, c1.getTo());
			assertEquals(secondaryRecipient, c1.getCc());
			assertEquals(subject, c1.getSubject());
			assertEquals(Constants.MFS_NONPRIVATE, c1.getPrivacy());
			Date c1Date = MfsUtil.DateFormatter.parse(c1.getDateTime());
			Date currentDate = new Date();
			assertTrue(currentDate.after(c1Date));
		} catch (CodingFailureException ex) {
			fail("Unexpected exception caught while validating MFS Container 1");
		} catch (ParseException e) {
			fail("Unexpected exception caught while processing date: " + e.getMessage());
		}
		
		// Validate container 2
		Container2 c2 = mfs.getContainer2();
		assertEquals(replyTo, c2.getMsgHeader(MoipMessageEntities.REPLY_TO_HEADER));
		assertEquals(lang, c2.getMsgHeader(MoipMessageEntities.LANGUAGE_HEADER));
		
		// Validate container 3 parts
		MsgBodyPart[] parts = mfs.getContainer3Parts();
		assertTrue(parts.length == 2);
		
		assertEquals("audio/wav", parts[0].getContentType());
		assertEquals(contentDescription, parts[0].getPartHeader(Constants.CONTENT_DESCRIPTION));
		assertEquals("3", parts[0].getPartHeader(Constants.CONTENT_DURATION));
		assertEquals(contentLanguage, parts[0].getPartHeader(Constants.CONTENT_LANGUAGE));
		String contentDisposition = parts[0].getPartHeader(Constants.CONTENT_DISPOSITION);
		assertTrue(contentDisposition.contains("inline"));
		assertTrue(contentDisposition.contains("filename=contentfilename.extension"));
		assertTrue(contentDisposition.contains("voice=Voice-Message"));

	
		assertEquals("audio/wav", parts[1].getContentType());
		assertEquals(contentDescription2, parts[1].getPartHeader(Constants.CONTENT_DESCRIPTION));
		assertEquals("1", parts[1].getPartHeader(Constants.CONTENT_DURATION));
		assertEquals(spokenLanguage, parts[1].getPartHeader(Constants.CONTENT_LANGUAGE));
		contentDisposition = parts[1].getPartHeader(Constants.CONTENT_DISPOSITION);
		assertTrue(contentDisposition.contains("inline"));
		assertTrue(contentDisposition.contains("filename=spokennamefilename.extension"));
		assertTrue(contentDisposition.contains("voice=Originator-Spoken-Name"));
		
		// Validate state attribute
		StateAttributes attributes = mfs.getStateAttributes();
		String messageState = attributes.getAttribute(StateAttributes.GLOBAL_MSG_STATE);
		assertEquals(MfsUtil.toMfsMessageState(StoredMessageState.NEW), messageState);
	}
}
