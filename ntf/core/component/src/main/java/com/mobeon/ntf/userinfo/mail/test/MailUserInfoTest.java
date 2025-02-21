/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.mail.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mail.MailUserInfo;
import java.util.*;
import junit.framework.*;

/**
 * Test of MurUMx_xUserInfo. This "anonymous" test should test the latest
 * version of the class. Tests for user info classes for older schema versions
 * (for backwards compatibility) should include the schema version in their
 * name.
 */
public class MailUserInfoTest extends NtfTestCase implements Constants {
    private static String mailhost = Config.getImapHost();

    private UserInfo user1;
    private UserInfo user2;
    private UserInfo user3;
    private UserInfo user4;
    private UserInfo user5;
    private UserInfo noSuchUser;
    private boolean found1;
    private boolean found2;
    private boolean found3;
    private boolean found4;
    private boolean found5;
    private NotificationEmail email1;

    public MailUserInfoTest(String name) {
        super(name);
    }

    protected void setUp() {
        email1 = new NotificationEmail(9999,
            "To: notification.off@" + mailhost + "\r\n"
            + "Subject: ipms/message\r\n"
            + "Ipms-Notification-Type: ntf.internal.sendtononsubscriber\r\n"
            + "X-Ipms-User-Attribute-telephonenumber: 123456\r\n"
            + "X-Ipms-User-Attribute-preferredLanguage: en\r\n"
            + "X-Ipms-User-Attribute-emFilter: 1;y;a;evf;SMS;ivrtosms;1;;;;;test;;\r\n"
            + "\r\n");

    }

    /*
     * Test that find finds an entry if it exists, that it finds the correct
     * entry, and that it does not find non-existing entries.
     */
    public void test() throws Exception {
        l("test");
        user1 = new MailUserInfo(email1);

        assertEquals("mail.123456", user1.getFullId());
        assertEquals("Non-mur-123456", user1.getMail());
        // No UserDevice: assertNotNull(user1.getDevices());
        // No UserDevice: assertEquals(1, user1.getDevices().size());
        assertEquals("123456", user1.getTelephoneNumber());
        NotificationFilter filter = user1.getFilter();
        assertNotNull(filter);
        assertTrue(!filter.isNotifDisabled());
        assertNull(filter.getOdlFilterInfo(email1, new GregorianCalendar(), null));
        assertNull(filter.getMmsFilterInfo(email1, new GregorianCalendar(), null));
        assertNull(filter.getWapFilterInfo(email1, new GregorianCalendar(), null));
        assertNull(filter.getWmwFilterInfo(email1, new GregorianCalendar(), null));
        SmsFilterInfo smsFilter = filter.getSmsFilterInfo(email1, new GregorianCalendar(), null);
        assertNotNull(smsFilter);
        assertEquals(1, smsFilter.getNumbers().length );
        //assertEquals(1, smsFilter[0].getDevices().length);
        assertTrue(!smsFilter.isMwi());
        assertTrue(smsFilter.isSms());
        assertEquals("ivrtosms", smsFilter.getNotifContent());
        assertTrue(!smsFilter.hasMwiCount());
        assertNull(user1.getMmsCenterId());
        assertEquals(6, user1.getNotifExpTime());
        assertEquals("123456", user1.getNotifNumber());
        assertEquals(1, user1.getNumberingPlan());
        assertEquals("yyyy/mm/dd", user1.getPreferredDateFormat());
        assertEquals("24", user1.getPreferredTimeFormat());
        GregorianCalendar cal = new GregorianCalendar(2002, 8, 2, 13, 0);
        assertEquals("2002/09/02", user1.getUsersDate(cal.getTime()));
        assertEquals("13:00", user1.getUsersTime(cal.getTime()));
        //JUNIT DEADLOCKS HERE !!! WHY ?
        //assertEquals("testSmsc", user1.getSmscId());
        assertEquals(1, user1.getTypeOfNumber());
        assertTrue(!user1.isBusinessTime(cal));
        assertTrue(user1.isMailboxDelivery());
        assertTrue(!user1.isAdministrator());
        assertEquals("en", user1.getPreferredLanguage());
        assertEquals("Non-mur", user1.getLogin());
        assertNull(user1.getWapGatewayId());
        assertTrue(!user1.isOutdialUser());
        assertTrue(!user1.isMwiUser());

        assertTrue(user1.hasMailType(2));
        assertEquals(2, user1.getServices().length);
        assertEquals("emservicename=msgtype_email,ou=services,o=non-mur", user1.getServices()[0]);
        assertEquals("emservicename=sms_notification,ou=services,o=non-mur", user1.getServices()[1]);
    }
}
