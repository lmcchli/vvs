/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.component.tests.sipunit.notification;

import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.util.NamedValue;

import javax.sip.RequestEvent;
import javax.sip.message.Request;
import javax.sip.header.ToHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.SubscriptionStateHeader;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;


import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * Call Manager component test case to verify notifications.
 * @author Mats Hägg
 */
public class NotifyTest extends NotificationCase {

    String cmHost;
    int cmPort = 5060;
    String accountUri1;
    String accountUri2;

    final static String SIMHOST = "localhost";
    final static int SIMPORT = 5090;
    final static String SIMUSER1 = "0123456789";
    final static String SIMURI1 = "sip:" + SIMUSER1 + "@" + SIMHOST +
            ":" + SIMPORT + ";user=phone";

    final static String ACCOUNT1 = "+9999-8888";
    final static String ACCOUNT2 = "222333444555";

    final static String SUBSCRIPTION_STATE_DEFAULT = "active";

    final static NamedValue<String,String> SENDTO1 = new NamedValue<String, String>(
            CallManager.SEND_TO, SIMUSER1);
    final static NamedValue<String,String> MSGWAIT_YES = new NamedValue<String, String>(
            CallManager.MESSAGES_WAITING, "yes");
    final static NamedValue<String,String> MSGWAIT_NO = new NamedValue<String, String>(
            CallManager.MESSAGES_WAITING, "no");

    final static NamedValue<String,String> MSGACCOUNT1 = new NamedValue<String, String>(
            CallManager.MESSAGE_ACCOUNT, ACCOUNT1);
    final static NamedValue<String,String> MSGACCOUNT2 = new NamedValue<String, String>(
            CallManager.MESSAGE_ACCOUNT, ACCOUNT2);


    final static NamedValue<String,String> VOICE_NEW_0 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_NEW, "0");
    final static NamedValue<String,String> VOICE_OLD_0 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_OLD, "0");
    final static NamedValue<String,String> VOICE_NEW_1 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_NEW, "1");
    final static NamedValue<String,String> VOICE_OLD_2 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_OLD, "2");
    final static NamedValue<String,String> VOICE_NEW_123456 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_NEW, "123456");
    final static NamedValue<String,String> VOICE_OLD_234567 = new NamedValue<String, String>(
            CallManager.VOICE_MESSAGE_OLD, "234567");

    final static NamedValue<String,String> VIDEO_NEW_0 = new NamedValue<String, String>(
            CallManager.VIDEO_MESSAGE_NEW, "0");
    final static NamedValue<String,String> VIDEO_OLD_0 = new NamedValue<String, String>(
            CallManager.VIDEO_MESSAGE_OLD, "0");
    final static NamedValue<String,String> VIDEO_NEW_3 = new NamedValue<String, String>(
            CallManager.VIDEO_MESSAGE_NEW, "3");
    final static NamedValue<String,String> VIDEO_OLD_4 = new NamedValue<String, String>(
            CallManager.VIDEO_MESSAGE_OLD, "4");

    final static NamedValue<String,String> FAX_NEW_0 = new NamedValue<String, String>(
            CallManager.FAX_MESSAGE_NEW, "0");
    final static NamedValue<String,String> FAX_OLD_0 = new NamedValue<String, String>(
            CallManager.FAX_MESSAGE_OLD, "0");
    final static NamedValue<String,String> FAX_NEW_5 = new NamedValue<String, String>(
            CallManager.FAX_MESSAGE_NEW, "5");
    final static NamedValue<String,String> FAX_OLD_6 = new NamedValue<String, String>(
            CallManager.FAX_MESSAGE_OLD, "6");

    final static NamedValue<String,String> EMAIL_NEW_0 = new NamedValue<String, String>(
            CallManager.EMAIL_MESSAGE_NEW, "0");
    final static NamedValue<String,String> EMAIL_OLD_0 = new NamedValue<String, String>(
            CallManager.EMAIL_MESSAGE_OLD, "0");
    final static NamedValue<String,String> EMAIL_NEW_7 = new NamedValue<String, String>(
            CallManager.EMAIL_MESSAGE_NEW, "7");
    final static NamedValue<String,String> EMAIL_OLD_8 = new NamedValue<String, String>(
            CallManager.EMAIL_MESSAGE_OLD, "8");

    final static NamedValue<String,String> OUTBOUND_CALL_SERVER_HOST_1 = new NamedValue<String, String>(
            CallManager.OUTBOUND_CALL_SERVER_HOST, "localhost");

    final static NamedValue<String,String> OUTBOUND_CALL_SERVER_PORT_1 = new NamedValue<String, String>(
            CallManager.OUTBOUND_CALL_SERVER_PORT, "5091");


    public void setUp() {
        try {
            super.setUp();
            cmHost = "localhost";
            accountUri1 = "sip:" + ACCOUNT1 + "@" + cmHost + ":" + cmPort +
                    ";user=phone";
            accountUri2 = "sip:" + ACCOUNT2 + "@" + cmHost + ":" + cmPort +
                    ";user=phone";
        } catch(Exception e) {
            //
        }

    }

    /**
     * Verifies that a SIP NOTIFY request looks correct
     * @throws Exception if test case fails.
     */
    public void testNotifyRequest1() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(VOICE_NEW_123456);
        params.add(VOICE_OLD_234567);
        params.add(VIDEO_NEW_3);
        params.add(VIDEO_OLD_4);
        params.add(FAX_NEW_5);
        params.add(FAX_OLD_6);
        params.add(EMAIL_NEW_7);
        params.add(EMAIL_OLD_8);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
