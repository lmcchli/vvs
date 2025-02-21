/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.abcxyz.services.moip.ntf;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.NtfCompletedListener;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.UserInfoAdapter;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.Constants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests NotificationGroup.
 */
public class NotificationGroupTest implements Constants {

    private LogAgent log = NtfCmnLogger.getLogAgent(NotificationHandler.class);
    private NotificationGroup ng = null;
    private CompletedListener completedListener = null;
    private NotificationEmail email= null;
    private UserInfoAdapter user1;
    private UserInfoAdapter user2;
    private TestMerAgent mer;
    private NtfEvent ntfEvent = null;

    private int completeCalls = 0;
    private int retryCalls = 0;
    private int failedCalls = 0;

    @BeforeClass
    static public void startup() throws Exception {
    }

    @AfterClass
    static public void tearDown() {
    }

    @Test
    public void testPendingRequest() throws Exception {
        NotificationGroup ng = this.createNotificationGroup();
        ng.setOutCount(user1, 4);
        ng.setOutCount(user2, 2);

        // All response cases (but OK) result in decreasing the feedback
        ng.failed(user1, Constants.NTF_SMS, null);
        assertEquals(3, ng.getOutCount(user1));
        assertEquals(0, completeCalls);
        assertEquals(0, retryCalls);
        assertTrue(ng.containsUser(user1));
        ng.expired(user1, NTF_SMS);
        assertEquals(2, ng.getOutCount(user1));
        assertEquals(0, completeCalls);
        assertEquals(0, retryCalls);
        assertTrue(ng.containsUser(user1));
        ng.retry(user1, NTF_SMS, null);
        assertEquals(1, ng.getOutCount(user1));
        assertEquals(0, completeCalls);
        assertEquals(0, retryCalls);
        assertTrue(ng.containsUser(user1));
        ng.ok(user1, NTF_MWI);
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(0, completeCalls);
        assertEquals(0, retryCalls);
        assertFalse(ng.containsUser(user1));

        // An OK response clears the feedback for the given user
        ng.ok(user2, NTF_SMS);
        assertFalse(ng.containsUser(user2));
        
        // No final answer to the CompletedListener since we may have extra users to deal with 
        assertEquals(0, completeCalls);
        assertEquals(0, retryCalls);
        
        // Confirmation of no new users to add to the NotificationGroup
        ng.noMoreUsers();
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
    }

    @Test
    public void testFeedback() throws Exception {
        NotificationGroup ng = this.createNotificationGroup();
        ng.noMoreUsers();
        
        ng.setOutCount(user1, 4);
        ng.ok(user1, NTF_SMS);
        // All the feedbacks are needed before sending back a response, here: user2 still waiting.
        assertFalse(ng.containsUser(user1));
        assertEquals(0, completeCalls);

        ng.ok(user2, NTF_SMS);
        assertFalse(ng.containsUser(user2));

        // All the feedbacks provided, CompletedListener must have been called.
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);
        
        /* TODO: MER must be fixed first
        assertEquals(3, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
        */
    }

    @Test
    public void testRetry() throws Exception {
        NotificationGroup ng = this.createNotificationGroup();
        ng.setOutCount(user1, 2);
        ng.setOutCount(user2, 1);
        ng.noMoreUsers();

        ng.ok(user2, NTF_SMS);
        ng.expired(user1, NTF_MMS);
        ng.retry(user1, NTF_SMS, "Test retry 1");
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(0, completeCalls);
        assertEquals(1, retryCalls);

        /* TODO: MER must be fixed first
        assertEquals(1, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());*/
    }


    @Test
    public void testAsync() throws Exception {
        NotificationGroup ng = this.createNotificationGroup();
        ng.ok(user2, NTF_SMS);
        assertEquals(0, ng.getOutCount(user2));

        // Test that feedback calls may come before setOutCount
        ng.expired(user1, NTF_SMS);
        assertEquals(-1, ng.getOutCount(user1));
        ng.setOutCount(user1, 2);
        assertEquals(1, ng.getOutCount(user1));
        ng.noMoreUsers();
        
        ng.failed(user1, NTF_SMS, "Test failed 1");
        assertEquals(0, ng.getOutCount(user1));
        assertEquals(1, completeCalls);
        assertEquals(0, retryCalls);

        /* TODO: MER must be fixed first
        assertEquals(1, mer.getDelivered());
        assertEquals(1, mer.getFailed());
        assertEquals(0, mer.getExpired());*/
    }

    @Test
    public void testNoMerEvent() throws Exception {
        NotificationGroup ng = this.createNotificationGroup();
        ng.noMerEvent();
        ng.ok(user1, NTF_SMS);
        ng.ok(user2, NTF_SMS);
        assertEquals(0, mer.getDelivered());
        assertEquals(0, mer.getFailed());
        assertEquals(0, mer.getExpired());
    }

    private NotificationGroup createNotificationGroup() {
        
        final MessageInfo msgInfo = new MessageInfo();
        msgInfo.omsa = new MSA("omsa");
        msgInfo.omsgid = "omsgid";
        msgInfo.rmsa = new MSA("rmsa");
        msgInfo.rmsgid = "rmsgid";

        ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), msgInfo, null, "id");
        
        mer = new TestMerAgent();
        email = new NotificationEmail(ntfEvent);
        completedListener = new CompletedListener();
        ng = new NotificationGroup(completedListener, email, log, mer);
        
        user1 = new UserInfoAdapter();
        user2 = new UserInfoAdapter();
        ng.addUser(user1);
        ng.addUser(user2);
        
        return ng; 
    }
    
    class CompletedListener implements NtfCompletedListener {
        public void notifCompleted(NtfEvent event) {
            ++completeCalls;
        }

        public void notifRetry(NtfEvent event) {
            ++retryCalls;
        }

        public void notifFailed(NtfEvent event) {
            ++failedCalls;
        }
    }
    
}
