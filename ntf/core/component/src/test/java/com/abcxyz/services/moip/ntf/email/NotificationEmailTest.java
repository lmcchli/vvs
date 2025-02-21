package com.abcxyz.services.moip.ntf.email;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import jakarta.mail.internet.MailDateFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.data.SipDate;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.message.MsgPartHeader;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.util.NtfUtil;


public class NotificationEmailTest {

	private static CommonMessagingAccess commonMessagingAccess =null;
	private String from = "12345";
	private String to = "56789";
	private String cc = "91011";
	private String bcc1 = "11111";
	private String bcc2 = "33333";
	private MessageInfo msgInfo;

	static String header;
	static String textheader;
	static String textbody;
	static String voiceheader;
	static String voicebody;
	static String voicebody1;
	static String externalbody;



	@BeforeClass
	static public void setUp() throws Exception
	{

		String userDir = System.getProperty("user.dir");
		System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
		System.setProperty("ntfHome", userDir + "/test/junit/" );

		CommonMessagingAccessTest.setUp();
		commonMessagingAccess = CommonMessagingAccess.getInstance();

		header=
			"Date: Thu, 06 Mar 2003 15:27:31 +0100 (MET)\r\n"
			+ "From: +5143445678\r\n"
			+ "To: +51142345678\r\n"
			+ "Subject: voice message.\r\n"
			+ "MIME-version: 1.0\r\n";


		textheader =
			"Subject: testemail\r\n"
			+ "Content-type:text/plain\r\n";

		textbody =
			"\r\nbody\r\n";

		voiceheader =
			"Content-type: MULTIPART/Voice-Message; boundary=\"-559023410-758783491-972026285=:8136\"; Version=2.0\r\n";
		externalbody =
			"\r\n"
			+ "---559023410-758783491-972026285=:8136\r\n"
			+ "Content-Type: message/external-body; access-type=local-file; name=\"mybody\"\r\n"
			+ "Content-Transfer-Encoding: 7bit\r\n"
			+ "---559023410-758783491-972026285=:8136--\r\n";

		voicebody =
			"\r\n"
			+ "---559023410-758783491-972026285=:8136\r\n"
			+ "Content-Type: AUDIO/wav\r\n"
			+ "Content-Transfer-Encoding: BASE64\r\n"
			+ "Content-Description: Cisco voice Message   (20 seconds )\r\n"
			+ "Content-Disposition: inline; voice=Voice-Message; filename=\"message .wav\"\r\n"
			+ "\r\n"
			+ "BODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODY\r\n"
			+ "\r\n"
			+ "---559023410-758783491-972026285=:8136--\r\n";
		voicebody1 =
			"\r\n"
			+ "---559023410-758783491-972026285=:8136\r\n"
			+ "Content-Type: text/plain\r\n"
			+ "Content-Transfer-Encoding: BASE64\r\n"
			+ "Content-Description: Cisco voice Message   (20 seconds )\r\n"
			+ "Content-Disposition: inline; voice=Voice-Message; filename=\"message .wav\"\r\n"
			+ "\r\n"
			+ "BODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODY\r\n"
			+ "\r\n"
			+ "---559023410-758783491-972026285=:8136--\r\n";
	}


    @AfterClass
    static public void tearDown()
    {
    	CommonMessagingAccessTest.stop();
    }



    @Test
    public void TestGetAllReceivers() throws Exception
    {
    	Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setBcc(bcc1);
		c1.setBcc(bcc2);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);

		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
    	email.init();

    	//String [] result = createArray(to, cc, bcc1, bcc2);

