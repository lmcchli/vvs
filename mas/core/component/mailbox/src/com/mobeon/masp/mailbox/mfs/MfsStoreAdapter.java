package com.mobeon.masp.mailbox.mfs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/*
import jakarta.mail.Folder;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
*/

import com.abcxyz.messaging.common.message.MSA;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseMailbox;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.QuotaUsageInventory;
import com.mobeon.masp.mailbox.javamail.JavamailQuotaUsageInventory;

public class MfsStoreAdapter extends BaseMailbox<MfsContext> implements IMailbox
{
	private static final ILogger LOGGER = ILoggerFactory.getILogger(MfsStoreAdapter.class);
	
	private Map<String,MfsFolderAdapter> openedFolders = new HashMap<String,MfsFolderAdapter>();
	private String msId;

	//protected MfsStoreAdapter(Store store,MfsContext context) {
	protected MfsStoreAdapter(MfsContext context, String msId) {
		super(context);
		this.msId = msId;
		}	

	

	@Override
	protected QuotaUsageInventory createQuotaUsageInventory() {
		return  new MfsQuotaUsageInventory(this,getContext());
	}

	
	public IFolder addFolder(String name) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteFolder(String name) throws MailboxException {
		// TODO Auto-generated method stub		
	}

	
	public IFolder getFolder(String name) throws MailboxException 
	{	
		if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(name=" + name + ")");
        try {
            //getContext().getMailboxLock().lock();
            MfsFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new MfsFolderAdapter(name, getContext(), this);                               
                openedFolders.put(name, result);
            } 
            if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(String) returns " + result);
            return result;
        } catch (Exception e) {
            throw new MailboxException("Could not get folder " + name + ": " + e.getMessage()+" URL: "+ this.getMsId() );
        } finally {
//            getContext().getMailboxLock().unlock();
        }        
		//return null;
	}
	
	public IFolder getFolder(String name, ICommonMessagingAccess mfs) throws MailboxException 
	{	
		if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(name=" + name + ")");
        try {
            //getContext().getMailboxLock().lock();
            MfsFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new MfsFolderAdapter(name, getContext(), this, mfs);                               
                openedFolders.put(name, result);
            } 
            if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(String) returns " + result);
            return result;
        } catch (Exception e) {
            throw new MailboxException("Could not get folder " + name + ": " + e.getMessage()+" URL: "+ this.getMsId() );
        } finally {
//            getContext().getMailboxLock().unlock();
        }        
		//return null;
	}

	public String getMsId() {
		return msId;
	}

	public void close() throws MailboxException {
		// TODO Auto-generated method stub		
	}

	public void setReadonly() throws MailboxException {
		// TODO Auto-generated method stub		
	}

	public void setReadwrite() throws MailboxException {
		// TODO Auto-generated method stub		
	}

}
