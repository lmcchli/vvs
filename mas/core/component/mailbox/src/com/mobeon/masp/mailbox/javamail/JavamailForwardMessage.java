/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;

/**
 * @author QHAST
 */
public class JavamailForwardMessage extends JavamailStorableMessage {

    private Message orignalMessage;

    JavamailForwardMessage(JavamailContext context, Message orignalMessage) {
        super(context,true);
        this.orignalMessage = orignalMessage;
    }

    @Override
    protected Multipart createMultipart() throws MessagingException {
        Multipart result = super.createMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(orignalMessage,"message/rfc822");
        result.addBodyPart(messageBodyPart);
        return result;
    }

}
