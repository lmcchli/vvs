/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.MailboxMessageType;

import java.util.List;
import java.util.Vector;
import java.util.Collections;

/**
 * TypeComparator Tester.
 * @author qhast
 * @see TypeComparator
 */
public class TypeComparatorTest extends MockObjectTestCase {

    Mock video0Message;
    Mock video1Message;
    Mock voiceMessage;
    Mock faxMessage;
    Mock emailMessage;

    public void setUp() throws Exception {

        super.setUp();

        video0Message = mock(IStoredMessage.class);
        video0Message.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.VIDEO));

        video1Message = mock(IStoredMessage.class);
        video1Message.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.VIDEO));

        voiceMessage = mock(IStoredMessage.class);
        voiceMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.VOICE));

        faxMessage = mock(IStoredMessage.class);
        faxMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.FAX));

        emailMessage = mock(IStoredMessage.class);
        emailMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.EMAIL));

    }

    public void testCompareEqualObjects() throws Exception {

        int result = new TypeComparator().compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(false).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(true).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(true,MailboxMessageType.FAX).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(false,MailboxMessageType.FAX).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(false,MailboxMessageType.FAX,MailboxMessageType.EMAIL).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(MailboxMessageType.FAX).compare(
                        (IStoredMessage)video0Message.proxy(),
                        (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(MailboxMessageType.FAX,MailboxMessageType.VIDEO).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(MailboxMessageType.VIDEO,MailboxMessageType.FAX,MailboxMessageType.VOICE).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = new TypeComparator(MailboxMessageType.VIDEO,MailboxMessageType.EMAIL).compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

    }

    public void testAddEnumValueCompareEqualObjects() throws Exception {


        //Ascending
        TypeComparator ascendingTc = new TypeComparator(false);

        int result = ascendingTc.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        ascendingTc.add(MailboxMessageType.FAX);

        result = ascendingTc.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        ascendingTc.add(MailboxMessageType.EMAIL);

        result = ascendingTc.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        for(int i=0; i<MailboxMessageType.values().length+5; i++) {
            ascendingTc.add(MailboxMessageType.VOICE);
            result = ascendingTc.compare(
                    (IStoredMessage)video0Message.proxy(),
                    (IStoredMessage)video1Message.proxy());
            assertTrue("Comparision result is not equal to zero",result==0);
        }

        //Ascending again
        TypeComparator ascendingTc2 = new TypeComparator(false,MailboxMessageType.FAX,MailboxMessageType.EMAIL);

        result = ascendingTc2.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        result = ascendingTc2.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        for(int i=0; i<MailboxMessageType.values().length+5; i++) {
            ascendingTc2.add(MailboxMessageType.VOICE);
            result = ascendingTc2.compare(
                    (IStoredMessage)video0Message.proxy(),
                    (IStoredMessage)video1Message.proxy());
            assertTrue("Comparision result is not equal to zero",result==0);
        }

        //Descending
        TypeComparator descendingTc = new TypeComparator(true);

        result = descendingTc.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        descendingTc.add(MailboxMessageType.FAX);

        result = descendingTc.compare(
                (IStoredMessage)video0Message.proxy(),
                (IStoredMessage)video1Message.proxy());
        assertTrue("Comparision result is not equal to zero",result==0);

        for(int i=0; i<MailboxMessageType.values().length+10; i++) {
           descendingTc.add(MailboxMessageType.VOICE);
            result = descendingTc.compare(
                    (IStoredMessage)video0Message.proxy(),
                    (IStoredMessage)video1Message.proxy());
            assertTrue("Comparision result is not equal to zero",result==0);
        }


    }

    public void testAddEnumCompareNonEqualObjects() throws Exception {

        //Ascending
        TypeComparator ascendingTc = new TypeComparator(false);

        int result = ascendingTc.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        ascendingTc.add(MailboxMessageType.VIDEO);

        result = ascendingTc.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        ascendingTc.add(MailboxMessageType.VOICE);
        ascendingTc.add(MailboxMessageType.FAX);

        result = ascendingTc.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        //Ascending again
        TypeComparator ascendingTc2 = new TypeComparator();

        ascendingTc2.add(MailboxMessageType.FAX);
        ascendingTc2.add(MailboxMessageType.VOICE);

        result = ascendingTc2.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        for(int i=0; i<MailboxMessageType.values().length+10; i++) {

            ascendingTc2.add(MailboxMessageType.FAX);
            result = ascendingTc2.compare(
                    (IStoredMessage) faxMessage.proxy(),
                    (IStoredMessage) video0Message.proxy());
            assertTrue("Comparision result is not lesser than zero", result < 0);
        }

        //Ascending and again
        TypeComparator ascendingTc3 = new TypeComparator(MailboxMessageType.FAX,MailboxMessageType.VOICE);

        result = ascendingTc3.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        for(int i=0; i<MailboxMessageType.values().length+10; i++) {

            ascendingTc3.add(MailboxMessageType.FAX);
            result = ascendingTc3.compare(
                    (IStoredMessage) faxMessage.proxy(),
                    (IStoredMessage) video0Message.proxy());
            assertTrue("Comparision result is not lesser than zero", result < 0);
        }

        //Descending
        TypeComparator descendingTc = new TypeComparator(true);

        descendingTc.add(MailboxMessageType.VIDEO);

        result = descendingTc.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        descendingTc.add(MailboxMessageType.VOICE);
        descendingTc.add(MailboxMessageType.FAX);

        result = descendingTc.compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        for(int i=0; i<MailboxMessageType.values().length+10; i++) {
            descendingTc.add(MailboxMessageType.VOICE);
            result = descendingTc.compare(
                    (IStoredMessage) voiceMessage.proxy(),
                    (IStoredMessage) faxMessage.proxy());
            assertTrue("Comparision result is not lesser than zero", result < 0);
        }

    }


    public void testCompareNonEqualObjects() throws Exception {

        int result = new TypeComparator().compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new TypeComparator(false).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new TypeComparator(false,MailboxMessageType.VIDEO).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new TypeComparator(false,MailboxMessageType.VIDEO,MailboxMessageType.VOICE,MailboxMessageType.FAX).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not greater than zero", result > 0);

        result = new TypeComparator(true,MailboxMessageType.VIDEO,MailboxMessageType.VOICE,MailboxMessageType.FAX).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        result = new TypeComparator(true,MailboxMessageType.VIDEO).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

        result = new TypeComparator(MailboxMessageType.FAX,MailboxMessageType.VOICE).compare(
                (IStoredMessage) faxMessage.proxy(),
                (IStoredMessage) voiceMessage.proxy());
        assertTrue("Comparision result is not lesser than zero", result < 0);

    }

    public void testListSort() throws Exception {

        List<IStoredMessage> v = new Vector<IStoredMessage>();
        v.add((IStoredMessage)video0Message.proxy());
        v.add((IStoredMessage)video1Message.proxy());
        v.add((IStoredMessage)faxMessage.proxy());
        v.add((IStoredMessage)voiceMessage.proxy());
        v.add((IStoredMessage)emailMessage.proxy());

        Collections.shuffle(v);
        Collections.sort(v,new TypeComparator());
        assertEquals(MailboxMessageType.VOICE,  v.get(0).getType());
        assertEquals(MailboxMessageType.VIDEO,  v.get(1).getType());
        assertEquals(MailboxMessageType.VIDEO,  v.get(2).getType());
        assertEquals(MailboxMessageType.FAX,    v.get(3).getType());
        assertEquals(MailboxMessageType.EMAIL,  v.get(4).getType());
                

    }

    public void testConstructorFailure() throws Exception {

        try {
            new TypeComparator(MailboxMessageType.VIDEO,MailboxMessageType.VIDEO);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new TypeComparator(MailboxMessageType.FAX,MailboxMessageType.VIDEO,MailboxMessageType.VIDEO);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

        try {
            new TypeComparator(MailboxMessageType.FAX,MailboxMessageType.VIDEO,MailboxMessageType.VIDEO,MailboxMessageType.VOICE);
            fail("Should throw IllegalArgumentException when using the same enum value twice.");
        }
        catch(IllegalArgumentException e) {
            //OK
        }

    }


}