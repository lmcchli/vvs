/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.javamail.LoggerJavamailDebugOutputStream;

import jakarta.mail.internet.MimeMessage;

/**
 * @author QHAST
 */
public class InternetMailSenderStub extends AbstractInternetMailSender implements IInternetMailSender {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(InternetMailSenderStub.class);

    public static final String PREFERRED_HOST = "PREFERRED_HOST";

    private MimeMessage arrivedMessage;
    private String host;
    private SmtpOptions smtpOptions;

    public void sendInternetMailWork(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException {
        this.smtpOptions = options;
        this.arrivedMessage = message;
        this.host = (host != null && host.length() > 0) ? host : PREFERRED_HOST;

        try {
            message.writeTo(new LoggerJavamailDebugOutputStream(LOGGER));
        } catch (Exception e) {
            new InternetMailSenderException("Tried to write message to logger.", e);
        }
    }

    public MimeMessage getArrivedMessage() {
        return arrivedMessage;
    }

    public String getHost() {
        return host;
    }

    public SmtpOptions getSmtpOptions() {
        return smtpOptions;
    }
}
