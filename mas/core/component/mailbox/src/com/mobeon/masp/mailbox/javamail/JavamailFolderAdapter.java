/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.ConnectionListener;
import jakarta.mail.event.FolderEvent;
import jakarta.mail.event.FolderListener;
import jakarta.mail.event.MessageChangedEvent;
import jakarta.mail.event.MessageChangedListener;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;

import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseFolder;
import com.mobeon.masp.mailbox.FolderAlreadyExistsException;
import com.mobeon.masp.mailbox.FolderNotFoundException;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.StoredMessageListImpl;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * @author qhast
 */
public class JavamailFolderAdapter extends BaseFolder<JavamailContext> implements IFolder {

    private static final ILogger log = ILoggerFactory.getILogger(JavamailFolderAdapter.class);

    protected Folder folder;
    //private boolean readOnlyMailbox = false;
    private int openMode = Folder.READ_WRITE;
    private JavamailStoreAdapter root;
    private String folderString;
    private Map<String, JavamailFolderAdapter> openedFolders = new HashMap<String, JavamailFolderAdapter>();

    JavamailFolderAdapter(Folder folder, JavamailContext context, JavamailStoreAdapter root) {
        super(context);
        this.folder = folder;
        this.root = root;
        if (log.isDebugEnabled()) {
            Debugger debugger = new Debugger();
            this.folder.addConnectionListener(debugger);
            this.folder.addFolderListener(debugger);
            this.folder.addMessageChangedListener(debugger);
            this.folder.addMessageCountListener(debugger);
        }
    }


    /**
     * Syncronize calls to this method.
     * If folder not is opend. The folder will be opend in selected mode.
     * If folder is opend in another mode then selected, the folder will be closed and reopend.
     * @throws MessagingException
     */
    void open() throws MessagingException {
        root.closeTouchedFolders(folder);

        // Is folder opend, if not open it.
        if (!folder.isOpen()) {
            folder.open(openMode);
            root.touchedFolders.add(folder);

            if (log.isDebugEnabled()) {
                String strMode = "read/write";
                if(openMode == Folder.READ_ONLY) strMode="read only";
                log.debug("open folder in "+strMode +" mode");
            }
        // is folder open in another mode then wanted. close folder and reopen in new mode.
        } else if (folder.isOpen() && folder.getMode()!= openMode ) {
            folder.close(false);
            folder.open(openMode);

            if (log.isDebugEnabled()) {
                String strMode = "read/write";
                if(openMode == Folder.READ_ONLY) strMode="read only";
                log.debug("close and reopen folder in "+strMode +" mode");
            }
        }
    }

    /**
     * Syncronize calls to this method.
     * @throws MailboxException
     */
    void create() throws MailboxException {
        try {
            root.closeTouchedFolders(folder);
            if (folder.exists()) {
                throw new FolderAlreadyExistsException(folder.getName()+" . URL: "+getURL());
            } else {
                folder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
            }
        } catch (MessagingException e) {
            throw new MailboxException("Tried to create Folder "+folder.getName()+". URL: "+getURL(),e);
        }
    }

    private jakarta.mail.URLName getURL() {
        return folder.getStore().getURLName();
    }

    /**
     * Syncronize calls to this method.
     * @throws MailboxException
     */
    void delete() throws MailboxException {
        try {
            root.closeTouchedFolders(folder);
            if (folder.exists()) {
                folder.delete(true);
            } else {
                throw new FolderNotFoundException(this.toString()+" . URL: "+getURL());
            }
        } catch (MessagingException e) {
            throw new MailboxException("Tried to delete Folder "+folder.getName()+" . URL: "+getURL(),e);
        }
    }
    
    
    @Override
    public IStoredMessage getMessage(MessageIdentifier msgId)
        throws MailboxException {
        return null;
    }

    protected IStoredMessageList searchMessagesWork(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) throws MailboxException {

        IStoredMessageList result;
        try {
            getContext().getMailboxLock().lock();

            open();

            if (log.isDebugEnabled()) log.debug("Search folder " + this + " for messages matching: " + (criteria == null ? "No criteria" : criteria.toString()));

            //Search messages
            Message[] messages;
            if (criteria == null) {
                messages = folder.getMessages();
            } else {
                messages = folder.search(SearchTermFactory.createSearchTerm(criteria));
            }

            if (log.isDebugEnabled()) log.debug("Found " + messages.length + " messages in folder " + this + (criteria == null ? "" : " (matching critera)"));

            //Fetch properties
            folder.fetch(messages, JavamailMessageAdapter.getStoredMessageProfile(getContext()));

            result = new StoredMessageListImpl(messages.length);
            for (Message m : messages) {
                JavamailMessageAdapter storedMessage = new JavamailMessageAdapter(m, getContext(),this);
                storedMessage.parseMessage();
                result.add(storedMessage);
            }

        } catch (MessagingException e) {
            throw new MailboxException("Tried to fetch and parse messages in "+this+" . URL: "+getURL(),e);
        } catch (IOException e) {
            throw new MailboxException("Tried to fetch and parse messages in "+this+" . URL: "+getURL(),e);
        } finally {
            getContext().getMailboxLock().unlock();
        }

        // Filter possible delivery reports of wrong type before sorting (delivery report types are only known
        // after parsing messages)
        result = result.select(criteria);

        if (comparator != null) {
            //Sort.
            Collections.sort(result, comparator);
        }

        return result;
    }

    public String getName() {
        if (log.isInfoEnabled()) log.info("getName() returns " + folder.getName());
        return folder.getName();
    }