    	//assertArrayEquals(result, email.getAllReceivers());
    }

    @Test
    public void TestGetHeaders() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setBcc(bcc1);
		c1.setBcc(bcc2);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);

		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	String[] headers = {"to", "cc", "bcc"};
    	String [] result = email.getHeaders(headers);

    	assertEquals(result[0], "tel:+1" + to);
    	assertEquals(result[1], "tel:+1" + cc);
    	assertEquals(result[2], "tel:+" + bcc1 + "," + "tel:+1" + bcc2);
    	//assertArrayEquals(result, email.getHeaders(headers));
    }

    @Test
    public void TestGetAllReceiversWithMultipleCc() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		cc = "44444;66666";
		c1.setCc(cc);
		c1.setBcc(bcc1);
		c1.setBcc(bcc2);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);

		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	//String [] result = createArray(to, cc, bcc1, bcc2);

    	//assertArrayEquals(result, email.getAllReceivers());
    }

    @Test
    public void TestGetAllReceiversWithMultipleBcc() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		bcc1 = "7777;8888";
		c1.setCc(cc);
		c1.setBcc(bcc1);
		c1.setBcc(bcc2);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);

		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	//String [] result = createArray(to, cc, bcc1, bcc2);

    	//assertArrayEquals(result, email.getAllReceivers());
    }

    @Test
    public void TestGetAllReceiversWithOnlyOneReceiver() throws Exception
    {
    	//String from = "caesar@host.domain";
    	String to = "22222";
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);

		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	/*HUZH check this test case
    	 * String [] receivers = email.getAllReceivers();

    	for (int j=0; j<receivers.length; j++)
    	{
    		assertEquals(to, receivers[j]);
    	}*/
    }

    @Test
    public void TestGetSubject() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		String subject = "ipms/message";
		c1.setSubject(subject);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals(subject, email.getSubject());
    }

    @Test
    public void TestIsUrgent() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.appendHeaderValue("Priority", "0");

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertTrue(email.isUrgent());
    }

    @Test
    public void TestIsNotUrgent() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.appendHeaderValue("Priority", "2");

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertFalse(email.isUrgent());
    }

    @Test
    public void TestGetMessageReceivedDate() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		SipDate sipDate = new SipDate();
		String date = sipDate.getDate();
		c1.setDateTime(date);
		Date expectedDate = NtfUtil.stringToDate(date);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	Date actualDate = email.getMessageReceivedDate();

    	assertEquals(expectedDate, actualDate);
    }

    @Test
    public void TestMailDateToDate() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		SipDate sipDate = new SipDate();
		String date = sipDate.getDate();
		c1.setDateTime(date);
		MailDateFormat mdf = new MailDateFormat();
		Date expectedDate = mdf.parse(date);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	new NotificationEmail(ntfEvent);
    	Date actualDate = NotificationEmail.mailDateToDate(date);

    	assertEquals(expectedDate, actualDate);
    }

    @Test
    public void TestGetMessageText() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String expectedText = "Please call me back!!!";

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart(MsgBodyPart.TEXT_PLAIN, expectedText.getBytes(), true);
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	String actualText = email.getMessageText();

    	assertEquals(expectedText, actualText);
    }

    @Test
    public void TestIsSlamdown() throws Exception
    {
    	Container1 c1 = new Container1();
    	c1.setFrom(from);
    	c1.setTo(to);
    	c1.setCc(cc);

    	String textMsg = "Please call me back!!!";

    	MsgBodyPart[] c3Parts = new MsgBodyPart[2];
    	c3Parts[0] =  new MsgBodyPart(MsgBodyPart.TEXT_PLAIN, textMsg.getBytes(), true);
    	c3Parts[1] =  new MsgBodyPart();

    	StateAttributes attributes = new StateAttributes();

    	msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
    	assertNotNull(msgInfo);

    	Properties eventProperties = new Properties();
    	eventProperties.put("id", "msgid");
    	eventProperties.put("to", "5143457900");
    	NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.SLAMDOWN.getName(), msgInfo, eventProperties, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
    	email.init();

    	assertTrue(email.isSlamdown());


    }

    @Test
    public void TestGetSender() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		Properties eventProperties = new Properties();
    	eventProperties.put("id", "msgid");
    	eventProperties.put("to", "5143457900");
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, eventProperties, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals("tel:+12345" , email.getSender());
    }

    @Test
    public void TestisMwiOff() throws Exception
    {
    	Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String textMsg = "Please call me back!!!";

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart(MsgBodyPart.TEXT_PLAIN, textMsg.getBytes(), true);
		c3Parts[1] =  new MsgBodyPart();

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		Properties eventProperties = new Properties();
    	eventProperties.put("id", "msgid");
    	eventProperties.put("to", "5143457900");
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.MWI_OFF.getName(), msgInfo, eventProperties, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertTrue(email.isMwiOff());
    }


    @Test
    public void TestGetEmailTypeWithVoice() throws Exception
    {
    	Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setMsgClass(ServiceName.VOICE);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateFile state = new StateFile(new MessageInfo());
		//state.setDestMsgClass(ServiceName.VOICE);

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, state.getAttributes());
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals(Constants.NTF_VOICE, email.getEmailType());
    }

    @Test
    public void TestGetEmailTypeWithVideo() throws Exception
    {
    	Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setMsgClass(ServiceName.VIDEO);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];
		c3Parts[0] =  new MsgBodyPart();
		c3Parts[1] =  new MsgBodyPart();

		StateFile state = new StateFile(new MessageInfo());
		//state.setDestMsgClass(ServiceName.VIDEO);

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, state.getAttributes());
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals(Constants.NTF_VIDEO, email.getEmailType());
    }

	private String [] createArray(String to, String cc, String bcc1, String bcc2) {
		StringBuffer sB = new StringBuffer();
    	sB.append(to);
    	sB.append(";");
    	sB.append(cc);
    	sB.append(";");
    	sB.append(bcc1);
    	sB.append(";");
    	sB.append(bcc2);
    	String s = new String(sB);
    	return s.split(";");
	}

	@Test
    public void testGetMVASMessageLengthNoContentDuration() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setMsgClass(ServiceName.VOICE);

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		c3Parts[0] =  part;

		StateFile state = new StateFile(new MessageInfo());
		//state.setDestMsgClass(ServiceName.VOICE);

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, state.getAttributes());
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals("20", email.getMessageLength());
    }

	@Test
    public void testGetMVASMessageLengthWithContentDuration() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setMsgClass(ServiceName.VOICE);

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");
		part.addPartHeader("Content-Duration", "3");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateFile state = new StateFile(new MessageInfo());
		//state.setDestMsgClass(ServiceName.VOICE);

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, state.getAttributes());
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals("3", email.getMessageLength());
    }

	@Test
    public void testGetMVASMessageLengthWithNoContentTypes() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setMsgClass(ServiceName.VOICE);

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateFile state = new StateFile(new MessageInfo());
		//state.setDestMsgClass(ServiceName.VOICE);

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, state.getAttributes());
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals("?", email.getMessageLength());
    }


	@Test
    public void testGetMessageSizeInBytes() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertNotNull(email.getMessageSizeInBytes());

    }

	@Test
    public void TestGetNoOfAttachmentsWhenOneAttachement() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		MsgPartHeader nvps1 = new MsgPartHeader();
        nvps1.setValue("external1_header1", "value1");
		MsgBodyPart part = new MsgBodyPart("filename", nvps1, null,true);

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals(1, email.getNoOfAttachments());

    }

	@Test
    public void TestGetNoOfAttachmentsWhenTwoAttachements() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];

		MsgPartHeader nvps1 = new MsgPartHeader();
        nvps1.setValue("external1_header1", "value1");
		MsgBodyPart part1 = new MsgBodyPart("filename1", nvps1, null,false);

		c3Parts[0] =  part1;

		MsgPartHeader nvps2 = new MsgPartHeader();
        nvps2.setValue("external1_header2", "value2");
		MsgBodyPart part2 = new MsgBodyPart("filename2", nvps2, null,true);


		c3Parts[1] = part2;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals(2, email.getNoOfAttachments());

    }

	@Test
    public void TestGetBodyPart() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		MsgBodyPart[] c3Parts = new MsgBodyPart[2];

		MsgPartHeader nvps1 = new MsgPartHeader();
        nvps1.setValue("external1_header1", "value1");
		MsgBodyPart part1 = new MsgBodyPart("filename1", nvps1, null,false);

		c3Parts[0] =  part1;

		MsgPartHeader nvps2 = new MsgPartHeader();
        nvps2.setValue("external1_header2", "value2");
		MsgBodyPart part2 = new MsgBodyPart("filename2", nvps2, null,true);


		c3Parts[1] = part2;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertNotNull(email.getBodyPart(1));

    	assertEquals("filename2", email.getBodyPart(1).getExternalFileName());

    }

	@Test
    public void TestGetVoiceMessageContentType() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals( "AUDIO/wav", email.getVoiceMessageContentType());

    }

	@Test
    public void TestGetBodyParameter() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String s = "Author=N. Freed, A. Cargille";
		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("text/plain", textMsg.getBytes(), true);
		part.setContent(s.getBytes());
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertEquals( "N. Freed, A. Cargille", email.getBodyParameter("Author"));

    }

	@Test
    public void TestGetVoiceAttachmentPart() throws Exception
    {
		Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);

		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

    	assertNotNull( email.getVoiceAttachmentPart());

    }

	@Test
	public void TestGetMimeMessage() throws Exception
	{
		Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setCc(cc);
		c1.setSubject("This is a voice message");
		String textMsg = header + voiceheader + voicebody;
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;

		StateAttributes attributes = new StateAttributes();

		msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);
		assertNotNull(msgInfo);
		NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
		NotificationEmail email = new NotificationEmail(ntfEvent);
        email.init();

		jakarta.mail.internet.MimeMessage msg = email.getMimeMessage();
		/*Enumeration<String> e = msg.getAllHeaderLines();
    	while (e.hasMoreElements()) {
    		  String element = e.nextElement();
    		  System.out.println(element);

    	}
    	MimeMultipart m = (MimeMultipart)msg.getContent();
    	if ( m instanceof MimeMultipart)
    		System.out.println("true");
    	System.out.println(m.getCount());
    	Enumeration<Header> en = m.getBodyPart(0).getAllHeaders();

    	while (en.hasMoreElements()) {
  		  Header element = en.nextElement();
  		  System.out.println(element.getName());
  		System.out.println(element.getValue());
  	}*/

		assertNotNull( msg );

	}


}
