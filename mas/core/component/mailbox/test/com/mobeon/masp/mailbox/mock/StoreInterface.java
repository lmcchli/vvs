/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mock;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.URLName;
import jakarta.mail.event.ConnectionListener;

/**
 * Store interface for mocking purposes
 *
 * @author mande
 */
public interface StoreInterface {
    public void addConnectionListener(ConnectionListener connectionListener);
    public void close() throws MessagingException;
    public void connect() throws MessagingException;
    public Folder getDefaultFolder() throws MessagingException;
    public Folder getFolder(String applicationName) throws MessagingException;
    public Folder getFolder(URLName urlName) throws MessagingException;
}
