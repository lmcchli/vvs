/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.test.MailtestUtil;
import com.mobeon.ntf.test.NtfTestCase;
import java.util.*;
import junit.framework.*;

import jakarta.mail.Flags;

/**
 * This class tests UserMailbox.
 */
public class UserMailboxTest extends NtfTestCase {

    UserMailbox mc;
    boolean res = true;

    public UserMailboxTest(String name) {
    super(name);
    }


    protected void setUp() {
        MailtestUtil.dropFolder("junit01", "abcd", "Fax");
        MailtestUtil.dropFolder("junit01", "abcd", "Voice");
        MailtestUtil.dropFolder("junit01", "abcd", "Video");
    }

    private void sendNormalMails(String user, int mail, int fax, int voice, int video) {
        int i;
        MailtestUtil.emptyMailbox(user, "abcd");
        for (i= 0; i < mail; i++) { MailtestUtil.sendMail(user, "mail.eml"); }
        for (i= 0; i < fax; i++) { MailtestUtil.sendMail(user, "fax.eml"); }
        for (i= 0; i < voice; i++) { MailtestUtil.sendMail(user, "voice.eml"); }
        for (i= 0; i < video; i++) { MailtestUtil.sendMail(user, "video.eml"); }
    }

    public void test() throws Exception {
    l("test");
        sendNormalMails("junit01", 1, 2, 4, 1);
        Thread.sleep(5000); //Give the mails some time to arrive
    mc= new UserMailbox("junit01", true, true, true, false);
    assertEquals(7, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(2, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        //	assertTrue(mc.isQuotaExceeded());

    mc= new UserMailbox("junit01", true, false, true, true);
    assertEquals(6, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(0, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", false, true, true, false);
    assertEquals(6, mc.getNewTotalCount());
    assertEquals(0, mc.getNewEmailCount());
    assertEquals(2, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", true, true, false, true);
    assertEquals(4, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(2, mc.getNewFaxCount());
    assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", false, false, true, false);
    assertEquals(4, mc.getNewTotalCount());
    assertEquals(0, mc.getNewEmailCount());
    assertEquals(0, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", false, true, false, true);
    assertEquals(3, mc.getNewTotalCount());
    assertEquals(0, mc.getNewEmailCount());
    assertEquals(2, mc.getNewFaxCount());
    assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", true, false, false, false);
    assertEquals(1, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(0, mc.getNewFaxCount());
    assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

    mc= new UserMailbox("junit01", false, false, false, false);
    assertEquals(0, mc.getNewTotalCount());
    assertEquals(0, mc.getNewEmailCount());
    assertEquals(0, mc.getNewFaxCount());
    assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

        MailtestUtil.flagMessages("junit01", "abcd", 1, 2, new jakarta.mail.Flags("saved"));
    mc= new UserMailbox("junit01", true, true, true, false);
    assertEquals(5, mc.getNewTotalCount());
    }

    public void testDeep() throws Exception {
    l("testDeep");
        sendNormalMails("junit01", 1, 2, 4, 3);
        MailtestUtil.sendMail("junit01", "fax_failed_01.eml");
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, true);
    assertEquals(11, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(3, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(3, mc.getNewVideoCount());
        MailtestUtil.sendMail("junit01", "fax_failed_02.eml");
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, false);
    assertEquals(8, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(3, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        MailtestUtil.sendMail("junit01", "fax_failed_03.eml");
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, false);
    assertEquals(8, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(3, mc.getNewFaxCount());
    assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        MailtestUtil.sendMail("junit01", "voice_failed.eml");
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, false);
    assertEquals(9, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(3, mc.getNewFaxCount());
    assertEquals(5, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        MailtestUtil.sendMail("junit01", "video_failed.eml");
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, true);
    assertEquals(13, mc.getNewTotalCount());
    assertEquals(1, mc.getNewEmailCount());
    assertEquals(3, mc.getNewFaxCount());
    assertEquals(5, mc.getNewVoiceCount());
        assertEquals(4, mc.getNewVideoCount());
    }

    public void testMultiple() throws Exception {
        l("testMultiple");
        Config.setDefaultValue("newmailfolders", "multiple");
        MailtestUtil.createFolder("junit01", "abcd", "Voice");
        MailtestUtil.createFolder("junit01", "abcd", "Fax");
        MailtestUtil.createFolder("junit01", "abcd", "Video");
        MailtestUtil.emptyMailbox("junit01", "abcd");
        sendNormalMails("junit01", 1, 2, 4, 1);
        Thread.sleep(5000); //Give the mails some time to arrive
        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(8, mc.getNewTotalCount());
        MailtestUtil.moveMessages("inbox", "Fax", 2, 2, "junit01", "abcd");
        MailtestUtil.moveMessages("inbox", "Voice", 2, 4, "junit01", "abcd");
        MailtestUtil.moveMessages("inbox", "Video", 2, 1, "junit01", "abcd");

        mc= new UserMailbox("junit01", true, true, true, false);
	assertEquals(7, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        //	assertTrue(mc.isQuotaExceeded());

	mc= new UserMailbox("junit01", true, false, true, true);
	assertEquals(6, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, true, true, false);
	assertEquals(6, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", true, true, false, true);
	assertEquals(4, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, false, true, false);
	assertEquals(4, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, true, false, true);
	assertEquals(3, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", true, false, false, false);
	assertEquals(1, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, false, false, false);
	assertEquals(0, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());


    }

    public void testUnknown() throws Exception {
        l("testUnknown");
        Config.setDefaultValue("newmailfolders", "unknown");
        MailtestUtil.createFolder("junit01", "abcd", "Voice");
        MailtestUtil.createFolder("junit01", "abcd", "Fax");
        MailtestUtil.createFolder("junit01", "abcd", "Video");
        MailtestUtil.emptyMailbox("junit01", "abcd");
        sendNormalMails("junit01", 1, 2, 4, 1);
        Thread.sleep(5000); //Give the mails some time to arrive
        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(8, mc.getNewTotalCount());
        MailtestUtil.moveMessages("inbox", "Fax", 2, 2, "junit01", "abcd");
        MailtestUtil.moveMessages("inbox", "Voice", 2, 4, "junit01", "abcd");
        MailtestUtil.moveMessages("inbox", "Video", 2, 1, "junit01", "abcd");

        mc= new UserMailbox("junit01", true, true, true, false);
	assertEquals(7, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        //	assertTrue(mc.isQuotaExceeded());

	mc= new UserMailbox("junit01", true, false, true, true);
	assertEquals(6, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, true, true, false);
	assertEquals(6, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", true, true, false, true);
	assertEquals(4, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, false, true, false);
	assertEquals(4, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(4, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, true, false, true);
	assertEquals(3, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(2, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", true, false, false, false);
	assertEquals(1, mc.getNewTotalCount());
	assertEquals(1, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());

	mc= new UserMailbox("junit01", false, false, false, false);
	assertEquals(0, mc.getNewTotalCount());
	assertEquals(0, mc.getNewEmailCount());
	assertEquals(0, mc.getNewFaxCount());
	assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());


    }

    public void testMultiCount() throws Exception {
        l("testMultiCount");

        assertTrue(getMultiCount());
    }

    private boolean getMultiCount() throws Exception {
        final int expected = 6;
        sendNormalMails("junit01", 0, 0, expected, 0);
        Thread.sleep(5000); //Give the mails some time to arrive
	mc= new UserMailbox("junit01", true, true, true, true);
	for( int i=0;i<6;i++ ) {
            new Thread() {
                public void run() {
                    int c = mc.getNewVoiceCount();
                    System.out.println(c);
                    if( c != expected ) {
                        res = false;
                    }

                }
            }.start();
        }
        //Thread.sleep(50);
        /*
        if( mc.getNewVoiceCount() != expected ) {
            res = false;
        }
         **/
        Thread.sleep(500);
        return res;
    }

    public void testSee() throws Exception {
        l("testSee");
        sendNormalMails("junit01", 0, 0, 4, 0);
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, true);
        String id = mc.getMessageId("2");
        assertNotNull(id);
        assertEquals(4, mc.getNewTotalCount());
        mc.seeMessage(id);
    mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(3, mc.getNewTotalCount());
    }

    public void testArrived() throws Exception {
        l("testArrived");
        sendNormalMails("junit01", 0, 0, 4, 0);
        Thread.sleep(5000); //Give the mail some time to arrive
    mc= new UserMailbox("junit01", true, true, true, true);
        String id = mc.getMessageId("2");
        assertNotNull(id);
        assertTrue(mc.hasMessageArrived(id));
        id = id.substring(1, 4) + "zz" + id.substring(6);
        assertFalse(mc.hasMessageArrived(id));
    }

    public void testLoginFailure() throws Exception {
        l("testloginfailure");

        Config.setCfgVar("imaprootpassword", "faulty");
        sendNormalMails("junit01", 0, 0, 1, 0);
        mc= new UserMailbox("junit01", true, true, true, true);
        mc.getNewTotalCount();
        assertFalse(mc.isConnected());


        Config.updateCfg();
    }

    public void testGetEmail() throws Exception {
        l("testGetEmail");
        String mail = "Return-path: <enikfyh@mobeon.com>\r\n" +
                "Received: from brage.mobeon.com (brage.mobeon.com [150.132.5.213])\r\n" +
                " by jawa.lab.mobeon.com\r\n" +
                " (iPlanet Messaging Server 5.2 HotFix 2.11 (built Mar 22 2006))\r\n" +
                " with ESMTP id <0JLZ008SZQG47J@jawa.lab.mobeon.com> for\r\n" +
                " niklasf1@lab.mobeon.com; Mon, 30 Jul 2007 13:55:16 +0200 (MEST)\r\n" +
                "Date: Mon, 30 Jul 2007 14:00:30 +0200\r\n" +
                "From: enikfyh@mobeon.com (enikfyh)\r\n" +
                "Subject: voice message 6-30-14-0-30\r\n" +
                "To: junit01@lab.mobeon.com\r\n" +
                "Message-id: <0JLZ008T0QG47J@jawa.lab.mobeon.com>\r\n" +
                "MIME-version: 1.0\r\n" +
                "Content-type: multipart/voice-message;\r\n" +
                " BOUNDARY=\"-222222222-222222222-2222222222=:22222\"\r\n" +
                "Original-recipient: rfc822;niklasf1@lab.mobeon.com\r\n" +
                "\r\n" +
                "---222222222-222222222-2222222222=:22222\r\n" +
                "Content-Type: AUDIO/wav\r\n" +
                "Content-Transfer-Encoding: BASE64\r\n" +
                "Content-Description: Abcxyz voice Message\r\n" +
                "Content-Disposition: inline; voice=Voice-Message; filename=\"message.wav\"\r\n" +
                "\r\n" +
                "UklGRhRjAgBXQVZFZm10IBAAAAAHAAEAQB8AAEAfAAABAAgAZGF0YfBiAgBf\r\n" +
                " bHZ1e/jr5u30cm7u7+rvaXd8cfJ7c+74fvp5fu5+aGxwb/ns7vB0b/rt5e52\r\n" +
                "\r\n" +
                "---222222222-222222222-2222222222=:22222--\r\n";

        String mail2 = "Return-path: <enikfyh@mobeon.com>\r\n" +
                "Received: from brage.mobeon.com (brage.mobeon.com [150.132.5.213])\r\n" +
                " by jawa.lab.mobeon.com\r\n" +
                " (iPlanet Messaging Server 5.2 HotFix 2.11 (built Mar 22 2006))\r\n" +
                " with ESMTP id <0JLZ008SZQG47J@jawa.lab.mobeon.com> for\r\n" +
                " niklasf1@lab.mobeon.com; Mon, 30 Jul 2007 13:55:16 +0200 (MEST)\r\n" +
                "Date: Mon, 30 Jul 2007 14:00:30 +0200\r\n" +
                "From: mail2@mobeon.com (mail2)\r\n" +
                "Subject: voice message 6-30-14-0-30\r\n" +
                "To: junit01@lab.mobeon.com\r\n" +
                "Message-id: <0JLZ008T0QG47J@jawa.lab.mobeon.com>\r\n" +
                "MIME-version: 1.0\r\n" +
                "Content-type: multipart/voice-message;\r\n" +
                " BOUNDARY=\"-222222222-222222222-2222222222=:22222\"\r\n" +
                "Original-recipient: rfc822;niklasf1@lab.mobeon.com\r\n" +
                "\r\n" +
                "---222222222-222222222-2222222222=:22222\r\n" +
                "Content-Type: AUDIO/wav\r\n" +
                "Content-Transfer-Encoding: BASE64\r\n" +
                "Content-Description: Abcxyz voice Message\r\n" +
                "Content-Disposition: inline; voice=Voice-Message; filename=\"message.wav\"\r\n" +
                "\r\n" +
                "UklGRhRjAgBXQVZFZm10IBAAAAAHAAEAQB8AAEAfAAABAAgAZGF0YfBiAgBf\r\n" +
                " bHZ1e/jr5u30cm7u7+rvaXd8cfJ7c+74fvp5fu5+aGxwb/ns7vB0b/rt5e52\r\n" +
                "\r\n" +
                "---222222222-222222222-2222222222=:22222--\r\n";

        MailtestUtil.emptyMailbox("junit01", "abcd");
        MailtestUtil.sendMailFromString("junit01", mail);
        Thread.sleep(1000); //Give the mail some time to arrive
        mc= new UserMailbox("junit01", true, true, true, true);

        assertEquals(1, mc.getNewTotalCount());
        NotificationEmail email = mc.getFirstUnseenMail();
        assertNotNull(email);
        assertNotNull( email.getSender() );

        String[] recievers = email.getAllReceivers();
        assertNotNull(recievers);
        assertEquals(1, recievers.length);
        assertEquals("junit01@lab.mobeon.com", recievers[0]);

        assertEquals("voice message 6-30-14-0-30", email.getSubject());
        assertEquals("Mon, 30 Jul 2007 14:00:30 +0200", email.getMessageDate());
        assertEquals("enikfyh@mobeon.com", email.getSender());

        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(1, mc.getNewTotalCount());

        MailtestUtil.sendMailFromString("junit01", mail);
        MailtestUtil.sendMailFromString("junit01", mail2);

        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(3, mc.getNewTotalCount());
        email = mc.getFirstUnseenMail();
        assertEquals("enikfyh@mobeon.com", email.getSender());


        // there should be 2 mail and 1 mail2 in the inbox now. Flag the 2 mail as seen.
        MailtestUtil.flagMessages("junit01", "abcd", 1, 2, new Flags(jakarta.mail.Flags.Flag.SEEN));
        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(1, mc.getNewTotalCount());
        email = mc.getFirstUnseenMail();
        assertEquals("mail2@mobeon.com", email.getSender());
    }

    public void testOldMessages() throws Exception {
        sendNormalMails("junit01", 5, 4, 3, 2);
        Thread.sleep(5000);
        MailtestUtil.flagMessages("junit01", "abcd", 1, 3, new jakarta.mail.Flags(jakarta.mail.Flags.Flag.SEEN));
        MailtestUtil.flagMessages("junit01", "abcd", 6, 1, new jakarta.mail.Flags(jakarta.mail.Flags.Flag.SEEN));
        MailtestUtil.flagMessages("junit01", "abcd", 10, 2, new jakarta.mail.Flags(jakarta.mail.Flags.Flag.SEEN));
        MailtestUtil.flagMessages("junit01", "abcd", 13, 1, new jakarta.mail.Flags(jakarta.mail.Flags.Flag.SEEN));
        // should be new = 2, 3, 1, 1  - old = 3, 1, 2, 1

        mc= new UserMailbox("junit01", true, true, true, true);
        assertEquals(7, mc.getNewTotalCount());
	    assertEquals(2, mc.getNewEmailCount());
	    assertEquals(3, mc.getNewFaxCount());
	    assertEquals(1, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());
        assertEquals(7, mc.getOldTotalCount());
	    assertEquals(3, mc.getOldEmailCount());
	    assertEquals(1, mc.getOldFaxCount());
	    assertEquals(2, mc.getOldVoiceCount());
        assertEquals(1, mc.getOldVideoCount());

        mc= new UserMailbox("junit01", true, true, false, false);
        assertEquals(5, mc.getNewTotalCount());
	    assertEquals(2, mc.getNewEmailCount());
	    assertEquals(3, mc.getNewFaxCount());
	    assertEquals(0, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        assertEquals(4, mc.getOldTotalCount());
	    assertEquals(3, mc.getOldEmailCount());
	    assertEquals(1, mc.getOldFaxCount());
	    assertEquals(0, mc.getOldVoiceCount());
        assertEquals(0, mc.getOldVideoCount());

        mc= new UserMailbox("junit01", false, true, true, true);
        assertEquals(5, mc.getNewTotalCount());
	    assertEquals(0, mc.getNewEmailCount());
	    assertEquals(3, mc.getNewFaxCount());
	    assertEquals(1, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());
        assertEquals(4, mc.getOldTotalCount());
	    assertEquals(0, mc.getOldEmailCount());
	    assertEquals(1, mc.getOldFaxCount());
	    assertEquals(2, mc.getOldVoiceCount());
        assertEquals(1, mc.getOldVideoCount());

        mc= new UserMailbox("junit01", true, false, true, true);
        assertEquals(4, mc.getNewTotalCount());
	    assertEquals(2, mc.getNewEmailCount());
	    assertEquals(0, mc.getNewFaxCount());
	    assertEquals(1, mc.getNewVoiceCount());
        assertEquals(1, mc.getNewVideoCount());
        assertEquals(6, mc.getOldTotalCount());
	    assertEquals(3, mc.getOldEmailCount());
	    assertEquals(0, mc.getOldFaxCount());
	    assertEquals(2, mc.getOldVoiceCount());
        assertEquals(1, mc.getOldVideoCount());

        mc= new UserMailbox("junit01", false, false, true, false);
        assertEquals(1, mc.getNewTotalCount());
	    assertEquals(0, mc.getNewEmailCount());
	    assertEquals(0, mc.getNewFaxCount());
	    assertEquals(1, mc.getNewVoiceCount());
        assertEquals(0, mc.getNewVideoCount());
        assertEquals(2, mc.getOldTotalCount());
	    assertEquals(0, mc.getOldEmailCount());
	    assertEquals(0, mc.getOldFaxCount());
	    assertEquals(2, mc.getOldVoiceCount());
        assertEquals(0, mc.getOldVideoCount());

    }
}
