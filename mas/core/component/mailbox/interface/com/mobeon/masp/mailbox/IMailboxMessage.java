/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.List;

/**
 * Represents any mailbox message.
 */
public interface IMailboxMessage {


    /**
     * Returns the spoken name of sender.
     * @return the spoken name of sender as a media object. (If exists, null otherwise)
     * @throws MailboxException
     */
    public IMediaObject getSpokenNameOfSender()
            throws MailboxException;

    /**
     * Get the message content parts top down.
     * @return list of message content.
     */
    public List<IMessageContent> getContent()
            throws MailboxException;


    /**
     * Gets message sender represented as a string.
     * @return message sender string.
     */
    public String getSender();

    /**
     * Gets message recipients. represented as an array of strings.
     * @return  message recipient string array.
     */
    public String[] getRecipients();

    /**
     * Gets message secondary recipients represented as an array of strings.
     * @return  message secondary recipient string array.
     */
    public String[] getSecondaryRecipients();

    /**
     * Gets message subsject.
     * @return message subject string.
     */
    public String getSubject();

    /**
     * Gets message reply-to address as a string.
     * @return message reply-to address string.
     */
    public String getReplyToAddress();


    /**
     * Gets message type.
     * @return message type.
     */
    public MailboxMessageType getType();

    /**
     * Indicates if the message is urgent.
     * @return true if message is defined as urgent.(false otherwise)
     */
    public boolean isUrgent();

    /**
     * Indicates if the message is confidential.
     * @return true if message is defined as confidential.(false otherwise)
     */
    public boolean isConfidential();

    /**
     * Gets the language tag.
     * @return language tag according to ISO 639-1
     */
    public String getLanguage();

    /**
     * Gets the broadcast announcement supported language.
     * @return language
     */
    public String getBroadcastLanguage();

     /**
     * Get an additional message property value. Null values are allowed.
     * Addtional properties are configurable.
     * @param name the of the property.
     * @return property value.
     */
    public String getAdditionalProperty(String name);

}
