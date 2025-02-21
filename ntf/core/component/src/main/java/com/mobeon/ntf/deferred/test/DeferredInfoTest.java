/*
 * DeferredInfoTest.java
 * JUnit based test
 *
 * Created on den 13 september 2004, 10:08
 */

package com.mobeon.ntf.deferred.test;


import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.Config;

import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.ntf.deferred.*;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Set;
import junit.framework.*;

/**
 *
 * @author QMIER
 */
public class DeferredInfoTest extends NtfTestCase {

    private static final String RECEIVER  = "mikael@mobeon.com";
    private static final String DATE_1    = "20 Oct 2004 15:52 -0100";
    private static final String FORWARDNR_1 = "123456789";


    String voiceMailOffDeferredHeader =
      "Ipms-Notification-Type: voicemailoff\r\n" +
      "Ipms-Notification-Content: body\r\n" +
      "X-Ipms-Deferred-Delivery: " + DATE_1 + "\r\n" ;
    String  separator  = "\r\n";
    String voiceMailOffReminderDeferredBody =
      "action=reminder\r\n" +
      "forwardingnumber=" + FORWARDNR_1 + "\r\n" +
      "unsetforwards=cf-no-reply,cf-not-reachable\r\n";
    String voiceMailOffAutoonDeferredBody =
      "action=autoon\r\n" +
      "forwardingnumber=" + FORWARDNR_1 + "\r\n" +
      "unsetforwards=cf-no-reply,cf-not-reachable\r\n";


    String cfuOnDeferredHeader =
      "Ipms-Notification-Type: cfuon\r\n" +
      "Ipms-Notification-Content: body\r\n" +
      "X-Ipms-Deferred-Delivery: " + DATE_1 + "\r\n" ;
    String cfuOnDeferredAutoOffBody =
      "action=autooff\r\n" +
      "forwardingnumber=" + FORWARDNR_1 + "\r\n";
    String cfuOnDeferredReminderBody =
      "action=reminder\r\n" +
      "forwardingnumber=" + FORWARDNR_1 + "\r\n";


    String tempGreetingDeferredHeader =
      "Ipms-Notification-Type: temporarygreetingon\r\n" +
      "Ipms-Notification-Content: body\r\n" +
      "X-Ipms-Deferred-Delivery: " + DATE_1 + "\r\n" ;
    String tempGreetingDeferredBody =
      "action=reminder\r\n";

    String header=
            "Return-path: <hej@su.eip.abcxyz.se>\r\n"
            + "Received: from sun81 (sun81.su.erm.abcxyz.se [150.132.5.147])\r\n"
            + " by jawa.ipms.su.erm.abcxyz.se\r\n"
            + " (iPlanet Messaging Server 5.2 HotFix 1.04 (built Oct 21 2002))\r\n"
            + " with ESMTP id <0HBC005SM05VO7@jawa.ipms.su.erm.abcxyz.se> for\r\n"
            + " andreas@ims-ms-daemon; Thu, 06 Mar 2003 15:27:32 +0100 (MET)\r\n"
            + "Date: Thu, 06 Mar 2003 15:27:31 +0100 (MET)\r\n"
            + "Date-warning: Date header was inserted by jawa.ipms.su.erm.abcxyz.se\r\n"
            + "To: " + RECEIVER + "\r\n"
            + "Message-id: <0HBC005SN05VO7@jawa.ipms.su.erm.abcxyz.se>\r\n"
            + "MIME-version: 1.0\r\n"
            + "Original-recipient: rfc822;andreas@ipms.su.erm.abcxyz.se\r\n";


    private Calendar gmtCal = null;

