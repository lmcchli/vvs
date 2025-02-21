/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Comparator;
import java.util.concurrent.Future;

/**
 * @author QHAST
 */
public abstract class BaseFolder<C extends BaseContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseFolder.class);

    private C context;

    protected BaseFolder(C context) {
        this.context = context;
    }

    protected C getContext() {
        return context;
    }

    protected abstract IStoredMessageList searchMessagesWork(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) throws MailboxException;


    public IStoredMessageList getMessages() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessages()");
        IStoredMessageList storedMessageList = searchMessagesWork(null, null);
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessages() returns " + storedMessageList.size() + " messages");
        return storedMessageList;
    }

    public IStoredMessageList getMessages(Comparator<IStoredMessage> comparator) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessages(comparator=" + comparator + ")");
        IStoredMessageList storedMessageList = searchMessagesWork(null, comparator);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("getMessages(Comparator<IStoredMessage>) returns " + storedMessageList.size() + " messages");
        return storedMessageList;
    }

    public IStoredMessageList searchMessages(Criteria<MessagePropertyCriteriaVisitor> criteria) throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("searchMessages(criteria=" + criteria + ")");
        IStoredMessageList storedMessageList = searchMessagesWork(criteria, null);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("searchMessages(Criteria<MessagePropertyCriteriaVisitor>) returns " + storedMessageList.size() + " messages");
        return storedMessageList;
    }

    public IStoredMessageList searchMessages(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) throws MailboxException {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("searchMessages(criteria=" + criteria + ", comparator=" + comparator + ")");
        IStoredMessageList storedMessageList = searchMessagesWork(criteria, comparator);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("searchMessages(Criteria<MessagePropertyCriteriaVisitor>, Comparator<IStoredMessage>) returns " + storedMessageList
                    .size() + " messages");
        return storedMessageList;
    }

    public Future<IStoredMessageList> getMessagesAsync(Comparator<IStoredMessage> comparator) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessagesAsync(comparator=" + comparator + ")");
        Future<IStoredMessageList> future = searchMessagesAsyncWork(null, comparator);
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessagesAsync(Comparator<IStoredMessage>) returns " + future);
        return future;
    }

    public Future<IStoredMessageList> getMessagesAsync() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessagesAsync()");
        Future<IStoredMessageList> future = searchMessagesAsyncWork(null, null);
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMessagesAsync() returns " + future);
        return future;
    }

    public Future<IStoredMessageList> searchMessagesAsync(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("searchMessagesAsync(criteria=" + criteria + ", comparator=" + comparator + ")");
        Future<IStoredMessageList> future = searchMessagesAsyncWork(criteria, comparator);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("searchMessagesAsync(Criteria<MessagePropertyCriteriaVisitor>, Comparator<IStoredMessage>) returns " + future);
        return future;
    }

    private Future<IStoredMessageList> searchMessagesAsyncWork(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) {
        return ExecutorServiceManager.getInstance().getExecutorService(BaseFolder.class).submit(new MessageSearchCall(this, criteria, comparator));
    }
}
