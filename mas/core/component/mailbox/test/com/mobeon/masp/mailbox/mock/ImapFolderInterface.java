package com.mobeon.masp.mailbox.mock;

import org.eclipse.angus.mail.imap.IMAPFolder;

import jakarta.mail.MessagingException;

/**
 * IMAPFolder interface for mocking purposes
 *
 * @author mande
 */
public interface ImapFolderInterface extends FolderInterface {
    public Object doCommand(IMAPFolder.ProtocolCommand protocolCommand) throws MessagingException;
}