    public DeferredInfoTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DeferredInfoTest.class);
        return suite;
    }

    public void setUp()
    {
        gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }




    /**
     * Test that a voicemailoff reminder command is handled correctly
     */
    public void testVoiceMailReminder()
      throws Exception
    {

        NotificationEmail email =
                new NotificationEmail(9999,
                                     header + voiceMailOffDeferredHeader +
                                     separator + voiceMailOffReminderDeferredBody);
        DeferredInfo dInfo = new DeferredInfo(email);

        assertEquals("Correct Type", 5, dInfo.getMailType());
        Calendar c = dInfo.getDeferredTime();
        // Move to GMT timezone to compare hours
        assertNotNull("We should have a date", c);
        gmtCal.setTime(c.getTime());
        assertEquals("Day ", 20, gmtCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month", 9, gmtCal.get(Calendar.MONTH)); // Calendar.Month is zero based
        assertEquals("Hour", 16, gmtCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Year", 2004, gmtCal.get(Calendar.YEAR));

        assertEquals("Forwardining", FORWARDNR_1, dInfo.getForwardingNumber());
        assertEquals("Action ", DeferredInfo.ACTION_REMINDER, dInfo.getAction());
        assertTrue("CFNoReply", dInfo.hasForwardNoReply());
        assertTrue("CFNotReach", dInfo.hasForwardNotReachable());
        assertFalse("CFBusy", dInfo.hasForwardBusy());
        assertFalse("CFU", dInfo.hasForwardUnconditional());
        checkSaveRestore(dInfo);
    }

    /**
     * Test voicemailoff action
     */
    public void testVoiceMailAutoOn()
        throws Exception
    {
        NotificationEmail email =
                new NotificationEmail(9999,
                                     header + voiceMailOffDeferredHeader +
                                     separator + voiceMailOffAutoonDeferredBody);

        DeferredInfo dInfo = new DeferredInfo(email);

        assertEquals("Correct Receiver", RECEIVER, dInfo.getReceiver());
        assertEquals("Correct Type", 5, dInfo.getMailType());
        Calendar c = dInfo.getDeferredTime();
        assertNotNull("We should have a date", c);
        gmtCal.setTime(c.getTime());
        assertEquals("Day ", 20, gmtCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month", 9, gmtCal.get(Calendar.MONTH)); // Calendar.Month is zero based
        assertEquals("Hour", 16, gmtCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Year", 2004, gmtCal.get(Calendar.YEAR));

        assertEquals("Forwardining", FORWARDNR_1, dInfo.getForwardingNumber());
        assertEquals("Action ", DeferredInfo.ACTION_AUTO_ON, dInfo.getAction());
        assertTrue("CFReply", dInfo.hasForwardNoReply());
        assertTrue("CFNotReach", dInfo.hasForwardNotReachable());
        assertFalse("CFBusy", dInfo.hasForwardBusy());
        assertFalse("CFU", dInfo.hasForwardUnconditional());
        checkSaveRestore(dInfo);
    }

    public void testCFUReminder()
        throws Exception
    {

        NotificationEmail email =
                new NotificationEmail(9999,
                                     header + cfuOnDeferredHeader +
                                     separator + cfuOnDeferredReminderBody);

        DeferredInfo dInfo = new DeferredInfo(email);

        assertEquals("Correct Receiver", RECEIVER, dInfo.getReceiver());
        assertEquals("Correct Type", 6, dInfo.getMailType());
        Calendar c = dInfo.getDeferredTime();
        assertNotNull("We should have a date", c);
        gmtCal.setTime(c.getTime());
        assertEquals("Day ", 20, gmtCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month", 9, gmtCal.get(Calendar.MONTH)); // Calendar.Month is zero based
        assertEquals("Hour", 16, gmtCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Year", 2004, gmtCal.get(Calendar.YEAR));

        assertEquals("Forwardining", FORWARDNR_1, dInfo.getForwardingNumber());
        assertEquals("Action ", DeferredInfo.ACTION_REMINDER, dInfo.getAction());
        assertFalse("CFNoReply", dInfo.hasForwardNoReply());
        assertFalse("CFNotReach", dInfo.hasForwardNotReachable());
        assertFalse("CFBusy", dInfo.hasForwardBusy());
        assertTrue("CFU", dInfo.hasForwardUnconditional()); // On for this mailtype
        checkSaveRestore(dInfo);
    }

    public void testCFUAutoOff()
        throws Exception
    {
        NotificationEmail email =
                new NotificationEmail(9999,
                                     header + cfuOnDeferredHeader +
                                     separator + cfuOnDeferredAutoOffBody);
        DeferredInfo dInfo = new DeferredInfo(email);

        assertEquals("Correct Receiver", RECEIVER, dInfo.getReceiver());
        assertEquals("Correct Type", 6, dInfo.getMailType());
        Calendar c = dInfo.getDeferredTime();
        assertNotNull("We should have a date", c);
        gmtCal.setTime(c.getTime());
        assertEquals("Day ", 20, gmtCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month", 9, gmtCal.get(Calendar.MONTH)); // Calendar.Month is zero based
        assertEquals("Hour", 16, gmtCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Year", 2004, gmtCal.get(Calendar.YEAR));

        assertEquals("Forwardining", FORWARDNR_1, dInfo.getForwardingNumber());
        assertEquals("Action ", DeferredInfo.ACTION_AUTO_OFF, dInfo.getAction());
        assertFalse("CFNoReply", dInfo.hasForwardNoReply());
        assertFalse("CFNotReach", dInfo.hasForwardNotReachable());
        assertFalse("CFBusy", dInfo.hasForwardBusy());
        assertTrue("CFU", dInfo.hasForwardUnconditional()); // On for this mailtype
        checkSaveRestore(dInfo);
    }

    public void testTempGreetReminder()
        throws Exception
    {
        NotificationEmail email =
                new NotificationEmail(9999,
                                     header + tempGreetingDeferredHeader +
                                     separator + tempGreetingDeferredBody);

        DeferredInfo dInfo = new DeferredInfo(email);

        assertEquals("Correct Receiver", RECEIVER, dInfo.getReceiver());
        assertEquals("Correct Type", 7, dInfo.getMailType());
        Calendar c = dInfo.getDeferredTime();
        assertNotNull("We should have a date", c);
        gmtCal.setTime(c.getTime());
        assertEquals("Day ", 20, gmtCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month", 9, gmtCal.get(Calendar.MONTH)); // Calendar.Month is zero based
        assertEquals("Hour", 16, gmtCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Year", 2004, gmtCal.get(Calendar.YEAR));

        assertEquals("Action ", DeferredInfo.ACTION_REMINDER, dInfo.getAction());
        assertFalse("CFNoReply", dInfo.hasForwardNoReply());
        assertFalse("CFNotReach", dInfo.hasForwardNotReachable());
        assertFalse("CFBusy", dInfo.hasForwardBusy());
        assertFalse("CFU", dInfo.hasForwardUnconditional());
        checkSaveRestore(dInfo);
    }




    /**
     * Checks that saving/restor to a DelayedInfo works.
     */
    private void checkSaveRestore(DeferredInfo info)
       throws Exception
    {
        DelayInfo di = info.getPersistentRepresentation();
        assertNotNull("We should have a delayinfo", di);

        byte[] data = di.getByteInfo();
        l("Di Bytes");
        for (int i = 0; i< data.length; i++) {
            byte b = data[i];
            l("" + i + " -> " + Integer.toHexString(b));
        }


        DeferredInfo restored = new DeferredInfo(di);

        assertEquals("Same Receiver", info.getReceiver(), restored.getReceiver());
        assertEquals("Same Type", info.getMailType(), restored.getMailType());
        assertEquals("Same Date", info.getDeferredTime(),
                                  restored.getDeferredTime());

        assertEquals("Same Version", info.getVersion(), restored.getVersion());
        assertEquals("Same Action ", info.getAction(), restored.getAction());

        assertEquals("Same CFNReply", info.hasForwardNoReply(),
                                     restored.hasForwardNoReply());
        assertEquals("Same CFNotReach", info.hasForwardNotReachable(),
                                        restored.hasForwardNotReachable());
        assertEquals("Same CFBusy", info.hasForwardBusy(),
                                    restored.hasForwardBusy());
        assertEquals("Same CFU", info.hasForwardUnconditional(),
                                 restored.hasForwardUnconditional());
    }





}


