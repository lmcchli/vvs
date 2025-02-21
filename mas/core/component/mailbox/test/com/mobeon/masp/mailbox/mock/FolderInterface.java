package com.mobeon.masp.mailbox.mock;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.search.SearchTerm;

/**
 * Folder interface for mocking purposes. This interface reflects the javamail Folder abstract class. This interface
 * is used for creating mock objects so that jmock expectations, constraints and stubs can be used.
 *
 * @author mande
 */
public interface FolderInterface {
    String getName();
    String getFullName();
    Folder getParent() throws MessagingException;
    boolean exists() throws MessagingException;
    Folder[] list() throws MessagingException;
    Folder[] list(String applicationName) throws MessagingException;
    char getSeparator() throws MessagingException;
    int getType() throws MessagingException;
    boolean create(int i) throws MessagingException;
    boolean hasNewMessages() throws MessagingException;
    Folder getFolder(String applicationName) throws MessagingException;
    boolean delete(boolean b) throws MessagingException;
    boolean renameTo(Folder folder) throws MessagingException;
    void open(int i) throws MessagingException;
    void close(boolean b) throws MessagingException;
    boolean isOpen();
    Flags getPermanentFlags();
    int getMessageCount() throws MessagingException;
    Message getMessage(int i) throws MessagingException;
    void appendMessages(Message[] messages) throws MessagingException;
    Message[] expunge() throws MessagingException;
    Message[] search(SearchTerm searchTerm) throws MessagingException;
    Message[] getMessages() throws MessagingException;
}
