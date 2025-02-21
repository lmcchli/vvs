/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jakarta.mail.Session;
import java.util.Properties;

/**
 * BasicStoreManager Tester.
 *
 * @author qhast
 */
public class BasicStoreManagerTest extends TestCase {
    private BasicStoreManager storeManager;
    private BasicStoreManager sessionInitializedstoreManager;
    private Properties sessionProperties = new Properties();
    private Session session = Session.getInstance(sessionProperties);

    public BasicStoreManagerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        storeManager = new BasicStoreManager();
        sessionInitializedstoreManager = new BasicStoreManager(session);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetProtocol() throws Exception {
        assertEquals("imap", storeManager.getProtocol());
        storeManager.setProtocol("imup");
        assertEquals("imup", storeManager.getProtocol());

        assertEquals("imap", sessionInitializedstoreManager.getProtocol());
        sessionInitializedstoreManager.setProtocol("imup");
        assertEquals("imup", sessionInitializedstoreManager.getProtocol());
    }

    public void testGetSession() throws Exception {
        assertNotNull(storeManager.getSession());
        assertEquals(session, sessionInitializedstoreManager.getSession());
    }

    public void testGetStore() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetStore1() throws Exception {
        //TODO: Test goes here...
    }

    public void testReturnStore() throws Exception {
        //TODO: Test goes here...
    }

    public void testOpened() throws Exception {
        //TODO: Test goes here...
    }

    public void testDisconnected() throws Exception {
        //TODO: Test goes here...
    }

    public void testClosed() throws Exception {
        //TODO: Test goes here...
    }

    public static Test suite() {
        return new TestSuite(BasicStoreManagerTest.class);
    }
}
