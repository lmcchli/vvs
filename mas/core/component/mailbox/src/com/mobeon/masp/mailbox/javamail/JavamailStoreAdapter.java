/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.mailbox.*;

import jakarta.mail.*;
import jakarta.mail.FolderNotFoundException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

//import sun.misc.Compare;

/**
 * @author qhast
 */
public class JavamailStoreAdapter extends BaseMailbox<JavamailContext> implements IMailbox {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailStoreAdapter.class);

    private int openMode = Folder.READ_WRITE;
    private Store store;
    private Map<String,JavamailFolderAdapter> openedFolders = new HashMap<String,JavamailFolderAdapter>();
    protected Set<Folder> touchedFolders = new CopyOnWriteArraySet<Folder>();

    JavamailStoreAdapter(Store store,JavamailContext context) {
        super(context);
        this.store = store;
    }

    Store getStore() {
        return store;
    }

    protected QuotaUsageInventory createQuotaUsageInventory(){
        return  new JavamailQuotaUsageInventory(this,getContext());
    }

    void closeTouchedFolders(Folder... exceptedFolders) throws MessagingException {
        if(getContext().getJavamailBehavior().getCloseNonSelectedFolders()) {
            try {
                if(LOGGER.isDebugEnabled()) LOGGER.debug("Closing touched folders, exceptions="+Arrays.asList(exceptedFolders));
                Collection<Folder> exceptions = Arrays.asList(exceptedFolders);
                getContext().getMailboxLock().lock();
                for(Folder touchedFolder : touchedFolders) {
                    if(!exceptions.contains(touchedFolder)) {
                        if(touchedFolder.isOpen()) {
                            touchedFolder.close(false);
                        }
                    }
                }
            } finally {
                getContext().getMailboxLock().unlock();
            }
        }
    }

    /**
     * Closes this mailbox if open and releases all allocated resources.
     * Deletes all messages marked for deletion.
     *
     * @throws com.mobeon.masp.mailbox.MailboxException
     *          if a problem occur.
     * @see com.mobeon.masp.mailbox.StoredMessageState
     */
    public void close() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("close()");
        try {
            getContext().getMailboxLock().lock();
            //todo
            for (Folder touchedFolder : touchedFolders) {
                if (touchedFolder.isOpen()) {
                    touchedFolder.close(true);
                    touchedFolders.remove(touchedFolder);
                }
            }
            //todo
            for (Folder touchedFolder : touchedFolders) {
                touchedFolder.open(Folder.READ_WRITE);
                touchedFolder.close(true);
            }
            getContext().getStoreManager().returnStore(store);
        } catch (MessagingException e) {
            throw new MailboxException("Error while closing Mailbox. URL: "+ store.getURLName()+". "+ e.getMessage());
        } finally {
            getContext().getMailboxLock().unlock();
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("close() returns void");
    }

/*    public void setReadonly() {
        this.readOnlyMailbox = true;
    }

  */
    public void setReadonly() {
        this.openMode = Folder.READ_ONLY;
    }

    public void setReadwrite() {
        this.openMode = Folder.READ_WRITE;
    }


    public JavamailFolderAdapter getFolder(String name) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();
            JavamailFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new JavamailFolderAdapter(store.getFolder(name), getContext(), this);
                if (openMode == Folder.READ_ONLY) {
                    result.setReadonly();
                } else {
                    result.setReadwrite();
                }
                result.open();
                openedFolders.put(name, result);

            } else{

                Integer mode = result.getMode();
                
                if (!mode.equals(openMode)) {
                    // the folder was opend. but in wrong mode, reeopen witn another mode
                    if (openMode == Folder.READ_ONLY) {
                        result.setReadonly();
                    } else {
                        result.setReadwrite();
                    }
                    result.open();
                }
            }
            if (LOGGER.isInfoEnabled()) LOGGER.info("getFolder(String) returns " + result);
            return result;
        } catch (FolderNotFoundException e) {
            throw new com.mobeon.masp.mailbox.FolderNotFoundException(name+" URL: "+store.getURLName());
        } catch (MessagingException e) {
            throw new MailboxException("Could not get folder " + name + ": " + e.getMessage()+" URL: "+store.getURLName());
        } finally {
            getContext().getMailboxLock().unlock();
        }

    }

    public JavamailFolderAdapter addFolder(String name) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("addFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();
            JavamailFolderAdapter result = new JavamailFolderAdapter(store.getFolder(name), getContext(), this);
            result.create();
            openedFolders.put(name, result);
            if (LOGGER.isInfoEnabled()) LOGGER.info("addFolder(String) returns " + result);
            return result;
        } catch (MessagingException e) {
            throw new MailboxException(e.getMessage()+" URL: "+store.getURLName());
        } finally {
            getContext().getMailboxLock().unlock();
        }
    }

    public void deleteFolder(String name) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("deleteFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();
            JavamailFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new JavamailFolderAdapter(store.getFolder(name), getContext(), this);
            }
            result.delete();
            openedFolders.remove(name);
        } catch (MessagingException e) {
            throw new MailboxException(e.getMessage()+" URL: "+store.getURLName());
        } finally {
            getContext().getMailboxLock().unlock();
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("deleteFolder(String) returns void");
    }


    public String toString() {
        return store.toString();
    }

	
	public IFolder getFolder(String name, ICommonMessagingAccess mfs)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}



}
