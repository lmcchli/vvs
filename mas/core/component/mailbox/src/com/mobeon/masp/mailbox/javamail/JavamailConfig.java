/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.configuration.*;
import com.mobeon.masp.mailbox.BaseConfig;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.QuotaUsageInventory;
import com.mobeon.masp.mailbox.QuotaUsageInventory.ByteUsageUnit;
import static com.mobeon.masp.mailbox.QuotaUsageInventory.ByteUsageUnit.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.string.NumberFormatter;

import java.util.Properties;
import java.util.List;

/**
 * @author QHAST
 */
public class JavamailConfig extends BaseConfig {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailConfig.class);

    //Specific session key values for org.eclipse.angus.mail.imap.IMAPStore
    public static final String IMAP_CONNECTION_TIMEOUT_KEY = "mail.imap.connectiontimeout";
    public static final String IMAP_COMMAND_TIMEOUT_KEY = "mail.imap.timeout";

    private Properties sessionProperties;

    protected JavamailConfig() {
        super();
    }

    protected void init(IGroup configuration) throws MailboxException {

        super.init(configuration);

        sessionProperties = new Properties();

        try {

            IGroup imapGroup = configuration.getGroup("imap");

            int connectionTimeout = imapGroup.getInteger("connectiontimeout",5000);
            if(connectionTimeout<0) {
                connectionTimeout = 0;
            }
            if(LOGGER.isDebugEnabled()) LOGGER.debug("connectionTimeout="+connectionTimeout);
            sessionProperties.put(IMAP_CONNECTION_TIMEOUT_KEY, NumberFormatter.PLAIN.format(connectionTimeout));

            int commandTimeout = imapGroup.getInteger("commandtimeout",5000);
            if(commandTimeout<0) {
                commandTimeout =0;
            }
            if(LOGGER.isDebugEnabled()) LOGGER.debug("commandTimeout="+commandTimeout);
            sessionProperties.put(IMAP_COMMAND_TIMEOUT_KEY,NumberFormatter.PLAIN.format(commandTimeout));


        } catch (ConfigurationException e) {
            throw new MailboxException(e.getMessage());
        }



    }

    Properties getSessionProperties() {
        return sessionProperties;
    }

}
