/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.Date;

/**
 * ReceivedDateComparator Tester.
 *
 * @author qhast
 */
public class ReceivedDateComparatorTest extends MockObjectTestCase
{

    Mock d100Message0;
    Mock d100Message1;
    Mock d50Message;
    Mock d20Message;
    Mock d10Message;

    public void setUp() throws Exception
    {
        super.setUp();
        d100Message0 = mock(IStoredMessage.class);
        d100Message0.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(100)));

        d100Message1 = mock(IStoredMessage.class);
        d100Message1.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(100)));

        d50Message = mock(IStoredMessage.class);
        d50Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(50)));

        d20Message = mock(IStoredMessage.class);
        d20Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(20)));

        d10Message = mock(IStoredMessage.class);
        d10Message.stubs().method("getReceivedDate").withNoArguments().will(returnValue(new Date(10)));
    }

    public void testCompareEqualObjects() throws Exception
    {
        int result = new ReceivedDateComparator(false).compare(
                (IStoredMessage)d100Message0.proxy(),
                (IStoredMessage)d100Message1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new ReceivedDateComparator(true).compare(
                (IStoredMessage)d100Message0.proxy(),
                (IStoredMessage)d100Message1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

    }

    public void testCompareNonEqualObjects() throws Exception
    {
        int result = new ReceivedDateComparator(false).compare(
                (IStoredMessage) d50Message.proxy(),
                (IStoredMessage) d10Message.proxy()
        );
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new ReceivedDateComparator(true).compare(
                (IStoredMessage) d50Message.proxy(),
                (IStoredMessage) d10Message.proxy()
        );
        assertTrue("Comparision result is not less than zero", result < 0);
    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)d100Message0.proxy());
        v.add((IStoredMessage)d100Message1.proxy());
        v.add((IStoredMessage)d50Message.proxy());
        v.add((IStoredMessage)d20Message.proxy());
        v.add((IStoredMessage)d10Message.proxy());

        Collections.shuffle(v);
        Collections.sort(v,ReceivedDateComparator.OLDEST_FIRST);
        assertEquals(new Date(10),   v.get(0).getReceivedDate());
        assertEquals(new Date(20),   v.get(1).getReceivedDate());
        assertEquals(new Date(50),   v.get(2).getReceivedDate());
        assertEquals(new Date(100),  v.get(3).getReceivedDate());
        assertEquals(new Date(100),  v.get(4).getReceivedDate());

        Collections.shuffle(v);
        Collections.sort(v,ReceivedDateComparator.NEWEST_FIRST);
        assertEquals(new Date(100),  v.get(0).getReceivedDate());
        assertEquals(new Date(100),  v.get(1).getReceivedDate());
        assertEquals(new Date(50),   v.get(2).getReceivedDate());
        assertEquals(new Date(20),   v.get(3).getReceivedDate());
        assertEquals(new Date(10),   v.get(4).getReceivedDate());

    }

    public static Test suite()
    {
        return new TestSuite(ReceivedDateComparatorTest.class);
    }
}
