/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author Håkan Stolt
 */
public class SmtpInternetMailSenderConfig {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(SmtpInternetMailSenderConfig.class);

    private String smtpServiceName;
    private int smtpRetries = 1;
    private int smtpConnectionTimeout = 5000;
    private int smtpCommandTimeout = 5000;

    protected void init(IGroup configuration) throws InternetMailSenderException {

        try {

            smtpServiceName = configuration.getString("smtpservicename");
            if (LOGGER.isDebugEnabled()) LOGGER.debug("smtpServiceName=" + smtpServiceName);

            smtpRetries = configuration.getInteger("smtpretries", 1);
            if (smtpRetries < 1) {
                smtpRetries = 1;
            }
            if (LOGGER.isDebugEnabled()) LOGGER.debug("smtpRetries=" + smtpRetries);

            smtpConnectionTimeout = configuration.getInteger("smtpconnectiontimeout", 5000);
            if (smtpConnectionTimeout < 0) {
                smtpConnectionTimeout = 0;
            }
            if (LOGGER.isDebugEnabled()) LOGGER.debug("smtpConnectionTimeout=" + smtpConnectionTimeout);

            smtpCommandTimeout = configuration.getInteger("smtpcommandtimeout", 5000);
            if (smtpCommandTimeout < 0) {
                smtpCommandTimeout = 0;
            }
            if (LOGGER.isDebugEnabled()) LOGGER.debug("smtpCommandTimeout=" + smtpCommandTimeout);

        } catch (ConfigurationException e) {
            throw new InternetMailSenderException(e.getMessage());
        }
    }

    public String getSmtpServiceName() {
        return smtpServiceName;
    }

    public int getSmtpRetries() {
        return smtpRetries;
    }

    public int getSmtpConnectionTimeout() {
        return smtpConnectionTimeout;
    }

    public int getSmtpCommandTimeout() {
        return smtpCommandTimeout;
    }
}
