package com.mobeon.masp.mailbox.mfs;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseMailboxAccountManager;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;



public class MfsMailboxAccountManager extends BaseMailboxAccountManager<MfsContext> implements IMailboxAccountManager
{

	MfsStoreAdapter mailbox = null;
	/**
     * Logger.
     */
    private static ILogger log = ILoggerFactory.getILogger(MfsMailboxAccountManager.class);

    /**
     * Default constructor.
     */
    public MfsMailboxAccountManager()
    {
    }



    /**
     * Tries to find and open a mailbox.
     *
     * @param mailboxProfile user profile
     * @return mailbox
     * @throws com.mobeon.masp.mailbox.MailboxNotFoundException
     *          if the requested mailbox not exists.
     * @throws com.mobeon.masp.mailbox.MailboxAuthenticationFailedException
     *
     */
    public IMailbox getMailbox(MailboxProfile mailboxProfile) throws MailboxException {
        Object perf = null;
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("MfsMailboxAccountManager.getMailbox(MailboxProfile)");
	        }
	
	        if (log.isInfoEnabled())
	            log.info("getMailbox(mailboxProfile=" + mailboxProfile + ")");
	
	        if (this.mailbox != null)
	        {
	        	return this.mailbox;
	        }
	        else
	        {
	        	MfsContext context = getContextFactory().create(mailboxProfile);
	        	MfsStoreAdapter storeAdapter = new MfsStoreAdapter(context, mailboxProfile.getAccountId());
	        	if (log.isInfoEnabled()) log.info("getMailbox(MailboxProfile) returns " + storeAdapter);
	        	return storeAdapter;
	        }
        } finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
        }
    }



	public IMailbox getMailbox(IServiceInstance serviceInstance,
			MailboxProfile mailboxProfile) throws MailboxException {
		Object perf = null;
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("MfsMailboxAccountManager.getMailbox(IServiceInstance,MailboxProfile)");
	        }
			if (log.isInfoEnabled())
	            log.info("getMailbox(mailboxProfile=" + mailboxProfile + ")");
	
	        if (this.mailbox != null)
	        {
	        	return this.mailbox;
	        }
	        else
	        {
	        	MfsContext context = getContextFactory().create(mailboxProfile);
	        	MfsStoreAdapter storeAdapter = new MfsStoreAdapter(context, mailboxProfile.getAccountId());
	        	if (log.isInfoEnabled()) log.info("getMailbox(IServiceInstance,MailboxProfile) returns " + storeAdapter);
	        	return storeAdapter;
	        }
        } finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
        }
	}

}
