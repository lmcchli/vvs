/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mock;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import java.io.InputStream;
import java.util.Date;

/**
 * <code>MimeMessageMock</code> does not actually mock a <code>MimeMessage</code>. It is used to be able to create
 * <code>MimeMessages</code> belonging to a specific <code>Folder</code> (most often a <code>FolderMock</code> object)
 * and to create <code>MimeMessages</code> by parsing an InputStream.
 *
 * @author mande
 */
public class MimeMessageMock extends MimeMessage {
    private Date receivedDate;

    public MimeMessageMock(Folder folder, int i) {
        super(folder, i);
    }

    public MimeMessageMock(Folder folder, InputStream is, int i) throws MessagingException {
        super(folder, is, i);
    }

    /**
     * For testing purposes. Used when testing ReceivedDate
     * @param date
     */
    public void setReceivedDate(Date date) {
        receivedDate = date;
    }

    /**
     * For testing purposes. Used when testing ReceivedDate
     * @return message received date
     */
    public Date getReceivedDate() {
        return receivedDate;
    }
}
