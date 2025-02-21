/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.execution_engine.platformaccess.util.SearchUtil;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.compare.ReceivedDateComparator;
import com.mobeon.masp.mailbox.compare.StoredMessageComparatorSequence;
import com.mobeon.masp.mailbox.compare.TypeComparator;
import com.mobeon.masp.mailbox.search.*;
import com.mobeon.masp.util.criteria.Criteria;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Test class for the SearchUtil class
 *
 * @author ermmaha
 */
public class SearchUtilTest extends MockObjectTestCase {
    private final static String VOICE = "voice";
    private final static String VIDEO = "video";
    private final static String FAX = "fax";
    private final static String EMAIL = "email";
    private final static String NEW = "new";
    private final static String READ = "read";
    private final static String DELETED = "deleted";
    private final static String SAVED = "saved";
    private final static String URGENT = "urgent";
    private final static String NONURGENT = "nonurgent";
    private final static String TYPE = "type";
    private final static String STATE = "state";
    private final static String PRIO = "prio";
    private final static String FIFO = "fifo";
    private final static String LIFO = "lifo";

    private Mock videoMessage;
    private Mock voiceMessage;
    private Mock faxMessage;
    private Mock emailMessage;

    public SearchUtilTest(String name) {
        super(name);

        Date now = Calendar.getInstance().getTime();
        Date earlier = new Date(now.getTime() - 60 * 60 * 24 * 1000);

        videoMessage = mock(IStoredMessage.class);
        videoMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.VIDEO));
        videoMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(true));
        videoMessage.stubs().method("getReceivedDate").withNoArguments().will(returnValue(now));

        voiceMessage = mock(IStoredMessage.class);
        voiceMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.VOICE));
        voiceMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
        voiceMessage.stubs().method("getReceivedDate").withNoArguments().will(returnValue(now));

        faxMessage = mock(IStoredMessage.class);
        faxMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.FAX));
        faxMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
        faxMessage.stubs().method("getReceivedDate").withNoArguments().will(returnValue(now));

        emailMessage = mock(IStoredMessage.class);
        emailMessage.stubs().method("getType").withNoArguments().will(returnValue(MailboxMessageType.EMAIL));
        emailMessage.stubs().method("isUrgent").withNoArguments().will(returnValue(false));
        emailMessage.stubs().method("getReceivedDate").withNoArguments().will(returnValue(earlier));
    }

    /**
     * Test the getSearchCriteria method
     *
     * @throws Exception
     */
    public void testGetSearchCriteria() throws Exception {
        //Make a testcriteria to compare for equality
        Criteria<MessagePropertyCriteriaVisitor> testCriteria = new AndCriteria(
                new OrCriteria(TypeCriteria.VOICE, TypeCriteria.VIDEO, TypeCriteria.FAX, TypeCriteria.EMAIL),
                new OrCriteria(StateCriteria.NEW, StateCriteria.READ, StateCriteria.DELETED, StateCriteria.SAVED),
                new OrCriteria(UrgentCriteria.URGENT, UrgentCriteria.NON_URGENT));

        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED, SAVED);
        String priorities = commaSeparate(URGENT, NONURGENT);

        SearchUtil searchUtil = new SearchUtil(types, states, priorities, "type", null);
        assertEquals(testCriteria, searchUtil.getSearchCriteria());

        //test another
        testCriteria = new AndCriteria(
                new OrCriteria(TypeCriteria.VOICE, TypeCriteria.VIDEO, TypeCriteria.FAX),
                new OrCriteria(StateCriteria.NEW, StateCriteria.DELETED),
                UrgentCriteria.NON_URGENT);

        types = commaSeparate(VOICE, VIDEO, FAX);
        states = commaSeparate(NEW, DELETED);
        priorities = commaSeparate(NONURGENT);
        searchUtil = new SearchUtil(types, states, priorities, "type", null);
        assertEquals(testCriteria, searchUtil.getSearchCriteria());

        //test another
        testCriteria = new OrCriteria(StateCriteria.NEW, StateCriteria.DELETED);

        states = commaSeparate(NEW, DELETED);
        searchUtil = new SearchUtil(null, states, "", null, null);
        assertEquals(testCriteria, searchUtil.getSearchCriteria());

        //and another...
        testCriteria = StateCriteria.NEW;

        states = commaSeparate(NEW);
        searchUtil = new SearchUtil("", states, null, "", null);
        assertEquals(testCriteria, searchUtil.getSearchCriteria());

        //test empty (null should be returned from SearchUtil)
        searchUtil = new SearchUtil(null, null, "", "", null);
        Criteria<MessagePropertyCriteriaVisitor> searchCriteria = searchUtil.getSearchCriteria();
        assertNull(null);

        //test faulty params
        assertFaultyParams("wrongtype", NEW, URGENT, "", "");
        assertFaultyParams(VOICE, "wrongstate", URGENT, "", "");
        assertFaultyParams(FAX, NEW, "wrongprio", "", "");
    }

    /**
     * Test the getSearchComparator method
     *
     * @throws Exception
     */
    public void testGetSearchComparator() throws Exception {
        // Make a testcomparator to compare for equality
        StoredMessageComparatorSequence testComparator = new StoredMessageComparatorSequence();
        testComparator.add(new TypeComparator(MailboxMessageType.VOICE, MailboxMessageType.VIDEO));
        testComparator.add(ReceivedDateComparator.OLDEST_FIRST);

        String types = commaSeparate(VOICE, VIDEO);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT);
        String orders = commaSeparate(TYPE, STATE, PRIO);

        SearchUtil searchUtil = new SearchUtil(types, states, priorities, orders, FIFO);
        Comparator<IStoredMessage> smcs = searchUtil.getSearchComparator();

        // test faulty params
        try {
            new SearchUtil(VIDEO, NEW, URGENT, "wrongorder", FIFO);
            fail("Expected SearchCriteriaException");
        } catch (SearchCriteriaException scx) {
        }

        try {
            new SearchUtil(VOICE, NEW, URGENT, STATE, "wrongtimeorder");
            fail("Expected SearchCriteriaException");
        } catch (SearchCriteriaException scx) {
        }

        // the searchcomparator should be empty if order and timeorder is null
        searchUtil = new SearchUtil(types, states, priorities, null, null);
        assertNotNull(searchUtil.getSearchComparator());

        // test invalid timeOrder
        try {
            new SearchUtil(types, states, null, orders, "invalidtimeorder");
            fail("Expected SearchCriteriaException");
        } catch (SearchCriteriaException scx) {
        }
    }

    /**
     * @throws Exception
     */
    public void testWithComparators() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String orders = commaSeparate(TYPE);

        SearchUtil searchUtil = new SearchUtil(types, null, null, orders, FIFO);
        Comparator<IStoredMessage> smcs = searchUtil.getSearchComparator();

        StubIStoredMessageList list = new StubIStoredMessageList();
        list.add((IStoredMessage) faxMessage.proxy());
        list.add((IStoredMessage) videoMessage.proxy());
        list.add((IStoredMessage) emailMessage.proxy());
        list.add((IStoredMessage) voiceMessage.proxy());

        //System.out.println("Before sorting: ");
        //printList(list);

        Collections.sort(list, smcs);

        //System.out.println("After sorting: ");
        //printList(list);

        assertEquals(VOICE.toUpperCase(), list.get(0).getType().toString());
        assertEquals(VIDEO.toUpperCase(), list.get(1).getType().toString());
        assertEquals(FAX.toUpperCase(), list.get(2).getType().toString());
        assertEquals(EMAIL.toUpperCase(), list.get(3).getType().toString());
    }

    /**
     * @throws Exception
     */
    public void testWithComparators2() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(PRIO);

        SearchUtil searchUtil = new SearchUtil(types, states, priorities, orders, FIFO);
        Comparator<IStoredMessage> smcs = searchUtil.getSearchComparator();

        StubIStoredMessageList list = new StubIStoredMessageList();
        list.add((IStoredMessage) faxMessage.proxy());
        list.add((IStoredMessage) videoMessage.proxy());
        list.add((IStoredMessage) emailMessage.proxy());
        list.add((IStoredMessage) voiceMessage.proxy());

        //System.out.println("Before sorting: ");
        //printList(list);

        Collections.sort(list, smcs);

        //System.out.println("After sorting: ");
        //printList(list);

        assertTrue(list.get(0).isUrgent());
        assertFalse(list.get(1).isUrgent());
        assertFalse(list.get(2).isUrgent());
        assertFalse(list.get(3).isUrgent());
    }

    /**
     * TR 27523
     *
     * @throws Exception
     */
    public void testWithComparators3() throws Exception {
        SearchUtil searchUtil = new SearchUtil(null, null, null, null, LIFO);
        Comparator<IStoredMessage> smcs = searchUtil.getSearchComparator();

        StubIStoredMessageList list = new StubIStoredMessageList();
        list.add((IStoredMessage) faxMessage.proxy());
        list.add((IStoredMessage) videoMessage.proxy());
        list.add((IStoredMessage) emailMessage.proxy());
        list.add((IStoredMessage) voiceMessage.proxy());

       // System.out.println("Before sorting: ");
       // printList(list);

        Collections.sort(list, smcs);

       // System.out.println("After sorting: ");
       // printList(list);

        //ToDo fix assertions
    }

    /**
     * TR 27564
     *
     * @throws Exception
     */
    public void testWithComparators4() throws Exception {
        SearchUtil searchUtil = new SearchUtil(null, null, null, null, null);
        Comparator<IStoredMessage> smcs = searchUtil.getSearchComparator();

        StubIStoredMessageList list = new StubIStoredMessageList();
        list.add((IStoredMessage) faxMessage.proxy());
        list.add((IStoredMessage) videoMessage.proxy());
        list.add((IStoredMessage) emailMessage.proxy());
        list.add((IStoredMessage) voiceMessage.proxy());

        System.out.println("Before sorting: ");
        printList(list);

        Collections.sort(list, smcs);

        System.out.println("After sorting: ");
        printList(list);

        // should be same order
        assertEquals(FAX.toUpperCase(), list.get(0).getType().toString());
        assertEquals(VIDEO.toUpperCase(), list.get(1).getType().toString());
        assertEquals(EMAIL.toUpperCase(), list.get(2).getType().toString());
        assertEquals(VOICE.toUpperCase(), list.get(3).getType().toString());
    }

    private void printList(StubIStoredMessageList list) {
        for (int i = 0; i < list.size(); i++) {
            IStoredMessage msg = list.get(i);
            System.out.println("[" + i + "] " + msg.getType() + (msg.isUrgent() ? " urgent" : " nonurgent") + " " + msg.getReceivedDate());
        }
    }

    private void assertFaultyParams(String types, String states, String priorities, String orders, String timeOrder) {
        try {
            new SearchUtil(types, states, priorities, orders, timeOrder);
        } catch (SearchCriteriaException scx) {
            return;
        }
        fail("Expected SearchCriteriaException");
    }

    private static String commaSeparate(String... strings) {
        String temp = "";
        for (int i = 0; i < strings.length; i++) {
            temp += strings[i];
            if ((i + 1) < strings.length) temp += ",";
        }
        return temp;
    }

    public static Test suite() {
        return new TestSuite(SearchUtilTest.class);
    }
}
