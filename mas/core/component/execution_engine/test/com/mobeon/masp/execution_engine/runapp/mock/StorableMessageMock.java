package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.common.message_sender.IInternetMailSender;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Mock object for a storable message.
 */
public class StorableMessageMock extends BaseMock implements IStorableMessage {

    /**
     * The mail sender for this storable message.
     */
    private IInternetMailSender mailSender = null;

    /**
     * Creates a mock object for a storable message.
     */
    public StorableMessageMock ()
    {
        super ();
        log.info ("StorableMessageMock.StorableMessageMock");
    }

    /**
     * Sets the mail sender for this object.
     *
     * @param mailSender The internet mail sender.
     */
    public void setMailSender(IInternetMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Returns with this objects mail sender.
     *
     * @return This objects mailsender.
     */
    public IInternetMailSender getMailSender() {
        return mailSender;
    }

    /**
     * Adds content to the message. Multiple calls allowed.
     * @param mediaObject not used
     * @deprecated Use {@link #addContent(IMediaObject, MessageContentProperties, boolean)} instead.
     */
    public void addContent(IMediaObject mediaObject)
    {
        log.info ("MOCK: StorableMessageMock.addContent");
        log.info ("MOCK: StorableMessageMock.addContent unimplemented!");
    }

    /**
     * Adds content to the message.
     *
     */
    public void addContent(IMediaObject mediaObject, MessageContentProperties mcp)
    {
        log.info ("MOCK: StorableMessageMock.addContent");
        log.info ("MOCK: StorableMessageMock.addContent unimplemented!");
    }


    /**
     * Adds content to the message.
     *
     */
    public void addContent(IMediaObject mediaObject, MessageContentProperties mcp, boolean appendContent)
    {
        log.info ("MOCK: StorableMessageMock.addContent with append boolean");
        log.info ("MOCK: StorableMessageMock.addContent with append boolean unimplemented!");
    }

    /**
     * Adds content to the message. Multiple calls allowed.
     * todo
     * @param mediaObject content
     * @param description content description
     * @param dispostionProperties Not used
     */
    public void addContent(IMediaObject mediaObject, String description, Map<String,String> dispostionProperties)
    {
        log.info ("MOCK: StorableMessageMock.addContent");
        log.info ("MOCK: StorableMessageMock.addContent unimplemented!");
    }


    /**
     * Sets the spoken name of sender.
     * @param mediaObject Not used
     * @deprecated Use {@link #setSpokenNameOfSender(IMediaObject, String, Map)} instead.
     */
    public void setSpokenNameOfSender(IMediaObject mediaObject)
    {
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender");
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender unimplemented!");
    }


    /**
     * Sets the spoken name of sender.
     * @param mediaObject content
     * @param description content description
     * @param dispostionProperties Not used
     */
    public void setSpokenNameOfSender(IMediaObject mediaObject, String description, Map<String,String> dispostionProperties)
    {
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender");
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender unimplemented!");
    }

    /**
     * Sets the spoken name of the sender.
     */
    public void setSpokenNameOfSender(IMediaObject mediaObject, MessageContentProperties mcp)
    {
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender");
        log.info ("MOCK: StorableMessageMock.setSpokenNameOfSender unimplemented!");
    }

    /**
     * Sets the sender.
     */
    public void setSender(String sender)
    {
        log.info ("MOCK: StorableMessageMock.setSender");
        log.info ("MOCK: StorableMessageMock.setSender unimplemented!");
    }

    /**
     * Sets the recipients
     */
    public void setRecipients(String... recipients)
    {
        log.info ("MOCK: StorableMessageMock.setRecipients");
        log.info ("MOCK: StorableMessageMock.setRecipients unimplemented!");
    }

    /**
     * Adds a recipient
     */
    public void addRecipient(String recipient)
    {
        log.info ("MOCK: StorableMessageMock.addRecipient");
        log.info ("MOCK: StorableMessageMock.addRecipient unimplemented!");
    }

    /**
     * Sets the secondary recipients.
     */
    public void setSecondaryRecipients(String... recipients)
    {
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients unimplemented!");
    }

    /**
     * Adds a secondary recipient.
     */
    public void addSecondaryRecipient(String recipient)
    {
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients unimplemented!");
    }

    /**
     * Sets the subject.
     */
    public void setSubject(String subject)
    {
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients unimplemented!");
    }

    /**
     * Sets the reply-to address.
     */
    public void setReplyToAddress(String replyToAddress)
    {
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients unimplemented!");

    }

    /**
     * Sets the message type.
     */
    public void setType(MailboxMessageType type)
    {
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.setSecondaryRecipients unimplemented!");

    }

    /**
     * Sets the urgent message indicator.
     */
    public void setUrgent(boolean urgent)
    {
        log.info ("MOCK: StorableMessageMock.setUrgent");
        log.info ("MOCK: StorableMessageMock.setUrgent unimplemented!");

    }

    /**
     * Sets the confidential indicator.
     */
    public void setConfidential(boolean confidential)
    {
        log.info ("MOCK: StorableMessageMock.setConfidential");
        log.info ("MOCK: StorableMessageMock.setConfidential unimplemented!");

    }

    /**
     * Sets the delivery date.
     */
    public void setDeliveryDate(Date deliveryDate)
    {
        log.info ("MOCK: StorableMessageMock.setDeliveryDate");
        log.info ("MOCK: StorableMessageMock.setDeliveryDate unimplemented!");

    }

    public Date getDeliveryDate() {
        log.info ("MOCK: StorableMessageMock.getDeliveryDate");
        log.info ("MOCK: StorableMessageMock.getDeliveryDate unimplemented!");
        return null;
    }

    /**
     * Sets the message language.
     */
    public void setLanguage(String language)
    {
        log.info ("MOCK: StorableMessageMock.setLanguage");
        log.info ("MOCK: StorableMessageMock.setLanguage unimplemented!");

    }

    public void setAdditionalProperty(String name, String value) {
        log.info("MOCK: setAdditionalProperty not implemented in MOCK");
    }

    /**
     * Stores the message in message repository.
     * A preferred host will be used.
     */
    public void store() throws MailboxException
    {
        log.info ("MOCK: StorableMessageMock.store");
        log.info ("MOCK: StorableMessageMock.store unimplemented!");
    }


    /**
     * Stores the message in message repository.
     * The given host will be used.
     */
    public void store(String host) throws MailboxException
    {
        log.info ("MOCK: StorableMessageMock.store");
        log.info ("MOCK: StorableMessageMock.store unimplemented!");
    }

    /**
     * Returns the spoken name of sender.
     * @return the spoken name of sender as a media object. (If exists, null otherwise)
     */
    public IMediaObject getSpokenNameOfSender()
            throws MailboxException
    {
        log.info ("MOCK: StorableMessageMock.getSpokenNameOfSender");
        log.info ("MOCK: StorableMessageMock.getSpokenNameOfSender unimplemented!");
        return null;
    }

    /**
     * Get the message content parts top down.
     * @return list of message content.
     */
    public List<IMessageContent> getContent()
            throws MailboxException
    {
        log.info ("MOCK: StorableMessageMock.getContent");
        log.info ("MOCK: StorableMessageMock.getContent unimplemented!");
        return null;
    }


    /**
     * Gets message sender represented as a string.
     * @return message sender string.
     */
    public String getSender()
    {
        log.info ("MOCK: StorableMessageMock.getSender");
        log.info ("MOCK: StorableMessageMock.getSender unimplemented!");
        return null;
    }

    /**
     * Gets message recipients. represented as an array of strings.
     * @return  message recipient string array.
     */
    public String[] getRecipients()
    {
        log.info ("MOCK: StorableMessageMock.getRecipients");
        log.info ("MOCK: StorableMessageMock.getRecipients unimplemented!");
        return null;
    }

    /**
     * Gets message secondary recipients represented as an array of strings.
     * @return  message secondary recipient string array.
     */
    public String[] getSecondaryRecipients()
    {
        log.info ("MOCK: StorableMessageMock.getSecondaryRecipients");
        log.info ("MOCK: StorableMessageMock.getSecondaryRecipients unimplemented!");
        return null;
    }

    /**
     * Gets message subsject.
     * @return message subject string.
     */
    public String getSubject()
    {
        log.info ("MOCK: StorableMessageMock.getSubject");
        log.info ("MOCK: StorableMessageMock.getSubject unimplemented!");
        return null;
    }

    /**
     * Gets message reply-to address as a string.
     * @return message reply-to address string.
     */
    public String getReplyToAddress()
    {
        log.info ("MOCK: StorableMessageMock.getReplyToAddress");
        log.info ("MOCK: StorableMessageMock.getReplyToAddress unimplemented!");
        return null;
    }


    /**
     * Gets message type.
     * @return message type.
     */
    public MailboxMessageType getType()
    {
        log.info ("MOCK: StorableMessageMock.getType");
        log.info ("MOCK: StorableMessageMock.getType unimplemented!");
        return null;
    }

    /**
     * Indicates if the message is urgent.
     * @return true if message is defined as urgent.(false otherwise)
     */
    public boolean isUrgent()
    {
        log.info ("MOCK: StorableMessageMock.isUrgent");
        log.info ("MOCK: StorableMessageMock.isUrgent unimplemented!");
        return false;
    }

    /**
     * Indicates if the message is confidential.
     * @return true if message is defined as confidential.(false otherwise)
     */
    public boolean isConfidential()
    {
        log.info ("MOCK: StorableMessageMock.isConfidential");
        log.info ("MOCK: StorableMessageMock.isConfidential unimplemented!");
        return false;
    }

    /**
     * Gets the language tag.
     * @return language tag according to ISO 639-1
     */
    public String getLanguage()
    {
        log.info ("MOCK: StorableMessageMock.getLanguage");
        log.info ("MOCK: StorableMessageMock.getLanguage unimplemented!");
        return null;
    }

     /**
     * Get an additional message property value.
     * Addtional properties are configurable.
     * @param name the of the prperty.
     * @return property value.
     */
    public String getAdditionalProperty(String name)
     {
         log.info ("MOCK: StorableMessageMock.getAdditionalProperty");
         log.info ("MOCK: StorableMessageMock.getAdditionalProperty unimplemented!");
         return null;
     }

	@Override
	public void setSenderVisibility(String value)
	{
	    log.info ("MOCK: StorableMessageMock.setSenderVisibility("+value+")");
	    log.info ("MOCK: StorableMessageMock.setSenderVisibility unimplemented!");
	}

	@Override
	public String getBroadcastLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

}
