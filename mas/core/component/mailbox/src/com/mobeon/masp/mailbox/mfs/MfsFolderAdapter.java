package com.mobeon.masp.mailbox.mfs;

import java.util.Collections;
import java.util.Comparator;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseFolder;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.StoredMessageListImpl;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

public class MfsFolderAdapter extends BaseFolder<MfsContext> implements IFolder 
{
	private static final ILogger log = ILoggerFactory.getILogger(MfsFolderAdapter.class);	
	private String folderString;
	private MSA msa;
	private ICommonMessagingAccess mfs;
	
	MfsFolderAdapter(String folderName, MfsContext context, MfsStoreAdapter root) {
        super(context);
        this.folderString = folderName;        
        this.msa = new MSA(root.getMsId());
        this.mfs = CommonMessagingAccess.getInstance();
    }
	
	MfsFolderAdapter(String folderName, MfsContext context, MfsStoreAdapter root, ICommonMessagingAccess mfs) {
        super(context);
        this.folderString = folderName;        
        this.msa = new MSA(root.getMsId());
        this.mfs = mfs;
    }


	@Override
    public IStoredMessage getMessage(MessageIdentifier msgId)
        throws MailboxException {
	    try {
            StateFile state = mfs.getStateFile(new MessageInfo(msgId), folderString);
            if(state != null){
                return new MfsMessageAdapter(state, getContext());
            }
        } catch (MsgStoreException e) {
            throw new MailboxException(e.getMessage());
        }
        return null;
    }

	@Override
	protected IStoredMessageList searchMessagesWork(
			Criteria<MessagePropertyCriteriaVisitor> criteria,
			Comparator<IStoredMessage> comparator) throws MailboxException {
		
		IStoredMessageList result;
		Object perf = null;
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("MfsFolderAdapter.searchMessagesWork(criteria,comparator)");
	        }
            
            if (log.isDebugEnabled()) log.debug("Search folder " + this + " for messages matching: " + (criteria == null ? "No criteria" : criteria.toString()));

            StateFile[] stateFiles = mfs.searchStateFiles(msa, StateAttributeFilterFactory.createStateAttributeFilter(criteria));


            if(stateFiles != null) {
            	if (log.isDebugEnabled()) log.debug("Found " + stateFiles.length + " messages in folder " + this + (criteria == null ? "" : " (matching critera)"));
            	result = new StoredMessageListImpl(stateFiles.length);
	            for (StateFile s : stateFiles) {            	
	            	MfsMessageAdapter storedMessage = new MfsMessageAdapter(s, getContext());
	                result.add(storedMessage);
	            }            	
            } else {
            	if (log.isDebugEnabled()) log.debug("Found 0 messages in folder " + this + (criteria == null ? "" : " (matching critera)"));
            	result = new StoredMessageListImpl(0);
            }
            
        }catch(MsgStoreException e) {
        	throw new MailboxException("Tried to fetch and parse messages in "+this+" . URL: "+ getName(),e);
        } finally {
            //getContext().getMailboxLock().unlock();
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
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
		return this.folderString;
	}

	public IFolder addFolder(String name) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteFolder(String name) throws MailboxException {
		// TODO Auto-generated method stub
		
	}

	public IFolder getFolder(String name) throws MailboxException {
		// TODO Auto-generated method stub
		return this;
	}
	
	public IFolder getFolder(String name, ICommonMessagingAccess mfs) throws MailboxException {
		this.mfs = mfs;
		return this;
	}

	public void setReadonly() throws MailboxException {
		// TODO Auto-generated method stub
		
	}

	public void setReadwrite() throws MailboxException {
		// TODO Auto-generated method stub
		
	}
	
//    private static void folderString(Folder folder, StringBuffer sb) {
//        sb.append(folder.getStore());
//        sb.append("/");
//        sb.append(folder);
//    }

//    private static String folderString(Folder folder) {
//        StringBuffer sb = new StringBuffer();
//        folderString(folder, sb);
//        return sb.toString();
//    }
    
    /**
     * Syncronize calls to this method.
     * If folder not is opend. The folder will be opend in selected mode.
     * If folder is opend in another mode then selected, the folder will be closed and reopend.
     */
