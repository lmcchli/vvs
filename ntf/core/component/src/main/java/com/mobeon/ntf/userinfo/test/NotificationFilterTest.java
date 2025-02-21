/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.test;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestUser;
import com.mobeon.ntf.userinfo.MmsFilterInfo;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.CmwFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.WapFilterInfo;
import com.mobeon.ntf.Constants;

import java.util.*;
import junit.framework.*;

/**
 * This class tests NotificationFilter.
 */
public class NotificationFilterTest extends NtfTestCase implements Constants {

    private String[] filterSpec = {"Test"};
    private Properties fSpec = null;
    private NotificationFilter f = null;
    private NotificationEmail email = null;
    private NotificationEmail slamdown = null;
    private NotificationEmail priomail = null;
    private NotificationEmail voicemail = null;
    private NotificationEmail faxmail = null;
    private NotificationEmail videomail = null;
    private GregorianCalendar when = null;
    private TestUser user;
    private boolean disableTypes = false;


    public NotificationFilterTest(String name) {
        super(name);
    }


    protected void setUp() {
        user = new TestUser();
        fSpec= new Properties();
        filterSpec= (String[])(fSpec.values().toArray(filterSpec));
        slamdown = new NotificationEmail(9999,
             "Return-Path: <sink>\r\n" +
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
             "Ipms-Notification-Type: ntf.internal.slamdown\r\n" +
             "Ipms-Notification-Content: ntf.internal.slamdown\r\n\r\n" +
             "+4660161068"
        );
        email= new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\n");
        priomail= new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:2\nSubject:testemail\n\n");
        voicemail= new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom:12345 <12345@host.domain>\nX-priority:3\nSubject:testemail\n\n");
        faxmail= new NotificationEmail(9999, "Content-type:multipart/fax-message\nFrom:12345@host.domain\nX-priority:3\nSubject:testemail\n\n");
        videomail = new NotificationEmail(9999, "Content-type:multipart/x-video-message\nFrom:12345@host.domain\nX-priority:3\nSubject:testvideomail\n\n");
    }

    public void test() throws Exception {
        l("test");
        f = new NotificationFilter(null, true, user);
        assertEquals("{NotificationFilter:}", f.toString());
        assertTrue(f.isNotifDisabled());

        setFS("first test", 0, false, false, "a", "e", "SMS", "c", "", "", false, "", null);
        f = new NotificationFilter(filterSpec, false, user);
        assertTrue(!f.isNotifDisabled());
        assertEquals("{NotificationFilter:\n"
                     + "  {FilterPart: name=first test,prio=0,active=false,notify=false,time=a,depType=e,contentForType={SMS=c},from=null,subject=null,urgent=false,voiceFaxFrom=null,readonly=null}"
                     + "}" , f.toString());
    }

    public void testNotifType() throws Exception {
        when= new GregorianCalendar(2002, 8, 18, 12, 00);
        l ("testNotifType");

        //Test some filter with different delivery profiles
        String[] deliveryProfileStrings = new String[]{"12345,060618532;SMS,ODL;",
                                                       "0702660291;SMS,WAP,MMS;"};

        setFS("single", 1, true, true, "a", "evf", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);

        SmsFilterInfo smsFilterInfo = f.getSmsFilterInfo(email, when, null);
        assertNotNull(smsFilterInfo);
        //Check the notifnumbers added into the UserDevice objects
        String[] numbers = smsFilterInfo.getNumbers();
        assertEquals(3, numbers.length);
        /*
        assertEquals("12345", numbers[0]); //order is preserved
        assertEquals("060618532", numbers[1]);
        assertEquals("0702660291", numbers[2]);
        */

        assertNull(f.getOdlFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getPagFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MWI", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        assertNotNull(f.getSmsFilterInfo(email, when, null));
        assertNull(f.getOdlFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getPagFilterInfo(email, when, null));

        //Test some filters without a delivery profile
        setFS("single", 1, true, true, "a", "evf", "ODL", "", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getSmsFilterInfo(email, when, null));

        OdlFilterInfo odlFilterInfo = f.getOdlFilterInfo(email, when, null);
        assertNotNull(odlFilterInfo);
        numbers = odlFilterInfo.getNumbers();
        //uds = odlFilterInfo.getDevices();
        assertEquals(1, numbers.length);
        //emnotifnumber is used as number when no delivery profiles
        assertEquals("9999901", numbers[0]);

        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getPagFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getSmsFilterInfo(email, when, null));
        assertNotNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getPagFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "PAG", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getSmsFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNotNull(f.getPagFilterInfo(email, when, null));

    }

