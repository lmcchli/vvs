/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.test;

import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.meragent.test.TestMerAgent;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserInfo;
import java.util.*;
import junit.framework.*;

/**
 * This class tests NotificationGroup.
 */
public class NotificationGroupTest
    extends NtfTestCase
    implements NotifCompletedListener {

    private String[] filterSpec= {"Test"};
    private Properties fSpec= null;
    private NotificationGroup ng= null;
    private NotificationEmail email= null;
    private TestUser user1;
    private TestUser user2;
    private TestUser user3;
    private TestUser user4;
    private TestUser user5;
    private TestMerAgent mer;

    //These counters count the calls to the NotifCompletedListener interface
    //methods
    private int completeCalls = 0;
    private int retryCalls = 0;

    public NotificationGroupTest(String name) {
        super(name);
        mer= new TestMerAgent();
    }


    protected void setUp() {
        email= new NotificationEmail(9999, "Content-type:text/plain\n"
                                     + "From:nisse@host.domain\n"
                                     + "X-priority:3\n"
                                     + "Subject:testemail\n"
                                     + "To:junit01@junit.ipms.su.erm.abcxyz.se\n"
                                     + "To:junit02@junit.ipms.su.erm.abcxyz.se\n"
                                     + "To:junit03@junit.ipms.su.erm.abcxyz.se\n"
                                     + "\n"
                                     + "Testing.\n"
                                     );
        NotificationGroup.setBLevelTypes(new int[0]); // No B levels per default
        ng= new NotificationGroup(this, email, log, mer);
        user1 = new TestUser();
        user1.setMail("junit01@junit.ipms.su.erm.abcxyz.se");
        user2 = new TestUser();
        user2.setMail("junit02@junit.ipms.su.erm.abcxyz.se");
        user3 = new TestUser();
        user3.setMail("junit03@junit.ipms.su.erm.abcxyz.se");
        user4 = new TestUser();
        user4.setMail("junit04@junit.ipms.su.erm.abcxyz.se");
        user5 = new TestUser();
        user5.setMail("2000001@ipms.su.erm.abcxyz.se");
        ng.addUser(user1);
        ng.addUser(user2);
    }

    public void testAdd() throws Exception {
        NotificationGroup ng;
        l("testAdd");
        ng= new NotificationGroup(this, email, log, mer);
        assertEquals("{NotificationGroup: }", ng.toString());
        assertTrue(!(ng.containsUser(user1)));
        ng.addUser(user1);
        assertTrue(ng.containsUser(user1));
        assertTrue(!(ng.containsUser(user2)));
        assertTrue(!(ng.containsUser(user3)));
        ng.addUser(user2);
        assertTrue(ng.containsUser(user1));
        assertTrue(ng.containsUser(user2));
        assertTrue(!(ng.containsUser(user3)));
        log.logMessage((ng.toString()));
    }

    public void testCount() throws Exception {
        l("testCount");
        ng.setOutCount(user1, 4);
        ng.setOutCount(user2, 2);
        assertEquals(4, ng.getOutCount(user1));
        assertEquals(2, ng.getOutCount(user2));
        assertEquals(0, ng.getOutCount(user3));

        ng.failed(user1, 0, null);
        assertEquals(3, ng.getOutCount(user1));
        ng.expired(user1, 0);
        assertEquals(2, ng.getOutCount(user1));
        ng.retry(user1, 0, null);
        assertEquals(1, ng.getOutCount(user1));
        ng.ok(user1, 1);
        assertEquals(0, ng.getOutCount(user1));

        ng.ok(user2, 0);
        assertEquals(0, ng.getOutCount(user2));
    }

    public void testOkSlamdown() throws Exception {
        l("testOkSlamdown");
        NotificationEmail mail = new NotificationEmail
            (9999, "Return-Path: <sink>\r\n" +
             "Received: from sun81 ([150.132.5.147]) by\r\n" +
             "valhall.su.erm.abcxyz.se (Netscape Messaging Server 4.15) with\r\n" +
             "ESMTP id HH1HUM00.R3G for <andreas.henningsson@mobeon.com>; Wed, \r\n" +
             "25 Jun 2003 15:37:34 +0200\r\n" +
             "Message-ID: <2214976.1056548266206.JavaMail.ermahen@sun81>\r\n" +
             "From: +4660161068\r\n" +
             "To: 2000001@ipms.su.erm.abcxyz.se\r\n" +
             "Subject: ipms/message\r\n" +
             "Mime-Version: 1.0\r\n" +
             "Ipms-Notification-Version: 1.0\r\n" +
             "Ipms-Component-From: emComponent=vespa.ipms.su.erm.abcxyz.se\r\n" +
             "Ipms-Notification-Type: mvas.subscriber.slamdown\r\n" +
             "Ipms-Notification-Content: mvas.subscriber.slamdown\r\n\r\n" +
             "+4660161068\r\n"
        );
        NotificationGroup ng1= new NotificationGroup(this, mail, log, mer);
        ng1.addUser(user5);
        ng1.setOutCount(user5, 1);
        ng1.noMoreUsers();
        ng1.ok(user5, 0);
        assertEquals(0, ng1.getOutCount(user5));
        assertEquals(1, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    public void testFailedSlamdown() throws Exception {
        l("testFailedSlamdown");
        NotificationEmail mail = new NotificationEmail
            (9999, "Return-Path: <sink>\r\n" +
             "Received: from sun81 ([150.132.5.147]) by\r\n" +
             "valhall.su.erm.abcxyz.se (Netscape Messaging Server 4.15) with\r\n" +
             "ESMTP id HH1HUM00.R3G for <andreas.henningsson@mobeon.com>; Wed, \r\n" +
             "25 Jun 2003 15:37:34 +0200\r\n" +
             "Message-ID: <2214976.1056548266206.JavaMail.ermahen@sun81>\r\n" +
             "From: +4660161068\r\n" +
             "To: 2000001@junit.su.erm.abcxyz.se\r\n" +
             "Subject: ipms/message\r\n" +
             "Mime-Version: 1.0\r\n" +
             "Ipms-Notification-Version: 1.0\r\n" +
             "Ipms-Component-From: emComponent=vespa.ipms.su.erm.abcxyz.se\r\n" +
             "Ipms-Notification-Type: mvas.subscriber.slamdown\r\n" +
             "Ipms-Notification-Content: mvas.subscriber.slamdown\r\n\r\n" +
             "+4660161068\r\n"
        );
        NotificationGroup ng1= new NotificationGroup(this, mail, log, mer);
        ng1.addUser(user5);
        ng1.setOutCount(user5, 1);
        ng1.noMoreUsers();
        ng1.failed(user5, 0, "Fel 1");
        assertEquals(0, ng1.getOutCount(user5));
        assertEquals(0, mer.getDelivered());
        assertEquals(1, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }


    public void testOk() throws Exception {
        l("testOk");
        ng.noMoreUsers();
        ng.setOutCount(user1, 4);
        ng.ok(user1, 0);
        assertEquals(0, ng.getOutCount(user1));
        ng.ok(user2, 0);
        ng.ok(user1, 2);
        assertEquals(0, ng.getOutCount(user2));
        assertEquals(3, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    public void testFailed() throws Exception {
        l("testFailed");
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user2, 0);
        ng.failed(user1, 0, "Fel 1");
        ng.failed(user1, 1, "Fel 2");
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(1, mer.getDelivered());
        assertEquals(1, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    public void testExpired() throws Exception {
        l("testExpired");
        ng.setOutCount(user1, 1);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user2, 0);
        ng.expired(user1, 1);
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(1, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(1, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    public void testRetry() throws Exception {
        l("testRetry");
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user2, 0);
        ng.retry(user1, 0, "Försök igen");
        ng.expired(user1, 1);
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(1, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(0, completeCalls);
        assertEquals(1, retryCalls);
    }


    //Test that feedback calls may come before setOutCount
    public void testAsync() throws Exception {
        l("testAsync");
        ng.ok(user2, 0);
        assertEquals(0, ng.getOutCount(user2));
        ng.setOutCount(user2, 1);
        assertEquals(0, ng.getOutCount(user2));

        ng.expired(user1, 1);
        assertEquals(-1, ng.getOutCount(user1));
        ng.setOutCount(user1, 2);
        assertEquals(1, ng.getOutCount(user1));
        ng.noMoreUsers();
        ng.failed(user1, 0, "Fel 1");
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(1, mer.getDelivered());
        assertEquals(1, mer.getFailed());
        assertEquals(0, mer.getExpired());
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    public void testNoMer() throws Exception {
        l("testNoMer");
        ng.noMerEvent();
        ng.ok(user1, 0);
        ng.ok(user2, 0);
        assertEquals(0, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
    }

    public void notestBLevelSucceedsALevelRetry() throws Exception {
        l("testBLevelSucceedsALevelRetry");
        NotificationGroup.setBLevelTypes(new int[] {1});
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user2, 5);
        ng.ok(user1, 1);
        ng.retry(user1, 5, "Retry1");
        assertEquals("All events done", 0, ng.getOutCount(user1));
        assertEquals("One delivered event", 1, mer.getDelivered());
        assertEquals("No failed", 0, mer.getFailed());
        assertEquals("No expired", 0, mer.getExpired());
        assertEquals("No complete since only MWI succeded",0, completeCalls);
        assertEquals("One retry since only MWI succeded", 1, retryCalls);
    }

    public void notestBLevelRetryALevelFail() throws Exception {
        l("testBLevelRetryALevelFail");
        NotificationGroup.setBLevelTypes(new int[] {1});
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user2, 5);
        ng.retry(user1, 1, "Retry on MWI");
        ng.failed(user1, 0, "Fail on SMS");
        assertEquals("All events done", 0, ng.getOutCount(user1));
        assertEquals("One delivered event", 1, mer.getDelivered());
        assertEquals("No failed since MWI gave retry", 0, mer.getFailed());
        assertEquals("No expired", 0, mer.getExpired());
        assertEquals("No complete",0, completeCalls);
        assertEquals("One retry since MWI gave retry", 1, retryCalls);
    }



    public void testOneBLevelSucceedsOneBLevelRetry() throws Exception {
        l("testOneBLevelSucceedsOneBLevelRetry");
        NotificationGroup.setBLevelTypes(new int[] {1});
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();
        ng.ok(user1, 1);
        ng.retry(user1, 1, "Retry1");
        ng.ok(user2, 1);
        assertEquals("All events done", 0, ng.getOutCount(user1));
        assertEquals("Both notifs delivered", 2, mer.getDelivered());
        assertEquals("No failed", 0, mer.getFailed());
        assertEquals("No expired", 0, mer.getExpired());
        assertEquals("No retry since no A-level retry", 0, retryCalls);
        assertEquals("Complete since no A-level retry",1, completeCalls);
    }


    public void notifCompleted(int notifId, int receiver) {
        ++completeCalls;
        log.logMessage("Done with " + notifId + ", " + receiver, log.L_DEBUG);
    }

    public void notifRetry(int notifId, int receiver, String retryAddresses) {
        ++retryCalls;
        log.logMessage("Retrying " + notifId + ", " + receiver + " for " + retryAddresses, log.L_DEBUG);
    }

    public void notifRenew(int notifId, int receiver) {

    }

    public void notifRelease(int notifId, int receiver) {
    }

}
