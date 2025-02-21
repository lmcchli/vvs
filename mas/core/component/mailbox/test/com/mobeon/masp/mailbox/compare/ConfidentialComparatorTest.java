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
 * ConfidentialComparator Tester.
 *
 * @author qhast
 */
public class ConfidentialComparatorTest extends MockObjectTestCase
{
    Mock confidentialMessage;
    Mock confidentialMessage1;
    Mock nonConfidentialMessage;
    Mock nonConfidentialMessage1;

    public void setUp() throws Exception {

        super.setUp();

        confidentialMessage = mock(IStoredMessage.class);
        confidentialMessage.stubs().method("isConfidential").withNoArguments().will(returnValue(true));

        confidentialMessage1 = mock(IStoredMessage.class);
        confidentialMessage1.stubs().method("isConfidential").withNoArguments().will(returnValue(true));

        nonConfidentialMessage = mock(IStoredMessage.class);
        nonConfidentialMessage.stubs().method("isConfidential").withNoArguments().will(returnValue(false));

        nonConfidentialMessage1 = mock(IStoredMessage.class);
        nonConfidentialMessage1.stubs().method("isConfidential").withNoArguments().will(returnValue(false));
    }

    public void testCompareEqualObject() throws Exception
    {
        int result = new ConfidentialComparator(false).compare(
                        (IStoredMessage)confidentialMessage.proxy(),
                        (IStoredMessage)confidentialMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new ConfidentialComparator(true).compare(
                        (IStoredMessage)confidentialMessage.proxy(),
                        (IStoredMessage)confidentialMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new ConfidentialComparator(false).compare(
                        (IStoredMessage)nonConfidentialMessage.proxy(),
                        (IStoredMessage)nonConfidentialMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new ConfidentialComparator(true).compare(
                        (IStoredMessage)nonConfidentialMessage.proxy(),
                        (IStoredMessage)nonConfidentialMessage1.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

    }

    public void testCompareNonEqualObject() throws Exception
    {
        int result = new ConfidentialComparator(false).compare(
                        (IStoredMessage)confidentialMessage.proxy(),
                        (IStoredMessage)nonConfidentialMessage.proxy());
        assertTrue("Comparision result is not greater than zero",result > 0);

        result = new ConfidentialComparator(true).compare(
                        (IStoredMessage)confidentialMessage.proxy(),
                        (IStoredMessage)nonConfidentialMessage.proxy());
        assertTrue("Comparision result is not less than zero",result < 0);

        result = new ConfidentialComparator(false).compare(
                        (IStoredMessage)nonConfidentialMessage.proxy(),
                        (IStoredMessage)confidentialMessage.proxy());
        assertTrue("Comparision result is not less than zero",result < 0);

        result = new ConfidentialComparator(true).compare(
                        (IStoredMessage)nonConfidentialMessage.proxy(),
                        (IStoredMessage)confidentialMessage.proxy());
        assertTrue("Comparision result is not greater than zero",result > 0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)confidentialMessage.proxy());
        v.add((IStoredMessage)confidentialMessage1.proxy());
        v.add((IStoredMessage)nonConfidentialMessage.proxy());

        Collections.sort(v,ConfidentialComparator.CONFIDENTIAL_FIRST);
        assertEquals(true, v.get(0).isConfidential());
        assertEquals(true, v.get(1).isConfidential());
        assertEquals(false,v.get(2).isConfidential());

        Collections.sort(v,ConfidentialComparator.NON_CONFIDENTIAL_FIRST);
        assertEquals(false, v.get(0).isConfidential());
        assertEquals(true, v.get(1).isConfidential());
        assertEquals(true,v.get(2).isConfidential());

    }

    public static Test suite()
    {
        return new TestSuite(ConfidentialComparatorTest.class);
    }
}
