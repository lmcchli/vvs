/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.mail.*;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.mail.test.MailtestUtil;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestUser;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.mail.EmailListHandler;
import java.util.*;
import java.io.*;
import junit.framework.*;

/**
 * This class tests UserMailbox.
 */
public class EmailListHandlerTest extends NtfTestCase implements Constants {

    static EmailListHandler handler = null;
    static boolean reply = true;
    static boolean ok = true;
    static boolean threads = false;
    static boolean wait = false;
    static Object waitObject = new Object();

    public EmailListHandlerTest(String name) {
	super(name);
    }


    protected void setUp() {
        if( handler == null ) {
            ThreadGroup g = new ThreadGroup("test");
            TestEmailStore store = new TestEmailStore();
            File dataFile = new File(Config.getDataDirectory() + "/EmailList.current");
            dataFile.delete();
            dataFile = new File(Config.getDataDirectory() + "/EmailList.last");
            dataFile.delete();
            handler = new EmailListHandler(g, store, 1 );
            handler.setDaemon(true);
            handler.MAXREPLYTIME = 2000;
            handler.start();
            Config.setCfgVar("smsmaxconn", "4");

        }

    }



    public void testOk() throws Exception {
        l("testOk");
        reply = true;
        ok = true;
        Vector info = new Vector();
	info.add(new SlamdownInfo("kalle@test.com", "message", 1));
	handler.addEmailList(info);

        Thread.sleep(5000);
        assertEquals(0, handler.getPendingCount());

        ok = false;
        Vector info2 = new Vector();
	info2.add(new SlamdownInfo("pelle@test.com", "message", 1));
	handler.addEmailList(info2);

        Thread.sleep(5000);
        assertEquals(0, handler.getPendingCount());
    }

    public void testNoReply() throws Exception {
        l("testNoReply");
        reply = false;
        Vector info = new Vector();
	info.add(new SlamdownInfo("kalle@test.com", "message", 1));
	handler.addEmailList(info);

        Thread.sleep(500);
        assertEquals(1, handler.getPendingCount());

        Vector info2 = new Vector();
	info2.add(new SlamdownInfo("pelle@test.com", "message", 1));
	handler.addEmailList(info2);
        Thread.sleep(500);
        assertEquals(2, handler.getPendingCount());

        // wait, the counter should be 0 since no replya has been done.
        Thread.sleep(handler.MAXREPLYTIME + 500);
        assertEquals(0, handler.getPendingCount());

    }

    public void testLongMix() throws Exception {
        l("testLongMix");
        reply = true;
        ok = true;

        Vector info = new Vector();
        info.add(new SlamdownInfo("kalle@test.com", "message", 1));
        info.add(new SlamdownInfo("pelle@test.com", "message", 1));
        info.add(new SlamdownInfo("olle@test.com", "message", 1));
        handler.addEmailList(info);

        Thread.sleep(500);
        assertEquals(0, handler.getPendingCount());

        reply = false;

        info = new Vector();
        for( int i=0;i<20;i++) {
            info.add(new SlamdownInfo("kalle@test.com", "message", 1));
        }


        handler.addEmailList(info);
        Thread.sleep(200);
        assertEquals(2, handler.getPendingCount());
        assertEquals(18, handler.getEmailCount());

        Thread.sleep(handler.MAXREPLYTIME + 200);
        assertEquals(2, handler.getPendingCount());
        assertEquals(16, handler.getEmailCount());

        reply = true;
        Thread.sleep(handler.MAXREPLYTIME + 200);
        assertEquals(0, handler.getPendingCount());
        assertEquals(0, handler.getEmailCount());
    }

    public void testNoReplyNoThread() throws Exception {
        l("testNoReplyNoThread");
        reply = false;
	handler.stopCleaner();

	Vector info = new Vector();
	info.add(new SlamdownInfo("kalle@test.com", "message", 1));
	handler.addEmailList(info);

        Thread.sleep(500);
        assertEquals(1, handler.getPendingCount());

        Vector info2 = new Vector();
	info2.add(new SlamdownInfo("pelle@test.com", "message", 1));
	handler.addEmailList(info2);
        Thread.sleep(500);
        assertEquals(2, handler.getPendingCount());

        Thread.sleep(handler.MAXREPLYTIME + 500);
        assertEquals(0, handler.getPendingCount());

        // set reply ok and send again. The lock hould be cleared now.
        reply = true;
        Vector info3 = new Vector();
	info3.add(new SlamdownInfo("olle@test.com", "message", 1));
	handler.addEmailList(info3);
        Thread.sleep(500);
        assertEquals(0, handler.getPendingCount());


    }
    /**
     *Test for previous synchronization problem where it could hang.
     */
    public void testSynchro() throws Exception {
        threads = true;
        handler.MAXREPLYTIME = 60000;
        Vector info = null;
        info = new Vector();
        for( int i=0;i<4;i++) {
            info.add(new SlamdownInfo("kalle@test.com", "message", 1));
        }
        handler.addEmailList(info);
        Thread.sleep(500);
        assertEquals(2, handler.getEmailCount());
        assertEquals(2, handler.getPendingCount());

        wait = true;
        handler.resetPendingCount();

        Thread.sleep(5000);
        assertEquals(1, handler.getEmailCount());
        assertEquals(0, handler.getPendingCount());

        wait = false;

        synchronized(waitObject) {
            waitObject.notifyAll();
        }
        Thread.sleep(3000);
        assertEquals(0, handler.getEmailCount());
        assertEquals(0, handler.getPendingCount());

        handler.MAXREPLYTIME = 2000;
        threads = false;
    }

    private class TestEmailStore implements EmailStore {
        private TestCleaner cleaner;

        public TestEmailStore() {
            cleaner = new TestCleaner();
        }

        public void putEmail(NotificationEmail email) {
            l("Got message " + email.getMessageId() );
            if( wait) {
                try {
                    synchronized(waitObject) {
                        waitObject.wait();
                    }
                } catch (Exception e) { l(e.toString()); }
            }
            final NotificationGroup ng = new NotificationGroup(cleaner, email, Logger.getLogger(), MerAgent.get());
            final TestUser user = new TestUser();
            ng.addUser(user);
            ng.setOutCount(user, 1);
            ng.noMoreUsers();
            if( reply ) {
                if( threads ) {
                    new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                ng.ok(user,NTF_SMS);

                            } catch(Exception e) { l(e.toString()); }

                        }
                    }.start();
                } else {
                    if( ok ) {
                        ng.ok(user, NTF_SMS);
                    } else {
                        ng.retry(user, NTF_SMS, "retryAddress");
                    }
                }
            }

        }

    }

    private class TestCleaner implements NotifCompletedListener {
         public void notifCompleted(int notifId, int receiver) {
            handler.notifCompleted(notifId);
         }
         public void notifRetry(int notifId, int receiver, String retryAddresses) {
             handler.notifRetry(notifId, retryAddresses);
         }

        public void notifRenew(int notifId, int receiver) {

        }

        public void notifRelease(int notifId, int receiver) {

        }
    }

}


