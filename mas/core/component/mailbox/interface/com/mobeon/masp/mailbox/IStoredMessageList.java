/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

import java.util.List;

/**
 * Represents the result of a folder search for messages.
 * @author qhast
 * @see IFolder
 */
public interface IStoredMessageList extends List<IStoredMessage> {


    /**
     * Selects messages matching a criteria from this list.
     * Each consecutive call to select will create a new list of references to the selected messges.
     * Implementation should do this in a efficient way.
     * @param criteria
     * @return list of messages matching the criteria.
     */
    public IStoredMessageList select(Criteria<MessagePropertyCriteriaVisitor> criteria);

}
