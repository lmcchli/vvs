/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.mailbox.IStoredMessage;

import java.util.Vector;
import java.util.Collections;
import java.util.List;

/**
 * UrgentComparator Tester.
 *
 * @author qhast
 */
public class UrgentComparatorTest extends MockObjectTestCase
{
    Mock urgentMessage;
    Mock urgentMessage1;
    Mock nonUrgentMessage;
    Mock nonUrgentMessage1;

    public void setUp() throws Exception {

        super.setUp();

        urgentMessage = mock(IStoredMessage.class);
        urgentMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(true));

        urgentMessage1 = mock(IStoredMessage.class);
        urgentMessage1.stubs().method("isUrgent").withNoArguments().will(returnValue(true));

        nonUrgentMessage = mock(IStoredMessage.class);
        nonUrgentMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(false));

        nonUrgentMessage1 = mock(IStoredMessage.class);
        nonUrgentMessage1.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
    }

    public void testCompareEqualObject() throws Exception
    {
        int result = new UrgentComparator(false).compare(
                        (IStoredMessage)urgentMessage.proxy(),
                        (IStoredMessage)urgentMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new UrgentComparator(true).compare(
                        (IStoredMessage)urgentMessage.proxy(),
                        (IStoredMessage)urgentMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new UrgentComparator(false).compare(
                        (IStoredMessage)nonUrgentMessage.proxy(),
                        (IStoredMessage)nonUrgentMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new UrgentComparator(true).compare(
                        (IStoredMessage)nonUrgentMessage.proxy(),
                        (IStoredMessage)nonUrgentMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

    }

    public void testCompareNonEqualObject() throws Exception
    {
        int result = new UrgentComparator(false).compare(
                        (IStoredMessage)urgentMessage.proxy(),
                        (IStoredMessage)nonUrgentMessage.proxy());
        assertTrue("Comparision result is not greater than zero",result>0);

        result = new UrgentComparator(true).compare(
                        (IStoredMessage)urgentMessage.proxy(),
                        (IStoredMessage)nonUrgentMessage.proxy());
        assertTrue("Comparision result is not less than zero",result<0);

        result = new UrgentComparator(false).compare(
                        (IStoredMessage)nonUrgentMessage.proxy(),
                        (IStoredMessage)urgentMessage.proxy());
        assertTrue("Comparision result is not less than zero",result<0);

        result = new UrgentComparator(true).compare(
                        (IStoredMessage)nonUrgentMessage.proxy(),
                        (IStoredMessage)urgentMessage.proxy());
        assertTrue("Comparision result is not greater than zero",result>0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)urgentMessage.proxy());
        v.add((IStoredMessage)urgentMessage1.proxy());
        v.add((IStoredMessage)nonUrgentMessage.proxy());

        Collections.sort(v,UrgentComparator.URGENT_FIRST);
        assertEquals(true, v.get(0).isUrgent());
        assertEquals(true, v.get(1).isUrgent());
        assertEquals(false,v.get(2).isUrgent());

        Collections.sort(v,UrgentComparator.NON_URGENT_FIRST);
        assertEquals(false, v.get(0).isUrgent());
        assertEquals(true, v.get(1).isUrgent());
        assertEquals(true,v.get(2).isUrgent());

    }

    public static Test suite()
    {
        return new TestSuite(UrgentComparatorTest.class);
    }
}
