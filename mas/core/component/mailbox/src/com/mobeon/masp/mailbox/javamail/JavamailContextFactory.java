/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.imap.ImapContextFactory;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Properties;

/**
 * @author QHAST
 */
public class JavamailContextFactory extends ImapContextFactory<JavamailContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailContextFactory.class);

    private IInternetMailSender internetMailSender;
    private Properties defaultSessionProperties;
    private JavamailBehavior javamailBehavior;

    public JavamailBehavior getJavamailBehavior() {
        return javamailBehavior;
    }

    public void setJavamailBehavior(JavamailBehavior javamailBehavior) {
        this.javamailBehavior = javamailBehavior;
    }

    public IInternetMailSender getInternetMailSender() {
        return internetMailSender;
    }

    public void setInternetMailSender(IInternetMailSender internetMailSender) {
        this.internetMailSender = internetMailSender;
    }

    public Properties getDefaultSessionProperties() {
        return defaultSessionProperties;
    }

    public void setDefaultSessionProperties(Properties defaultSessionProperties) {
        this.defaultSessionProperties = defaultSessionProperties;
    }

    protected JavamailContext newContext() {
        return  new JavamailContext(defaultSessionProperties,internetMailSender,getImapProperties(),javamailBehavior);
    }


}
