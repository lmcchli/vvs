/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */


package com.mobeon.masp.callmanager.notification;
/**
 * Date: 2007-mar-19
 * @author Mats Hägg
 */

import junit.framework.*;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.EventDispatcherMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.SessionMock;

import java.util.HashMap;

public class NotificationDispatcherTest extends TestCase {

    NotificationDispatcher notificationDispatcher;
    OutboundNotification on1;


    public void setUp() throws Exception {
        notificationDispatcher = new NotificationDispatcher();
        on1 = new OutboundNotification(
            CallManager.METHOD_MWI,
            new HashMap<String,String>(),
            new EventDispatcherMock(),
            new SessionMock());
    }


    public void testAddOngoingNotification() throws Exception {

        // Only tests different null arguments
        // Functional tests are made as sipunit tests
        notificationDispatcher.clearOngoingNotifications();
        notificationDispatcher.addOngoingNotification(null,null);
        assertEquals(0,notificationDispatcher.amountOfOngoingNotifications());
        notificationDispatcher.addOngoingNotification(on1,null);
        assertEquals(0,notificationDispatcher.amountOfOngoingNotifications());
        notificationDispatcher.removeOngoingNotification(on1,null);
        assertEquals(0,notificationDispatcher.amountOfOngoingNotifications());
        notificationDispatcher.removeOngoingNotification(null,null);
        assertEquals(0,notificationDispatcher.amountOfOngoingNotifications());

    }


    public void testGetNotification() throws Exception {
        assertNull(notificationDispatcher.getNotification(null));
    }
}