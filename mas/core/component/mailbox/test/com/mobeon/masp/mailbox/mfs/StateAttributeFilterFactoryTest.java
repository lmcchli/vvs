/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Date;

import junit.framework.Assert;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.search.AndCriteria;
import com.mobeon.masp.mailbox.search.ConfidentialCriteria;
import com.mobeon.masp.mailbox.search.NotCriteria;
import com.mobeon.masp.mailbox.search.OrCriteria;
import com.mobeon.masp.mailbox.search.ReceivedDateCriteria;
import com.mobeon.masp.mailbox.search.RecipientCriteria;
import com.mobeon.masp.mailbox.search.ReplyToAddressCriteria;
import com.mobeon.masp.mailbox.search.SecondaryRecipientCriteria;
import com.mobeon.masp.mailbox.search.SenderCriteria;
import com.mobeon.masp.mailbox.search.StateCriteria;
import com.mobeon.masp.mailbox.search.SubjectCriteria;
import com.mobeon.masp.mailbox.search.TypeCriteria;
import com.mobeon.masp.mailbox.search.UrgentCriteria;
import com.mobeon.masp.util.criteria.QuantitativeValueCriteria.Comparison;

/**
 * @author egeobli
 *
 */
@RunWith(JMock.class)
public class StateAttributeFilterFactoryTest {
	