//        System.out.println("****** BODY=\n" + body);
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);
        assertTrue(body.indexOf("Voice-Message: 123456/234567") >= 0);
        assertTrue(body.indexOf("Multimedia-Message: 3/4") >= 0);
        assertTrue(body.indexOf("Fax-Message: 5/6") >= 0);
        assertTrue(body.indexOf("Text-Message: 7/8") >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());


        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, "My own way of saying ok", null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("My own way of saying ok", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }

    /**
     * Verifies that a SIP NOTIFY request looks correct
     * @throws Exception if test case fails.
     */
    public void testNotifyRequest2() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT2);
        params.add(VOICE_NEW_0);
        params.add(VOICE_OLD_234567);
        params.add(VIDEO_NEW_0);
        params.add(VIDEO_OLD_4);
        params.add(FAX_NEW_0);
        params.add(FAX_OLD_6);
        params.add(EMAIL_NEW_0);
        params.add(EMAIL_OLD_8);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        System.out.println("****** BODY=\n" + body);
        assertTrue(body.indexOf("Messages-Waiting: no") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri2) >= 0);
        assertTrue(body.indexOf("Voice-Message: 0/234567") >= 0);
        assertTrue(body.indexOf("Multimedia-Message: 0/4") >= 0);
        assertTrue(body.indexOf("Fax-Message: 0/6") >= 0);
        assertTrue(body.indexOf("Text-Message: 0/8") >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());


        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, "My own way of saying ok", null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("My own way of saying ok", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }


    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * Tests also custom reason phrase
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOkResponse1() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());


        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, "My own way of saying ok", null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("My own way of saying ok", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * Standard reason phrase
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOkResponse2() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);


        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("OK", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * 100 Trying sent before ok
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOkResponse3() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        SipResponse sipResponse;
        // Sim sends 100 Trying
        sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 100, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        Thread.sleep(200);

        // Sim sends OK response back
        sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("OK", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * 100 Trying and 183 Session progress sent before ok
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOkResponse4() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        doCommonNotifyTest(params);

    }



    /**
     * Verifies that a SIP NOTIFY request responded to with an error response
     * will result in a correct SipMessageResponseEvent
     * @throws Exception if test case fails.
     */
    public void testNotifyWithErrorResponse1() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 404, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());


        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("not found", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT).toLowerCase());
        assertEquals("404",pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an error response
     * will result in a correct SipMessageResponseEvent
     * @throws Exception if test case fails.
     */
    public void testNotifyWithErrorResponse2() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 503, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());


        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("service unavailable", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT).toLowerCase());
        assertEquals("503", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }


    /**
     * Verifies that a SIP NOTIFY request responded to with an error response
     * will result in a correct SipMessageResponseEvent
     * Tests also custom reason phrase and retry-after
     * @throws Exception if test case fails.
     */
    public void testNotifyWithErrorResponse3() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 503, "Overloaded, try again later...", 60);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());


        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("Overloaded, try again later...", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("503", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertEquals("60", pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }


    /**
     * Verifies that a SIP NOTIFY request responded to with an error response
     * will result in a correct SipMessageResponseEvent
     * Tests also retry-after
     * @throws Exception if test case fails.
     */
    public void testNotifyWithErrorResponse4() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        SipResponse sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 600, null, 3600);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());


        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("busy everywhere", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT).toLowerCase());
        assertEquals("600", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertEquals("3600", pmap.get(SipMessageResponseEvent.RETRY_AFTER));

    }




    /**
     * Verifies that a SIP NOTIFY request that times out
     * will result in a correct SipMessageResponseEvent with code 408
     * @throws Exception if test case fails.
     */
    public void testNotifyWithTimeout() throws Exception {

        setupNotifySim(SIMURI1,SIMHOST,SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        long t0 = System.currentTimeMillis();

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(SIMURI1, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Timeout should occur after 2s (See siptimers in callmanager_with_remoteua.xml)
        // Note that the configurable timers are in multiples of T1 (=500ms)
        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        long deltaT = System.currentTimeMillis() - t0;

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());

        assertEquals("request timeout", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT).toLowerCase());
        assertEquals("408", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

        assertTrue("Timed out too soon: " + deltaT + "ms instead of expected 2000ms",
                deltaT >= 2000);
        assertTrue("Timed out too late: " + deltaT + "ms instead of expected 2000ms",
                deltaT < 3000);


    }

    private String verifyRequest(String SimURI, Request request) {

        // Verify NOTIFY request message
        ToHeader to = (ToHeader)request.getHeader(ToHeader.NAME);
        assertEquals("<" + SimURI + ">", to.getAddress().toString());
        ContactHeader contact = (ContactHeader)request.getHeader(ContactHeader.NAME);
        assertEquals("<sip:mas@" + cmHost + ":" + cmPort + ">", contact.getAddress().toString());
        EventHeader eventHdr = (EventHeader)request.getHeader(EventHeader.NAME);
        assertEquals("message-summary", eventHdr.getEventType());

        SubscriptionStateHeader subState = (SubscriptionStateHeader)request.
                getHeader(SubscriptionStateHeader.NAME);
        assertEquals(SUBSCRIPTION_STATE_DEFAULT, subState.getState());

        // Verify P-Charging-Vector header
        PChargingVectorHeader pcvHdr = (PChargingVectorHeader)request.getHeader(PChargingVectorHeader.NAME);
        assertNotNull(pcvHdr);
        assertNotNull(pcvHdr.getICID());
        assertTrue(pcvHdr.getICID().length() > 0);
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getOriginatingIOI());
        assertEquals(CMUtils.getInstance().getLocalHost(),pcvHdr.getICIDGeneratedAt());
        assertNull(pcvHdr.getTerminatingIOI());

        String body = new String(request.getRawContent());
//        System.out.println("BODY=\n"+body);
        return body;

    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * 100 Trying and 183 Session progress sent before ok
     * This testcase uses outbound call server and default port.
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOutboundCallServer1() throws Exception {
        assertConfigurationContainsNoSsp();
        ConfigurationReader.getInstance().getConfig().setOutboundCallServerPort(SIMPORT);

        setupNotifySim(SIMURI1,OUTBOUND_CALL_SERVER_HOST_1.getValue(),SIMPORT);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(OUTBOUND_CALL_SERVER_HOST_1);

        doCommonNotifyTest(params);
    }


    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * 100 Trying and 183 Session progress sent before ok .
     * This testcase uses outbound call server and non-default port.
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOutboundCallServer2() throws Exception {
        assertConfigurationContainsNoSsp();
        int port = Integer.parseInt(OUTBOUND_CALL_SERVER_PORT_1.getValue());
        ConfigurationReader.getInstance().getConfig().setOutboundCallServerPort(port);

        final String simuri = "sip:" + SIMUSER1 + "@" + OUTBOUND_CALL_SERVER_HOST_1.getValue() +
            ":" + port + ";user=phone";

        setupNotifySim(simuri,OUTBOUND_CALL_SERVER_HOST_1.getValue(),port);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(OUTBOUND_CALL_SERVER_HOST_1);
        params.add(OUTBOUND_CALL_SERVER_PORT_1);

        doCommonNotifyTest(params);
    }

    /**
     * Verifies that IllegalArgumentException is thrown if the value of the
     * outbound call server host is null.
     * @throws Exception if test case fails.
     */
    public void testNotifyWithNullOutboundCallServer() throws Exception {
        assertConfigurationContainsNoSsp();
        ConfigurationReader.getInstance().getConfig().setOutboundCallServerPort(SIMPORT);

        final NamedValue<String,String> host = new NamedValue<String, String>(
                CallManager.OUTBOUND_CALL_SERVER_HOST, null);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(host);

        assertEquals(0, CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        boolean gotException = false;
        try {
            sendNotify(params);
        } catch(IllegalArgumentException e){
            gotException = true;
        }

        assertTrue(gotException);
    }

    /**
     * Verifies that a SIP NOTIFY request with port 65536 results in an error events ent back.
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOutboundCallServerPortOutOfRange() throws Exception {
        doPortOutOfRange(65536);
    }

    /**
     * Verifies that a SIP NOTIFY request with port -1 results in an error events ent back.
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOutboundCallServerPortOutOfRange2() throws Exception {
        doPortOutOfRange(-1);
    }

    private void doPortOutOfRange(int port) throws Exception {
        assertConfigurationContainsNoSsp();

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(OUTBOUND_CALL_SERVER_HOST_1);

        NamedValue<String,String> PORT = new NamedValue<String, String>(
                CallManager.OUTBOUND_CALL_SERVER_PORT, ""+port);
        params.add(PORT);
        sendNotify(params);

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());

        assertEquals("500", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));
    }

    /**
     * Verifies that a SIP NOTIFY request that times out
     * will result in a correct SipMessageResponseEvent with code 408.
     * Outboundcallserver is used.
     * @throws Exception if test case fails.
     */
    public void testTimeoutWithOutboundCallServer() throws Exception {

        assertConfigurationContainsNoSsp();
        int port = Integer.parseInt(OUTBOUND_CALL_SERVER_PORT_1.getValue());
        ConfigurationReader.getInstance().getConfig().setOutboundCallServerPort(port);

        final String simuri = "sip:" + SIMUSER1 + "@" + OUTBOUND_CALL_SERVER_HOST_1.getValue() +
            ":" + port + ";user=phone";

        setupNotifySim(simuri,OUTBOUND_CALL_SERVER_HOST_1.getValue(),port);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(OUTBOUND_CALL_SERVER_HOST_1);
        params.add(OUTBOUND_CALL_SERVER_PORT_1);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        long t0 = System.currentTimeMillis();

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        String body = verifyRequest(simuri, requestEvent.getRequest());
        assertTrue(body.indexOf("Messages-Waiting: yes") >= 0);
        assertTrue(body.indexOf("Message-Account: " + accountUri1) >= 0);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Timeout should occur after 2s (See siptimers in callmanager_with_remoteua.xml)
        // Note that the configurable timers are in multiples of T1 (=500ms)
        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        long deltaT = System.currentTimeMillis() - t0;

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());

        assertEquals("request timeout", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT).toLowerCase());
        assertEquals("408", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));

        assertTrue("Timed out too soon: " + deltaT + "ms instead of expected 2000ms",
                deltaT >= 2000);
        assertTrue("Timed out too late: " + deltaT + "ms instead of expected 2000ms",
                deltaT < 3000);
    }

    /**
     * Verifies that a SIP NOTIFY request responded to with an OK response
     * will result in a correct SipMessageResponseEvent
     * 100 Trying and 183 Session progress sent before ok .
     * In this testcase, outbound call server is used, and an SSP is defined
     * (but it is expected to not be used).
     * @throws Exception if test case fails.
     */
    public void testNotifyWithOutboundCallServer3() throws Exception {
        assertConfigurationContainsSsp();
        int port = Integer.parseInt(OUTBOUND_CALL_SERVER_PORT_1.getValue());
        ConfigurationReader.getInstance().getConfig().setOutboundCallServerPort(port);

        final String simuri = "sip:" + SIMUSER1 + "@" + OUTBOUND_CALL_SERVER_HOST_1.getValue() +
            ":" + port + ";user=phone";

        setupNotifySim(simuri,OUTBOUND_CALL_SERVER_HOST_1.getValue(),port);

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(SENDTO1);
        params.add(MSGACCOUNT1);
        params.add(MSGWAIT_YES);
        params.add(MSGWAIT_YES);
        params.add(OUTBOUND_CALL_SERVER_HOST_1);
        params.add(OUTBOUND_CALL_SERVER_PORT_1);

        doCommonNotifyTest(params);
    }


    private void doCommonNotifyTest(Collection<NamedValue<String, String>> params) throws Exception {
        assertEquals(0, CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Trigger CallManager to send a MWI (NOTIFY)
        sendNotify(params);

        // Wait for NOTIFY request to arrive at simulated receiver
        RequestEvent requestEvent = notifyReceiverSim.assertNotifyReceived();

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        SipResponse sipResponse;
        // Sim sends 100 Trying
        sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 100, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        Thread.sleep(200);

        // Sim sends 183 Session progress
        sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 183, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        Thread.sleep(200);

        assertEquals(1,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        // Sim sends OK response back
        sipResponse = notifyReceiverSim.createResponse(
                requestEvent, 200, null, null);
        notifyReceiverSim.sendResponse(requestEvent, sipResponse.getResponse());

        SipMessageResponseEvent event = (SipMessageResponseEvent)
                assertEventReceived(SipMessageResponseEvent.class);

        assertEquals(0,CMUtils.getInstance().getNotificationDispatcher().
                amountOfOngoingNotifications());

        Collection<NamedValue<String,String>> eventParams = event.getParams();
        Map<String,String> pmap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : eventParams)
            pmap.put(nv.getName(),nv.getValue());


        assertEquals("OK", pmap.get(SipMessageResponseEvent.RESPONSE_TEXT));
        assertEquals("200", pmap.get(SipMessageResponseEvent.RESPONSE_CODE));
        assertNull(pmap.get(SipMessageResponseEvent.RETRY_AFTER));
    }

}
