package com.abcxyz.services.moip.ntf;

import static org.junit.Assert.assertEquals;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.text.TestUser;
import com.mobeon.ntf.text.TextCreator;

/**
 * 
 * @author lmcjhut
 *
 *   Test class for the Sync SMS Template formats.
 *    
 *   The following formats are tested (they are defined in the en.chpr file):
 *   New Message    :  vvmdep={"//VVM:SYNC:ev=NM;id=" UID ";c=" VCOUNT ";t=v;s=" FROM ";dt=" DATE=dd/MM/yyyy_HH:mm_Z ";l=" SIZE}
 *   Greeting update:  vvmgre={"//VVM:SYNC:ev=GU;c=" VCOUNT ";t=v"}
 *   Message Expired:  vvmexp={"//VVM:SYNC:ev=MBU;c=" VCOUNT ";t=v"}
 *   User Logged out:  vvmlog={"//VVM:SYNC:ev=MBU;c=" VCOUNT ";t=v"}
 *   
 *   NOTE:
 *   This test expects that you have 
 */
public class TemplateTest {

	static NotificationEmail email = null;
	
	// FIXME This test class needs reviewing - it does not initialize properly on Linux
	
//	@BeforeClass
	static public void setUp() throws Exception
	{
		String from = "12345";
		String to = "56789";
		MessageInfo msgInfo;
		MsgBodyPart part = null;
		StateAttributes attributes = new StateAttributes();

		String voicebody =
			"\r\n"
			+ "---559023410-758783491-972026285=:8136\r\n"
			+ "Content-Type: AUDIO/wav\r\n"
			+ "\r\n"
			+ "BODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODYBODY\r\n"
			+ "\r\n"
			+ "---559023410-758783491-972026285=:8136--\r\n";

		String userDir = System.getProperty("user.dir");
		System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
		System.setProperty("ntfHome", userDir + "/instance_template/" );

		CommonMessagingAccessTest.setUp();
        CommonMessagingAccess.setMcd(new McdStub());
		Config.updateCfg();

        /**
         * Create the message. 
         * First the C1 Headers
         */
		String date = "Thu, 06 Mar 2003 15:27:31 +00 GMT";
    	Container1 c1 = new Container1();
    	c1.setFrom(from);
		c1.setTo(to);
		c1.setMsgClass(ServiceName.VOICE);
		c1.setDateTime(date);

        /**
         * Now add the content body and its headers
         */
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		String textMsg = voicebody;
		part = new MsgBodyPart("AUDIO/wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Transfer-Encoding", "BASE64");
		part.addPartHeader("Content-Duration","20");
		part.addPartHeader("Content-Description","Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition","inline; voice=Voice-Message; filename=\"message .wav\"");
		c3Parts[0] =  part;

		attributes.setAttribute("uid", "55555");
        /**
         * Now store the test message
         */
		msgInfo = CommonMessagingAccess.getInstance().storeMessageTest(c1, new Container2(), c3Parts, attributes);
    	
        /**
         * Now create a NotificationEmail for this message
         */
    	NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, null);
    	email = new NotificationEmail(ntfEvent);
    	email.init();
	}

//    @AfterClass
    static public void tearDown()
    {
    	CommonMessagingAccessTest.stop();
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    /**
     * Test the SYNC message format for the new message event (vvmdep)
     */
    public void TestSyncNewMessage()throws Exception {

    	String expectedText = "//VVM:SYNC:ev=NM;id=55555;c=-1;t=v;s=tel:+12345;dt=06/03/2003 10:27 -0500;l=20";
		TextCreator tc = TextCreator.get();
    	String text = tc.generateText(null, email, new TestUser(), "vvmdep", true, null);
    	assertEquals("Did not get expected output string.", text, expectedText);
    }
    
    @Ignore("Ignored until class initialization is fixed")
    @Test
    /**
     * Test the SYNC message format for the new message event (vvmgre)
     */
    public void TestSyncGreeting()throws Exception {

    	String expectedText = "//VVM:SYNC:ev=GU;c=-1;t=v";
		TextCreator tc = TextCreator.get();
    	String text = tc.generateText(null, email, new TestUser(), "vvmgre", true, null);
    	assertEquals("Did not get expected output string.", text, expectedText);
    }
    
    @Ignore("Ignored until class initialization is fixed")
    @Test
    /**
     * Test the SYNC message format for the new message event (vvmexp)
     */
    public void TestSyncExpiry()throws Exception {

    	String expectedText = "//VVM:SYNC:ev=MBU;c=-1;t=v";
		TextCreator tc = TextCreator.get();
    	String text = tc.generateText(null, email, new TestUser(), "vvmexp", true, null);
    	assertEquals("Did not get expected output string.", text, expectedText);
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    /**
     * Test the SYNC message format for the new message event (vvmlog)
     */
    public void TestSyncLogout()throws Exception {

    	String expectedText = "//VVM:SYNC:ev=MBU;c=-1;t=v";
		TextCreator tc = TextCreator.get();
    	String text = tc.generateText(null, email, new TestUser(), "vvmlog", true, null);
    	assertEquals("Did not get expected output string.", text, expectedText);
    }
}
