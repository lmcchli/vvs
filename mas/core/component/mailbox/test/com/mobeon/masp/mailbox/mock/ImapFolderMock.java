package com.mobeon.masp.mailbox.mock;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.builder.NameMatchBuilder;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.search.SearchTerm;

/**
 * IMAPFolder class for mocking purposes
 *
 * @author mande
 */
public class ImapFolderMock extends IMAPFolder {

    Mock mockImapFolder;
    private ImapFolderInterface imapFolder;

    public ImapFolderMock(String s, char c, IMAPStore imapStore) {
        super(s, c, imapStore);
    }

    public void setMockImapFolder(Mock mockImapFolder) {
        this.mockImapFolder = mockImapFolder;
        imapFolder = (ImapFolderInterface)mockImapFolder.proxy();
    }

    public NameMatchBuilder expects(InvocationMatcher expectation) {
        return mockImapFolder.expects(expectation);
    }

    public String getName() {
        return imapFolder.getName();
    }

    public String getFullName() {
        return imapFolder.getFullName();
    }

    public Folder getParent() throws MessagingException {
        return imapFolder.getParent();
    }

    public boolean exists() throws MessagingException {
        return imapFolder.exists();
    }

    public Folder[] list(String applicationName) throws MessagingException {
        return imapFolder.list(applicationName);
    }

    public char getSeparator() throws MessagingException {
        return imapFolder.getSeparator();
    }

    public int getType() throws MessagingException {
        return imapFolder.getType();
    }

    public boolean create(int i) throws MessagingException {
        return imapFolder.create(i);
    }

    public boolean hasNewMessages() throws MessagingException {
        return imapFolder.hasNewMessages();
    }

    public Folder getFolder(String applicationName) throws MessagingException {
        return imapFolder.getFolder(applicationName);
    }

    public boolean delete(boolean b) throws MessagingException {
        return imapFolder.delete(b);
    }

    public boolean renameTo(Folder folder) throws MessagingException {
        return this.imapFolder.renameTo(folder);
    }

    public void open(int i) throws MessagingException {
        imapFolder.open(i);
    }

    public void close(boolean b) throws MessagingException {
        imapFolder.close(b);
    }

    public boolean isOpen() {
        return imapFolder.isOpen();
    }

    public Flags getPermanentFlags() {
        return imapFolder.getPermanentFlags();
    }

    public int getMessageCount() throws MessagingException {
        return imapFolder.getMessageCount();
    }

    public Message getMessage(int i) throws MessagingException {
        return imapFolder.getMessage(i);
    }

    public void appendMessages(Message[] messages) throws MessagingException {
        imapFolder.appendMessages(messages);
    }

    public Message[] expunge() throws MessagingException {
        return imapFolder.expunge();
    }

    public Message[] search(SearchTerm searchTerm) throws MessagingException {
        return imapFolder.search(searchTerm);
    }

    public Object doCommand(ProtocolCommand protocolCommand) throws MessagingException {
        return imapFolder.doCommand(protocolCommand);
    }
}
