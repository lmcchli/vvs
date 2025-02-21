/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.util.criteria.Criteria;

import java.util.List;

/**
 * @author qhast
 */
public final class StoredMessageMatcher extends MessagePropertyCriteriaIsomorphBuilder<Boolean> {

    private IStoredMessage target;

    private StoredMessageMatcher(Object startParent,IStoredMessage target) {
        super(startParent);
        this.target = target;
    }

    /**
     * Matches a stored message to abstract criteria.
     * @param abstractCriteria
     * @return true if message matches criteria.
     */
    public static boolean match(Criteria<MessagePropertyCriteriaVisitor> abstractCriteria,IStoredMessage target) {
        StoredMessageMatcher builder = new StoredMessageMatcher(abstractCriteria.getParent(),target);
        abstractCriteria.accept(builder);
        return builder.getIsomorph();
    }

     protected Boolean createNotCriteriaIsomorph(Boolean concreteCriteria) {
        return !concreteCriteria;
    }

    protected Boolean createAndCriteriaIsomorph(List<Boolean> concreteCriterias) {
        for(Boolean b : concreteCriterias) {
            if(!b) return false;
        }
        return true;
    }

    protected Boolean createOrCriteriaIsomorph(List<Boolean> concreteCriterias) {
        for(Boolean b : concreteCriterias) {
            if(b) return true;
        }
        return false;
    }

    protected Boolean createRecipientCriteriaIsomorph(RecipientCriteria criteria) {
        for(String s : target.getRecipients()) {
            if(criteria.matchValue(s)) return true;
        }
        return false;
    }

    protected Boolean createReplyToAddressCriteriaIsomorph(ReplyToAddressCriteria criteria) {
        return criteria.matchValue(target.getReplyToAddress());
    }

    protected Boolean createSecondaryRecipientCriteriaIsomorph(SecondaryRecipientCriteria criteria) {
        for(String s : target.getSecondaryRecipients()) {
            if(criteria.matchValue(s)) return true;
        }
        return false;
    }

    protected Boolean createSenderCriteriaIsomorph(SenderCriteria criteria) {
        return criteria.matchValue(target.getSender());
    }

    protected Boolean createSubjectCriteriaIsomorph(SubjectCriteria criteria) {
        return criteria.matchValue(target.getSubject());
    }

    protected Boolean createReceivedDateCriteriaIsomorph(ReceivedDateCriteria criteria) {
        return criteria.matchValue(target.getReceivedDate());
    }



    protected Boolean createTypeCriteriaIsomorph(TypeCriteria criteria) {
        return criteria.matchValue(target.getType());
    }

    protected Boolean createConfidentialCriteriaIsomorph(ConfidentialCriteria criteria) {
        return criteria.matchValue(target.isConfidential());
    }


    protected Boolean createUrgentCriteriaIsomorph(UrgentCriteria criteria) {
        return criteria.matchValue(target.isUrgent());
    }


    protected Boolean createStateCriteriaIsomorph(StateCriteria criteria) {
        return criteria.matchValue(target.getState());
    }

	@Override
	protected Boolean createLanguageCriteriaIsomorph(LanguageCriteria criteria) {
		return criteria.matchValue(target.getBroadcastLanguage());
	}

}
