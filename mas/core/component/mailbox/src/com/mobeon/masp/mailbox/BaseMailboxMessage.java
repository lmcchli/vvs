/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;

/**
 * Base implementation for a mailbox message.
 * It only works as a holder of references to message properties.
 *
 * @author qhast
 */
public class BaseMailboxMessage<C extends BaseContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseMailboxMessage.class);

    private C context;

    /**
     * Spoken name of sender.
     */
    protected IMessageContent spokenNameOfSender;

    /**
     * Message content.
     */
    protected List<IMessageContent> content;

    /**
     * Address or identifier of the sender of the message.
     */
    protected String sender;

    /**
     * Addresses or identifiers of the recipients of the message.
     */
    protected String[] recipients;

    /**
     * Addresses or identifiers of the secondary recipients of the message.
     */
    protected String[] secondaryRecipients;

    /**
     * Message subject.
     */
    protected String subject;


    /**
     * Address where reply should be sent to.
     */
    protected String replyToAddress;

    /**
     * Message language.
     */
    protected String language;

    /**
     * Broadcast announcement language
     */
    protected String broadcastAnnouncementLanguage;

    /**
     * Message type.
     */
    protected MailboxMessageType type;


    /**
     * Indicates if message should be considered as urgent.
     */
    protected Boolean urgent;

    /**
     * Indicates if message should be considered as confidential.
     */
    protected Boolean confidential;

    /**
     * Map holding configured additional properties.
     */
    protected Map<String, String> additionalProperties;

    /**
     * Indicates if message sender should be hidden
     */
    protected String senderVisibility;

    /**
     * Constructs a BaseMailboxMessage and initializes additionalProperties map
     * with the configured additionalProperties names. If a additional property name
     * is null or an empty string it will not be added.
     *
     * @param context
     */
    BaseMailboxMessage(C context) {
        additionalProperties = new HashMap<String, String>();
        this.context = context;
    }

    protected C getContext() {
        return context;
    }

    /**
     * Returns the spoken name of sender.
     *
     * @return the spoken name of sender as a media object. (If exists)
     * @throws MailboxException
     */
    public IMediaObject getSpokenNameOfSender() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getSpokenNameOfSender()");
        IMediaObject result = spokenNameOfSender != null ? spokenNameOfSender.getMediaObject() : null;
        if (LOGGER.isInfoEnabled()) LOGGER.info("getSpokenNameOfSender() returns " + result);
        return result;
    }


    /**
     * Get the message content.
     * Returns null if list not has been initialized.
     *
     * @return list of message content.
     */
    public List<IMessageContent> getContent() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getContent()");
        List<IMessageContent> result = content != null ? Collections.unmodifiableList(content) : null;
        if (LOGGER.isInfoEnabled()) LOGGER.info("getContent() returns " + result);
        return result;
    }

    protected void addMessageContent(IMessageContent messageContent, boolean appendContent) {
        if (content == null) {
            resetMessageContent();
        }
        if (appendContent){
            if (LOGGER.isInfoEnabled()) LOGGER.info("addMessageContent(), appendContent is true");
        	content.add(messageContent);
        }
        else {
            if (LOGGER.isInfoEnabled()) LOGGER.info("addMessageContent(), appendContent is false, so prepending content");
        	// Lets prepend the content; create a new list, place the new element first,
        	// and then add back the elements of content
            if (LOGGER.isInfoEnabled()) LOGGER.info("getContent()");
        	List<IMessageContent> newContent = new ArrayList<IMessageContent>();
        	newContent.add(messageContent);
        	Iterator<IMessageContent> contentIterator = content.iterator();
        	while (contentIterator.hasNext()){
        		newContent.add(contentIterator.next());
        	}
        	content = newContent;
        }
    }

    protected void resetMessageContent() {
        content = new ArrayList<IMessageContent>();
    }

    /**
     * Gets message sender represented as a string.
     *
     * @return message sender string.
     */
    public String getSender() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getSender() returns " + sender);
        return sender;
    }


    /**
     * Gets message recipients. represented as an array of strings.
     *
     * @return message recipient string array.
     */
    public String[] getRecipients() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("getRecipients() returns " + (recipients == null ? null : Arrays.asList(recipients)));
        return recipients;
    }

    /**
     * Gets message secondary recipients represented as an array of strings.
     *
     * @return message secondary recipient string array.
     */
    public String[] getSecondaryRecipients() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("getRecipients() returns " + (secondaryRecipients == null ? null : Arrays.asList(secondaryRecipients)));
        /*if(secondaryRecipients==null) {
            LOGGER.info("getRecipients() returns null");
        } else {
            LOGGER.info("getRecipients() returns "+Arrays.asList(secondaryRecipients));
        }
        */
        return secondaryRecipients;
    }

    /**
     * Gets message subsject.
     *
     * @return message subject string.
     */
    public String getSubject() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getSubject() returns " + subject);
        return subject;
    }


    /**
     * Gets message reply-to address as a string.
     *
     * @return message reply-to address string.
     */
    public String getReplyToAddress() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getReplyToAddress() returns " + replyToAddress);
        return replyToAddress;
    }

    /**
     * Gets the language tag.
     *
     * @return language tag according to ISO 639-1
     */
    public String getLanguage() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getLanguage() returns " + language);
        return language;
    }


    /**
     * Gets the broadcast announcement language.
     *
     * @return language
     */
    public String getBroadcastLanguage() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getBroadcastAnnouncementLanguage() returns " + broadcastAnnouncementLanguage);
        return broadcastAnnouncementLanguage;
    }


    /**
     * Gets message type.
     *
     * @return message type.
     */
    public MailboxMessageType getType() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getType() returns " + type);
        return type;
    }

    /**
     * Indicates if the message is urgent.
     *
     * @return true if message is defined as urgent.(false otherwise)
     */
    public boolean isUrgent() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("isUrgent() returns " + urgent);
        return urgent;
    }

    /**
     * Indicates if the message is confidential.
     *
     * @return true if message is defined as confidential.(false otherwise)
     */
    public boolean isConfidential() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("isConfidential() returns " + confidential);
        return confidential;
    }

    /**
     * Get an additional message property value.
     * Addtional properties are configurable.
     *
     * @param name Addtional property key.
     * @return property value. Null-values are allowed.
     */
    public String getAdditionalProperty(String name) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getAdditionalProperty(name=" + name + ")");
        String value = additionalProperties.get(getAdditionalPropertyName(name));
        if (LOGGER.isInfoEnabled()) LOGGER.info("getAdditionalProperty(String) returns \"" + value + "\"");
        return value;
    }

    protected String getAdditionalPropertyName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Additional property name cannot be null or empty!");
        }
        name = name.toLowerCase();
        if (!getContext().getConfig().getAdditionalPropertyMap().keySet().contains(name)) {
            throw new IllegalArgumentException("Additional property name \"" + name + "\" is not mapped to a field in configuration!");
        }
        return name;
    }

    /**
     * Get the sender's visibility flag
     * @return
     */
    public String getSenderVisibility() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getSenderVisibility() returns " + senderVisibility);
        return senderVisibility;
    }

}
