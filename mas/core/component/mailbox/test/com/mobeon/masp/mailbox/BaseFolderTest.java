/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

import java.util.Comparator;
import java.util.concurrent.Future;

import org.jmock.Mock;

/**
 * BaseFolder Tester.
 *
 * @author qhast
 */
public class BaseFolderTest extends BaseMailboxTestCase
{
    private BaseFolder<BaseContext> baseFolder;
    private BaseContext baseContext;
    private IStoredMessageList storedMessageList;
    private long searchDelay = 0;


    public BaseFolderTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        Mock storedMessageListMock =  mock(IStoredMessageList.class);
        storedMessageListMock.stubs().method("size").withNoArguments().will(returnValue(0));
        storedMessageList = (IStoredMessageList) storedMessageListMock.proxy();
        baseContext = new BaseContext(){
            protected BaseConfig newConfig() {
                return new BaseConfig();
            }
        };
        baseFolder = new BaseFolder<BaseContext>(baseContext){
            protected IStoredMessageList searchMessagesWork(Criteria<MessagePropertyCriteriaVisitor> criteria, Comparator<IStoredMessage> comparator) throws MailboxException {
                try {
                    Thread.sleep(searchDelay);
                } catch (InterruptedException e) {
                    throw new MailboxException("Tried to sleep during test!",e);
                }
                return storedMessageList;
            }
        };
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetContext() throws Exception
    {
        assertEquals(baseContext,baseFolder.getContext());
    }

    public void testGetMessages() throws Exception
    {
        Comparator<IStoredMessage> comparator = new Comparator<IStoredMessage>(){
            public int compare(IStoredMessage o1, IStoredMessage o2) {
                return 0;
            }
        };

        assertEquals(storedMessageList,baseFolder.getMessages());
        assertEquals(storedMessageList,baseFolder.getMessages(comparator));
    }

    public void testSearchMessages() throws Exception
    {
        Comparator<IStoredMessage> comparator = new Comparator<IStoredMessage>(){
            public int compare(IStoredMessage o1, IStoredMessage o2) {
                return 0;
            }
        };

        Criteria<MessagePropertyCriteriaVisitor> criteria = new Criteria<MessagePropertyCriteriaVisitor>() {
            public void accept(MessagePropertyCriteriaVisitor visitor) {

            }
            public String toString() {
                return null;
            }
            @SuppressWarnings({"CloneDoesntCallSuperClone"})
            public Criteria<MessagePropertyCriteriaVisitor> clone() {
                return null;
            }
            protected int generateHashCode() {
                return 123;
            }
        };

        assertEquals(storedMessageList,baseFolder.searchMessages(criteria));
        assertEquals(storedMessageList,baseFolder.searchMessages(criteria,comparator));
    }


    public void testSearchMessagesAsync() throws Exception
    {
        searchDelay = 1000;

        Comparator<IStoredMessage> comparator = new Comparator<IStoredMessage>(){
            public int compare(IStoredMessage o1, IStoredMessage o2) {
                return 0;
            }
        };

        Criteria<MessagePropertyCriteriaVisitor> criteria = new Criteria<MessagePropertyCriteriaVisitor>() {
            public void accept(MessagePropertyCriteriaVisitor visitor) {

            }
            public String toString() {
                return null;
            }
            @SuppressWarnings({"CloneDoesntCallSuperClone"})
            public Criteria<MessagePropertyCriteriaVisitor> clone() {
                return null;
            }
            protected int generateHashCode() {
                return 123;
            }
        };

        long start = System.currentTimeMillis();
        Future<IStoredMessageList> future = baseFolder.searchMessagesAsync(criteria, comparator);
        assertFalse(future.isDone());
        long split = System.currentTimeMillis();
        IStoredMessageList storedMessageList = future.get();
        long finnish = System.currentTimeMillis();
        assertTrue(split-start<searchDelay);
        assertTrue(finnish-start>searchDelay-100);
        assertEquals(this.storedMessageList,storedMessageList);


    }


    public void testGetMessagesAsync() throws Exception
    {
        searchDelay = 1000;

        long start = System.currentTimeMillis();
        Future<IStoredMessageList> future = baseFolder.getMessagesAsync();
        assertFalse(future.isDone());
        long split = System.currentTimeMillis();
        IStoredMessageList storedMessageList = future.get();
        long finnish = System.currentTimeMillis();
        assertTrue(split-start<searchDelay);
        assertTrue(finnish-start>searchDelay-100);
        assertEquals(this.storedMessageList,storedMessageList);

    }

    public void testGetMessagesAsync2() throws Exception
    {
        searchDelay = 1000;

        Comparator<IStoredMessage> comparator = new Comparator<IStoredMessage>(){
            public int compare(IStoredMessage o1, IStoredMessage o2) {
                return 0;
            }
        };

        long start = System.currentTimeMillis();
        Future<IStoredMessageList> future = baseFolder.getMessagesAsync(comparator);
        assertFalse(future.isDone());
        long split = System.currentTimeMillis();
        IStoredMessageList storedMessageList = future.get();
        long finnish = System.currentTimeMillis();
        assertTrue(split-start<searchDelay);
        assertTrue(finnish-start>searchDelay-100);
        assertEquals(this.storedMessageList,storedMessageList);

    }



    public static Test suite()
    {
        return new TestSuite(BaseFolderTest.class);
    }
}
