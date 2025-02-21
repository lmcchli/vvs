/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.io.PrintStream;
import java.util.Properties;


import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseConfig;
import com.mobeon.masp.mailbox.BaseContext;
import com.mobeon.masp.mailbox.ContextFactory;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.imap.ImapContext;
import com.mobeon.masp.mailbox.javamail.JavamailConfig;
import com.mobeon.common.message_sender.IInternetMailSender;

/**
 * @author egeobli
 *
 */
//BaseContext<MfsConfig> 
//public class MfsContext <C extends MfsConfig> extends BaseContext<C>
public class MfsContext extends BaseContext <MfsConfig>
{
	
	 private static final ILogger LOGGER = ILoggerFactory.getILogger(MfsContext.class);
//	 private IInternetMailSender internetMailSender;
	 private final Properties sessionProperties;
//	 private static Session javamailSession;
//	 private static StoreManager storeManager;
//	 private JavamailBehavior javamailBehavior;
	
	 private static IGroup configGroup;
	 
	 MfsContext(Properties defaultSessionProperties) {
	                
	        if(defaultSessionProperties != null) {
	            this.sessionProperties = (Properties) defaultSessionProperties.clone();
	        } else {
	            this.sessionProperties = new Properties();
	        }
	    }


	 protected MfsConfig newConfig() {
	        return new MfsConfig();
	    }
	
	protected synchronized void init(IGroup configGroup) throws MailboxException {
		super.init(configGroup);
		// Cache the Session and StoreManager objects, reread only at changed configuration
		// The reason is that JavaMail should not search the file system for new providers each
		// time a new JavamailContext is created.
		if (MfsContext.configGroup != configGroup) 
		{
			sessionProperties.putAll(getConfig().getSessionProperties());
			if (LOGGER.isDebugEnabled()) LOGGER.debug("sessionProperties=" + sessionProperties);
//			javamailSession = Session.getInstance(sessionProperties);
//			if (javamailSession.getDebug()) {
//				javamailSession.setDebugOut(new PrintStream(new LoggerJavamailDebugOutputStream(LOGGER)));
//			}
//			storeManager = new BasicStoreManager(javamailSession);
			MfsContext.configGroup = configGroup;
		}
	}
	
}
