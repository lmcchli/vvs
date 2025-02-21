/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.util.Date;

import com.mobeon.masp.mailbox.search.UrgentCriteria;
import com.mobeon.masp.mailbox.search.StateCriteria;

/**
 * StoredMessageListImpl Tester.
 *
 * @author qhast
 */
public class StoredMessageListImplTest extends BaseMailboxTestCase
{

    Mock newUrgent50Message0;
    Mock newUrgent50Message1;
    Mock newNonUrgent10Message;
    Mock readUrgent30Message;
    Mock deletedNonUrgent90Message;

    public StoredMessageListImplTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {

        super.setUp();

        newUrgent50Message0 = mock(IStoredMessage.class);
        newUrgent50Message0.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.NEW));
        newUrgent50Message0.stubs().method("isUrgent").withNoArguments().will(returnValue(true));
        newUrgent50Message0.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(50)));

        newUrgent50Message1 = mock(IStoredMessage.class);
        newUrgent50Message1.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.NEW));
        newUrgent50Message1.stubs().method("isUrgent").withNoArguments().will(returnValue(true));
        newUrgent50Message1.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(50)));

        newNonUrgent10Message = mock(IStoredMessage.class);
        newNonUrgent10Message.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.NEW));
        newNonUrgent10Message.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
        newNonUrgent10Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(10)));

        readUrgent30Message = mock(IStoredMessage.class);
        readUrgent30Message.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.READ));
        readUrgent30Message.stubs().method("isUrgent").withNoArguments().will(returnValue(true));
        readUrgent30Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(30)));

        deletedNonUrgent90Message = mock(IStoredMessage.class);
        deletedNonUrgent90Message.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.DELETED));
        deletedNonUrgent90Message.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
        deletedNonUrgent90Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(90)));

    }

    /**
     * Tests that stored messages are added correctly and can be selected.
     * @throws Exception
     */
    public void testSelect() throws Exception
    {

        IStoredMessageList list = new StoredMessageListImpl();
        list.add((IStoredMessage)newUrgent50Message0.proxy());
        list.add((IStoredMessage)newUrgent50Message0.proxy());
        list.add((IStoredMessage)newNonUrgent10Message.proxy());
        list.add((IStoredMessage)readUrgent30Message.proxy());
        list.add((IStoredMessage)deletedNonUrgent90Message.proxy());
        assertEquals("List should contain 5 messages!",5,list.size());

        //Test select
        IStoredMessageList list2 = list.select(UrgentCriteria.URGENT);
        assertEquals("Selection should result in 3 urgent messages!",3,list2.size());
        for(IStoredMessage m : list2) {
            assertEquals("Message should be urgent!",true,m.isUrgent());
        }

        //Test "subselect"
        IStoredMessageList list3 = list2.select(StateCriteria.NEW);
        assertEquals("Selection should result in 2 state new messages!",2,list3.size());
        for(IStoredMessage m : list3) {
            assertEquals("Message should have state new!",StoredMessageState.NEW, m.getState());
        }

        //Test consecutive select
        IStoredMessageList list4 = list.select(StateCriteria.NEW);
        assertEquals("Selection should result in 3 state new messages!",3, list4.size());
        for(IStoredMessage m : list4) {
            assertEquals("Message should have state new!",StoredMessageState.NEW, m.getState());
        }

        //Test consecutive select
        IStoredMessageList list5 = list.select(UrgentCriteria.URGENT);
        assertEquals("Selection should still result in 3 urgent messages!",3,list5.size());
        for(IStoredMessage m : list5) {
            assertEquals("Message should be urgent!",true,m.isUrgent());
        }


        //Test select "all"
        IStoredMessageList list6 = list.select(null);
        assertEquals("Selection should result in 3 urgent messages!",5,list6.size());
        assertTrue("List 6 should be equal to original list!",          list6.equals(list));
        assertTrue("Original list should be equal to original list 6!", list.equals(list6));


    }

    public void testConstruct() throws Exception {

        IStoredMessageList list = new StoredMessageListImpl();
        list.add((IStoredMessage)newUrgent50Message0.proxy());
        list.add((IStoredMessage)newUrgent50Message0.proxy());
        list.add((IStoredMessage)newNonUrgent10Message.proxy());
        list.add((IStoredMessage)readUrgent30Message.proxy());
        list.add((IStoredMessage)deletedNonUrgent90Message.proxy());
        assertEquals("List should contain 5 messages!",5,list.size());

        IStoredMessageList containing_list = new StoredMessageListImpl(list);
        assertEquals("List should contain 5 messages!",5,containing_list.size());

        IStoredMessageList c10 = new StoredMessageListImpl(10);
        assertEquals("List should contain 0 messages!",0,c10.size());

        IStoredMessageList containing_c10 = new StoredMessageListImpl(c10);
        assertEquals("List should contain 0 messages!",0,containing_c10.size());
    }


    public static Test suite()
    {
        return new TestSuite(StoredMessageListImplTest.class);
    }
}
