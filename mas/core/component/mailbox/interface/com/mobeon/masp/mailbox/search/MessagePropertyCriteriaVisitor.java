/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.CriteriaVisitor;

/**
 * Defines a Visitor able to be accepted by a message property criteria tree.
 * Classes implementing {@link MessagePropertyCriteriaVisitor} can
 * "visit" a message property criteria tree by using
 * {@link com.mobeon.masp.util.criteria.Criteria#accept}.
 *  <br>
 * When accepting a visitor the message property criteria tree
 * do a depth-first search for nodes. Criteria children are always
 * visited before parents. For example in the below tree will E and F
 * be visited before A and D. D before A. C before B and A. B before A.
 * <br>
 * This is only guarenteed for children and ancestors NOT for siblings, cousins etc.
 * e.g. B may be visited before E, E before F and vice versa.
 * <pre>
 * OrCriteria A
 *  |
 *  +-- NotCriteria B
 *  |    |
 *  |    +-- ConfidentialCriteria C
 *  |
 *  +-- AndCriteria D
 *       |
 *       +-- StoredMessageStateCriteria E
 *       |
 *       +-- MailboxMessageTypeCriteria F
 *
 * </pre>
 * @author qhast
 * @see com.mobeon.masp.util.criteria.Criteria
 */
public interface MessagePropertyCriteriaVisitor extends CriteriaVisitor {


    /**
     * Called by ConfidentialCriteria nodes.
     * @param c
     */
    public void visitConfidentialCriteria(ConfidentialCriteria c);

    /**
     * Called by UrgentCriteria nodes.
     * @param c
     */
    public void visitUrgentCriteria(UrgentCriteria c);

    /**
     * Called by SenderCriteria nodes.
     * @param c
     */
    public void visitSenderCriteria(SenderCriteria c);

    /**
    * Called by SenderCriteria nodes.
    * @param c
    */
    public void visitSubjectCriteria(SubjectCriteria c);

    /**
     * Called by RecipientCriteria nodes.
     * @param c
     */
    public void visitRecipientCriteria(RecipientCriteria c);

    /**
     * Called by SecondaryRecipientCriteria nodes.
     * @param c
     */
    public void visitSecondaryRecipientCriteria(SecondaryRecipientCriteria c);

    /**
     * Called by ReplyToAddressCriteria nodes.
     * @param c
     */
    public void visitReplyToAddressCriteria(ReplyToAddressCriteria c);

    /**
     * Called by ReceivedDateCriteria nodes.
     * @param c
     */
    public void visitReceivedDateCriteria(ReceivedDateCriteria c);

    /**
     * Called by TypeCriteria nodes.
     * @param c
     */
    public void visitTypeCriteria(TypeCriteria c);

    /**
     * Called by StateCriteria nodes.
     * @param c
     */
    public void visitStateCriteria(StateCriteria c);

    /**
     * Called by LanguageCriteria nodes.
     * @param c
     */
    public void visitLanguageCriteria(LanguageCriteria c);


}
