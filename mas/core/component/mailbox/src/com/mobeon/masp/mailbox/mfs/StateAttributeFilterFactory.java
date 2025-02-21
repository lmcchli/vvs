/**
 *
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.services.broadcastannouncement.BroadcastAnnouncement;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.masp.mailbox.search.ConfidentialCriteria;
import com.mobeon.masp.mailbox.search.LanguageCriteria;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.mailbox.search.ReceivedDateCriteria;
import com.mobeon.masp.mailbox.search.RecipientCriteria;
import com.mobeon.masp.mailbox.search.ReplyToAddressCriteria;
import com.mobeon.masp.mailbox.search.SecondaryRecipientCriteria;
import com.mobeon.masp.mailbox.search.SenderCriteria;
import com.mobeon.masp.mailbox.search.StateCriteria;
import com.mobeon.masp.mailbox.search.SubjectCriteria;
import com.mobeon.masp.mailbox.search.TypeCriteria;
import com.mobeon.masp.mailbox.search.UrgentCriteria;
import com.mobeon.masp.util.criteria.AndCriteria;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.NotCriteria;
import com.mobeon.masp.util.criteria.OrCriteria;

/**
 * Builds a String from an isomorph tree.
 *
 * @author egeobli
 */
public class StateAttributeFilterFactory implements MessagePropertyCriteriaVisitor {

	private StateAttributesFilter stateAttributesFilter;
	private Hashtable<String, Vector<String>> criterias;


	/**
	 * @param parent Root element of the search tree.
	 */
	private StateAttributeFilterFactory(StateAttributesFilter stateAttributes) {
		this.stateAttributesFilter = stateAttributes;
		criterias = new Hashtable<String, Vector<String>>();
	}

    public static StateAttributesFilter createStateAttributeFilter(Criteria<MessagePropertyCriteriaVisitor> abstractCriteria) {
    	if(abstractCriteria == null) {
    		return null;
    	}
    	StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
    	StateAttributeFilterFactory builder = new StateAttributeFilterFactory(stateAttributesFilter);
        abstractCriteria.accept(builder);
        builder.storeCollectedCriterias();
        return stateAttributesFilter;
    }

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitConfidentialCriteria(com.mobeon.masp.mailbox.search.ConfidentialCriteria)
	 */
	public void visitConfidentialCriteria(ConfidentialCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Privacy),
				MfsUtil.toMfsConfidentialState(c.getValue()));
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitReceivedDateCriteria(com.mobeon.masp.mailbox.search.ReceivedDateCriteria)
	 */
	public void visitReceivedDateCriteria(ReceivedDateCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Date_time),
		        CommonMessagingAccess.DateFormatter.get().format(c.getValue()));
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitRecipientCriteria(com.mobeon.masp.mailbox.search.RecipientCriteria)
	 */
	public void visitRecipientCriteria(RecipientCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.To), c.getValue());
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitReplyToAddressCriteria(com.mobeon.masp.mailbox.search.ReplyToAddressCriteria)
	 */
	public void visitReplyToAddressCriteria(ReplyToAddressCriteria c) {
		addSearchCriteria(MoipMessageEntities.REPLY_TO_HEADER, c.getValue());
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitSecondaryRecipientCriteria(com.mobeon.masp.mailbox.search.SecondaryRecipientCriteria)
	 */
	public void visitSecondaryRecipientCriteria(SecondaryRecipientCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Cc), c.getValue());
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitSenderCriteria(com.mobeon.masp.mailbox.search.SenderCriteria)
	 */
	public void visitSenderCriteria(SenderCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.From), c.getValue());
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitStateCriteria(com.mobeon.masp.mailbox.search.StateCriteria)
	 */
	public void visitStateCriteria(StateCriteria c) {
		addSearchCriteria(StateAttributes.GLOBAL_MSG_STATE_KEY, MfsUtil.toMfsMessageState(c.getValue()));
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitSubjectCriteria(com.mobeon.masp.mailbox.search.SubjectCriteria)
	 */
	public void visitSubjectCriteria(SubjectCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Subject), c.getValue());
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitTypeCriteria(com.mobeon.masp.mailbox.search.TypeCriteria)
	 */
	public void visitTypeCriteria(TypeCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Message_class), MfsUtil.toMfsMessageType(c.getValue()));
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor#visitUrgentCriteria(com.mobeon.masp.mailbox.search.UrgentCriteria)
	 */
	public void visitUrgentCriteria(UrgentCriteria c) {
		addSearchCriteria(StateAttributes.getC1StateName(Container1.Priority),
				Integer.toString(MfsUtil.toMfsPriority(c.getValue())));
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.util.criteria.CriteriaVisitor#visitAndCriteria(com.mobeon.masp.util.criteria.AndCriteria)
	 */
	@SuppressWarnings("unchecked")
	public void visitAndCriteria(AndCriteria c) {
		storeCollectedCriterias();
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.util.criteria.CriteriaVisitor#visitNotCriteria(com.mobeon.masp.util.criteria.NotCriteria)
	 */
	@SuppressWarnings("unchecked")
	public void visitNotCriteria(NotCriteria c) {
		Iterator<Map.Entry<String, Vector<String>>> it =  criterias.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, Vector<String>> entry = it.next();
			Vector<String> values = entry.getValue();
			stateAttributesFilter.setNotEqualAttributeValue(entry.getKey(),
					values.toArray(new String[values.size()]));
		}
		criterias.clear();
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.util.criteria.CriteriaVisitor#visitOrCriteria(com.mobeon.masp.util.criteria.OrCriteria)
	 */
	@SuppressWarnings("unchecked")
	public void visitOrCriteria(OrCriteria c) {
		storeCollectedCriterias();
	}

	/**
	 * Adds a search criteria.
	 *
	 * @param name Criteria's name
	 * @param value Criteria's value
	 */
	private void addSearchCriteria(String name, String value) {
		Vector<String> criteriaValues = criterias.get(name);
		if (criteriaValues == null) {
			criteriaValues = new Vector<String>();
			criterias.put(name, criteriaValues);
		}
		criteriaValues.add(value);
	}


	private void storeCollectedCriterias() {
		Iterator<Map.Entry<String, Vector<String>>> it =  criterias.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, Vector<String>> entry = it.next();
			Vector<String> values = entry.getValue();
			stateAttributesFilter.setAttributeValue(entry.getKey(),
					values.toArray(new String[values.size()]));
		}
		criterias.clear();
	}

	@Override
	public void visitLanguageCriteria(LanguageCriteria c) {
		addSearchCriteria(BroadcastAnnouncement.BROADCAST_LANGUAGE_STATEFILE_HEADER, c.getValue());
	}
}
