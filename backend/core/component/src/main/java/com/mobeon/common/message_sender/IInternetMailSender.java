/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import jakarta.mail.internet.MimeMessage;

/**
 * A IInternetMailSender is able to send a MIME message using a mail server host.
 */
public interface IInternetMailSender {

    /**
     * Sends a MIME message to the recipients specified in message.
     * Implementation shall request a preferred host from the Component registry.
     *
     * @param message a MIME message.
     * @throws InternetMailSenderException If a problem arises upon sending the message.
     */
    public void sendInternetMail(MimeMessage message) throws InternetMailSenderException;

    /**
     * Similar to {@link #sendInternetMail(MimeMessage message)} except that a specific host can
     * be specified. Implementation shall first try the given host and secondary
     * request a preferred host from the Component registry in case of failure.
     *
     * @param message
     * @param host    the host to connect to.
     * @throws InternetMailSenderException
     */
    public void sendInternetMail(MimeMessage message, String host) throws InternetMailSenderException;

    /**
     * Similar to {@link #sendInternetMail(MimeMessage message)} but the SMTP behavior
     * can be change by specifying SMTP options.
     *
     * @param message
     * @param options
     * @throws InternetMailSenderException
     */
    public void sendInternetMail(MimeMessage message, SmtpOptions options) throws InternetMailSenderException;

    /**
     * Similar to {@link #sendInternetMail(jakarta.mail.internet.MimeMessage, String)} but the SMTP behavior
     * can be change by specifying SMTP options.
     *
     * @param message
     * @param options
     * @throws InternetMailSenderException
     */
    public void sendInternetMail(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException;

}