//    void open() throws MessagingException {
    void open() {
//        root.closeTouchedFolders(folder);
//
//        // Is folder opend, if not open it.
//        if (!folder.isOpen()) {
//            folder.open(openMode);
//            root.touchedFolders.add(folder);
//
//            if (log.isDebugEnabled()) {
//                String strMode = "read/write";
//                if(openMode == Folder.READ_ONLY) strMode="read only";
//                log.debug("open folder in "+strMode +" mode");
//            }
//        // is folder open in another mode then wanted. close folder and reopen in new mode.
//        } else if (folder.isOpen() && folder.getMode()!= openMode ) {
//            folder.close(false);
//            folder.open(openMode);
//
//            if (log.isDebugEnabled()) {
//                String strMode = "read/write";
//                if(openMode == Folder.READ_ONLY) strMode="read only";
//                log.debug("close and reopen folder in "+strMode +" mode");
//            }
//        }
    }

    public String toString() {
        if (folderString == null) {
            //folderString = folderString(folder);
        	return "null";
        }
        return folderString;
    }

	
//   private static class Debugger implements ConnectionListener, FolderListener, MessageCountListener, MessageChangedListener {
//
//        private Debugger() {
//        }
//
//
//        private String source(ConnectionEvent event) {
//            Object source = event.getSource();
//            return source.toString();            
//        }
//
//        private String source(MessageChangedEvent event) {
//            Object source = event.getSource();
//            if (source instanceof Folder) {
//                Folder folder = (Folder) source;
//                StringBuffer sb = new StringBuffer();
//                folderString(folder, sb);
//                sb.append("/Message[");
//                sb.append(event.getMessage().getMessageNumber());
//                sb.append("]");
//                return sb.toString();
//            } else {
//                return source.toString();
//            }
//        }
//
//        private String source(MessageCountEvent event) {
//            Object source = event.getSource();
//            return source.toString();            
//        }
//
//        private String source(FolderEvent event) {
//            return null;
//        }
//
//
//        public void opened(ConnectionEvent connectionEvent) {
//            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " opened!");
//        }
//
//        public void disconnected(ConnectionEvent connectionEvent) {
//            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " disconnected!");
//        }
//
//        public void closed(ConnectionEvent connectionEvent) {
//            if (log.isDebugEnabled()) log.debug(source(connectionEvent) + " closed!");
//        }
//
//        public void folderCreated(FolderEvent folderEvent) {
//            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " created!");
//        }
//
//        public void folderDeleted(FolderEvent folderEvent) {
//            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " deleted!");
//        }
//
//        public void folderRenamed(FolderEvent folderEvent) {
//            if (log.isDebugEnabled()) log.debug(source(folderEvent) + " renamed to " + folderEvent.getNewFolder().getName() + "!");
//        }
//
//        public void messageChanged(MessageChangedEvent messageChangedEvent) {
//
//            if (messageChangedEvent.getMessageChangeType() == MessageChangedEvent.ENVELOPE_CHANGED) {
//                if (log.isDebugEnabled()) log.debug(source(messageChangedEvent) + " has updated it's envelope!");
//            } else if (messageChangedEvent.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
//                if (log.isDebugEnabled()) log.debug(source(messageChangedEvent) + " has updated it's flags!");
//            } else {
//                if (log.isDebugEnabled()) log.debug(messageChangedEvent);
//            }
//
//        }
//
//        public void messagesAdded(MessageCountEvent messageCountEvent) {
//            if (log.isDebugEnabled())
//                log.debug("Added " + messageCountEvent.getMessages().length + " messages to " + source(messageCountEvent) + "!");
//        }
//
//        public void messagesRemoved(MessageCountEvent messageCountEvent) {
//            if (log.isDebugEnabled())
//                log.debug("Removed " + messageCountEvent.getMessages().length + " messages from " + source(messageCountEvent) + "!");
//        }
//
//    }

}
