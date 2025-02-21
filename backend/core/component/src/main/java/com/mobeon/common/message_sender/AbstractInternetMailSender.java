/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;

/**
 * This class provides a skeletal implementation of the {@link IInternetMailSender} interface,
 * to minimize the effort required to implement this interface.
 *
 * @author QHAST
 */
public abstract class AbstractInternetMailSender implements IInternetMailSender {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(AbstractInternetMailSender.class);

    public void sendInternetMail(MimeMessage message) throws InternetMailSenderException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(message=" + message + ")");
        sendInternetMailWork(message, null, null);
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(MimeMessage) returns void");
    }

    public void sendInternetMail(MimeMessage message, String host) throws InternetMailSenderException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(message=" + message + ",host=" + host + ")");
        sendInternetMailWork(message, host, null);
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(MimeMessage,String) returns void");
    }


    public void sendInternetMail(MimeMessage message, SmtpOptions options) throws InternetMailSenderException {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("sendInternetMail(message=" + message + ",smtpOptions=" + options + ")");
        sendInternetMailWork(message, null, options);
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(MimeMessage,SmtpOptions) returns void");
    }

    public void sendInternetMail(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("sendInternetMail(message=" + message + ",host=" + host + ",smtpOptions=" + options + ")");
        sendInternetMailWork(message, host, options);
        if (LOGGER.isInfoEnabled()) LOGGER.info("sendInternetMail(MimeMessage,String,SmtpOptions) returns void");
    }

    protected abstract void sendInternetMailWork(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException;


    protected String toString(Message message) throws InternetMailSenderException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            message.writeTo(bos);
            bos.close();
        } catch (Exception e) {
            throw new InternetMailSenderException("Unable to build a String of Message object.", e);
        }
        return bos.toString();
    }


}
