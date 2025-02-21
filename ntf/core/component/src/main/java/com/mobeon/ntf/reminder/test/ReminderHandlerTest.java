package com.mobeon.ntf.reminder.test;

import com.mobeon.common.storedelay.*;
import com.mobeon.ntf.reminder.ReminderInfo;
import com.mobeon.ntf.reminder.ReminderHandler;
import com.mobeon.ntf.reminder.ReminderCaller;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.DelayLoggerProxy;

import java.util.Properties;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-apr-12
 * Time: 10:02:20
 * To change this template use File | Settings | File Templates.
 */
public class ReminderHandlerTest extends NtfTestCase {
    private DelayHandler delayer;
    private int sendCount;

    public ReminderHandlerTest(String name) {
        super(name);
    }

    public void testInfo() throws Exception {
        ReminderInfo info = new ReminderInfo("mail@mobeon.com", new Date().getTime(), "userDn"  );
        assertEquals("mail@mobeon.com", info.getUserEmail());
        assertEquals("userDn", info.getUserDN());
        assertEquals(0, info.getRetryAttempts());

        DelayInfo dInfo = info.getPersistentRepresentation();
        ReminderInfo info2 = new ReminderInfo(dInfo);

        assertEquals(info.getUserEmail(), info2.getUserEmail());
        assertEquals(info.getUserDN(), info2.getUserDN());
        assertEquals(info.getRetryAttempts(), info2.getRetryAttempts());

        DelayInfo dInfo2 = info2.getPersistentRepresentation();
        assertEquals(dInfo, dInfo2);
    }

    public void testFull() throws Exception {
        sendCount = 0;
        SDLogger.setLogger(new DelayLoggerProxy());
        makeDelayer();
        UserFactory userFactory = new UserFactory();
        ReminderHandler handler = new ReminderHandler(delayer, userFactory);
        handler.setReminderCaller(new TestCaller());
        delayer.registeringDone();


        UserInfo user = userFactory.findUserByMail("junit01@" + mailDomain);
        Config.setCfgVar("unreadmessageremindertype", "SMS");
        Config.setCfgVar("unreadmessagereminderinterval", "1");
        Config.setCfgVar("unreadmessageremindermaxtimes", "2");

        boolean res = handler.doReminder(user);
        assertTrue(res);
        Thread.sleep(3500);

        assertEquals(2, sendCount);
        sendCount = 0;

        handler.doReminder(user);
        Thread.sleep(1500);
        assertEquals(1, sendCount);
        handler.cancel(user);
        Thread.sleep(2000);
        assertEquals(1, sendCount);

        sendCount = 0;
        Config.setCfgVar("unreadmessageremindertype", "none");
        res = handler.doReminder(user);
        assertFalse(res);

    }

    private void makeDelayer()
        throws DelayException
    {
        Properties p = new Properties();
        p.put(DBDelayHandler.KEY_STORAGE_DIR, "/tmp/ntf/outdial");
        p.put(DBDelayHandler.KEY_STORAGE_BASE, "odllistener");
        delayer = new DBDelayHandler(p);
    }

    private class TestCaller implements ReminderCaller {


        public void send(ReminderInfo info, UserInfo user, UserMailbox inbox) {
            sendCount++;
        }
    }
}
