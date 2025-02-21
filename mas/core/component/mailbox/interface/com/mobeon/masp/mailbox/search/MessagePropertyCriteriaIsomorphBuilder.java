/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.CriteriaIsomorphBuilder;

import java.util.List;

/**
 * Builds an isomorph tree from a critera tree.
 * @author qhast
 * @see com.mobeon.masp.util.criteria.Criteria
 */
public abstract class MessagePropertyCriteriaIsomorphBuilder<T>
        extends CriteriaIsomorphBuilder<T>
        implements MessagePropertyCriteriaVisitor {


    protected MessagePropertyCriteriaIsomorphBuilder(Object startParent) {
        super(startParent);
    }

    public void visitNotCriteria(NotCriteria c) {
        List<T> concreteChildren = getCriteriaChildrenMap().get(c);
        save(c.getParent(),createNotCriteriaIsomorph(concreteChildren.get(0)));
    }

    public void visitAndCriteria(AndCriteria c) {
        save(c.getParent(), createAndCriteriaIsomorph(getCriteriaChildrenMap().get(c)));
    }

    public void visitOrCriteria(OrCriteria c) {
        save(c.getParent(), createOrCriteriaIsomorph(getCriteriaChildrenMap().get(c)));
    }

    public void visitConfidentialCriteria(ConfidentialCriteria c) {
        save(c.getParent(),createConfidentialCriteriaIsomorph(c));
    }

    public void visitUrgentCriteria(UrgentCriteria c) {
        save(c.getParent(),createUrgentCriteriaIsomorph(c));
    }

    public void visitSenderCriteria(SenderCriteria c) {
        save(c.getParent(),createSenderCriteriaIsomorph(c));
    }

    public void visitSubjectCriteria(SubjectCriteria c) {
        save(c.getParent(),createSubjectCriteriaIsomorph(c));
    }

    public void visitRecipientCriteria(RecipientCriteria c) {
        save(c.getParent(),createRecipientCriteriaIsomorph(c));
    }

    public void visitSecondaryRecipientCriteria(SecondaryRecipientCriteria c) {
        save(c.getParent(),createSecondaryRecipientCriteriaIsomorph(c));
    }

    public void visitReplyToAddressCriteria(ReplyToAddressCriteria c) {
        save(c.getParent(),createReplyToAddressCriteriaIsomorph(c));
    }

    public void visitReceivedDateCriteria(ReceivedDateCriteria c) {
        save(c.getParent(),createReceivedDateCriteriaIsomorph(c));
    }

    public void visitTypeCriteria(TypeCriteria c) {
        save(c.getParent(),createTypeCriteriaIsomorph(c));
    }

    public void visitStateCriteria(StateCriteria c) {
        save(c.getParent(),createStateCriteriaIsomorph(c));
    }

    public void visitLanguageCriteria(LanguageCriteria c) {
        save(c.getParent(),createLanguageCriteriaIsomorph(c));
    }

    /**
     * Creates a recipient property criteria isomorph from a {@link RecipientCriteria}.
     * @param criteria
     * @return recipient property criteria isomorph.
     */
    protected abstract T createRecipientCriteriaIsomorph(RecipientCriteria criteria);

    /**
     * Creates an replyTo address property criteria isomorph from a {@link ReplyToAddressCriteria}.
     * @param criteria
     * @return replyTo address property criteria isomorph.
     */
    protected abstract T createReplyToAddressCriteriaIsomorph(ReplyToAddressCriteria criteria);

    /**
     * Creates a secondary recipient property criteria isomorph from a {@link SecondaryRecipientCriteria}.
     * @param criteria
     * @return secondary recipient property criteria isomorph.
     */
    protected abstract T createSecondaryRecipientCriteriaIsomorph(SecondaryRecipientCriteria criteria);

    /**
     * Creates a sender property criteria isomorph from a {@link SenderCriteria}.
     * @param criteria
     * @return sender property criteria isomorph.
     */
    protected abstract T createSenderCriteriaIsomorph(SenderCriteria criteria);

    /**
     * Creates a subject property criteria isomorph from a {@link SubjectCriteria}.
     * @param criteria
     * @return sender property criteria isomorph.
     */
    protected abstract T createSubjectCriteriaIsomorph(SubjectCriteria criteria);

    /**
     * Creates a received date property criteria isomorph from a {@link ReceivedDateCriteria}.
     * @param criteria
     * @return received date property criteria isomorph.
     */
    protected abstract T createReceivedDateCriteriaIsomorph(ReceivedDateCriteria criteria);

    /**
     * Creates a stored message state property criteria isomorph from a {@link StateCriteria}.
     * @param criteria
     * @return stored message state property criteria isomorph.
     */
    protected abstract T createStateCriteriaIsomorph(StateCriteria criteria);

    /**
     * Creates a mailbox message type property criteria isomorph from a {@link TypeCriteria}.
     * @param criteria
     * @return mailbox message type property criteria isomorph.
     */
    protected abstract T createTypeCriteriaIsomorph(TypeCriteria criteria);

    /**
     * Creates a confidential property criteria isomorph from a {@link ConfidentialCriteria}.
     * @param criteria
     * @return confidential property criteria isomorph.
     */
    protected abstract T createConfidentialCriteriaIsomorph(ConfidentialCriteria criteria);

    /**
     * Creates a urgent property criteria isomorph from a {@link UrgentCriteria}.
     * @param criteria
     * @return urgent property criteria isomorph.
     */
    protected abstract T createUrgentCriteriaIsomorph(UrgentCriteria criteria);

    /**
     * Creates a language property criteria isomorph from a {@link LanguageCriteria}.
     * @param criteria
     * @return language property criteria isomorph.
     */
    protected abstract T createLanguageCriteriaIsomorph(LanguageCriteria criteria);

}
