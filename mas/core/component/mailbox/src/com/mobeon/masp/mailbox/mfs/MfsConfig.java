/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Properties;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseConfig;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.javamail.JavamailConfig;
import com.mobeon.masp.util.string.NumberFormatter;

/**
 * @author egeobli
 *
 */
public class MfsConfig extends BaseConfig {

	private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailConfig.class);

    //Specific session key values for org.eclipse.angus.mail.imap.IMAPStore
//    public static final String MFS_CONNECTION_TIMEOUT_KEY = "mail.imap.connectiontimeout";
//    public static final String IMAP_COMMAND_TIMEOUT_KEY = "mail.imap.timeout";

    private Properties sessionProperties;

    protected MfsConfig() {
        super();
    }

    protected void init(IGroup configuration) throws MailboxException {

        super.init(configuration);

        sessionProperties = new Properties();

    }

    Properties getSessionProperties() {
        return sessionProperties;
    }

}
