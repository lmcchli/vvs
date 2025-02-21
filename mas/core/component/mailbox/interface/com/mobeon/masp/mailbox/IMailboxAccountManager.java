/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * A Mailbox account manager is able to locate and return a mailbox.
 * @author qhast
 */
public interface IMailboxAccountManager {

    /**
     * Tries to find and open a mailbox.
     * @param serviceInstance from profile
     * @param mailboxProfile
     * @return mailbox
     * @throws MailboxNotFoundException if the requested mailbox not exists.
     * @throws MailboxAuthenticationFailedException 
     */
    public IMailbox getMailbox(IServiceInstance serviceInstance, MailboxProfile mailboxProfile) throws MailboxException;    
   

}
