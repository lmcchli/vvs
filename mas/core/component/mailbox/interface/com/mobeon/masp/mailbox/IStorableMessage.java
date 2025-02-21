/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.Date;

import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * This class represents a new message intended to be stored i the message repository.
 * @author qhast
 */
public interface IStorableMessage extends IMailboxMessage {


    /**
     * Adds content to the message. Multiple calls allowed.
     * @param mediaObject content
     */
    public void addContent(IMediaObject mediaObject, MessageContentProperties contentProperties);

    /**
     * Adds content to the message. Multiple calls allowed. If appendContent is true, we will put the new content
     * at the end of the message. Else we will place it first.
     * @param mediaObject content
     */
    public void addContent(IMediaObject mediaObject, MessageContentProperties contentProperties, boolean appendContent);

    /**
     * Sets the spoken name of sender.
     * @param mediaObject content
     */
    public void setSpokenNameOfSender(IMediaObject mediaObject, MessageContentProperties contentProperties);

    /**
     * Sets the sender.
     */
    public void setSender(String sender);

    /**
     * Sets the recipients
     */
    public void setRecipients(String... recipients);

    /**
     * Adds a recipient
     */
    public void addRecipient(String recipient);

    /**
     * Sets the secondary recipients.
     */
    public void setSecondaryRecipients(String... recipients);

    /**
     * Adds a secondary recipient.
     */
    public void addSecondaryRecipient(String recipient);

    /**
     * Sets the subject.
     */
    public void setSubject(String subject);

    /**
     * Sets the reply-to address.
     */
    public void setReplyToAddress(String replyToAddress);

    /**
     * Sets the message type.
     */
    public void setType(MailboxMessageType type);

    /**
     * Sets the urgent message indicator.
     */
    public void setUrgent(boolean urgent);

    /**
     * Sets the confidential indicator.
     */
    public void setConfidential(boolean confidential);

    /**
     * Sets the delivery date.
     */
    public void setDeliveryDate(Date deliveryDate);

     /**
     * Gets the message delivery date.
     * @return deliveryDate.
     */
    public Date getDeliveryDate();

    /**
     * Sets the message language.
     */
    public void setLanguage(String language);

    /**
     * Sets an additional message property that corresponds to name.
     * Null values are allowed.
     *
     */
    public void setAdditionalProperty(String name, String value);

    /**
     * Sets the sender visibility
     */
    public void setSenderVisibility(String value);
    
    /**
     * Similiar to {@link #store(String)} but a preferred host will be chosen by the system.
     */
    public void store() throws MailboxException;


    /**
     * Stores the message in message repository.
     * The given host will be used.
     *@throws com.mobeon.masp.mailbox.InvalidMessageException if any property ha an invalid value.
     *@throws com.mobeon.masp.mailbox.MailboxException if an error occurs.
     */
    public void store(String host) throws MailboxException;


}
