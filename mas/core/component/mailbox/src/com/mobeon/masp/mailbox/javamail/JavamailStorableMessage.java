/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.util.ContentTypePatterns;
import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.jaf.DataSourceAdapter;
import com.mobeon.common.message_sender.InternetMailSenderException;
import com.mobeon.common.message_sender.SmtpOptions;
import com.mobeon.masp.util.javamail.LoggerJavamailDebugOutputStream;

import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @author qhast
 */
public class JavamailStorableMessage extends BaseStorableMessage<JavamailContext> {

    private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();


    private ILogger LOGGER = ILoggerFactory.getILogger(JavamailStorableMessage.class);


    private SmtpOptions smtpOptions;

    private boolean prependAddedContent;

    JavamailStorableMessage(JavamailContext context) {
        this(context,false);
    }

    JavamailStorableMessage(JavamailContext context, boolean prependAddedContent) {
        super(context);
        this.prependAddedContent = prependAddedContent;
    }

    SmtpOptions getSmtpOptions() {
        return smtpOptions;
    }

    void setSmtpOptions(SmtpOptions smtpOptions) {
        this.smtpOptions = smtpOptions;
    }

    /**
     * @throws MessagingException Not thrown by this implementation 
     */
    protected Multipart createMultipart() throws MessagingException {
        return new MimeMultipart(ContentTypePatterns.getMultipartSubType(getType()));
    }


    protected void storeWork(String host) throws MailboxException {

        try {

            MimeMessage javaMailMessage = new MimeMessage(getContext().getJavamailSession());

            // set Subject header
            javaMailMessage.setSubject(subject, "utf-8");

            // set From header
            String senderName = sender.substring(0, sender.indexOf('<'));
            String senderAddress = sender.substring(sender.indexOf('<'));
            try {
                senderName = MimeUtility.encodeText(senderName, "utf-8", "B");
            } catch (UnsupportedEncodingException e) {
                LOGGER.warn("Error while encoding From into utf-8", e);
            }
            sender = senderName + senderAddress;
            javaMailMessage.setHeader("From",sender);

            // set Language
            javaMailMessage.setContentLanguage(language.split(","));

            if (deliveryDate != null && deliveryDate.before(new Date())) {
                setDeliveryDate(null);
            }

            // set To header (Add deferred delivery subdomain if nessesary)
            javaMailMessage.setRecipients(
                    MimeMessage.RecipientType.TO,
                    createAddresses(deliveryDate != null, recipients)
            );

            // set Cc header (Add deferred delivery subdomain if nessesary)
            javaMailMessage.setRecipients(
                    MimeMessage.RecipientType.CC,
                    createAddresses(deliveryDate!= null, secondaryRecipients)
            );

            //set Date and Deferred-Delivery header
            if (deliveryDate != null) {
                javaMailMessage.setSentDate(deliveryDate);
                javaMailMessage.setHeader("Deferred-Delivery", MAIL_DATE_FORMAT.format(deliveryDate));
            }

            //set Reply-To header
            if (replyToAddress != null && replyToAddress.length() > 0) {
                javaMailMessage.setReplyTo(createAddresses(replyToAddress));
            }

            // set urgent header
            if (urgent) {
                UrgentHeaderUtil.setUrgentHeader(javaMailMessage);
            } else {
                UrgentHeaderUtil.unsetUrgentHeader(javaMailMessage);
            }

            // set confidential header
            if (confidential) {
                ConfidentialHeaderUtil.setConfidentialHeader(javaMailMessage);
            } else {
                ConfidentialHeaderUtil.unsetConfidentialHeader(javaMailMessage);
            }

            //Additional properties
            Map<String, String> additionalPropertyMap = getContext().getConfig().getAdditionalPropertyMap();
            for (String name : additionalPropertyMap.keySet()) {
                String field = additionalPropertyMap.get(name);
                String value = getAdditionalProperty(name);
                if (value != null) {
                    javaMailMessage.setHeader(field, value);
                }
            }

            //Set Content
            Multipart multipartContent = createMultipart();
            if(content != null) {
                int pos;
                if(prependAddedContent) {
                    pos = 0;
                } else {
                    pos = multipartContent.getCount();
                }
                for (IMessageContent c : content) {
                    try {
                        MimeBodyPart messageContentBodyPart = createBodyPart(c, false);
                        multipartContent.addBodyPart(messageContentBodyPart,pos++);
                    } catch (Exception e) {
                        LOGGER.warn("Error while adding Message Content!", e);
                    }
                }
            }
            if (spokenNameOfSender != null) {

                try {
                    MimeBodyPart spokenNameContentBodyPart = createBodyPart(spokenNameOfSender, true);
                    multipartContent.addBodyPart(spokenNameContentBodyPart);
                } catch (Exception e) {
                    LOGGER.warn("Error while adding Spoken Name of Sender!", e);
                }

            } else {
                LOGGER.debug("Spoken Name of Sender not set in Storable Message.");
            }
            javaMailMessage.setContent(multipartContent);

            // Save changes before sending.
            javaMailMessage.saveChanges();

            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Storing message {");
                sb.append("subject=").append(getSubject());
                sb.append(",type=").append(getType());
                sb.append(",sender=").append(getSender());
                sb.append(",recipients=").append(Arrays.asList(getRecipients()).toString());
                sb.append(",secondaryRecipients=").append(Arrays.asList(getSecondaryRecipients()).toString());
                sb.append(",urgent=").append(isUrgent());
                sb.append(",confidential=").append(isConfidential());
                sb.append(",replyToAddress=").append(getReplyToAddress());
                sb.append(",deliveryDate=").append(getDeliveryDate());
                sb.append(",language=").append(getLanguage());
                sb.append(",spokenNameOfSender=").append(getSpokenNameOfSender());
                sb.append(",content=").append(getContent());
                for (String additionalPropertyName : additionalPropertyMap.keySet()) {
                    sb.append(",additionalprop::").append(additionalPropertyName);
                    sb.append("=").append(getAdditionalProperty(additionalPropertyName));
                }
                sb.append("}");
                LOGGER.debug(sb.toString());

                try {
                    javaMailMessage.writeTo(new LoggerJavamailDebugOutputStream(LOGGER));
                } catch (IOException e) {
                    LOGGER.debug("Could not write javamail message to log: " + e.getMessage());
                }

            }

            if (host != null) {
                getContext().getInternetMailSender().sendInternetMail(javaMailMessage, host, smtpOptions);
            } else {
                getContext().getInternetMailSender().sendInternetMail(javaMailMessage, smtpOptions);
            }

        } catch (MessagingException e) {
            throw new MailboxException("Error while preparing JavaMail message. Host: "+host,e);
        } catch (InternetMailSenderException e) {
            throw new MailboxException("Error while sending JavaMail message. Host: "+host,e);
        }

    }

