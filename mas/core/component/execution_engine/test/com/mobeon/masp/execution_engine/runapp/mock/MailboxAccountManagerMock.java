package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;

/**
 * The mock object for the mailbox Account Manager.
 */
public class MailboxAccountManagerMock extends BaseMock implements IMailboxAccountManager {

    /**
     * Creates the mock object for the mailbox account manager.
     */
    public MailboxAccountManagerMock ()
    {
        super ();
    }

    /**
     * Returns with the mailbox.
     *
     * @param serviceInstance Name of the service.
     * @param accountId The account id.
     * @param accountPassword The password for the account.
     * @return The accounts mailbox
     * @throws MailboxException
     */
    public IMailbox getMailbox(IServiceInstance serviceInstance, String accountId, String accountPassword)
            throws MailboxException
    {
        log.info ("MOCK: MailboxAccountManagerMock.getMailbox");
        log.info ("MOCK: MailboxAccountManagerMock.getMailbox unimplemented");
        log.info ("MOCK: MailboxAccountManagerMock.getMailbox");        
        return null;
    }

    public IMailbox getMailbox(IServiceInstance serviceInstance, MailboxProfile mailboxProfile) throws MailboxException {
         log.info ("MOCK: MailboxAccountManagerMock.getMailbox");
        log.info ("MOCK: MailboxAccountManagerMock.getMailbox unimplemented");
        return null;
    }
}
