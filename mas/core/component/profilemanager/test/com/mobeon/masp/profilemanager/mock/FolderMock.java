/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.mock;

import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.builder.NameMatchBuilder;

import jakarta.mail.*;
import jakarta.mail.search.SearchTerm;

/**
 * Folder class for mocking purposes
 *
 * @author mande
 */
public class FolderMock extends Folder {

    Mock mockFolder;
    private FolderInterface folder;

    public FolderMock(Store store) {
        super(store);
    }

    public void setMockFolder(Mock mockFolder) {
        this.mockFolder = mockFolder;
        folder = (FolderInterface)mockFolder.proxy();
    }

    public NameMatchBuilder expects(InvocationMatcher expectation) {
        return mockFolder.expects(expectation);
    }

    public Message[] search(SearchTerm searchTerm) throws MessagingException {
        return folder.search(searchTerm);
    }

    public String getName() {
        return folder.getName();
    }

    public String getFullName() {
        return folder.getFullName();
    }

    public Folder getParent() throws MessagingException {
        return folder.getParent();
    }

    public boolean exists() throws MessagingException {
        return folder.exists();
    }

    public Folder[] list(String applicationName) throws MessagingException {
        return folder.list(applicationName);
    }

    public char getSeparator() throws MessagingException {
        return folder.getSeparator();
    }

    public int getType() throws MessagingException {
        return folder.getType();
    }

    public boolean create(int i) throws MessagingException {
        return folder.create(i);
    }

    public boolean hasNewMessages() throws MessagingException {
        return folder.hasNewMessages();
    }

    public Folder getFolder(String applicationName) throws MessagingException {
        return folder.getFolder(applicationName);
    }

    public boolean delete(boolean b) throws MessagingException {
        return folder.delete(b);
    }

    public boolean renameTo(Folder folder) throws MessagingException {
        return this.folder.renameTo(folder);
    }

    public void open(int i) throws MessagingException {
        folder.open(i);
    }

    public void close(boolean b) throws MessagingException {
        folder.close(b);
    }

    public boolean isOpen() {
        return folder.isOpen();
    }

    public Flags getPermanentFlags() {
        return folder.getPermanentFlags();
    }

    public int getMessageCount() throws MessagingException {
        return folder.getMessageCount();
    }

    public Message getMessage(int i) throws MessagingException {
        return folder.getMessage(i);
    }

    public void appendMessages(Message[] messages) throws MessagingException {
        folder.appendMessages(messages);
    }

    public Message[] expunge() throws MessagingException {
        return folder.expunge();
    }
}