    private MimeBodyPart createBodyPart(IMessageContent messageContent, boolean spokenNameOfSender) throws MessagingException, MailboxException {


        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        IMediaObject mediaObject = messageContent.getMediaObject();
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        MessageContentProperties contentProperties = messageContent.getContentProperties();

        //Content-Duration
        if (mediaProperties.hasLengthInUnit(MILLISECONDS)) {
            long milliseconds = mediaProperties.getLengthInUnit(MILLISECONDS);
            long seconds = Math.round(((double) milliseconds) / 1000); //Convert milliseconds to seconds.
            mimeBodyPart.setHeader("Content-Duration", String.valueOf(seconds));
        }

        //Content-Description
        mimeBodyPart.setDescription(contentProperties.getDescription(), "utf-8");

        //Content-Language
        mimeBodyPart.setContentLanguage(contentProperties.getLanguage().split(","));

        //Content-Disposition
        ContentDisposition contentDisposition =
                ContentDispositionHeaderUtil.createContentDisposition(spokenNameOfSender, contentProperties, mediaProperties);
        mimeBodyPart.setDisposition(contentDisposition.toString());

        //Add content
        mimeBodyPart.setDataHandler(new DataHandler(new DataSourceAdapter(mediaObject,
                contentDisposition.getParameter(ContentDispositionHeaderUtil.FILENAME_PARAMETER_NAME))));

        return mimeBodyPart;

    }

    private jakarta.mail.Address[] createAddresses(String... recipients) throws AddressException {
        return createAddresses(false, recipients);
    }

    private jakarta.mail.Address[] createAddresses(boolean addDeferredSubDomain, String... recipients) throws AddressException {
        jakarta.mail.Address[] result = new jakarta.mail.Address[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            String recipient = recipients[i];
            if (addDeferredSubDomain) {
                recipient = recipient.replaceFirst("@", "@deferred.");
            }
            result[i] = new InternetAddress(recipient);
        }
        return result;
    }

	@Override
	public String getBroadcastLanguage() {
		// TODO Auto-generated method stub
		return null;
    }


}
