/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.mock.MimeMessageMock;
import com.mobeon.masp.mailbox.search.*;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.QuantitativeValueCriteria;
import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.search.*;
import java.util.*;


public class SearchTermFactoryTest extends JavamailBaseTestCase {
    // Different expected SearchTerms
    private static final HeaderTerm REPORT_SEARCH_TERM = new HeaderTerm("Content-Type", "multipart/report");
    private static final SearchTerm VOICE_SEARCH_TERM = new OrTerm(
            new HeaderTerm("Content-Type", "multipart/voice-message"),
            REPORT_SEARCH_TERM
    );
    private static final SearchTerm VIDEO_SEARCH_TERM = new OrTerm(
            new HeaderTerm("Content-Type", "multipart/x-video-message"),
            REPORT_SEARCH_TERM
    );
    private static final SearchTerm FAX_SEARCH_TERM = new OrTerm(
            new HeaderTerm("Content-Type", "multipart/fax-message"),
            REPORT_SEARCH_TERM
    );

    public SearchTermFactoryTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the Create method on an AndCriteria
     * @throws Exception
     */
    public void testCreateAnd() throws Exception {
        for (MailboxMessageType actualType : MailboxMessageType.values()) {
            for (StoredMessageState actualState : StoredMessageState.values()) {
                for (MailboxMessageType expectedType : MailboxMessageType.values()) {
                    for (StoredMessageState expectedState : StoredMessageState.values()) {
                        AndCriteria andCriteria = new AndCriteria(
                                new TypeCriteria(expectedType),
                                new StateCriteria(expectedState)
                        );
                        Message message = getMessage(actualType, actualState);
                        if (expectedType.equals(actualType) && expectedState.equals(actualState)) {
                            assertTrue(SearchTermFactory.createSearchTerm(andCriteria).match(message));
                        } else {
                            assertFalse(SearchTermFactory.createSearchTerm(andCriteria).match(message));
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the create method on a ConfidentialCriteria
     *
     * @throws Exception
     */
    public void testCreateConfidential() throws Exception {
        Message unconfidentialMessage = getMessage();
        assertFalse(SearchTermFactory.createSearchTerm(new ConfidentialCriteria(true)).match(unconfidentialMessage));
        assertTrue(SearchTermFactory.createSearchTerm(new ConfidentialCriteria(false)).match(unconfidentialMessage));
        Message[] confidentialMessages = getConfidentialMessages();
        for (Message message : confidentialMessages) {
            assertTrue(SearchTermFactory.createSearchTerm(new ConfidentialCriteria(true)).match(message));
            assertFalse(SearchTermFactory.createSearchTerm(new ConfidentialCriteria(false)).match(message));
        }
    }

    /**
     * Test the Create method on a NotCriteria
     *
     * @throws Exception
     */
    public void testCreateNot() throws Exception {
        String subject = "subject";
        String nonSubject = "nonsubject";
        Message message = getMessage("Subject: " + subject);
        assertFalse(SearchTermFactory.createSearchTerm(new NotCriteria(new SubjectCriteria(subject))).match(message));
        assertTrue(SearchTermFactory.createSearchTerm(new NotCriteria(new SubjectCriteria(nonSubject))).match(message));
    }

    /**
     * Test the create method on an OrCriteria
     * @throws Exception
     */
    public void testCreateOr() throws Exception {
        for (MailboxMessageType actualType : MailboxMessageType.values()) {
            for (StoredMessageState actualState : StoredMessageState.values()) {
                for (MailboxMessageType expectedType : MailboxMessageType.values()) {
                    for (StoredMessageState expectedState : StoredMessageState.values()) {
                        OrCriteria orCriteria = new OrCriteria(
                                new TypeCriteria(expectedType),
                                new StateCriteria(expectedState)
                        );
                        Message message = getMessage(actualType, actualState);
                        if (expectedType.equals(actualType) || expectedState.equals(actualState)) {
                            assertTrue(SearchTermFactory.createSearchTerm(orCriteria).match(message));
                        } else {
                            assertFalse(SearchTermFactory.createSearchTerm(orCriteria).match(message));
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the catch method on a ReceivedDateCriteria
     *
     * @throws Exception
     */
    public void testCatchReceivedDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date oldDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date newDate = calendar.getTime();
        MimeMessageMock message = new MimeMessageMock(folderMock, 1);
        message.setReceivedDate(date);
        ReceivedDateCriteria criteria;
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.EQ);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.NE);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.LT);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.LE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.GT);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(date, QuantitativeValueCriteria.Comparison.GE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));

        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.EQ);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.NE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.LT);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.LE);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.GT);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(oldDate, QuantitativeValueCriteria.Comparison.GE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));

        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.EQ);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.NE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.LT);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.LE);
        assertTrue(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.GT);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
        criteria = new ReceivedDateCriteria(newDate, QuantitativeValueCriteria.Comparison.GE);
        assertFalse(SearchTermFactory.createSearchTerm(criteria).match(message));
    }

    /**
     * Test the create method on a RecipientCriteria
     *
     * @throws Exception
     */
    public void testCreateRecipient() throws Exception {
        String recipient = "recipient";
        String nonRecipient = "nonrecipient";
        Message message = getMessage("To: " + recipient);
        assertTrue(SearchTermFactory.createSearchTerm(new RecipientCriteria(recipient)).match(message));
        assertFalse(SearchTermFactory.createSearchTerm(new RecipientCriteria(nonRecipient)).match(message));
    }

    /**
     * Test the create method on a ReplyToAddressCriteria
     *
     * @throws Exception
     */
    public void testCreateReplyToAddress() throws Exception {
        String replyToAddress = "replytoaddress";
        String nonReplyToAddress = "nonreplytoaddress";
        Message message = getMessage("Reply-To: " + replyToAddress);
        assertTrue(SearchTermFactory.createSearchTerm(new ReplyToAddressCriteria(replyToAddress)).match(message));
        assertFalse(SearchTermFactory.createSearchTerm(new ReplyToAddressCriteria(nonReplyToAddress)).match(message));
    }

    /**
     * Test the create method on a SecondaryRecipientCriteria
     *
     * @throws Exception
     */
    public void testCreateSecondaryRecipient() throws Exception {
        String secondaryRecipient = "secondaryrecipient";
        String nonSecondaryRecipient = "nonsecondaryrecipient";
        Message message = getMessage("Cc: " + secondaryRecipient);
        assertTrue(SearchTermFactory.createSearchTerm(new SecondaryRecipientCriteria(secondaryRecipient)).match(message));
        assertFalse(SearchTermFactory.createSearchTerm(new SecondaryRecipientCriteria(nonSecondaryRecipient)).match(message));
    }

    /**
     * Test the create method on a SenderCriteria
     *
     * @throws Exception
     */
    public void testCreateSender() throws Exception {
        String sender = "sender";
        String nonSender = "nonsender";
        Message message = getMessage("Sender: " + sender);
        assertTrue(SearchTermFactory.createSearchTerm(new SenderCriteria(sender)).match(message));
        assertFalse(SearchTermFactory.createSearchTerm(new SenderCriteria(nonSender)).match(message));
    }

    /**
     * Test the create method on a StateCriteria
     *
     * @throws Exception
     */
    public void testCreateState() throws Exception {
        for (StoredMessageState state : StoredMessageState.values()) {
            Message message = getMessage(state);
            assertTrue(SearchTermFactory.createSearchTerm(new StateCriteria(state)).match(message));
        }
    }

    /**
     * Test the create method on a SubjectCriteria
     *
     * @throws Exception
     */
    public void testCreateSubject() throws Exception {
        String subject = "subject";
        String nonSubject = "nonsubject";
        Message message = getMessage("Subject: " + subject);
        assertTrue(SearchTermFactory.createSearchTerm(new SubjectCriteria(subject)).match(message));
        assertFalse(SearchTermFactory.createSearchTerm(new SubjectCriteria(nonSubject)).match(message));
    }

    /**
     * Test the create method on a TypeCriteria
     *
     * @throws Exception
     */
    public void testCreateType() throws Exception {
        for (MailboxMessageType type : MailboxMessageType.values()) {
            Message message = getMessage(type);
            assertTrue(SearchTermFactory.createSearchTerm(new TypeCriteria(type)).match(message));
        }
    }

    /**
     * Test the match method on a UrgentCriteria
     *
     * @throws Exception
     */
    public void testCreateUrgent() throws Exception {
        Message nonUrgentMessage = getMessage();
        assertFalse(SearchTermFactory.createSearchTerm(new UrgentCriteria(true)).match(nonUrgentMessage));
        assertTrue(SearchTermFactory.createSearchTerm(new UrgentCriteria(false)).match(nonUrgentMessage));
        Message[] urgentMessages = getUrgentMessages();
        for (Message message : urgentMessages) {
            assertTrue(SearchTermFactory.createSearchTerm(new UrgentCriteria(true)).match(message));
            assertFalse(SearchTermFactory.createSearchTerm(new UrgentCriteria(false)).match(message));
        }
    }

    public void testCreate() throws Exception {

        SearchTerm expected = new AndTerm(
                new SubjectTerm("review"),
                new FromStringTerm("555-343434")
        );
        expected = new NotTerm(expected);

        expected = new OrTerm(new SearchTerm[]{
                expected,
                new ReceivedDateTerm(SizeTerm.LE,new Date(0)),
                new AndTerm(
                        new FlagTerm(new Flags(Flags.Flag.DELETED), true),
                        new ReceivedDateTerm(SizeTerm.LE,new Date(0))
                        )
                }
            );


        Criteria<MessagePropertyCriteriaVisitor> criteria =
                new AndCriteria(
                new SubjectCriteria("review"),
                new SenderCriteria("555-343434")
            );
        criteria = new NotCriteria(criteria);

        Criteria<MessagePropertyCriteriaVisitor> rDateCriteria = new ReceivedDateCriteria(new Date(0),ReceivedDateCriteria.Comparison.LE);

        criteria = new OrCriteria(
                criteria,
                rDateCriteria,
                new AndCriteria(
                        new StateCriteria(StoredMessageState.DELETED),
                        rDateCriteria
                )
        );

        SearchTerm created = SearchTermFactory.createSearchTerm(criteria);
        assertEquals(expected,created);
    }

    /**
     * Tests that type criterias include search terms for delivery-reports
     * @throws Exception
     */
    public void testTypeCriterias() throws Exception {
        Map<SearchTerm, Criteria<MessagePropertyCriteriaVisitor>> expectations =
                new HashMap<SearchTerm, Criteria<MessagePropertyCriteriaVisitor>>();
        expectations.put(
                VOICE_SEARCH_TERM,
                new TypeCriteria(MailboxMessageType.VOICE)
        );
        expectations.put(
                VIDEO_SEARCH_TERM,
                new TypeCriteria(MailboxMessageType.VIDEO)
        );
        expectations.put(
                FAX_SEARCH_TERM,
                new TypeCriteria(MailboxMessageType.FAX)
        );
        expectations.put(
                new OrTerm(
                        new NotTerm(
                                new OrTerm(
                                        new SearchTerm[] {
                                                VOICE_SEARCH_TERM,
                                                VIDEO_SEARCH_TERM,
                                                FAX_SEARCH_TERM
                                        }
                                )
                        ),
                        REPORT_SEARCH_TERM
                ),
                new TypeCriteria(MailboxMessageType.EMAIL)
        );

        Set<Map.Entry<SearchTerm, Criteria<MessagePropertyCriteriaVisitor>>> entries = expectations.entrySet();
        for (Map.Entry<SearchTerm, Criteria<MessagePropertyCriteriaVisitor>> entry : entries) {
            assertEquals(entry.getKey(), SearchTermFactory.createSearchTerm(entry.getValue()));
        }
    }

    private Message getMessage(MailboxMessageType type) throws Exception {
        switch(type) {
            case VOICE:
                return getMessage("Content-Type: multipart/voice-message");
            case VIDEO:
                return getMessage("Content-Type: multipart/x-video-message");
            case FAX:
                return getMessage("Content-Type: multipart/fax-message");
            case EMAIL:
                return getMessage("Content-Type: text/plain");
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }

    private Message getMessage(StoredMessageState state) throws Exception {
        MimeMessageMock message = new MimeMessageMock(folderMock, 1);
        setMessageState(state, message);
        return message;
    }

    private Message getMessage(MailboxMessageType type, StoredMessageState state) throws Exception {
        Message message = getMessage(type);
        setMessageState(state, message);
        return message;
    }

    private void setMessageState(StoredMessageState state, Message message) throws MessagingException {
        switch(state) {
            case NEW:
                break;
            case READ:
                message.setFlag(Flags.Flag.SEEN, true);
                break;
            case SAVED:
                message.setFlags(new Flags("saved"), true);
                break;
            case DELETED:
                message.setFlag(Flags.Flag.DELETED, true);
                break;
            default:
                throw new IllegalArgumentException(state.toString());
        }
    }

    public static Test suite() {
        return new TestSuite(SearchTermFactoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}