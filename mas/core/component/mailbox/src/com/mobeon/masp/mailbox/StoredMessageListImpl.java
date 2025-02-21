/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.mailbox.search.StoredMessageMatcher;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation based on an array list.
 * @author qhast
 */
public class StoredMessageListImpl extends ArrayList<IStoredMessage> implements IStoredMessageList {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(StoredMessageListImpl.class);

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list.
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public StoredMessageListImpl(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public StoredMessageListImpl() {
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  The <tt>ArrayList</tt> instance has an initial capacity of
     * 110% the size of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     * @throws NullPointerException if the specified collection is null.
     */
    public StoredMessageListImpl(Collection<IStoredMessage> c) {
        super(c);
    }

    public IStoredMessageList select(Criteria<MessagePropertyCriteriaVisitor> criteria) {
	  Object perf = null;
      try {
    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            perf = CommonOamManager.profilerAgent.enterCheckpoint("StoredMessageListImpl.select(criteria)");
        }
    	if(LOGGER.isDebugEnabled()) LOGGER.debug("Select messages matching: "+(criteria==null?"No crieteria (all messages will match)":criteria.toString()));
        IStoredMessageList messagseList = new StoredMessageListImpl();
        if(criteria == null) {
            messagseList.addAll(this);
        } else {
            for(IStoredMessage m : this) {
                 if(StoredMessageMatcher.match(criteria,m)) messagseList.add(m);
            }
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found "+messagseList.size()+" messages (matching critera)");
                for(IStoredMessage m : messagseList) {
                    LOGGER.debug("Found "+m);
                }
            }
        }
        return messagseList;
      } finally {
      	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
      }
    }

}
