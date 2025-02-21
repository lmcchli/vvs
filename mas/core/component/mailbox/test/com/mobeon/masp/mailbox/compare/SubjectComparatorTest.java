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

/**
 * SubjectComparator Tester.
 *
 * @author qhast
 */
public class SubjectComparatorTest extends MockObjectTestCase
{

    Mock meetingMessage0;
    Mock meetingMessage1;
    Mock infoMessage;
    Mock freePizzaMessage;
    Mock freePIZZAMessage;

    public void setUp() throws Exception
    {
        super.setUp();
        meetingMessage0 = mock(IStoredMessage.class);
        meetingMessage0.stubs().method("getSubject").withNoArguments().will(returnValue("meeting"));

        meetingMessage1 = mock(IStoredMessage.class);
        meetingMessage1.stubs().method("getSubject").withNoArguments().will(returnValue("meeting"));

        infoMessage = mock(IStoredMessage.class);
        infoMessage.stubs().method("getSubject").withNoArguments().will(returnValue("info"));

        freePizzaMessage = mock(IStoredMessage.class);
        freePizzaMessage.stubs().method("getSubject").withNoArguments().will(returnValue("freePizza"));

        freePIZZAMessage = mock(IStoredMessage.class);
        freePIZZAMessage.stubs().method("getSubject").withNoArguments().will(returnValue("freePIZZA"));
    }

    public void testCompareEqualObjects() throws Exception
    {
        int result = SubjectComparator.ASCENDING.compare(
                (IStoredMessage)meetingMessage0.proxy(),
                (IStoredMessage)meetingMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SubjectComparator.DESCENDING.compare(
                (IStoredMessage)meetingMessage0.proxy(),
                (IStoredMessage)meetingMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SubjectComparator.ASCENDING_IGNORECASE.compare(
                (IStoredMessage)meetingMessage0.proxy(),
                (IStoredMessage)meetingMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SubjectComparator.DESCENDING_IGNORECASE.compare(
                (IStoredMessage)meetingMessage0.proxy(),
                (IStoredMessage)meetingMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);
    }

    public void testCompareNonEqualObjects() throws Exception
    {
        int result = SubjectComparator.ASCENDING.compare(
                (IStoredMessage) infoMessage.proxy(),
                (IStoredMessage) freePizzaMessage.proxy()
        );
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = SubjectComparator.DESCENDING.compare(
                (IStoredMessage) infoMessage.proxy(),
                (IStoredMessage) freePizzaMessage.proxy()
        );
        assertTrue("Comparision result is not less than zero", result < 0);

        result = SubjectComparator.ASCENDING.compare(
                (IStoredMessage) freePizzaMessage.proxy(),
                (IStoredMessage) freePIZZAMessage.proxy()
        );
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = SubjectComparator.DESCENDING.compare(
                (IStoredMessage) freePizzaMessage.proxy(),
                (IStoredMessage) freePIZZAMessage.proxy()
        );
        assertTrue("Comparision result is not less than zero", result < 0);

        result = SubjectComparator.ASCENDING_IGNORECASE.compare(
                (IStoredMessage) freePizzaMessage.proxy(),
                (IStoredMessage) freePIZZAMessage.proxy()
        );
        assertTrue("Comparision result is not equal to zero", result == 0);

        result = SubjectComparator.DESCENDING_IGNORECASE.compare(
                (IStoredMessage) freePizzaMessage.proxy(),
                (IStoredMessage) freePIZZAMessage.proxy()
        );
        assertTrue("Comparision result is not equal to zero", result == 0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)meetingMessage0.proxy());
        v.add((IStoredMessage)infoMessage.proxy());
        v.add((IStoredMessage)freePizzaMessage.proxy());
        v.add((IStoredMessage)meetingMessage1.proxy());
        v.add((IStoredMessage)freePIZZAMessage.proxy());

        Collections.shuffle(v);
        Collections.sort(v,SubjectComparator.ASCENDING);
        assertEquals("freePIZZA",   v.get(0).getSubject());
        assertEquals("freePizza",   v.get(1).getSubject());
        assertEquals("info",        v.get(2).getSubject());
        assertEquals("meeting",     v.get(3).getSubject());
        assertEquals("meeting",     v.get(4).getSubject());

        Collections.shuffle(v);
        Collections.sort(v,SubjectComparator.DESCENDING);
        assertEquals("meeting",     v.get(0).getSubject());
        assertEquals("meeting",     v.get(1).getSubject());
        assertEquals("info",        v.get(2).getSubject());
        assertEquals("freePizza",   v.get(3).getSubject());
        assertEquals("freePIZZA",   v.get(4).getSubject());

        Collections.shuffle(v);
        Collections.sort(v,SubjectComparator.ASCENDING_IGNORECASE);
        assertEquals("freepizza",   v.get(0).getSubject().toLowerCase());
        assertEquals("freepizza",   v.get(1).getSubject().toLowerCase());
        assertEquals("info",        v.get(2).getSubject());
        assertEquals("meeting",     v.get(3).getSubject());
        assertEquals("meeting",     v.get(4).getSubject());

        Collections.shuffle(v);
        Collections.sort(v,SubjectComparator.DESCENDING_IGNORECASE);
        assertEquals("meeting",     v.get(0).getSubject());
        assertEquals("meeting",     v.get(1).getSubject());
        assertEquals("info",        v.get(2).getSubject());
        assertEquals("freepizza",   v.get(3).getSubject().toLowerCase());
        assertEquals("freepizza",   v.get(4).getSubject().toLowerCase());



    }

    public static Test suite()
    {
        return new TestSuite(SubjectComparatorTest.class);
    }
}
