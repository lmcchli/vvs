package com.mobeon.masp.execution_engine.runapp.mock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IQuotaUsageInventory;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * Created by IntelliJ IDEA.
 * User: etomste
 * Date: 2006-jan-13
 * Time: 11:15:10
 * To change this template use File | Settings | File Templates.
 */
public class MailboxMock extends BaseMock implements IMailbox, IFolder {

    private class StubIStoredMessageList extends ArrayList<IStoredMessage> implements IStoredMessageList {

        public IStoredMessageList select(Criteria<MessagePropertyCriteriaVisitor> criteria) {
            // currently we do not care about the criteria and return all contents
            return this;

        }
    }

    public static List<StoredMessageMock> storedMessages = new ArrayList<StoredMessageMock>();

    String name;

    /**
     * Creates the mock object
     */
    public MailboxMock ()
    {
        super ();
        log.info ("MOCK: MailboxMock.MailboxMock");
        this.name = "noname";
    }

    /**
     * Creates the mock object
     */
    public MailboxMock (String name)
    {
        super ();
        log.info ("MOCK: MailboxMock.MailboxMock");
        this.name = name;
    }

    public IQuotaUsageInventory getQuotaUsageInventory() throws MailboxException {
        return null;
    }

    public IFolder[] getFolders() throws MailboxException {
        return null;
    }

    /**
     * Returns the total number of bytes used in the mailbox.
     * @return The number of bytes used in the mailbox.
     * @throws com.mobeon.masp.mailbox.MailboxException if a problem occur.
     */
    public int getByteUsage() throws MailboxException
    {
        log.info ("MOCK: MailboxMock.getByteUsage");
        return 15212;
    }

    /**
     * Returns the total number of messages in the mailbox.
     * @return The number of messges in the mailbox.
     * @throws MailboxException if a problem occur.
     */
    public int getMessageUsage() throws MailboxException
    {
        log.info ("MOCK: MailboxMock.getMessageUsage");
        return 5;
    }

    /**
     * Closes this mailbox if open and releases all allocated resources.
     * Deletes all messages marked for deletion.
     * @throws MailboxException if a problem occur.
     * @see com.mobeon.masp.mailbox.StoredMessageState
     */
    public void close() throws MailboxException
    {
        log.info ("MOCK: MailboxMock.close");
    }

    /**
     * Tries to Find an existing folder. If the folder not exists an exception is thrown.
     *
     * @param name Folder name.
     * @return the requested folder. (if exists)
     * @throws com.mobeon.masp.mailbox.FolderNotFoundException if the requested folder not exists.
     * @throws MailboxException        if problems occur.
     */
    public IFolder getFolder(String name)
            throws MailboxException
    {
        log.info ("MOCK: MailboxMock.getFolder");
        log.info ("MOCK: MailboxMock.addFolder name "+name);
        return new MailboxMock (name);
    }

    /**
     * Tries to add a {@link IFolder} to the folder parent.
     * If a folder with the same name already exist an exception is thrown.
     * @param name Folder name.
     * @return a new folder with the requested name. (if not already exists)
     * @throws com.mobeon.masp.mailbox.FolderAlreadyExistsException if the requested folder already exists.
     * @throws MailboxException             if problems occur.
     */
    public IFolder addFolder(String name)
            throws MailboxException
    {
        log.info ("MOCK: MailboxMock.addFolder");
        log.info ("MOCK: MailboxMock.addFolder name "+name);
        return new MailboxMock (name);
    }


    /**
     * Deletes the folder and deletes all subfolders.
     * All messages within the folder are removed.
     * If the folder not exists an exception is thrown.
     * @param name Folder name.
     * @throws com.mobeon.masp.mailbox.FolderNotFoundException if the requested folder not exists.
     * @throws MailboxException        if problems occur.
     */
    public void deleteFolder(String name)
            throws MailboxException
    {
        log.info ("MOCK: MailboxMock.deleteFolder");
        log.info ("MOCK: MailboxMock.deleteFolder unimplemented");
    }

