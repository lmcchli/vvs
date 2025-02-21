/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.mock;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.search.SearchTerm;

/**
 * Folder interface for mocking purposes
 *
 * @author mande
 */
public interface FolderInterface {
    public String getName();
    public String getFullName();
    public Folder getParent() throws MessagingException;
    public boolean exists() throws MessagingException;
    public Folder[] list(String applicationName) throws MessagingException;
    public char getSeparator() throws MessagingException;
    public int getType() throws MessagingException;
    public boolean create(int i) throws MessagingException;
    public boolean hasNewMessages() throws MessagingException;
    public Folder getFolder(String applicationName) throws MessagingException;
    public boolean delete(boolean b) throws MessagingException;
    public boolean renameTo(Folder folder) throws MessagingException;
    public void open(int i) throws MessagingException;
    public void close(boolean b) throws MessagingException;
    public boolean isOpen();
    public Flags getPermanentFlags();
    public int getMessageCount() throws MessagingException;
    public Message getMessage(int i) throws MessagingException;
    public void appendMessages(Message[] messages) throws MessagingException;
    public Message[] expunge() throws MessagingException;
    public Message[] search(SearchTerm searchTerm) throws MessagingException;    
}
