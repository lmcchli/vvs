/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender.jakarta;

import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.message_sender.InternetMailSenderException;
import com.mobeon.common.message_sender.SmtpInternetMailSender;
import com.mobeon.common.message_sender.SmtpInternetMailSenderConfig;
import com.mobeon.common.message_sender.SmtpOptions;
import static com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender.SendReport.SendStatus.*;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Håkan Stolt
 */
public class JakartaCommonsSmtpInternetMailSender extends SmtpInternetMailSender<SmtpInternetMailSenderConfig> {

    private static final HostedServiceLogger LOGGER = new HostedServiceLogger(ILoggerFactory.getILogger(JakartaCommonsSmtpInternetMailSender.class));

    private int soLingerTime = 5;
    private boolean tcpNoDelay = false;

    public JakartaCommonsSmtpInternetMailSender() {
        atomicConfig = new AtomicReference<SmtpInternetMailSenderConfig>();
    }

    public int getSoLingerTime() {
        return soLingerTime;
    }

    public void setSoLingerTime(int soLingerTime) {
        if (soLingerTime > 0) {
            this.soLingerTime = soLingerTime / 1000;
            if (this.soLingerTime < 1) {
                this.soLingerTime = 1;
            }
        } else {
            this.soLingerTime = soLingerTime;
        }
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    protected SmtpInternetMailSenderConfig newConfig() {
        return new SmtpInternetMailSenderConfig();
    }

    private SendReport sendWork(MimeMessage message, SmtpServiceInstanceDecorator smtpService, SmtpOptions options, SmtpInternetMailSenderConfig currentConfig) throws InternetMailSenderException {
        // Check mail for addresses without domain
        addDomainToAddresses(message, smtpService.getHost());

        //Prepare parameters
        String sender = getMailSender(message, options);
        String[] recipientsEmailAddresses = getRecipientsEmailAddresses(message);

        //Prepare SMTP socket client.
        SMTPClient smtpClient = new SMTPClient();
        smtpClient.setDefaultTimeout(currentConfig.getSmtpConnectionTimeout());
        if (LOGGER.isDebugEnabled()) {
            smtpClient.addProtocolCommandListener(PROTOCOL_COMMAND_LISTENER);
        }
        SmtpRequestResponseTracker tracker = new SmtpRequestResponseTracker();
        smtpClient.addProtocolCommandListener(tracker);


        try {

            //Connect
            IOException connectException = null;
            try {
                smtpClient.connect(smtpService.getHost(), smtpService.getPort());
            } catch (IOException e) {
                connectException = e;
            }
            if (connectException != null) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Could not connect to ").append(smtpService).append(" : ");
                errorMessage.append(connectException.getClass().getName()).append(" : ");
                errorMessage.append(connectException.getMessage());
                LOGGER.notAvailable(
                        smtpService.getProtocol(),
                        smtpService.getHost(),
                        smtpService.getPort(),
                        errorMessage.toString());
                throw connectException;
            } else {
                int replyCode = smtpClient.getReplyCode();
                if (!SMTPReply.isPositiveCompletion(replyCode)) {
                    String errorMessage = "Could not connect to " + smtpService + " : " + smtpClient.getReplyString().trim();
                    LOGGER.notAvailable(
                            smtpService.getProtocol(),
                            smtpService.getHost(),
                            smtpService.getPort(),
                            errorMessage);
                    throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
                } else {
                    LOGGER.available(smtpService.getProtocol(), smtpService.getHost(), smtpService.getPort());
                }
            }

            //Set socket properties.
            smtpClient.setSoTimeout(currentConfig.getSmtpCommandTimeout());
            smtpClient.setSoLinger(soLingerTime > -1, soLingerTime);
            smtpClient.setTcpNoDelay(tcpNoDelay);

            //Login
            if (!smtpClient.login()) {
                String errorMessage = "Initiating contact with the server was unsuccesful! : " + smtpClient.getReplyString().trim();
                throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
            }

            //Mail from
            if (!smtpClient.setSender(sender)) {
                String errorMessage = "Setting Mail From unsuccesful! : " + smtpClient.getReplyString().trim();
                throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
            }

            //Add recipients
            for (String emailAddress : recipientsEmailAddresses) {
                if (!smtpClient.addRecipient(emailAddress)) {
                    String errorMessage = "Adding recipient unsuccesful! : " + smtpClient.getReplyString().trim();
                    if (UnsuccessfulCommandException.isPermanentFailure(smtpClient.getReplyCode())) { //Permanent failure. Syntax error or uknown user. Should not happen. Just logged if it happens.
                        if (LOGGER.isInfoEnabled()) LOGGER.info(errorMessage);
                    } else {
                        throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
                    }
                }
            }

            //Send data
            Writer dataWriter = smtpClient.sendMessageData();
            if (dataWriter == null) {
                String errorMessage = "Cannot send data: " + smtpClient.getReplyString().trim();
                throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
            }
            String data = toString(message);
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Sending message data. (" + data.getBytes().length + " bytes)");
            dataWriter.write(data);
            dataWriter.flush();
            dataWriter.close();

            try {
                if (!smtpClient.completePendingCommand()) {
                    String errorMessage = "Message data dot termination unsuccessful! : " + smtpClient.getReplyString().trim();
                    throw new UnsuccessfulCommandException(smtpClient.getReplyCode(), errorMessage);
                }
            } catch (SocketTimeoutException e) {
                //Likely
                return new SendReport(LIKELY, e, "Timeout while waiting for response from message data dot termination.");
            }

            try {
                smtpClient.logout();
            } catch (Throwable e) {
                //Likely
                return new SendReport(LIKELY, e, "Sent \"" + tracker.getLatestRequest() + "\"");
            }

        } catch (SocketTimeoutException e) {
            getServiceLocator().reportServiceError(smtpService.getDecoratedServiceInstance());//Should this really be reported as an error?
            return new SendReport(FAILED, e, "Sent \"" + tracker.getLatestRequest() + "\"");
        } catch (IOException e) {
            getServiceLocator().reportServiceError(smtpService.getDecoratedServiceInstance());
            return new SendReport(FAILED, e, "Sent \"" + tracker.getLatestRequest() + "\"");
        } catch (UnsuccessfulCommandException e) {
            if (e.isPermanentSyntaxFailure()) {
                throw new InternetMailSenderException("Sent \"" + tracker.getLatestRequest() + "\" : " + e.getMessage());
            }
            if (e.isPermanentFailure()) {
                getServiceLocator().reportServiceError(smtpService.getDecoratedServiceInstance());
            }
            return new SendReport(FAILED, e, "Sent \"" + tracker.getLatestRequest() + "\"");
        } finally {
            try {
                if (smtpClient.isConnected()) {
                    smtpClient.disconnect();
                }
            } catch (Throwable t) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Exception while disconnecting SMTP client!", t);
            }
        }
        return new SendReport(OK);

    }

    protected void sendInternetMailWork(MimeMessage message, String host, SmtpOptions options) throws InternetMailSenderException {


        SmtpInternetMailSenderConfig currentConfig = getConfig();

        int i = 0;
        SendReport report = null;
        while (++i <= currentConfig.getSmtpRetries() && (report == null || report.getSendStatus().equals(FAILED))) {
            SmtpServiceInstanceDecorator smtpService = getSmtpService(host, currentConfig);
            report = sendWork(message, smtpService, options, currentConfig);
            if (report.getSendStatus().equals(FAILED)) {
                if (i < currentConfig.getSmtpRetries()) {
                    if (LOGGER.isDebugEnabled()) LOGGER.debug("SMTP try #" + i + " was unsuccessful: " + report.getMessage());
                    host = null;
                } else {
                    InternetMailSenderException imse = new InternetMailSenderException("Sending message with SMTP was unsuccessful after " + i + " tries. Last try reported:" + report.getMessage());
                    LOGGER.error(imse.getMessage());
                    throw imse;
                }
            } else {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Send report: " + report.getMessage());
            }
        }
    }

    private static ProtocolCommandListener PROTOCOL_COMMAND_LISTENER = new ProtocolCommandListener() {

        /**
         * This method is invoked by a ProtocolCommandEvent source after
         * sending a protocol command to a server.
         * <p/>
         *
         * @param event The ProtocolCommandEvent fired.
         */
        public void protocolCommandSent(ProtocolCommandEvent event) {
            LOGGER.debug(("COMMAND: " + event.getMessage()).trim());
        }

        /**
         * This method is invoked by a ProtocolCommandEvent source after
         * receiving a reply from a server.
         * <p/>
         *
         * @param event The ProtocolCommandEvent fired.
         */
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            LOGGER.debug(("REPLY:   " + event.getMessage()).trim());
        }

    };

    private static class UnsuccessfulCommandException extends Exception {

        private int replyCode;

        private UnsuccessfulCommandException(int replyCode, String message) {
            super(message);
            this.replyCode = replyCode;
        }

        public int getReplyCode() {
            return replyCode;
        }

        public boolean isPermanentFailure() {
            return isPermanentFailure(replyCode);
        }

        public static boolean isPermanentFailure(int replyCode) {
            return replyCode >= 500 && replyCode <= 599;
        }

        public boolean isPermanentSyntaxFailure() {
            return isPermanentSyntaxFailure(replyCode);
        }

        public static boolean isPermanentSyntaxFailure(int replyCode) {
            return replyCode >= 500 && replyCode <= 509;
        }

        public boolean isPermanentMailsystemFailure() {
            return isPermanentMailsystemFailure(replyCode);
        }

        public static boolean isPermanentMailsystemFailure(int replyCode) {
            return replyCode >= 550 && replyCode <= 559;
        }

    }


    public static class SendReport {

        public enum SendStatus {
            FAILED,OK,LIKELY}

        private SendStatus sendStatus = SendStatus.FAILED;
        private Throwable cause = null;
        private String message;

        private SendReport(SendStatus sendStatus) {
            this(sendStatus, null, null);
        }

        @SuppressWarnings({"UNUSED_SYMBOL"})
        private SendReport(SendStatus sendStatus, Throwable cause) {
            this(sendStatus, cause, null);
        }

        @SuppressWarnings({"UNUSED_SYMBOL"})
        private SendReport(SendStatus sendStatus, String message) {
            this(sendStatus, null, message);
        }

        private SendReport(SendStatus sendStatus, Throwable cause, String message) {
            this.sendStatus = sendStatus;
            this.cause = cause;
            StringBuilder sb = new StringBuilder();
            sb.append(sendStatus);
            if (message != null && message.length() > 0) {
                sb.append(": ").append(message);
            }
            if (cause != null) {
                sb.append(": ").append(cause.getClass().getName());
                if (cause.getMessage() != null && cause.getMessage().length() > 0) {
                    sb.append(": ").append(cause.getMessage());
                }
            }
            this.message = sb.toString();
        }


        public SendStatus getSendStatus() {
            return sendStatus;
        }

        public Throwable getCause() {
            return cause;
        }

        public String getMessage() {
            return message;
        }
    }


}
