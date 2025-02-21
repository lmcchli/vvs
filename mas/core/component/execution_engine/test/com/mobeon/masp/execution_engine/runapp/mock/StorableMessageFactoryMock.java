package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mailbox.IStorableMessage;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.common.message_sender.IInternetMailSender;

/**
 * Mock object for a storable message factory.
 */
public class StorableMessageFactoryMock extends BaseMock implements IStorableMessageFactory {

    /**
     * The internet sender for this object.
     */
    private IInternetMailSender internetMailSender = null;

    /**
     * Returns with this objects internet sender.
     *
     * @return The internet mail sender.
     */
    public IInternetMailSender getInternetMailSender() {
        return internetMailSender;
    }

    /**
     * Sets this obejcts internet mail sender.
     *
     * @param internetMailSender The internet sender of this object.
     */
    public void setInternetMailSender(IInternetMailSender internetMailSender) {
        this.internetMailSender = internetMailSender;
    }

    /**
     * Creates the mock class
     */
    public StorableMessageFactoryMock ()
    {
        super ();
        log.info ("MOCK: StorableMessageFactoryMock.StorableMessageFactoryMock");
    }

    /**
     * Creates a new storable messsage.
     * @return
     * @throws com.mobeon.masp.mailbox.MailboxException if a storbable message not could be created of some reason.
     */
    public IStorableMessage create() throws MailboxException
    {
        log.info ("MOCK: StorableMessageFactoryMock.create");
        StorableMessageMock ism = new StorableMessageMock ();
        ism.setMailSender (internetMailSender);
        return ism;
    }

}