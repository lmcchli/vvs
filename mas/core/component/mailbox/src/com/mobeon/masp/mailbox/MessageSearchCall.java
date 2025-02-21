/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

import java.util.Comparator;
import java.util.concurrent.Callable;

/**
 * MessageSearchCall call
 * {@link IFolder#searchMessages(com.mobeon.masp.util.criteria.Criteria<com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor>, java.util.Comparator<com.mobeon.masp.mailbox.IStoredMessage>)}
 * and returns the result or may throw an exception from the call.
 */
public class MessageSearchCall implements Callable<IStoredMessageList> {


    /**
     * Target folder.
     */
    private BaseFolder folder;

    /**
     * Search criteria.
     */
    private Criteria<MessagePropertyCriteriaVisitor> criteria;

    /**
     * Result sort comparator.
     */
    private Comparator<IStoredMessage> comparator;

    /**
     * Create a MessageSearchCall with target folder and serach criteria.
     * @param folder
     * @param criteria
     */
    public MessageSearchCall(BaseFolder folder, Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator)
    {
        this.folder = folder;
        this.criteria = criteria;
        this.comparator = comparator;
    }

    public IStoredMessageList call() throws Exception {
        return folder.searchMessagesWork(criteria,comparator);
    }
}
