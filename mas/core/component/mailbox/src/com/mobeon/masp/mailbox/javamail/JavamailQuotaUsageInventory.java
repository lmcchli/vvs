/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.FolderNotFoundException;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.QuotaName;
import static com.mobeon.masp.mailbox.QuotaName.TOTAL;
import com.mobeon.masp.mailbox.imap.ImapQuotaUsageInventory;
import org.eclipse.angus.mail.imap.IMAPStore;

import jakarta.mail.*;
import java.util.HashMap;

/**
 * @author QHAST
 */
public class JavamailQuotaUsageInventory extends ImapQuotaUsageInventory<JavamailStoreAdapter,JavamailContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailQuotaUsageInventory.class);


    JavamailQuotaUsageInventory(JavamailStoreAdapter storeAdapter, JavamailContext context) {
        super(storeAdapter,context);
    }

    protected void init() throws MailboxException {
            Store store = null;
        try {
            JavamailStoreAdapter mailbox = getMailbox();
            getContext().getMailboxLock().lock();
            mailbox.closeTouchedFolders();
            store = mailbox.getStore();
            if (store instanceof IMAPStore) {
                init((IMAPStore) store);
            } else {
                throw new UnsupportedOperationException("Quota usage check not supported for "+store.getClass().getName());
            }

            //Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.
            if(getQuota(TOTAL).getMessageUsage()<0) {
                String[] messageQuotaUsageFolders = getContext().getImapProperties().getMessageUsageFolderNames();

                setMessageUsage(TOTAL,StoredMessageCounter.countMessages(mailbox,messageQuotaUsageFolders));
            }
            //--- end interim solution.

        } catch (MessagingException e) {
            LOGGER.warn("Error while getting quota root. Host=" + store, e);

            throw new MailboxException("Error while getting quota root: " + e.getMessage()+ ". URL: "+getURL(store));
        } finally {
            getContext().getMailboxLock().unlock();
        }

    }

    private URLName getURL(Store store) {
        if(store == null)
            return null;
        else
            return store.getURLName();
    }


    private void init(IMAPStore store) throws MessagingException {

        Quota[] quotas = store.getQuota(getContext().getImapProperties().getQuotaRootMailboxName());
        HashMap<String, Quota> imapQuotaMap = new HashMap<String, Quota>(quotas.length);
        for (Quota q : quotas) {
            imapQuotaMap.put(q.quotaRoot, q);
        }

        for (QuotaName name : QuotaName.values()) {
            Quota q = imapQuotaMap.get(composeQuotaName(name));
            if (q != null) {
                for (Quota.Resource r : q.resources) {
                    if (r.name.equalsIgnoreCase(getContext().getImapProperties().getByteUsageQuotaRootResourceName())) {
                        //setByteUsage(name, r.usage, getContext().getImapProperties().getByteUsageQuotaRootResourceUnit());
                    } else if (r.name.equalsIgnoreCase(getContext().getImapProperties().getMessageUsageQuotaRootResourceName())) {
                        setMessageUsage(name, r.usage);
                    }
                }
            }

        }

    }

    private static class StoredMessageCounter {

        private long count = 0;
        private JavamailStoreAdapter mailbox;
        private String[] folderNames;

        private StoredMessageCounter(JavamailStoreAdapter mailbox, String[] folderNames) {
            this.mailbox = mailbox;
            this.folderNames = folderNames;
        }

        private static long countMessages(JavamailStoreAdapter mailbox, String[] folderNames) throws MailboxException {
            StoredMessageCounter counter = new StoredMessageCounter(mailbox,folderNames);
            return counter.count();
        }

        private long count() throws MailboxException {
            for (String folderName : folderNames) {
                try {
                    JavamailFolderAdapter folder = mailbox.getFolder(folderName);
                    countMessages(folder.folder);
                }  catch(FolderNotFoundException e) {
                    LOGGER.debug(e.getMessage());
                }
            }
            return count;
        }

        private void countMessages(Folder folder) throws MailboxException {
            try {
                count += folder.getMessageCount();
                for(Folder f: folder.list()) {
                    countMessages(f);
                }
            } catch (MessagingException e) {
                throw new MailboxException("Unable to count messages in "+folder.getFullName()+": "+e.getMessage()+". URL: "+mailbox.getStore().getURLName());
            }

        }
    }


}
