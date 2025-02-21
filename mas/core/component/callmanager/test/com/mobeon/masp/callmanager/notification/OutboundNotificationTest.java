/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.notification;
/**
 * Date: 2007-mar-09
 * @author mmath
 */

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.EventDispatcherMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.SessionMock;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.sipunit.NotifyReceiverSimulator;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.MockObjectTestCase;

public class OutboundNotificationTest extends MockObjectTestCase {

    IEventDispatcher eventDispatcher;
    OutboundNotification notification;
    ISession mySession;

    Map<String, String> pmap;
    Map<String, String> pmap1;
    final String MSG_ACCOUNT1 = "001234";
    final String SEND_TO1 = "1234";

    protected SystemSimulator simulatedSystem;
    private CallManagerToVerify callManager;

        protected NotifyReceiverSimulator notifyReceiverSim;


        public void setUp() throws Exception {
            System.gc();
            ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

            simulatedSystem = new SystemSimulator();

            // Create and initiate call manager
            callManager = new CallManagerToVerify(
                    InetAddress.getLocalHost().getHostAddress(), 5060,
                    CallManagerTestContants.CALLMANAGER_WITH_REMOTEUA_XML, simulatedSystem);

            // Create the simulated system, i.e. execution engine etc.
            simulatedSystem.create(callManager);

            eventDispatcher = new EventDispatcherMock();
            mySession = new SessionMock();

            pmap = new HashMap<String,String>();

            pmap1 = new HashMap<String,String>();
            pmap1.put(CallManager.SEND_TO, SEND_TO1);
            pmap1.put(CallManager.MESSAGE_ACCOUNT, MSG_ACCOUNT1);
            pmap1.put(CallManager.VOICE_MESSAGE_NEW, "3");
            pmap1.put(CallManager.VOICE_MESSAGE_OLD, "2");

            notification = new OutboundNotification(CallManager.METHOD_MWI,
                    pmap1, eventDispatcher, mySession);


        }



//    protected void setUp() throws Exception {
//
//        eventDispatcher = new EventDispatcherMock();
//        session = new SessionMock();
//
//        pmap = new HashMap<String,String>();
//
//        pmap1 = new HashMap<String,String>();
//        pmap1.put(CallManager.SEND_TO, SEND_TO1);
//        pmap1.put(CallManager.MESSAGE_ACCOUNT, MSG_ACCOUNT1);
//        pmap1.put(CallManager.VOICE_MESSAGE_NEW, "3");
//        pmap1.put(CallManager.VOICE_MESSAGE_OLD, "2");
//
//        notification = new OutboundNotification(CallManager.METHOD_MWI,
//                pmap1, eventDispatcher, session);
//
//    }


    public void tearDown() throws Exception {

        // Wait a while to make sure that the SIP stack is ready to be deleted
        Thread.sleep(100);
        callManager.delete();
        simulatedSystem.delete();
    }


    /**
     * Negative tests (Error handling)
     * @throws Exception
     */
    public void testCreateMwiBody_neg() throws Exception {

        assertNull(notification.createMwiBody(null));

        String typeList[] = {
                CallManager.VOICE_MESSAGE_NEW,
                CallManager.VOICE_MESSAGE_OLD,
                CallManager.FAX_MESSAGE_NEW,
                CallManager.FAX_MESSAGE_OLD,
                CallManager.VIDEO_MESSAGE_NEW,
                CallManager.VIDEO_MESSAGE_OLD,
                CallManager.EMAIL_MESSAGE_NEW,
                CallManager.EMAIL_MESSAGE_OLD,
        };

        String errStrings[] = { "", "123abc", "-1",
                "5000000000", "987236495782369457826394569234752389452723"};

        for (String type : typeList) {
            for (String testStr : errStrings) {
                pmap.clear();
                pmap.put(CallManager.VOICE_MESSAGE_NEW, "0");
                pmap.put(CallManager.VOICE_MESSAGE_OLD, "1");
                pmap.put(CallManager.FAX_MESSAGE_NEW,   "2");
                pmap.put(CallManager.FAX_MESSAGE_OLD,   "4");
                pmap.put(CallManager.VIDEO_MESSAGE_NEW, "5");
                pmap.put(CallManager.VIDEO_MESSAGE_OLD, "6");
                pmap.put(CallManager.EMAIL_MESSAGE_NEW, "7");
                pmap.put(CallManager.EMAIL_MESSAGE_OLD, "8");
                pmap.put(type, testStr); // Modify one of them to be faulty
                assertNull("Type="+type+" testStr="+testStr,notification.createMwiBody(pmap));
            }
        }

        pmap.clear();
        pmap.put(CallManager.MESSAGES_WAITING,"abc");
        assertNull(notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.MESSAGES_WAITING,"");
        assertNull(notification.createMwiBody(pmap));



    }

