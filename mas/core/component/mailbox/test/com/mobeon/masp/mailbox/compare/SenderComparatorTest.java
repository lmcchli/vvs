/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
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
public class SenderComparatorTest extends MockObjectTestCase
{

    Mock john_a_acme_comMessage0;
    Mock john_a_acme_comMessage1;
    Mock astrid_a_acme_comMessage;
    Mock ASTRID_a_Acme_comMessage;
    Mock n0046705555555Message;

    public void setUp() throws Exception
    {
        super.setUp();
        john_a_acme_comMessage0 = mock(IStoredMessage.class);
        john_a_acme_comMessage0.stubs().method("getSender").withNoArguments().will(returnValue("john@acme.com"));

        john_a_acme_comMessage1 = mock(IStoredMessage.class);
        john_a_acme_comMessage1.stubs().method("getSender").withNoArguments().will(returnValue("john@acme.com"));

        n0046705555555Message = mock(IStoredMessage.class);
        n0046705555555Message.stubs().method("getSender").withNoArguments().will(returnValue("0046705555555"));

        astrid_a_acme_comMessage = mock(IStoredMessage.class);
        astrid_a_acme_comMessage.stubs().method("getSender").withNoArguments().will(returnValue("astrid@acme.com"));

        ASTRID_a_Acme_comMessage = mock(IStoredMessage.class);
        ASTRID_a_Acme_comMessage.stubs().method("getSender").withNoArguments().will(returnValue("ASTRID@Acme.com"));
    }

    public void testCompareEqualObjects() throws Exception
    {
        int result = SenderComparator.ASCENDING.compare(
                (IStoredMessage)john_a_acme_comMessage0.proxy(),
                (IStoredMessage)john_a_acme_comMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SenderComparator.DESCENDING.compare(
                (IStoredMessage)john_a_acme_comMessage0.proxy(),
                (IStoredMessage)john_a_acme_comMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SenderComparator.ASCENDING_IGNORECASE.compare(
                (IStoredMessage)john_a_acme_comMessage0.proxy(),
                (IStoredMessage)john_a_acme_comMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);

        result = SenderComparator.DESCENDING_IGNORECASE.compare(
                (IStoredMessage)john_a_acme_comMessage0.proxy(),
                (IStoredMessage)john_a_acme_comMessage1.proxy()
        );
        assertTrue("Comparision result is not equal to zero",result==0);
    }

    public void testCompareNonEqualObjects() throws Exception
    {
        int result = SenderComparator.ASCENDING.compare(
                (IStoredMessage) n0046705555555Message.proxy(),
                (IStoredMessage) astrid_a_acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not less than zero", result < 0);

        result = SenderComparator.DESCENDING.compare(
                (IStoredMessage) n0046705555555Message.proxy(),
                (IStoredMessage) astrid_a_acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = SenderComparator.ASCENDING.compare(
                (IStoredMessage) astrid_a_acme_comMessage.proxy(),
                (IStoredMessage) ASTRID_a_Acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = SenderComparator.DESCENDING.compare(
                (IStoredMessage) astrid_a_acme_comMessage.proxy(),
                (IStoredMessage) ASTRID_a_Acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not less than zero", result < 0);

        result = SenderComparator.ASCENDING_IGNORECASE.compare(
                (IStoredMessage) astrid_a_acme_comMessage.proxy(),
                (IStoredMessage) ASTRID_a_Acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not equal to zero", result == 0);

        result = SenderComparator.DESCENDING_IGNORECASE.compare(
                (IStoredMessage) astrid_a_acme_comMessage.proxy(),
                (IStoredMessage) ASTRID_a_Acme_comMessage.proxy()
        );
        assertTrue("Comparision result is not equal to zero", result == 0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)john_a_acme_comMessage0.proxy());
        v.add((IStoredMessage)john_a_acme_comMessage1.proxy());
        v.add((IStoredMessage)n0046705555555Message.proxy());
        v.add((IStoredMessage)astrid_a_acme_comMessage.proxy());
        v.add((IStoredMessage)ASTRID_a_Acme_comMessage.proxy());

        Collections.shuffle(v);
        Collections.sort(v,SenderComparator.ASCENDING);
        assertEquals("0046705555555",   v.get(0).getSender());
        assertEquals("ASTRID@Acme.com", v.get(1).getSender());
        assertEquals("astrid@acme.com", v.get(2).getSender());
        assertEquals("john@acme.com",   v.get(3).getSender());
        assertEquals("john@acme.com",   v.get(4).getSender());

        Collections.shuffle(v);
        Collections.sort(v,SenderComparator.DESCENDING);
        assertEquals("john@acme.com",   v.get(0).getSender());
        assertEquals("john@acme.com",   v.get(1).getSender());
        assertEquals("astrid@acme.com", v.get(2).getSender());
        assertEquals("ASTRID@Acme.com", v.get(3).getSender());
        assertEquals("0046705555555",   v.get(4).getSender());

        Collections.shuffle(v);
        Collections.sort(v,SenderComparator.ASCENDING_IGNORECASE);
        assertEquals("0046705555555",   v.get(0).getSender());
        assertEquals("astrid@acme.com", v.get(1).getSender().toLowerCase());
        assertEquals("astrid@acme.com", v.get(2).getSender().toLowerCase());
        assertEquals("john@acme.com",   v.get(3).getSender());
        assertEquals("john@acme.com",   v.get(4).getSender());


        Collections.shuffle(v);
        Collections.sort(v,SenderComparator.DESCENDING_IGNORECASE);
        assertEquals("john@acme.com",   v.get(0).getSender());
        assertEquals("john@acme.com",   v.get(1).getSender());
        assertEquals("astrid@acme.com", v.get(2).getSender().toLowerCase());
        assertEquals("astrid@acme.com", v.get(3).getSender().toLowerCase());
        assertEquals("0046705555555",   v.get(4).getSender());

    }

    public static Test suite()
    {
        return new TestSuite(SenderComparatorTest.class);
    }
}