    public void setReadonly() throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setReadwrite() throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Examines folder content by searching messages that match the criteria.
     * Result collection is sorted according to the compartor.
     * @param criteria search criteria.
     * @param comparator gives the sort order.
     * @return list of "light-weight" stored messages. Light-weight meaning that message content is not loaded.
     * @throws MailboxException
     * @see com.mobeon.masp.mailbox.compare
     */
    public IStoredMessageList searchMessages(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator)
            throws MailboxException
    {
        log.info ("MOCK: MailboxMock.searchMessages");
        StubIStoredMessageList stubIStoredMessageList = new StubIStoredMessageList();
        for (StoredMessageMock storedMessageMock : storedMessages) {
            stubIStoredMessageList.add(storedMessageMock);
        }

        return stubIStoredMessageList;
    }


    public IStoredMessageList searchMessages(Criteria<MessagePropertyCriteriaVisitor> criteria)
        throws MailboxException {
        return null;
    }

    public IStoredMessageList getMessages(Comparator<IStoredMessage> comparator)
            throws MailboxException
    {
        log.info ("MOCK: MailboxMock.getMessages");
        log.info ("MOCK: MailboxMock.getMessages unimplemented!");
        return null;
    }

    @Override
    public IStoredMessage getMessage(MessageIdentifier msgId)
        throws MailboxException {
        log.info ("MOCK: MailboxMock.getMessage");
        log.info ("MOCK: MailboxMock.getMessage unimplemented!");
        return null;
    }
    
     /**
     * Gets all messages in the folder.<br>
     * <br>
     * Similar to {@link #getMessages(Comparator<.IStoredMessage>)} but without a comparator.
     * Messages will appear in list as ordered in folder.
     * @return
     * @throws MailboxException
     */
    public IStoredMessageList getMessages()
            throws MailboxException
     {
         log.info ("MOCK: MailboxMock.getMessages");
         log.info ("MOCK: MailboxMock.getMessages unimplemented!");
         return null;
     }

    public Future<IStoredMessageList> searchMessagesAsync(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator)
    {
        log.info ("MOCK: MailboxMock.searchMessagesAsync");
        log.info ("MOCK: MailboxMock.searchMessagesAsync unimplemented!");
        return null;
    }


    public Future<IStoredMessageList> getMessagesAsync(Comparator<IStoredMessage> comparator)
    {
        log.info ("MOCK: MailboxMock.getMessagesAsync");
        log.info ("MOCK: MailboxMock.getMessagesAsync unimplemented!");
        return null;
    }



    /**
     * Gets all messages in the folder asynchronously.<br>
     * <br>
     * Similar to {@link #getMessagesAsync(Comparator<IStoredMessage>)} but without a comparator.
     * @return a future result.
     */
    public Future<IStoredMessageList> getMessagesAsync()
    {
        log.info ("MOCK: MailboxMock.getMessagesAsync");
        log.info ("MOCK: MailboxMock.getMessagesAsync unimplemented!");
        return null;
    }


    /**
     * Copy the specified messages from this Folder into another Folder.
     * This operation appends this Message to the destination Folder.
     * <br>
     * Note that the specified Messages object must belong to this folder.
     *
     *
     * @param folder target folder.
     * @param messages message to be copied.
     * @throws MailboxException if a problem occurs.
     */
    public void copyMessage(IFolder folder,IStoredMessage... messages) throws MailboxException
    {
        log.info ("MOCK: MailboxMock.copyMessage");
        log.info ("MOCK: MailboxMock.copyMessage unimplemented!");
    }


    /**
     * Get the folder name.
     * @return folder name
     */
    public String getName()
    {
        log.info ("MOCK: MailboxMock.getName");
        return this.name;
    }

	@Override
	public IFolder getFolder(String name, ICommonMessagingAccess mfs)
			throws MailboxException {
        log.info ("MOCK: MailboxMock.addFolder");
        log.info ("MOCK: MailboxMock.addFolder name "+name);
        return new MailboxMock (name);
	}

}