    public void testCreateMwiBody() throws Exception {

        // Test that default counter values is zero
        pmap.clear();
        pmap.put(CallManager.VOICE_MESSAGE_OLD,"3");
        assertEquals(mwiWait("no") + mwiVoice(0,3), notification.createMwiBody(pmap));
       /* String body = notification.createMwiBody(pmap);
        assertTrue( body.contains(mwiWait("no") + mwiVoice(0,3)));*/

        pmap.clear();
        pmap.put(CallManager.VOICE_MESSAGE_NEW,"4");
        assertEquals(mwiWait("yes") + mwiVoice(4,0),notification.createMwiBody(pmap));
        //assertTrue( notification.createMwiBody(pmap).matches(mwiWait("yes") + mwiVoice(4,0)+"*"));

        pmap.clear();
        pmap.put(CallManager.FAX_MESSAGE_OLD,"3");
        assertEquals(mwiWait("no") + mwiFax(0,3), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.FAX_MESSAGE_NEW,"4");
        assertEquals(mwiWait("yes") + mwiFax(4,0), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.VIDEO_MESSAGE_OLD,"3");
        assertEquals(mwiWait("no") + mwiMM(0,3), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.VIDEO_MESSAGE_NEW,"4");
        assertEquals(mwiWait("yes") + mwiMM(4,0), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.EMAIL_MESSAGE_OLD,"3");
        assertEquals(mwiWait("no") + mwiText(0,3), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.EMAIL_MESSAGE_NEW,"4");
        assertEquals(mwiWait("yes") + mwiText(4,0), notification.createMwiBody(pmap));

        // Test message-waiting
        pmap.clear();
        pmap.put(CallManager.MESSAGES_WAITING,"yes");
        assertEquals(mwiWait("yes"), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.MESSAGES_WAITING,"no");
        assertEquals(mwiWait("no"), notification.createMwiBody(pmap));

        // Default is "no"
        pmap.clear();
        assertEquals(mwiWait("no"), notification.createMwiBody(pmap));

        pmap.clear();
        pmap.put(CallManager.SEND_TO, SEND_TO1);
        pmap.put(CallManager.MESSAGE_ACCOUNT, MSG_ACCOUNT1);
        pmap.put(CallManager.VOICE_MESSAGE_NEW,"0");
        pmap.put(CallManager.VOICE_MESSAGE_OLD,"1");
        pmap.put(CallManager.FAX_MESSAGE_NEW,"2");
        pmap.put(CallManager.FAX_MESSAGE_OLD,"4");
        pmap.put(CallManager.VIDEO_MESSAGE_NEW,"5");
        pmap.put(CallManager.VIDEO_MESSAGE_OLD,"6");
        pmap.put(CallManager.EMAIL_MESSAGE_NEW,"7");
        pmap.put(CallManager.EMAIL_MESSAGE_OLD,"8");
        pmap.put("foo", "bar");

        assertEquals(mwiWait("yes") + mwiAcc(MSG_ACCOUNT1) +
                mwiVoice(0,1) + mwiFax(2,4) +
                mwiMM(5,6) + mwiText(7,8),
                notification.createMwiBody(pmap));


        // Test that messages-waiting have precedance
        pmap.clear();
        pmap.put(CallManager.SEND_TO, SEND_TO1);
        pmap.put(CallManager.MESSAGE_ACCOUNT, MSG_ACCOUNT1);
        pmap.put(CallManager.MESSAGES_WAITING, "no");
        pmap.put(CallManager.VOICE_MESSAGE_NEW,"0");
        pmap.put(CallManager.VOICE_MESSAGE_OLD,"1");
        pmap.put(CallManager.FAX_MESSAGE_NEW,"2");
        pmap.put(CallManager.FAX_MESSAGE_OLD,"4");
        pmap.put(CallManager.VIDEO_MESSAGE_NEW,"5");
        pmap.put(CallManager.VIDEO_MESSAGE_OLD,"6");
        pmap.put(CallManager.EMAIL_MESSAGE_NEW,"7");
        pmap.put(CallManager.EMAIL_MESSAGE_OLD,"8");
        pmap.put("foo", "bar");

        assertEquals(mwiWait("no") + mwiAcc(MSG_ACCOUNT1) +
                mwiVoice(0,1) + mwiFax(2,4) +
                mwiMM(5,6) + mwiText(7,8),
                notification.createMwiBody(pmap));

    }




