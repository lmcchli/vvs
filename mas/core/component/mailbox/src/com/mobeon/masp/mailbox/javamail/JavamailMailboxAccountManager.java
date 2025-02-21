/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.*;

import jakarta.mail.*;

/**
 * This class uses JavaMail to implement the {@link com.mobeon.masp.mailbox.IMailboxAccountManager}.
 * @author Håkan Stolt
 */
public class JavamailMailboxAccountManager extends BaseMailboxAccountManager<JavamailContext> implements IMailboxAccountManager {

    /**
     * Logger.
     */
    private static ILogger log = ILoggerFactory.getILogger(JavamailMailboxAccountManager.class);


    /**
     * Default constructor.
     */
    public JavamailMailboxAccountManager() {
    }

    /**
     * Tries to find and open a mailbox.
     *
     * @param serviceInstance the instance to connect to
     * @param mailboxProfile user profile
     * @return mailbox
     * @throws com.mobeon.masp.mailbox.MailboxNotFoundException
     *          if the requested mailbox not exists.
     * @throws com.mobeon.masp.mailbox.MailboxAuthenticationFailedException
     *
     */
    public IMailbox getMailbox(IServiceInstance serviceInstance, MailboxProfile mailboxProfile) throws MailboxException {

        if (log.isInfoEnabled())
            log.info("getMailbox(serviceInstance=" + serviceInstance + ",mailboxProfile=" + mailboxProfile + ")");

        String host = getHost(serviceInstance);
        int port = getPort(serviceInstance);

        Store store;
        JavamailContext context = getContextFactory().create(mailboxProfile);

        try {
            store = context.getStoreManager()
                    .getStore(host, port, mailboxProfile.getAccountId(), mailboxProfile.getAccountPassword());

        } catch (AuthenticationFailedException e) {
            MailboxException e2 = new MailboxAuthenticationFailedException(host, mailboxProfile.getAccountId());
            log.debug(e2.getMessage());
            throw e2;
        } catch (MessagingException e) {
            MailboxException e2 = new MailboxException("Tried to open imap://" + host + ":" + port + ".", e);
            log.error(e2.getMessage());
            throw e2;
        }

        JavamailStoreAdapter storeAdapter = new JavamailStoreAdapter(store, context);
        if (log.isInfoEnabled()) log.info("getMailbox(IServiceInstance,MailboxProfile) returns " + storeAdapter);
        return storeAdapter;
    }
    
    public IMailbox getMailbox(MailboxProfile mailboxProfile) throws MailboxException {
    	return null;    
    }
}
