/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.Comparator;
import java.util.concurrent.Future;

import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * Folders can contain messages.
 * Each folder provides facilities to examine the folder content.
 */
public interface IFolder extends IFolderParent {

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
            throws MailboxException;

    public IStoredMessageList searchMessages(Criteria<MessagePropertyCriteriaVisitor> criteria)
            throws MailboxException;

    /**
     * Gets all messages in the folder and sorts the result list.<br>
     * <br>
     * Similar to {@link #searchMessagess(com.mobeon.masp.util.criteria.Criteria<com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor>, java.util.Comparator<com.mobeon.masp.mailbox.IStoredMessage>)}
     *  but without a critera.
     * @param comparator
     * @return
     * @throws MailboxException
     */
    public IStoredMessageList getMessages(Comparator<IStoredMessage> comparator)
            throws MailboxException;

    /**
     * Get a specific message in the folder.<br>
     * <br>
     * @return
     * @throws MailboxException
     */
    public IStoredMessage getMessage(MessageIdentifier msgId)
            throws MailboxException;
    
     /**
     * Gets all messages in the folder.<br>
     * <br>
     * Similar to {@link #getMessages(Comparator<.IStoredMessage>)} but without a comparator.
     * Messages will appear in list as ordered in folder.
     * @return
     * @throws MailboxException
     */
    public IStoredMessageList getMessages()
            throws MailboxException;

    /**
     * Similar to {@link #searchMessages(com.mobeon.masp.util.criteria.Criteria<com.mobeon.masp.mailbox.IStoredMessage,com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor>, java.util.Comparator<com.mobeon.masp.mailbox.IStoredMessage>)} 
     * but reply is fetched asynchronously.
     * @param criteria search criteria.
     * @param comparator gives the sort order.
     * @return a future result.
     */
    public Future<IStoredMessageList> searchMessagesAsync(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator);


    /**
     * Gets all messages in the folder asynchronously mand sorts the result list.<br>
     * <br>
     * Similar to {@link #searchMessagesAsync(com.mobeon.masp.util.criteria.Criteria<com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor>, java.util.Comparator<com.mobeon.masp.mailbox.IStoredMessage>)}
     * @param comparator
     * @return a future result.
     */
    public Future<IStoredMessageList> getMessagesAsync(Comparator<IStoredMessage> comparator);


    /**
     * Gets all messages in the folder asynchronously.<br>
     * <br>
     * Similar to {@link #getMessagesAsync(Comparator<IStoredMessage>)} but without a comparator.
     * @return a future result.
     */
    public Future<IStoredMessageList> getMessagesAsync();

    /**
     * Get the folder name.
     * @return folder name
     */
    public String getName();

}