    // --- Helper methods ---
    private String mwiAcc(String messageAccount) {
        try {
            // Todo: fetch addres & port from stack?!
            return "Message-Account: sip:" + messageAccount + "@" +
                    InetAddress.getLocalHost().getHostAddress() + ":5060" +
                    ";user=phone\r\n";
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String mwiWait(String waiting) {
        return "Messages-Waiting: " + waiting + "\r\n";
    }

    private String mwiVoice(long n, long o) {
        return "Voice-Message: " + n + "/" + o + "(0/0)\r\n";
    }

    private String mwiFax(long n, long o) {
        return "Fax-Message: " + n + "/" + o + "\r\n";
    }

    private String mwiMM(long n, long o) {
        return "Multimedia-Message: " + n + "/" + o + "\r\n";
    }

    private String mwiText(long n, long o) {
        return "Text-Message: " + n + "/" + o + "\r\n";
    }

    public void testOutboundNotification() throws Exception {
        try {
            new OutboundNotification("blaha", pmap1, eventDispatcher,
                    mySession);
            fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new OutboundNotification(null, pmap1, eventDispatcher, mySession);
            fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new OutboundNotification(CallManager.METHOD_MWI, null,
                    eventDispatcher, mySession);
            fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new OutboundNotification(CallManager.METHOD_MWI, pmap1, null,
                    mySession);
            fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }

    }

    public void testCreateSipMessageResponseEvent() throws Exception {
        try {
            notification.createSipMessageResponseEvent(99, "abc", null);
            fail("Should have thrown an Exception");
        } catch (IllegalArgumentException ex) {}

        try {
            notification.createSipMessageResponseEvent(-1, "abc", null);
            fail("Should have thrown an Exception");
        } catch (IllegalArgumentException ex) {}
        try {
            notification.createSipMessageResponseEvent(1000, "abc", null);
            fail("Should have thrown an Exception");
        } catch (IllegalArgumentException ex) {}


        Map<String,String> pmap;
        SipMessageResponseEvent event;
        Iterator it;


        // Test 200 abc, no retry after
        pmap = new HashMap<String,String>();
        event = notification.createSipMessageResponseEvent(200, "abc", null);
        it = event.getParams().iterator();
        while (it.hasNext()) {
            NamedValue<String,String> nv = (NamedValue<String,String>) it.next();
            pmap.put(nv.getName(),nv.getValue());
        }
        assertEquals("200",pmap.get("responsecode"));
        assertEquals("abc",pmap.get("responsetext"));
        assertEquals(2,pmap.size());


        // Test 400 Bad request
        pmap = new HashMap<String,String>();
        event = notification.createSipMessageResponseEvent(400, "Bad request", 42);
        it = event.getParams().iterator();
        while (it.hasNext()) {
            NamedValue<String,String> nv = (NamedValue<String,String>) it.next();
            pmap.put(nv.getName(),nv.getValue());
        }

        assertEquals("400",pmap.get("responsecode"));
        assertEquals("Bad request",pmap.get("responsetext"));
        assertEquals("42",pmap.get("retryafter"));
        assertEquals(3,pmap.size());


    }
}