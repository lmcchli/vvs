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

/**
 * StoredMessageStateComparator Tester.
 * @author qhast
 * @see StateComparator
 */
public class StateComparatorTest extends MockObjectTestCase {

    Mock newMessage0;
    Mock newMessage1;
    Mock savedMessage;
    Mock readMessage;
    Mock deletedMessage;

    public void setUp() throws Exception {

        super.setUp();

        newMessage0 = mock(IStoredMessage.class);
        newMessage0.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.NEW));

        newMessage1 = mock(IStoredMessage.class);
        newMessage1.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.NEW));

        savedMessage = mock(IStoredMessage.class);
        savedMessage.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.SAVED));

        readMessage = mock(IStoredMessage.class);
        readMessage.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.READ));

        deletedMessage = mock(IStoredMessage.class);
        deletedMessage.stubs().method("getState").withNoArguments().will(returnValue(StoredMessageState.DELETED));

    }

    public void testCompareEqualObjects() throws Exception {

        int result = new StateComparator().compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(false).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(true).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(true,StoredMessageState.NEW).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(false,StoredMessageState.NEW).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(false,StoredMessageState.NEW,StoredMessageState.READ).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(StoredMessageState.NEW).compare(
                        (IStoredMessage)newMessage0.proxy(),
                        (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(StoredMessageState.NEW,StoredMessageState.READ).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(StoredMessageState.NEW,StoredMessageState.READ,StoredMessageState.DELETED).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new StateComparator(StoredMessageState.READ,StoredMessageState.DELETED).compare(
                (IStoredMessage)newMessage0.proxy(),
                (IStoredMessage)newMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

    }

    public void testCompareNonEqualObjects() throws Exception {

        int result = new StateComparator().compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new StateComparator(false).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new StateComparator(false,StoredMessageState.DELETED).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new StateComparator(false,StoredMessageState.DELETED,StoredMessageState.READ,StoredMessageState.NEW).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new StateComparator(true,StoredMessageState.DELETED,StoredMessageState.READ,StoredMessageState.NEW).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        result = new StateComparator(true,StoredMessageState.READ).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        result = new StateComparator(StoredMessageState.NEW,StoredMessageState.READ).compare(
                (IStoredMessage) newMessage0.proxy(),
                (IStoredMessage) readMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)newMessage0.proxy());
        v.add((IStoredMessage)newMessage1.proxy());
        v.add((IStoredMessage)readMessage.proxy());
        v.add((IStoredMessage)savedMessage.proxy());
        v.add((IStoredMessage)deletedMessage.proxy());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator());
        assertEquals(StoredMessageState.DELETED, v.get(0).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(1).getState());
        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(StoredMessageState.NEW,     v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(false));
        assertEquals(StoredMessageState.DELETED, v.get(0).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(1).getState());
        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(StoredMessageState.NEW,     v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(true));
        assertEquals(StoredMessageState.NEW,     v.get(0).getState());
        assertEquals(StoredMessageState.NEW,     v.get(1).getState());
        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(3).getState());
        assertEquals(StoredMessageState.DELETED, v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(false,StoredMessageState.DELETED));
        assertEquals(StoredMessageState.DELETED, v.get(0).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(1).getState());
        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(StoredMessageState.NEW,     v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(false,StoredMessageState.DELETED,StoredMessageState.SAVED,StoredMessageState.READ));
        assertEquals(StoredMessageState.DELETED, v.get(0).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(1).getState());
        assertEquals(StoredMessageState.READ,    v.get(2).getState());
        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(StoredMessageState.NEW,     v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(true,StoredMessageState.NEW));
        assertEquals(StoredMessageState.READ,    v.get(0).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(1).getState());
        assertEquals(StoredMessageState.DELETED, v.get(2).getState());
        assertEquals(StoredMessageState.NEW,     v.get(3).getState());
        assertEquals(StoredMessageState.NEW,     v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(false,StoredMessageState.NEW));
        assertEquals(StoredMessageState.NEW,     v.get(0).getState());
        assertEquals(StoredMessageState.NEW,     v.get(1).getState());
        assertEquals(StoredMessageState.DELETED, v.get(2).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(3).getState());
        assertEquals(StoredMessageState.READ,    v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(StoredMessageState.NEW));
        assertEquals(StoredMessageState.NEW,     v.get(0).getState());
        assertEquals(StoredMessageState.NEW,     v.get(1).getState());
        assertEquals(StoredMessageState.DELETED, v.get(2).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(3).getState());
        assertEquals(StoredMessageState.READ,    v.get(4).getState());

        Collections.shuffle(v);
        Collections.sort(v,new StateComparator(StoredMessageState.READ,StoredMessageState.NEW,StoredMessageState.DELETED,StoredMessageState.SAVED));
        assertEquals(StoredMessageState.READ,    v.get(0).getState());
        assertEquals(StoredMessageState.NEW,     v.get(1).getState());
        assertEquals(StoredMessageState.NEW,     v.get(2).getState());
        assertEquals(StoredMessageState.DELETED, v.get(3).getState());
        assertEquals(StoredMessageState.SAVED,   v.get(4).getState());

    }

    public void testConstructorFailure() throws Exception {

        try {
            new StateComparator(StoredMessageState.READ,StoredMessageState.READ);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new StateComparator(StoredMessageState.READ,StoredMessageState.NEW,StoredMessageState.NEW);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new StateComparator(StoredMessageState.READ,StoredMessageState.NEW,StoredMessageState.NEW,StoredMessageState.DELETED);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

    }


}