	Mockery mockery = new JUnit4Mockery();


	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitConfidentialCriteria(com.mobeon.masp.mailbox.search.ConfidentialCriteria)}.
	 */
	@Test
	public void testVisitConfidentialCriteria() {
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Privacy, Constants.MFS_NONPRIVATE);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);

		ConfidentialCriteria criteria = new ConfidentialCriteria(false);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
		
		criteria = new ConfidentialCriteria(true);
		filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		
		stateAttributes.setC1Attribute(Container1.Privacy, Constants.MFS_PRIVATE);
		stateFile.setAttributes(stateAttributes);

		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitReceivedDateCriteria(com.mobeon.masp.mailbox.search.ReceivedDateCriteria)}.
	 */
	@Test
	public void testVisitReceivedDateCriteria() {
		Date date = new Date();
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Date_time, 
				MfsUtil.DateFormatter.format(date));
		
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		ReceivedDateCriteria criteria = new ReceivedDateCriteria(date, Comparison.EQ);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
		
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitRecipientCriteria(com.mobeon.masp.mailbox.search.RecipientCriteria)}.
	 */
	@Test
	public void testVisitRecipientCriteria() {
		String recipient = "sip:myuser@abcxyz.com";
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.To, recipient);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		RecipientCriteria criteria = new RecipientCriteria(recipient);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitReplyToAddressCriteria(com.mobeon.masp.mailbox.search.ReplyToAddressCriteria)}.
	 */
	@Test
	public void testVisitReplyToAddressCriteria() {
		String recipient = "sip:replytouser@abcxyz.com";
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setAttribute(MoipMessageEntities.REPLY_TO_HEADER, recipient);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		ReplyToAddressCriteria criteria = new ReplyToAddressCriteria(recipient);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitSecondaryRecipientCriteria(com.mobeon.masp.mailbox.search.SecondaryRecipientCriteria)}.
	 */
	@Test
	public void testVisitSecondaryRecipientCriteria() {
		String recipient = "sip:secondaryuser@abcxyz.com";
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Cc, recipient);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		SecondaryRecipientCriteria criteria = new SecondaryRecipientCriteria(recipient);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitSenderCriteria(com.mobeon.masp.mailbox.search.SenderCriteria)}.
	 */
	@Test
	public void testVisitSenderCriteria() {
		String sender = "sip:senderuser@abcxyz.com";
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.From, sender);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		SenderCriteria criteria = new SenderCriteria(sender);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitStateCriteria(com.mobeon.masp.mailbox.search.StateCriteria)}.
	 */
	@Test
	public void testVisitStateCriteria() {
		StoredMessageState state = StoredMessageState.NEW;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MfsUtil.toMfsMessageState(state));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		StateCriteria criteria = new StateCriteria(state);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitSubjectCriteria(com.mobeon.masp.mailbox.search.SubjectCriteria)}.
	 */
	@Test
	public void testVisitSubjectCriteria() {
		String subject = "Testing criteria subject handling.";
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Subject, subject);
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		SubjectCriteria criteria = new SubjectCriteria(subject);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitTypeCriteria(com.mobeon.masp.mailbox.search.TypeCriteria)}.
	 */
	@Test
	public void testVisitTypeCriteria() {
		MailboxMessageType state = MailboxMessageType.VOICE;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setAttribute(StateAttributes.getC1StateName(Container1.Message_class), 
				MfsUtil.toMfsMessageType(state));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		TypeCriteria criteria = new TypeCriteria(state);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitUrgentCriteria(com.mobeon.masp.mailbox.search.UrgentCriteria)}.
	 */
	@Test
	public void testVisitUrgentCriteria() {
		boolean urgent = true;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setAttribute(StateAttributes.getC1StateName(Container1.Priority), 
				Integer.toString(MfsUtil.toMfsPriority(urgent)));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		UrgentCriteria criteria = new UrgentCriteria(urgent);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitAndCriteria(com.mobeon.masp.util.criteria.AndCriteria)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testVisitAndCriteria() {
		boolean urgent = true;
		MailboxMessageType state = MailboxMessageType.VOICE;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Priority, Integer.toString(MfsUtil.toMfsPriority(urgent)));
		stateAttributes.setC1Attribute(Container1.Message_class, MfsUtil.toMfsMessageType(state));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		UrgentCriteria urgentCriteria = new UrgentCriteria(urgent);
		TypeCriteria typeCriteria = new TypeCriteria(state);
		AndCriteria criteria = new AndCriteria(urgentCriteria, typeCriteria);
		
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
		
		stateAttributes.setC1Attribute(Container1.Priority, Integer.toString(MfsUtil.toMfsPriority(false)));
		Assert.assertFalse(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitNotCriteria(com.mobeon.masp.util.criteria.NotCriteria)}.
	 */
	@Test
	public void testVisitNotCriteria() {
		StoredMessageState state = StoredMessageState.NEW;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MfsUtil.toMfsMessageState(state));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		StateCriteria stateCriteria = new StateCriteria(state);
		NotCriteria criteria = new NotCriteria(stateCriteria);
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertFalse(filter.isMatching(stateFile));
		
		stateAttributes.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, 
				MfsUtil.toMfsMessageState(StoredMessageState.READ));
		Assert.assertTrue(filter.isMatching(stateFile));
	}

	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.StateAttributeFilterFactory#visitOrCriteria(com.mobeon.masp.util.criteria.OrCriteria)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testVisitOrCriteria() {
		MailboxMessageType state1 = MailboxMessageType.VOICE;
		MailboxMessageType state2 = MailboxMessageType.EMAIL;
		
		StateAttributes stateAttributes = new StateAttributes();
		stateAttributes.setC1Attribute(Container1.Message_class, MfsUtil.toMfsMessageType(state1));
		StateFile stateFile = new StateFile(null, null, null, null);
		stateFile.setAttributes(stateAttributes);
		
		TypeCriteria typeCriteria1 = new TypeCriteria(state1);
		TypeCriteria typeCriteria2 = new TypeCriteria(state2);
		OrCriteria criteria = new OrCriteria(typeCriteria1, typeCriteria2);
		
		StateAttributesFilter filter = StateAttributeFilterFactory.createStateAttributeFilter(criteria);
		Assert.assertTrue(filter.isMatching(stateFile));
		
		stateAttributes.setC1Attribute(Container1.Message_class, MfsUtil.toMfsMessageType(MailboxMessageType.FAX));
		Assert.assertFalse(filter.isMatching(stateFile));
	}
}