    public void testBusinessTime() throws Exception {
        l("testBusinessTime");
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches
        String[] deliveryProfileStrings = new String[]{"12345,060618532;MMS;"};

        setFS("single", 1, true, true, "b", "evf", "MMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        //2002-08-23 is a friday
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 24, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 25, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 26, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 27, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 28, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 29, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 22, 00), null));

        //Since no filter matches there should be a default non-notification
        assertNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 22, 00), null));
    }

    public void testNonBusinessTime() throws Exception {
        l("testNonBusinessTime");
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches

        String[] deliveryProfileStrings = new String[]{"12345,060618532;MMS;"};

        setFS("single", 1, true, true, "nb", "evf", "MMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        //2002-09-23 is a monday
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 24, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 25, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 26, 12, 00), null));
        assertNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 27, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 28, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 29, 12, 00), null));
        assertNotNull(f.getMmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 22, 00), null));

        //Since no filter matches there should be a default non-notification
        assertNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 12, 00), null));
    }

    public void testAllTime() throws Exception {
        l("testAllTime");
        String[] deliveryProfileStrings = new String[]{"12345,060618532;SMS;"};

        setFS("single", 1, true, true, "a", "evf", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        //2002-09-23 is a monday
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 24, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 25, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 26, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 27, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 28, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 29, 12, 00), null));
        assertNotNull(f.getSmsFilterInfo(email, new GregorianCalendar(2002, 8, 23, 22, 00), null));
    }

    public void testDepositTypes() throws Exception {
        l("testDepositTypes");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches

        String[] deliveryProfileStrings = new String[]{"12345,060618532;MMS;"};

        setFS("single", 1, true, true, "a", "e", "MMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "v", "MMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        assertNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "f", "MMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        assertNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "v", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));

        setFS("single", 1, true, true, "a", "e", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(voicemail, when, null));

        setFS("single", 1, true, true, "a", "f", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(voicemail, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));

        setFS("single", 1, true, true, "a", "f", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(faxmail, when, null));

        setFS("single", 1, true, true, "a", "e", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(faxmail, when, null));

        setFS("single", 1, true, true, "a", "v", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(faxmail, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(faxmail, when, null));

        //Test some video filters
        setFS("single", 1, true, true, "a", "m", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(videomail, when, null));

        setFS("single", 1, true, true, "a", "e", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(videomail, when, null));

        setFS("single", 1, true, true, "a", "v", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(videomail, when, null));

        setFS("single", 1, true, true, "a", "evfm", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(videomail, when, null));
    }


    public void testFrom() throws Exception {
        l("testFrom");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "nisse@host.domain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "nisse@host.domain,kalle@host.domain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "pelle@host.domain,nisse@host.domain,kalle@host.domain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "pelle@host.domain,kalle@host.domain,nisse@host.domain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "pelle@host.domain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "nisse@host.otherdomain", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));
    }

    public void testVoiceFaxFrom() throws Exception {
        l("testVoiceFaxFrom");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "12345");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "12345,67890");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "11111,12345,67890");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "11111,67890,12345");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "67890");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(voicemail, when, null));
    }

    public void testSubject() throws Exception {
        l("testSubject");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        //MMS is used in test cases, since SMS will give a default notification
        //if no filter matches
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "testemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        //Test case insensitivity
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "tesTemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        //Test substring match
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "testemai", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        //Test non-matches
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "tostemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "etestemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));

        //Test semicolon in match
        email= new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:2\nSubject:;test;email;\n");
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%3b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "test%3bemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%3btest%3b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%3bemail%3b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%3btest%3bemail%3b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%3btest%2bemail%3b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));

        //Test percent in match
        email= new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:2\nSubject:test%3bemail\n");
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%25", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%253b", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "test%25", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "%253bemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "test%253bemail", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getMmsFilterInfo(email, when, null));

        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "25", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));
    }

    public void testUrgent() throws Exception {
        l("testUrgent");
        setFS("single", 1, true, true, "a", "evf", "MMS", "c", "", "", true, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNotNull(f.getMmsFilterInfo(priomail, when, null));
   }

    public void testMultiPart() throws Exception {
        l("testMultiPart");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        setFS("email", 2, true, true, "a", "e", "SMS", "c", "", "", false, "");
        setFS("voice", 3, true, true, "a", "v", "MMS", "c", "", "", false, "");
        setFS("fax", 4, true, true, "a", "f", "WAP", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getSmsFilterInfo(email, when, null));
        assertNull(f.getSmsFilterInfo(voicemail, when, null));
        assertNull(f.getSmsFilterInfo(faxmail, when, null));

        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNotNull(f.getMmsFilterInfo(voicemail, when, null));
        assertNull(f.getMmsFilterInfo(faxmail, when, null));

        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(voicemail, when, null));
        assertNotNull(f.getWapFilterInfo(faxmail, when, null));

        setFS("stop", 1, true, false, "a", "evf", "", "", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNull(f.getSmsFilterInfo(email, when, null));
        assertNull(f.getSmsFilterInfo(voicemail, when, null));
        assertNull(f.getSmsFilterInfo(faxmail, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(voicemail, when, null));
        assertNull(f.getMmsFilterInfo(faxmail, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(voicemail, when, null));
        assertNull(f.getWapFilterInfo(faxmail, when, null));
    }

    public void testActive() throws Exception {
        l("testActive");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        setFS("stop", 1, false, false, "a", "evf", "", "", "", "", false, "");
        setFS("email", 2, true, true, "a", "e", "SMS", "c", "", "", false, "");
        setFS("voice", 3, false, true, "a", "v", "MMS", "c", "", "", false, "");
        setFS("fax", 4, true, true, "a", "f", "WAP", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);
        assertNotNull(f.getSmsFilterInfo(email, when, null));
        assertNull(f.getSmsFilterInfo(voicemail, when, null));//Default no notification
        assertNull(f.getSmsFilterInfo(faxmail, when, null));

        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(voicemail, when, null));
        assertNull(f.getMmsFilterInfo(faxmail, when, null));

        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(voicemail, when, null));
        assertNotNull(f.getWapFilterInfo(faxmail, when, null));
    }

    public void testActiveSlamdown() throws Exception {
        l("testActiveSlamdown");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        setFS("stop", 1, false, false, "a", "evf", "", "", "", "", false, "");
        setFS("email", 2, true, true, "a", "e", "SMS", "c", "", "", false, "");
        setFS("voice", 3, false, true, "a", "v", "MMS", "c", "", "", false, "");
        setFS("fax", 4, true, true, "a", "f", "WAP", "c", "", "", false, "");
        setFS("slamdown", 5, true, true, "a", "s", "SMS", "slamdown", "", "", false, "");

        f= new NotificationFilter(filterSpec, false, user);

        assertNotNull(f.getSmsFilterInfo(slamdown, when, null));
        assertNull(f.getSmsFilterInfo(voicemail, when, null));//Default no notification
        assertNull(f.getSmsFilterInfo(faxmail, when, null));

        assertNull(f.getMmsFilterInfo(slamdown, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(voicemail, when, null));
        assertNull(f.getMmsFilterInfo(faxmail, when, null));

        assertNull(f.getWapFilterInfo(slamdown, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(voicemail, when, null));
    }

    public void testNoFilterForSlamdown() throws Exception {
        l("testActiveSlamdown");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        f= new NotificationFilter(null, false, user);
        assertNotNull(f.getSmsFilterInfo(slamdown, when, null));
        assertNull(f.getSmsFilterInfo(voicemail, when, null));//Default no notification
        assertNull(f.getSmsFilterInfo(faxmail, when, null));
        assertNull(f.getMmsFilterInfo(slamdown, when, null));
        assertNull(f.getMmsFilterInfo(email, when, null));
        assertNull(f.getMmsFilterInfo(voicemail, when, null));
        assertNull(f.getMmsFilterInfo(faxmail, when, null));
        assertNull(f.getWapFilterInfo(slamdown, when, null));
        assertNull(f.getWapFilterInfo(email, when, null));
        assertNull(f.getWapFilterInfo(voicemail, when, null));
    }

    public void testDeliverProfileNumber() throws Exception {
        String[] deliveryProfileStrings = new String[]{"111,222;SMS,ODL;",
                                                       "333;SMS,WAP,MMS;M",
	                                               "444,555;SMS,ODL;F"};

        setFS("single", 1, true, true, "a", "evf", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        String[] array = f.getMatchingDeliveryProfileNumbers("SMS");
        String[] expected = { "111", "222", "333", "444", "555" };
        for( int i=0;i<array.length;i++ ) {
            assertEquals( expected[i], array[i] );
        }
        array = f.getMatchingDeliveryProfileNumbers("ODL");
        String[] expected2 = { "111", "222", "444", "555" };
        for( int i=0;i<array.length;i++ ) {
            assertEquals( expected2[i], array[i] );
        }
        array = f.getMatchingDeliveryProfileNumbers("MWI");
        assertNull( array );

	    assertTrue("No mob/fixed set to mobile", f.isNumberMobile("111"));
	    assertTrue("Unknown set to mobile", f.isNumberMobile("321321"));
	    assertFalse("Fixed is not mobile", f.isNumberMobile("555"));
    }

    public void testDeliveryProfileTransports() throws Exception {
        String[] deliveryProfileStrings = new String[]{"111,222;MWI,ODL;",
                                                       "333;MWI,WAP,MMS;M",
                                                       "444;MWI;I",
                                                       "555,666;MWI,ODL;F"};
        setFS("single", 1, true, true, "a", "evf", "MWI", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        String[] array = f.getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_MOBILE);
        String[] expected = { "111", "222", "333" };
        for( int i=0;i<array.length;i++ ) {
            assertEquals( expected[i], array[i] );
        }
        array = f.getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_IP);
        String[] expected2 = { "444" };
        for( int i=0;i<array.length;i++ ) {
            assertEquals( expected2[i], array[i] );
        }

        array = f.getMatchingDeliveryProfileNumbers("ODL");
        String[] expected3 = { "111", "222", "555", "666" };
        for( int i=0;i<array.length;i++ ) {
            assertEquals( expected3[i], array[i] );
        }

    }

    public void testNotifNumbers() throws Exception {
        String[] deliveryProfileStrings = new String[]{"111,222;SMS,ODL;",
                                                       "333;SMS,WAP,MMS;M",
	                                               "444,555;SMS,ODL;F"};

        setFS("single", 1, true, true, "a", "evf", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, deliveryProfileStrings);
        // notif number = 9999901

        String[] smsNumbers = f.getNotifNumbers("SMS");
        String[] expectedSmsNumbers = { "111", "222", "333", "444", "555" };
        assertNotNull(smsNumbers);
        assertEquals(expectedSmsNumbers.length, smsNumbers.length);
        for( int i=0;i<expectedSmsNumbers.length;i++ ) {
            assertEquals(expectedSmsNumbers[i], smsNumbers[i] );
        }

        String[] mwiNumbers = f.getNotifNumbers("MWI");
        String[] expectedMwiNumbers = { "9999901" };
        assertNotNull(mwiNumbers);
        assertEquals(expectedMwiNumbers.length, mwiNumbers.length);
        for( int i=0;i<expectedMwiNumbers.length;i++ ) {
            assertEquals(expectedMwiNumbers[i], mwiNumbers[i] );
        }


    }

    public void testNotifTypeOnFilter() throws Exception {
        l("testNotifTypeOnFilter");
        when= new GregorianCalendar(2002, 8, 23, 12, 00);
        clearAllFS();
        setFS("stop", 1, false, false, "a", "evf", "", "", "", "", false, "");
        setFS("email", 2, true, true, "a", "e", "SMS", "c", "", "", false, "");
        setFS("voice", 3, false, true, "a", "v", "MMS", "c", "", "", false, "");
        setFS("fax", 4, true, true, "a", "f", "WAP", "c", "", "", false, "");
        f= new NotificationFilter(filterSpec, false, user);

        assertTrue(f.isNotifTypeOnFilter("SMS"));
        assertTrue(f.isNotifTypeOnFilter("WAP"));
        assertFalse(f.isNotifTypeOnFilter("MMS"));
        assertFalse(f.isNotifTypeOnFilter("MWI"));

    }

    public void testFilterMatchesMailType() throws Exception {
        clearAllFS();
        setFS("A", 1, true, true, "a", "evfm", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, null);

        assertTrue(f.filterMatchesMailType(NTF_EMAIL, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_VOICE, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_VIDEO, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_FAX, "SMS"));

        assertFalse(f.filterMatchesMailType(NTF_EMAIL, "MWI"));
        assertFalse(f.filterMatchesMailType(NTF_VOICE, "FLS"));
        assertFalse(f.filterMatchesMailType(NTF_VIDEO, "ODL"));
        assertFalse(f.filterMatchesMailType(NTF_FAX, "MWI"));

        clearAllFS();
        setFS("A", 1, true, true, "a", "e", "MWI", "c", "", "", false, "", null);
        setFS("B", 2, false, true, "a", "evfm", "SMS", "c", "", "", false, "", null);
        setFS("C", 3, true, false, "a", "evfm", "FLS", "c", "", "", false, "", null);
        setFS("D", 4, true, true, "b", "fm", "SMS", "c", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, null);

        assertFalse(f.filterMatchesMailType(NTF_EMAIL, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_EMAIL, "MWI"));
        assertFalse(f.filterMatchesMailType(NTF_VOICE, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_VIDEO, "SMS"));
        assertTrue(f.filterMatchesMailType(NTF_FAX, "SMS"));
        assertFalse(f.filterMatchesMailType(NTF_FAX, "FLS"));
    }

    public void testGetTemplatesForType() throws Exception {
        clearAllFS();
        setFS("A", 1, true, true, "a", "e", "MWI", "c", "", "", false, "", null);
        setFS("B", 2, false, true, "a", "evfm", "SMS", "c", "", "", false, "", null);
        setFS("C", 3, true, false, "a", "evfm", "FLS", "c", "", "", false, "", null);
        setFS("D", 4, true, true, "a", "fm", "SMS", "c", "", "", false, "", null);
        setFS("E", 5, true, true, "b", "fm", "SMS", "s", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, null);

        assertNull(f.getTemplatesForType(NTF_EMAIL, "SMS"));
        assertNotNull(f.getTemplatesForType(NTF_EMAIL, "MWI"));
        assertNull(f.getTemplatesForType(NTF_VOICE, "SMS"));
        String[] templates = f.getTemplatesForType(NTF_FAX, "SMS");
        assertNotNull(templates);
        assertEquals(2, templates.length);
        assertEquals("c", templates[0]);
        assertEquals("s", templates[1]);
    }

    public void testHasNotifType() throws Exception {
        clearAllFS();
        setFS("A", 1, true, true, "a", "e", "MWI", "c", "", "", false, "", null);
        setFS("B", 2, false, true, "a", "evfm", "SMS", "c", "", "", false, "", null);
        setFS("C", 3, true, false, "a", "evfm", "FLS", "c", "", "", false, "", null);
        setFS("D", 4, true, true, "a", "fm", "SMS", "c", "", "", false, "", null);
        setFS("E", 5, true, true, "b", "fm", "SMS", "s", "", "", false, "", null);
        f= new NotificationFilter(filterSpec, false, user, null);

        assertTrue(f.hasNotifType("SMS"));
        assertTrue(f.hasNotifType("MWI"));
        assertFalse(f.hasNotifType("FLS"));
        assertFalse(f.hasNotifType("ODL"));

    }

    /*
     * Set the named filter string according to the parameters, creating it if
     * necessary.
     */
    private void setFS(String name,
                       int prio,
                       boolean active,
                       boolean notify,
                       String time,
                       String depType,
                       String notifType,
                       String content,
                       String from,
                       String subject,
                       boolean urgent,
                       String voiceFaxFrom) {
        fSpec.setProperty(name, ""
                          + (active? "1": "0") + ";"
                          + (notify? "y": "n") + ";"
                          + time + ";"
                          + depType + ";"
                          + notifType + ";"
                          + content + ";"
                          + prio + ";"
                          + from + ";"
                          + subject + ";"
                          + (urgent? "y": "") + ";"
                          + voiceFaxFrom + ";"
                          + name + ";"
                          + ";");
        filterSpec= (String[])(fSpec.values().toArray(filterSpec));
    }

    private void setFS(String name,
                       int prio,
                       boolean active,
                       boolean notify,
                       String time,
                       String depType,
                       String notifType,
                       String content,
                       String from,
                       String subject,
                       boolean urgent,
                       String voiceFaxFrom,
                       String readOnly) {
        fSpec.setProperty(name, ""
                          + (active? "1": "0") + ";"
                          + (notify? "y": "n") + ";"
                          + time + ";"
                          + depType + ";"
                          + notifType + ";"
                          + content + ";"
                          + prio + ";"
                          + from + ";"
                          + subject + ";"
                          + (urgent? "y": "") + ";"
                          + voiceFaxFrom + ";"
                          + name + ";"
                          + readOnly +
                          ";");
        filterSpec= (String[])(fSpec.values().toArray(filterSpec));
    }

    private void removeFS(String name) {
        fSpec.remove(name);
        if (fSpec.size() > 0) {
            filterSpec= (String[])(fSpec.values().toArray());
        } else {
            filterSpec= new String[0];
        }
    }

    private void clearAllFS() {
        fSpec = new Properties();
        filterSpec= new String[0];
    }
}
