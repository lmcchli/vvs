package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.common.message_sender.InternetMailSenderException;
import com.mobeon.common.message_sender.SmtpOptions;

import jakarta.mail.internet.MimeMessage;

/**
 * The mock object for the mail sender factory.
 */
public class MailSenderFactoryMock extends BaseMock implements IInternetMailSender {

    /**
     * Creates a MailSenderFactoryMock.
     */
    public MailSenderFactoryMock() {
        super();
    }



    /**
     * Sends a MIME message to the recipients specified in message.
     * Implementation shall locate a preferred host through the Component Registry.
     *
     * @param message a MIME message.
     * @throws com.mobeon.common.message_sender.InternetMailSenderException
     *          If a problem arises upon sending the message.
     */
    public void sendInternetMail(MimeMessage message) throws InternetMailSenderException {
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail");
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail unimplemented");
    }

    /**
     * Similar to sendInternetMail(MimeMessage message) except a specific host can be specified.
     * Implementation shall use given and shall NOT locate a preferred host through the
     * Component Registry in case of failure.
     *
     * @param message
     * @param host    the host to connect to.
     * @throws InternetMailSenderException
     */
    public void sendInternetMail(MimeMessage message, String host) throws InternetMailSenderException {
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail");
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail unimplemented");
    }

    public void sendInternetMail(MimeMessage message, SmtpOptions options) throws InternetMailSenderException {
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail");
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail unimplemented");
    }

    public void sendInternetMail(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException {
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail");
        log.info("MOCK: MailSenderFactoryMock.sendInternetMail unimplemented");
    }
}
