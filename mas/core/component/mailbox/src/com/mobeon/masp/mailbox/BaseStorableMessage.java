/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * @author qhast
 */
public abstract class BaseStorableMessage<C extends BaseContext> extends BaseMailboxMessage<C> implements IStorableMessage{

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseStorableMessage.class);

    protected Date deliveryDate;

    protected BaseStorableMessage(C context) {
        super(context);
        deliveryDate = null;
        subject = "";
        sender = null;
        language = Locale.ENGLISH.getLanguage();
        recipients = new String[]{};
        secondaryRecipients = new String[]{};
        replyToAddress = null;
        urgent = false;
        confidential = false;
        type = MailboxMessageType.EMAIL;
        senderVisibility = "";
        resetMessageContent();
    }

    protected abstract void storeWork(String host) throws MailboxException;


    /**
     * Similiar to {@link #store(String)} but a preferred host will be chosen by the system.
     */
    public final void store() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("store()");
        validateProperties();
        storeWork(null);
        if (LOGGER.isInfoEnabled()) LOGGER.info("store() returns void");
    }


    /**
     * Stores the message in message repository.
     * The given host will be used.
     *@throws com.mobeon.masp.mailbox.InvalidMessageException if any property ha an invalid value.
     *@throws com.mobeon.masp.mailbox.MailboxException if an error occurs.
     */
    public final void store(String host) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("store(host=" + host + ")");
        validateProperties();
        storeWork(host);
    }

    private void validateProperties() throws MailboxException {

        InvalidMessageException invalidMessageException = new InvalidMessageException();
        //TODO : ecyncot fix validation
        //Validate sender
        /*try {
            Address.validate(sender);
        } catch(AddressParseException e) {
            invalidMessageException.addInvalidPropertyValue("sender",sender);
        }

        //Check for added invalid properties.
        if(invalidMessageException.getInvalidProperties().size()>0) {
            throw invalidMessageException;
        }*/

    }

    /**
     * Adds content to the message. Multiple calls allowed.
     *
     * @param mediaObject content
     */
    public void addContent(IMediaObject mediaObject, MessageContentProperties contentProperties) {
        addContent(mediaObject, contentProperties, true);
    }
    
    public void addContent(IMediaObject mediaObject, MessageContentProperties contentProperties, boolean appendContent) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("addContent(mediaObject=" + mediaObject + ", contentProperties=" + contentProperties + ")");
        addMessageContent(new BasicMessageContent(mediaObject, contentProperties), appendContent);
        if (LOGGER.isInfoEnabled()) LOGGER.info("addContent(IMediaObject,MessageContentProperties) returns void");
    }

    /**
     * Sets the spoken name of sender.
     * Content media object must be immutable.
     */
    public void setSpokenNameOfSender(final IMediaObject mediaObject, final MessageContentProperties contentProperties) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("setSpokenNameOfSender(mediaObject=" + mediaObject + ", contentProperties=" + contentProperties + ")");
        spokenNameOfSender = new BasicMessageContent(mediaObject, contentProperties);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("setSpokenNameOfSender(IMediaObject,MessageContentProperties) return void");
    }

    public void setSender(String sender) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSender(sender=" + sender + ")");
        this.sender = sender;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSender(String) returns void");
    }

    public void setRecipients(String... recipients) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setRecipients(recipients=" + Arrays.asList(recipients) + ")");
        this.recipients = recipients;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setRecipients(String...) returns void");
    }

    public void addRecipient(String recipient) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("addRecipient(recipient=" + recipient + ")");
        List<String> tmp = new ArrayList<String>(Arrays.asList(this.recipients));
        tmp.add(recipient);
        this.recipients = tmp.toArray(new String[tmp.size()]);
        if (LOGGER.isInfoEnabled()) LOGGER.info("addRecipient(String) returns void");
    }

    public void setSecondaryRecipients(String... secondaryRecipients) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("setSecondaryRecipients(recipients=" + Arrays.asList(secondaryRecipients) + ")");
        this.secondaryRecipients = secondaryRecipients;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSecondaryRecipients(String...) returns void");
    }

    public void addSecondaryRecipient(String secondaryRecipient) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("addRecipient(recipient=" + secondaryRecipient + ")");
        List<String> tmp = new ArrayList<String>(Arrays.asList(this.secondaryRecipients));
        tmp.add(secondaryRecipient);
        this.secondaryRecipients = tmp.toArray(new String[tmp.size()]);
        if (LOGGER.isInfoEnabled()) LOGGER.info("addSecondaryRecipient(String) returns void");
    }

    public void setSubject(String subject) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSubject(subject=" + subject + ")");
        this.subject = subject;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSubject(String) returns void");
    }

    public void setReplyToAddress(String replyToAddress) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setReplyToAddress(replyToAddress=" + replyToAddress + ")");
        this.replyToAddress = replyToAddress;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setReplyToAddress(String) returns void");
    }

    public void setLanguage(String language) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setLanguage(language=" + language + ")");
        this.language = language;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setLanguage(String) returns void");
    }

    public void setType(MailboxMessageType type) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setType(type=" + type + ")");
        this.type = type;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setType(MailboxMessageType) returns void");
    }

    public void setUrgent(boolean urgent) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setUrgent(urgent=" + urgent + ")");
        this.urgent = urgent;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setUrgent(boolean) returns void");
    }

    public void setConfidential(boolean confidential) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setConfidential(confidential=" + confidential + ")");
        this.confidential = confidential;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setConfidential(boolean) returns void");
    }

    public void setDeliveryDate(Date deliveryDate) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setDeliveryDate(deliveryDate=" + deliveryDate + ")");
        this.deliveryDate = deliveryDate;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setDeliveryDate(Date) returns void");
    }

    public Date getDeliveryDate() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getDeliveryDate() retuens " + deliveryDate);
        return deliveryDate;
    }

    /**
     * Get an additional message property value.
     * Addtional properties are configurable.
     *
     * @param name Addtional property key.
     * @param value property value. Null-values are allowed.
     */
    public void setAdditionalProperty(String name, String value) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setAdditionalProperty(name=" + name + ",value=" + value + ")");
        additionalProperties.put(getAdditionalPropertyName(name), value);
        if (LOGGER.isInfoEnabled()) LOGGER.info("setAdditionalProperty(String,String) returns void");
    }

    public void setSenderVisibility(String senderVisibility) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSenderVisibility(senderVisibility=" + senderVisibility + ")");
        this.senderVisibility = senderVisibility;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setSenderVisibility(Date) returns void");
    }
    
}
