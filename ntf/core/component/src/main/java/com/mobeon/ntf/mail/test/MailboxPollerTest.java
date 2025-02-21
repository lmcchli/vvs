 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.MailboxPoller;
import com.mobeon.ntf.mail.MailstorePoller;
import com.mobeon.ntf.mail.test.TestEmailListHandler;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.test.NtfTestCase;
import java.util.*;
import java.io.ByteArrayInputStream;
import jakarta.mail.Flags;
import jakarta.mail.internet.MimeMessage;

import junit.framework.*;

/**
 * This class tests MailboxPoller.
 */
public class MailboxPollerTest
    extends
        NtfTestCase
    implements
        com.mobeon.ntf.mail.EmailStore,
        com.mobeon.ntf.mail.MailMemoryManager {

    private MailboxPoller[] mp;
    private int mailCount = 0;
    private int boxes = 0;
    private MailstorePoller msp = new MailstorePoller();
    private TestEmailListHandler telh = new TestEmailListHandler();

    public long getMailMemoryUsed() { return 0; }
    public int getMailMemoryCount() { return 0; }
    public synchronized int reserveMemory(int poller, int msgId, int mailSize) { return 0; }
    public synchronized void releaseMemory(int msgId, int poller){ ; }


    String header=
            "Return-path: <hej@su.eip.abcxyz.se>\r\n"
            + "Received: from sun81 (sun81.su.erm.abcxyz.se [150.132.5.147])\r\n"
            + " by jawa.ipms.su.erm.abcxyz.se\r\n"
            + " (iPlanet Messaging Server 5.2 HotFix 1.04 (built Oct 21 2002))\r\n"
            + " with ESMTP id <0HBC005SM05VO7@jawa.lab.mobeon.com> for\r\n"
            + " andreas@ims-ms-daemon; Thu, 06 Mar 2003 15:27:32 +0100 (MET)\r\n"
            + "Date: Thu, 06 Mar 2003 15:27:31 +0100 (MET)\r\n"
            + "Date-warning: Date header was inserted by jawa.lab.mobeon.com\r\n"
            + "To: totte@host.domain\r\n"
            + "Message-id: <0HBC005SN05VO7@jawa.lab.mobeon.com>\r\n"
            + "MIME-version: 1.0\r\n"
            + "Original-recipient: rfc822;andreas@ipms.su.erm.abcxyz.se\r\n";

    String emailNotificationHeader =
       "X-Ipms-EmailNotification\r\n";

    String  voiceheader =
            "Subject: voice message.\r\n"
            + "Content-type: MULTIPART/Voice-Message;\r\n"
            + " BOUNDARY=\"-559023410-758783491-972026285=:8136\"; Version=2.0\r\n";
    String voicenobody =
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


    public MailboxPollerTest(String name) {
	super(name);
        boxes = Config.getImapThreads();
    }


    public void putEmail(NotificationEmail email) {
        ++mailCount;
    }
    /**
     * The following lines are comments since the message target machine (jawa.ipms.su.erm.abcxyz.se)
     * status has changed.
     *
    protected void setUp() {

        mp = new MailboxPoller[boxes];
        msp = new MailstorePoller();
        for (int i = 0; i < boxes; i++) {
            mp[i] = new MailboxPoller(new ThreadGroup("A thread group"), this, i, null, this, null, msp);
            mp[i].setDaemon(true);
        }
    }

    protected void tearDown() {
        l(": test done, cleaning mailboxes");
        ManagementInfo.get().setExit(true);
        emptyBoxes("junit04", "abcd");
        try { Thread.sleep(15000); } catch (Exception e) { ; }
        ManagementInfo.get().setExit(false);
    }

    private void emptyBoxes(String user, String pw) {
        int i;

        MailtestUtil.emptyMailbox(user, pw);
        for (i = 0; i < Config.getImapThreads(); i++) {
            MailtestUtil.emptyMailbox("gnotification3_" + i, "system");
        }
    }

    public void sendAndCountNotif(int nMails, int firstSeen, int nSeen, int firstFlagged, int nFlagged) throws Exception {
        int i;
        int retries = 0;
        int user;
        int gnot;

        log.logMessage("Sending " + nMails + " mails");
        for (i= 0; i < nMails; i++) {
            MailtestUtil.sendMail("junit04", "mail.eml");
        }
        log.logMessage("Sent mail");
        i = 0;
        do {  //Give the mails some time to arrive
            Thread.sleep(2000);
            ++i;
            user = MailtestUtil.countMessages("junit04", "abcd");
            gnot = 0;
            for (int j = 0; j < Config.getImapThreads(); j++) {
                gnot += MailtestUtil.countMessages("gnotification3_" + j, "system");
            }
            log.logMessage("Mails arrived " + i + ": " + user + ", " + gnot);
        } while (i < 10 && (user < nMails || gnot < nMails));
        log.logMessage("All mails arrived, starting mailbox pollers ");
        log.logMessage("assertEquals 1: expected=" + nMails + " but was=" + user);
        assertEquals(nMails, user);
        log.logMessage("assertEquals 2: expected=" + nMails + " but was=" + gnot);
        assertEquals(nMails, gnot);
        for (i = 0; i < boxes; i++) {
            mp[i].start();
        }
        log.logMessage("Mailbox pollers started, setting flags");
        Thread.sleep(5000);
        if (firstSeen > 0 ) {
            MailtestUtil.flagMessages("gnotification3_"+ msp.getInstanceWithMail(), "system", firstSeen, nSeen, new Flags("\\Seen"));
        }
        if (firstFlagged > 0 ) {
            MailtestUtil.flagMessages("gnotification3_"+ msp.getInstanceWithMail(), "system", firstFlagged, nFlagged, new Flags("\\Flagged"));
            Thread.sleep(1000 * Config.getRetryInterval());
            log.logMessage("Flags set, activating mailbox pollers");
        }
        Thread.sleep(30000);
        log.logMessage("Mailbox pollers activated, waiting for all mails to be processed");
        i = 0;
        do { //Give NTF some time to process the mails
            Thread.sleep(1000);
            log.logMessage("Processed " + mailCount + " mails");
            ++i;
        } while (i < 40 && mailCount < nMails);
        int expected = nMails - nSeen + nFlagged;
        log.logMessage("assertEquals 3: expected=" + expected + " but was=" + mailCount);
        assertEquals(nMails - nSeen + nFlagged, mailCount);
    }

    public void testEmpty() throws Exception {
	l("testEmpty");
        emptyBoxes("junit04", "abcd");
        sendAndCountNotif(0, 0, 0, 0, 0);
    }

    public void testServerNew() throws Exception {
	l("testServerNew");
        sendAndCountNotif(Config.getImapBatchSize(), 0, 0, 0, 0);
    }

    public void testNtfNew() throws Exception {
	l("testNtfNew");
        sendAndCountNotif(2*Config.getImapBatchSize(), 0, 0, 0, 0);
    }

    //(nMails, firstSeen, nSeen, firstFlagged, nFlagged=
    public void testServerNewWithSeen() throws Exception {
	l("testServerNewWithSeen");
        sendAndCountNotif(Config.getImapBatchSize(), 5, 4, 0, 0);
    }

    public void testNtfNewWithSeen() throws Exception {
	l("testNtfNewWithSeen");
        sendAndCountNotif(2*Config.getImapBatchSize(), 5, Config.getImapBatchSize() + 1, 0, 0);
    }

    public void testServerNewWithRetry() throws Exception {
	l("testServerNewWithRetry");
        sendAndCountNotif(Config.getImapBatchSize(), 5, 4, 6, 2);
    }

    public void testNtfNewWithRetry() throws Exception {
	l("testNtfNewWithRetry");
        sendAndCountNotif(2*Config.getImapBatchSize(), 5, Config.getImapBatchSize() + 1, 6, Config.getImapBatchSize() - 2);
    }
    */

    public void testSetNotifMessage() throws Exception {
        l("testSetNotifMessage");
        String m1 = "1";
        String m2 = "2";
        MailboxPoller.startCleanMessageIDs();
        MailboxPoller.testSetMessageID(m1);
        MailboxPoller.testSetMessageID(m1);
        assertTrue(MailboxPoller.testCheckMessageID(m1));

        MailboxPoller.testSetMessageID(m2);
        assertTrue(MailboxPoller.testCheckMessageID(m2));

        Thread.sleep(Config.getImapPollInterval()*1000);

        while(MailboxPoller.testCheckMessageID(m2)) {
            Thread.sleep(1000);
        }
        assertTrue(!MailboxPoller.testCheckMessageID(m2));
        MailboxPoller.stopCleanMessageIDs();
    }


    public void testEmailNotification()
        throws Exception
    {
        telh.resetCount();
        int beginCount = mailCount;
        MailboxPoller poller = new MailboxPoller(
                new ThreadGroup("A thread group"), this, 0, null, this, telh, msp);

        String content = header + voiceheader + voicenobody;
        MimeMessage msg = new MimeMessage(null,new ByteArrayInputStream(content.getBytes()));

        poller.forwardNotif(msg, 99);

        assertEquals(0, telh.getCount());
        assertEquals(1+beginCount, mailCount);

        // no forward of this message
        content = header + emailNotificationHeader + voiceheader + voicenobody;
        msg = new MimeMessage(null,new ByteArrayInputStream(content.getBytes()));

        poller.forwardNotif(msg, 99);

        assertEquals(0, telh.getCount());
        assertEquals(1+beginCount, mailCount);


    }

}