    public void setReadonly() {
        this.openMode = Folder.READ_ONLY;
    }

    public void setReadwrite() {
        this.openMode = Folder.READ_WRITE;
    }

    // returns the mode for the opend folder
    public int getMode() {
        if (folder.isOpen()) {
            return folder.getMode();
        } else {
            return 0;   // Folder not opend
        }
    }


    public JavamailFolderAdapter getFolder(String name) throws MailboxException {
        if (log.isInfoEnabled()) log.info("getFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();

            JavamailFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new JavamailFolderAdapter(folder.getFolder(name), getContext(), root);
                result.open();
                openedFolders.put(name, result);
            }
            if (log.isInfoEnabled()) log.info("getFolder(String) returns " + result);
            return result;
        } catch (jakarta.mail.FolderNotFoundException e) {
            throw new FolderNotFoundException(name+" . URL: "+getURL());
        } catch (MessagingException e) {
            throw new MailboxException("Could not get folder " + name + ": " + e.getMessage()+" . URL: "+getURL());
        } finally {
            getContext().getMailboxLock().unlock();
        }
    }

    public JavamailFolderAdapter addFolder(String name) throws MailboxException {
        if (log.isInfoEnabled()) log.info("addFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();
            JavamailFolderAdapter result = new JavamailFolderAdapter(folder.getFolder(name), getContext(), root);
            result.create();
            openedFolders.put(name, result);
            if (log.isInfoEnabled()) log.info("addFolder(String) returns " + result);
            return result;
        } catch (MessagingException e) {
            throw new MailboxException(e.getMessage()+" . URL: "+getURL());
        } finally {
            getContext().getMailboxLock().unlock();
        }
    }

    public void deleteFolder(String name) throws MailboxException {
        if (log.isInfoEnabled()) log.info("deleteFolder(name=" + name + ")");
        try {
            getContext().getMailboxLock().lock();
            JavamailFolderAdapter result = openedFolders.get(name);
            if (result == null) {
                result = new JavamailFolderAdapter(folder.getFolder(name), getContext(), root);
            }
            result.delete();
            openedFolders.remove(name);
        } catch (MessagingException e) {
            throw new MailboxException(e.getMessage()+" . URL: "+getURL());
        } finally {
            getContext().getMailboxLock().unlock();
        }
        if (log.isInfoEnabled()) log.info("deleteFolder(String) returns vois");
    }


    private static void folderString(Folder folder, StringBuffer sb) {
        sb.append(folder.getStore());
        sb.append("/");
        sb.append(folder);
    }

    private static String folderString(Folder folder) {
        StringBuffer sb = new StringBuffer();
        folderString(folder, sb);
        return sb.toString();
    }

    public String toString() {
        if (folderString == null) {
            folderString = folderString(folder);
        }
        return folderString;
    }


    private static class Debugger implements ConnectionListener, FolderListener, MessageCountListener, MessageChangedListener {

        private Debugger() {
        }


        private String source(ConnectionEvent event) {
            Object source = event.getSource();
            if (source instanceof Folder) {
                Folder folder = (Folder) source;
                return folderString(folder);
            } else {
                return source.toString();
            }
        }

        private String source(MessageChangedEvent event) {
            Object source = event.getSource();
            if (source instanceof Folder) {
                Folder folder = (Folder) source;
                StringBuffer sb = new StringBuffer();
                folderString(folder, sb);
                sb.append("/Message[");
                sb.append(event.getMessage().getMessageNumber());
                sb.append("]");
                return sb.toString();
            } else {
                return source.toString();
            }
        }

        private String source(MessageCountEvent event) {
            Object source = event.getSource();
            if (source instanceof Folder) {
                Folder folder = (Folder) source;
                return folderString(folder);
            } else {
                return source.toString();
            }
        }

        private String source(FolderEvent event) {
            return folderString(event.getFolder());
        }


        public void opened(ConnectionEvent connectionEvent) {
            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " opened!");
        }

        public void disconnected(ConnectionEvent connectionEvent) {
            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " disconnected!");
        }

        public void closed(ConnectionEvent connectionEvent) {
            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " closed!");
        }

        public void folderCreated(FolderEvent folderEvent) {
            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " created!");
        }

        public void folderDeleted(FolderEvent folderEvent) {
            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " deleted!");
        }

        public void folderRenamed(FolderEvent folderEvent) {
            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " renamed to " + folderEvent.getNewFolder().getName() + "!");
        }

        public void messageChanged(MessageChangedEvent messageChangedEvent) {

            if (messageChangedEvent.getMessageChangeType() == MessageChangedEvent.ENVELOPE_CHANGED) {
                if (log.isDebugEnabled()) log.debug(source(messageChangedEvent) + " has updated it's envelope!");
            } else if (messageChangedEvent.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
                if (log.isDebugEnabled()) log.debug(source(messageChangedEvent) + " has updated it's flags!");
            } else {
                if (log.isDebugEnabled()) log.debug(messageChangedEvent);
            }

        }

        public void messagesAdded(MessageCountEvent messageCountEvent) {
            if (log.isDebugEnabled())
                log.debug("Added " + messageCountEvent.getMessages().length + " messages to " + source(messageCountEvent) + "!");
        }

        public void messagesRemoved(MessageCountEvent messageCountEvent) {
            if (log.isDebugEnabled())
                log.debug("Removed " + messageCountEvent.getMessages().length + " messages from " + source(messageCountEvent) + "!");
        }

    }


	public IFolder getFolder(String name, ICommonMessagingAccess mfs)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}


}
