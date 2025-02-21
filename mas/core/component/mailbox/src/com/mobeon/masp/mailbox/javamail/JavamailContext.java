/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.imap.ImapContext;
import com.mobeon.masp.mailbox.imap.ImapProperties;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.masp.util.javamail.BasicStoreManager;
import com.mobeon.masp.util.javamail.LoggerJavamailDebugOutputStream;
import com.mobeon.masp.util.javamail.StoreManager;

import jakarta.mail.Session;
import java.io.PrintStream;
import java.util.Properties;

/**
 * @author QHAST
 */
public class JavamailContext extends ImapContext<JavamailConfig> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailContext.class);

    private IInternetMailSender internetMailSender;
    private final Properties sessionProperties;
    private static Session javamailSession;
    private static StoreManager storeManager;
    private JavamailBehavior javamailBehavior;
    private static IGroup configGroup;

    JavamailContext(Properties defaultSessionProperties, IInternetMailSender internetMailSender, ImapProperties imapProperties, JavamailBehavior javamailBehavior) {
        super(imapProperties);

        if(internetMailSender == null) throw new IllegalArgumentException("internetMailSender cannot be null!");
        this.internetMailSender = internetMailSender;

        if(defaultSessionProperties != null) {
            this.sessionProperties = (Properties) defaultSessionProperties.clone();
        } else {
            this.sessionProperties = new Properties();
        }

        if(javamailBehavior == null) {
            this.javamailBehavior = new JavamailBehavior();
        } else {
            this.javamailBehavior = javamailBehavior;
        }

    }

    final JavamailBehavior getJavamailBehavior() {
        return javamailBehavior;
    }

    final IInternetMailSender getInternetMailSender() {
        return internetMailSender;
    }

    final synchronized StoreManager getStoreManager() {
        return storeManager;
    }

    final synchronized Session getJavamailSession() {
        return javamailSession;
    }

    protected JavamailConfig newConfig() {
        return new JavamailConfig();
    }

    @Override
    protected synchronized void init(IGroup configGroup) throws MailboxException {
        super.init(configGroup);
        // Cache the Session and StoreManager objects, reread only at changed configuration
        // The reason is that JavaMail should not search the file system for new providers each
        // time a new JavamailContext is created.
        if (JavamailContext.configGroup != configGroup) {
            sessionProperties.putAll(getConfig().getSessionProperties());
            if (LOGGER.isDebugEnabled()) LOGGER.debug("sessionProperties=" + sessionProperties);
            javamailSession = Session.getInstance(sessionProperties);
            if (javamailSession.getDebug()) {
                javamailSession.setDebugOut(new PrintStream(new LoggerJavamailDebugOutputStream(LOGGER)));
            }
            storeManager = new BasicStoreManager(javamailSession);
            JavamailContext.configGroup = configGroup;
        }
    }





}
