/*
 * DeferredInfoTest.java
 * JUnit based test
 *
 * Created on den 13 september 2004, 10:08
 */

package com.mobeon.ntf.deferred.test;


import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DBDelayHandler;
import com.mobeon.common.storedelay.SDLogger;

import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.Config;

import com.mobeon.ntf.deferred.*;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Set;
import java.util.Properties;
import junit.framework.*;

/**
 *
 * @author QMIER
 */
public class DeferredCmdHandlerTest extends TestCase {





    private NotificationEmail makeDeferredMail
                         (String receiver, String notiftype, String date,
                          String body, int msgId)
    {
        String msg =
            "Return-path: <hej@su.eip.abcxyz.se>\r\n"
            + "Received: from sun81 (sun81.su.erm.abcxyz.se [150.132.5.147])\r\n"
            + " by jawa.ipms.su.erm.abcxyz.se\r\n"
            + " (iPlanet Messaging Server 5.2 HotFix 1.04 (built Oct 21 2002))\r\n"
            + " with ESMTP id <0HBC005SM05VO7@jawa.ipms.su.erm.abcxyz.se> for\r\n"
            + " andreas@ims-ms-daemon; Thu, 06 Mar 2003 15:27:32 +0100 (MET)\r\n"
            + "Date: Thu, 06 Mar 2003 15:27:31 +0100 (MET)\r\n"
            + "Date-warning: Date header was inserted by jawa.ipms.su.erm.abcxyz.se\r\n"
            + "To: " + receiver + "\r\n"
            + "Message-id: <0HBC005SN05VO7@jawa.ipms.su.erm.abcxyz.se>\r\n"
            + "MIME-version: 1.0\r\n"
            + "Original-recipient: rfc822;andreas@ipms.su.erm.abcxyz.se\r\n"
            + "Ipms-Notification-Type: " + notiftype + "\r\n"
            + "Ipms-Notification-Content: body\r\n"
            + "X-Ipms-Deferred-Delivery: " + date + "\r\n"
            + "\r\n"
            + body;

        NotificationEmail email = new NotificationEmail(msgId, msg);
        return email;
    }

    public DeferredCmdHandlerTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DeferredCmdHandlerTest.class);
        return suite;
    }

   private DelayHandler delayer = null;

   public void setUp()
    {
       try {
           Properties p = new Properties();
           p.put(DBDelayHandler.KEY_STORAGE_BASE, "deferredtest");
           p.put(DBDelayHandler.KEY_STORAGE_DIR, "/tmp/ntf/deferredtest");
           delayer = new DBDelayHandler(p);
       } catch (Exception e) {
           SDLogger.log(SDLogger.ERROR,"Could not start delayer");
       }
    }

    public void tearDown()

    {
        SDLogger.log(SDLogger.DEBUG, "Stop running");
        if (delayer != null) {
            try {Thread.sleep(1500);}catch (Exception ignored) {}
            com.mobeon.ntf.management.ManagementInfo.get().setExit(true);
            try {Thread.sleep(5000);}catch (Exception ignored) {}
            delayer = null;
            com.mobeon.ntf.management.ManagementInfo.get().setExit(false);
        }
        // Only one testcase in this class!
        SDLogger.log(SDLogger.INFO, "Going to call System.exit");
        System.exit(0);
    }

    private static final String RECEIVER_1  = "qmier1@junit.su.erm.abcxyz.se";
    private static final String RECEIVER_2 = "qmier2@junit.su.erm.abcxyz.se";

    private static final String DATE_1    = "20 Sep 2004 13:04 +0200";
    private static final String DATE_2    = "20 Sep 2004 13:05 +0200";
    private static final String DATE_3    = "20 Sep 2004 13:06 +0200";
    private static final String DATE_4    = "20 Sep 2004 13:07 +0200";
    private static final String DATE_5    = "20 Sep 2004 13:03 +0200";
    private static final String FORWARDNR_1 = "9999";


    /**
     * Test that a voicemailoff reminder command is handled correctly
     */
    public void testSchedulings()
        throws Exception
    {
        // Configure to give an SMS
        NotificationEmail email_1 =
            makeDeferredMail(RECEIVER_1, "voicemailoff",
                             DATE_1,
                             "action=reminder\r\n" +
                             "forwardingnumber=" + FORWARDNR_1 + "\r\n" +
                             "unsetforwards=cf-no-reply,cf-not-reachable\r\n",
                             1);

        // Configure to NOT give an SMS
        NotificationEmail email_2 =
            makeDeferredMail(RECEIVER_2, "voicemailoff",
                             DATE_2,
                             "action=reminder\r\n" +
                             "forwardingnumber=" + FORWARDNR_1 + "\r\n" +
                             "unsetforwards=cf-no-reply,cf-not-reachable\r\n",
                             2);

        // Configured to be on
        NotificationEmail email_3 =
            makeDeferredMail(RECEIVER_1, "cfuon",
                             DATE_3,
                             "action=reminder\r\n" +
                             "forwardingnumber=" + FORWARDNR_1 + "\r\n",
                             3);

        // Configured to be off
        NotificationEmail email_4 =
            makeDeferredMail(RECEIVER_2, "cfuon",
                             DATE_4,
                             "action=reminder\r\n" +
                             "forwardingnumber=" + FORWARDNR_1 + "\r\n",
                             4);
        NotificationEmail email_5 =
            makeDeferredMail(RECEIVER_1, "temporarygreetingon",
                             DATE_5,
                             "action=reminder\r\n",
                             5);


        UserFactory uf =  new UserFactory();
        DeferredCmdHandler handler = new DeferredCmdHandler(delayer, uf);
        NotifCompletedListener cleaner = new CompletedListener();
        handler.handleDeferredCommand(email_1, cleaner);
        handler.handleDeferredCommand(email_2, cleaner);
        handler.handleDeferredCommand(email_3, cleaner);
        handler.handleDeferredCommand(email_4, cleaner);
        handler.handleDeferredCommand(email_5, cleaner);
        delayUntil(2004, 9, 20, 13, 10);
    }

    private void delayUntil(int year, int month, int day, int hour, int minute)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month-1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        SDLogger.log(SDLogger.DEBUG, "Delaying until " + hour + ":" + minute);
        Calendar now = Calendar.getInstance();
        while (now.before(c)) {
            try {Thread.sleep(10000);} catch (Exception ignored) {}
            now = Calendar.getInstance();
        }
    }



    private class CompletedListener implements NotifCompletedListener
    {

        public void notifCompleted(int notifId, int receiver)
        {
            SDLogger.log(SDLogger.DEBUG, "Notif Completed : " + notifId);
        }


        public void notifRetry(int notifId, int receiver, String retryAddresses)
        {
            SDLogger.log(SDLogger.DEBUG, "Notif retry");
        }

        public void notifRenew(int notifId, int receiver) {

        }

        public void notifRelease(int notifId, int receiver) {
        }

    }

}
