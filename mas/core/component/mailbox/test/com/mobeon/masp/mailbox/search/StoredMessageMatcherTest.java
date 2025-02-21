/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.search;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.MailboxMessageType;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.util.Date;

/**
 * StoredMessageMatcher Tester.
 *
 * @author MANDE
 * @version 1.0
 * @since <pre>12/19/2006</pre>
 */
public class StoredMessageMatcherTest extends MockObjectTestCase {
    Mock mockStoredMessage;

    public StoredMessageMatcherTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        mockStoredMessage = mock(IStoredMessage.class);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the match method on an AndCriteria
     * @throws Exception
     */
    public void testMatchAnd() throws Exception {
        for (MailboxMessageType actualType : MailboxMessageType.values()) {
            for (StoredMessageState actualState : StoredMessageState.values()) {
                for (MailboxMessageType expectedType : MailboxMessageType.values()) {
                    for (StoredMessageState expectedState : StoredMessageState.values()) {
                        AndCriteria andCriteria = new AndCriteria(
                                new TypeCriteria(expectedType),
                                new StateCriteria(expectedState)
                        );
                        mockStoredMessage.expects(once()).method("getType").will(returnValue(actualType));
                        mockStoredMessage.expects(once()).method("getState").will(returnValue(actualState));
                        if (expectedType.equals(actualType) && expectedState.equals(actualState)) {
                            assertTrue(StoredMessageMatcher.match(andCriteria, getStoredMessage()));
                        } else {
                            assertFalse(StoredMessageMatcher.match(andCriteria, getStoredMessage()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the match method on a ConfidentialCriteria
     *
     * @throws Exception
     */
    public void testMatchConfidential() throws Exception {
        for (boolean actual : new boolean[] { true, false }) {
            for (boolean expected : new boolean[] { true, false }) {
                ConfidentialCriteria criteria = new ConfidentialCriteria(expected);
                mockStoredMessage.expects(once()).method("isConfidential").will(returnValue(actual));
                if (expected == actual) {
                    assertTrue(StoredMessageMatcher.match(criteria, getStoredMessage()));
                } else {
                    assertFalse(StoredMessageMatcher.match(criteria, getStoredMessage()));
                }
            }
        }
    }

    /**
     * Test the match method on a NotCriteria
     *
     * @throws Exception
     */
    public void testMatchNot() throws Exception {
        for (boolean actual : new boolean[] { true, false }) {
            for (boolean expected : new boolean[] { true, false }) {
                NotCriteria criteria = new NotCriteria(new ConfidentialCriteria(expected));
                mockStoredMessage.expects(once()).method("isConfidential").will(returnValue(actual));
                if (expected != actual) {
                    assertTrue(StoredMessageMatcher.match(criteria, getStoredMessage()));
                } else {
                    assertFalse(StoredMessageMatcher.match(criteria, getStoredMessage()));
                }
            }
        }
    }

    /**
     * Test the match method on an OrCriteria
     * @throws Exception
     */
    public void testMatchOr() throws Exception {
        for (MailboxMessageType actualType : MailboxMessageType.values()) {
            for (StoredMessageState actualState : StoredMessageState.values()) {
                for (MailboxMessageType expectedType : MailboxMessageType.values()) {
                    for (StoredMessageState expectedState : StoredMessageState.values()) {
                        OrCriteria orCriteria = new OrCriteria(
                                new TypeCriteria(expectedType),
                                new StateCriteria(expectedState)
                        );
                        mockStoredMessage.expects(once()).method("getType").will(returnValue(actualType));
                        mockStoredMessage.expects(once()).method("getState").will(returnValue(actualState));
                        if (expectedType.equals(actualType) || expectedState.equals(actualState)) {
                            assertTrue(StoredMessageMatcher.match(orCriteria, getStoredMessage()));
                        } else {
                            assertFalse(StoredMessageMatcher.match(orCriteria, getStoredMessage()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the match method on a ReceivedDateCriteria
     *
     * @throws Exception
     */
    public void testMatchReceivedDate() throws Exception {
        Date date = new Date();
        Date wrongDate = new Date(0);
        mockStoredMessage.expects(once()).method("getReceivedDate").will(returnValue(date));
        assertTrue(StoredMessageMatcher.match(new ReceivedDateCriteria(date), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getReceivedDate").will(returnValue(wrongDate));
        assertFalse(StoredMessageMatcher.match(new ReceivedDateCriteria(date), getStoredMessage()));
    }

    /**
     * Test the match method on a RecipientCriteria
     *
     * @throws Exception
     */
    public void testMatchRecipient() throws Exception {
        String recipient1 = "recipient1";
        String recipient2 = "recipient2";
        String nonRecipient = "nonrecipient";
        String[] recipients = new String[]{recipient1, recipient2};
        mockStoredMessage.expects(once()).method("getRecipients").will(returnValue(recipients));
        assertTrue(StoredMessageMatcher.match(new RecipientCriteria(recipient1), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getRecipients").will(returnValue(recipients));
        assertTrue(StoredMessageMatcher.match(new RecipientCriteria(recipient2), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getRecipients").will(returnValue(recipients));
        assertFalse(StoredMessageMatcher.match(new RecipientCriteria(nonRecipient), getStoredMessage()));
    }

    /**
     * Test the match method on a ReplyToAddressCriteria
     *
     * @throws Exception
     */
    public void testMatchReplyToAddress() throws Exception {
        String replyToAddress = "replytoaddress";
        String nonReplyToAddress = "nonreplytoaddress";
        mockStoredMessage.expects(once()).method("getReplyToAddress").will(returnValue(replyToAddress));
        assertTrue(StoredMessageMatcher.match(new ReplyToAddressCriteria(replyToAddress), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getReplyToAddress").will(returnValue(replyToAddress));
        assertFalse(StoredMessageMatcher.match(new ReplyToAddressCriteria(nonReplyToAddress), getStoredMessage()));
    }

    /**
     * Test the match method on a SecondaryRecipientCriteria
     *
     * @throws Exception
     */
    public void testMatchSecondaryRecipient() throws Exception {
        String secondaryRecipient1 = "secondaryrecipient1";
        String secondaryRecipient2 = "secondaryrecipient2";
        String nonSecondaryRecipient = "nonsecondaryrecipient";
        String[] secondaryRecipients = new String[] { secondaryRecipient1, secondaryRecipient2 };
        mockStoredMessage.expects(once()).method("getSecondaryRecipients").will(returnValue(secondaryRecipients));
        assertTrue(StoredMessageMatcher.match(new SecondaryRecipientCriteria(secondaryRecipient1), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getSecondaryRecipients").will(returnValue(secondaryRecipients));
        assertTrue(StoredMessageMatcher.match(new SecondaryRecipientCriteria(secondaryRecipient2), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getSecondaryRecipients").will(returnValue(secondaryRecipients));
        assertFalse(StoredMessageMatcher.match(new SecondaryRecipientCriteria(nonSecondaryRecipient), getStoredMessage()));
    }

    /**
     * Test the match method on a SenderCriteria
     *
     * @throws Exception
     */
    public void testMatchSender() throws Exception {
        String sender = "sender";
        String nonSender = "nonsender";
        mockStoredMessage.expects(once()).method("getSender").will(returnValue(sender));
        assertTrue(StoredMessageMatcher.match(new SenderCriteria(sender), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getSender").will(returnValue(sender));
        assertFalse(StoredMessageMatcher.match(new SenderCriteria(nonSender), getStoredMessage()));
    }

    /**
     * Test the match method on a StateCriteria
     *
     * @throws Exception
     */
    public void testMatchState() throws Exception {
        for (StoredMessageState actual : StoredMessageState.values()) {
            for (StoredMessageState expected : StoredMessageState.values()) {
                mockStoredMessage.expects(once()).method("getState").will(returnValue(actual));
                if (expected.equals(actual)) {
                    assertTrue(StoredMessageMatcher.match(new StateCriteria(expected), getStoredMessage()));
                } else {
                    assertFalse(StoredMessageMatcher.match(new StateCriteria(expected), getStoredMessage()));
                }
            }
        }
    }

    /**
     * Test the match method on a SubjectCriteria
     *
     * @throws Exception
     */
    public void testMatchSubject() throws Exception {
        String subject = "subject";
        String nonSubject = "nonsubject";
        mockStoredMessage.expects(once()).method("getSubject").will(returnValue(subject));
        assertTrue(StoredMessageMatcher.match(new SubjectCriteria(subject), getStoredMessage()));
        mockStoredMessage.expects(once()).method("getSubject").will(returnValue(subject));
        assertFalse(StoredMessageMatcher.match(new SubjectCriteria(nonSubject), getStoredMessage()));
    }

    /**
     * Test the match method on a TypeCriteria
     *
     * @throws Exception
     */
    public void testMatchType() throws Exception {
        for (MailboxMessageType actual : MailboxMessageType.values()) {
            for (MailboxMessageType expected : MailboxMessageType.values()) {
                mockStoredMessage.expects(once()).method("getType").will(returnValue(actual));
                if (expected.equals(actual)) {
                    assertTrue(StoredMessageMatcher.match(new TypeCriteria(expected), getStoredMessage()));
                } else {
                    assertFalse(StoredMessageMatcher.match(new TypeCriteria(expected), getStoredMessage()));
                }
            }
        }
    }

    /**
     * Test the match method on a UrgentCriteria
     *
     * @throws Exception
     */
    public void testMatchUrgent() throws Exception {
        mockStoredMessage.expects(once()).method("isUrgent").will(returnValue(true));
        assertTrue(StoredMessageMatcher.match(new UrgentCriteria(true), getStoredMessage()));
        mockStoredMessage.expects(once()).method("isUrgent").will(returnValue(true));
        assertFalse(StoredMessageMatcher.match(new UrgentCriteria(false), getStoredMessage()));
        mockStoredMessage.expects(once()).method("isUrgent").will(returnValue(false));
        assertFalse(StoredMessageMatcher.match(new UrgentCriteria(true), getStoredMessage()));
        mockStoredMessage.expects(once()).method("isUrgent").will(returnValue(false));
        assertTrue(StoredMessageMatcher.match(new UrgentCriteria(false), getStoredMessage()));
    }

    private IStoredMessage getStoredMessage() {
        return (IStoredMessage)mockStoredMessage.proxy();
    }


    public static Test suite() {
        return new TestSuite(StoredMessageMatcherTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
