/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;

/**
 * @author QHAST
 */
public interface StoreManager {

    public Session getSession();

    public Store getStore(String host, int port, String accountId, String accountPassword) throws MessagingException;

    public void returnStore(Store store) throws MessagingException;

}
