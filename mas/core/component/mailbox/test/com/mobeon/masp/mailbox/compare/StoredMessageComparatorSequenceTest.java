/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.StoredMessageState;

import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.Date;

/**
 * StoredMessageComparatorSequence Tester.
 * @author qhast
 * @see com.mobeon.masp.mailbox.compare.StateComparator
 */
public class StoredMessageComparatorSequenceTest extends MockObjectTestCase {

    Mock newUrgent50Message0;
    Mock newUrgent50Message1;
    Mock newNonUrgent10Message;
    Mock readUrgent30Message;
    Mock deletedNonUrgent90Message;

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

    public void testCompareEqualObjects() throws Exception {

        int result = new StoredMessageComparatorSequence(new StateComparator()).compare(
                (IStoredMessage)newUrgent50Message0.proxy(),
                (IStoredMessage)newUrgent50Message1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StoredMessageComparatorSequence(UrgentComparator.URGENT_FIRST,new StateComparator(false)).compare(
                (IStoredMessage)newUrgent50Message0.proxy(),
                (IStoredMessage)newUrgent50Message1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StoredMessageComparatorSequence(new StateComparator(false),ReceivedDateComparator.OLDEST_FIRST,UrgentComparator.URGENT_FIRST).compare(
                (IStoredMessage)newUrgent50Message0.proxy(),
                (IStoredMessage)newUrgent50Message1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StoredMessageComparatorSequence().compare(
                (IStoredMessage)newUrgent50Message0.proxy(),
                (IStoredMessage)newUrgent50Message1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);


    }

    public void testCompareNonEqualObjects() throws Exception {

        int result = new StoredMessageComparatorSequence().compare(
                (IStoredMessage) newNonUrgent10Message.proxy(),
                (IStoredMessage) readUrgent30Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);


    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)newUrgent50Message0.proxy());
        v.add((IStoredMessage)newUrgent50Message1.proxy());
        v.add((IStoredMessage)newNonUrgent10Message.proxy());
        v.add((IStoredMessage)readUrgent30Message.proxy());
        v.add((IStoredMessage)deletedNonUrgent90Message.proxy());

        //Urgent first, any state
        Collections.shuffle(v);
        Collections.sort(v,new StoredMessageComparatorSequence(UrgentComparator.URGENT_FIRST));
        assertEquals(true,  v.get(0).isUrgent());
        assertEquals(true,  v.get(1).isUrgent());
        assertEquals(true,  v.get(2).isUrgent());
        assertEquals(false, v.get(3).isUrgent());
        assertEquals(false, v.get(4).isUrgent());


        //Urgent first, state declared order
        Collections.shuffle(v);
        Collections.sort(v,
                new StoredMessageComparatorSequence(
                        UrgentComparator.URGENT_FIRST,
                        new StateComparator()
                ));
        assertEquals(StoredMessageState.READ, v.get(0).getState());
        assertEquals(true,  v.get(0).isUrgent());

        assertEquals(StoredMessageState.NEW,   v.get(1).getState());
        assertEquals(true,  v.get(1).isUrgent());

        assertEquals(StoredMessageState.NEW,    v.get(2).getState());
        assertEquals(true,  v.get(2).isUrgent());

        assertEquals(StoredMessageState.DELETED,     v.get(3).getState());
        assertEquals(false,  v.get(3).isUrgent());

        assertEquals(StoredMessageState.NEW,     v.get(4).getState());
        assertEquals(false,  v.get(4).isUrgent());


        //Urgent first, state new first
        Collections.shuffle(v);
        Collections.sort(v,
                new StoredMessageComparatorSequence(
                        UrgentComparator.URGENT_FIRST,
                        new StateComparator(StoredMessageState.NEW)
                ));
        assertEquals(StoredMessageState.NEW, v.get(0).getState());
        assertEquals(true,  v.get(0).isUrgent());

        assertEquals(StoredMessageState.NEW,   v.get(1).getState());
        assertEquals(true,  v.get(1).isUrgent());

        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(true,  v.get(2).isUrgent());

        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(false,  v.get(3).isUrgent());

        assertEquals(StoredMessageState.DELETED,     v.get(4).getState());
        assertEquals(false,  v.get(4).isUrgent());


        //state new first, oldest first, Urgent first,
        Collections.shuffle(v);
        Collections.sort(v,
                new StoredMessageComparatorSequence(
                        new StateComparator(StoredMessageState.NEW),
                        ReceivedDateComparator.OLDEST_FIRST,
                        UrgentComparator.URGENT_FIRST
                ));
        assertEquals(StoredMessageState.NEW, v.get(0).getState());
        assertEquals(false,  v.get(0).isUrgent());
        assertEquals(new Date(10),v.get(0).getReceivedDate());

        assertEquals(StoredMessageState.NEW,   v.get(1).getState());
        assertEquals(true,  v.get(1).isUrgent());
        assertEquals(new Date(50),v.get(1).getReceivedDate());

        assertEquals(StoredMessageState.NEW,    v.get(2).getState());
        assertEquals(true,  v.get(2).isUrgent());
        assertEquals(new Date(50),v.get(2).getReceivedDate());

        assertEquals(StoredMessageState.DELETED,     v.get(3).getState());
        assertEquals(false,  v.get(3).isUrgent());
        assertEquals(new Date(90),v.get(3).getReceivedDate());

        assertEquals(StoredMessageState.READ,     v.get(4).getState());
        assertEquals(true,  v.get(4).isUrgent());
        assertEquals(new Date(30),v.get(4).getReceivedDate());


        //state new first, oldest first, Urgent first,
        Collections.shuffle(v);
        Collections.sort(v,
                new StoredMessageComparatorSequence(
                        new StateComparator(true,StoredMessageState.DELETED),
                        ReceivedDateComparator.NEWEST_FIRST,
                        UrgentComparator.NON_URGENT_FIRST
                ));
        assertEquals(StoredMessageState.NEW, v.get(0).getState());
        assertEquals(true,  v.get(0).isUrgent());
        assertEquals(new Date(50),v.get(0).getReceivedDate());

        assertEquals(StoredMessageState.NEW,   v.get(1).getState());
        assertEquals(true,  v.get(1).isUrgent());
        assertEquals(new Date(50),v.get(1).getReceivedDate());

        assertEquals(StoredMessageState.NEW,    v.get(2).getState());
        assertEquals(false,  v.get(2).isUrgent());
        assertEquals(new Date(10),v.get(2).getReceivedDate());

        assertEquals(StoredMessageState.READ,     v.get(3).getState());
        assertEquals(true,  v.get(3).isUrgent());
        assertEquals(new Date(30),v.get(3).getReceivedDate());

        assertEquals(StoredMessageState.DELETED,     v.get(4).getState());
        assertEquals(false,  v.get(4).isUrgent());
        assertEquals(new Date(90),v.get(4).getReceivedDate());

    }


    public void testAddComparator() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage) newUrgent50Message0.proxy());
        v.add((IStoredMessage) newUrgent50Message1.proxy());
        v.add((IStoredMessage) newNonUrgent10Message.proxy());
        v.add((IStoredMessage) readUrgent30Message.proxy());
        v.add((IStoredMessage) deletedNonUrgent90Message.proxy());

        StoredMessageComparatorSequence cSeq = new StoredMessageComparatorSequence(
                new StateComparator(true, StoredMessageState.DELETED)
        );
        cSeq.add(ReceivedDateComparator.NEWEST_FIRST);
        cSeq.add(UrgentComparator.NON_URGENT_FIRST);
        Collections.shuffle(v);
        Collections.sort(v, cSeq);

        assertEquals(StoredMessageState.NEW, v.get(0).getState());
        assertEquals(true, v.get(0).isUrgent());
        assertEquals(new Date(50), v.get(0).getReceivedDate());

        assertEquals(StoredMessageState.NEW, v.get(1).getState());
        assertEquals(true, v.get(1).isUrgent());
        assertEquals(new Date(50), v.get(1).getReceivedDate());

        assertEquals(StoredMessageState.NEW, v.get(2).getState());
        assertEquals(false, v.get(2).isUrgent());
        assertEquals(new Date(10), v.get(2).getReceivedDate());

        assertEquals(StoredMessageState.READ, v.get(3).getState());
        assertEquals(true, v.get(3).isUrgent());
        assertEquals(new Date(30), v.get(3).getReceivedDate());

        assertEquals(StoredMessageState.DELETED, v.get(4).getState());
        assertEquals(false, v.get(4).isUrgent());
        assertEquals(new Date(90), v.get(4).getReceivedDate());


    }


}
