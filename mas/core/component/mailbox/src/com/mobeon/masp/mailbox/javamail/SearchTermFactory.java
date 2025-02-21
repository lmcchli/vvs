/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import static com.mobeon.masp.mailbox.javamail.JavamailFlags.*;
import static com.mobeon.masp.mailbox.javamail.UrgentHeaderUtil.*;
import static com.mobeon.masp.mailbox.javamail.ConfidentialHeaderUtil.*;
import static com.mobeon.masp.mailbox.javamail.ContentTypeHeaderUtil.*;
import com.mobeon.masp.mailbox.search.*;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.QuantitativeValueCriteria;

import jakarta.mail.Message;
import jakarta.mail.search.*;

import java.util.List;

/**
 * This factory creates a {@link SearchTerm} from a {@link Criteria<MessagePropertyCriteriaVisitor>}.
 * @author qhast
 */
public final class SearchTermFactory extends MessagePropertyCriteriaIsomorphBuilder<SearchTerm> {

    private SearchTermFactory(Object startParent) {
        super(startParent);
    }

    /**
     * Creates a JavaMail criteria from abstract criteria.
     * @param abstractCriteria
     * @return JavaMail criteria.
     */
    public static SearchTerm createSearchTerm(Criteria<MessagePropertyCriteriaVisitor> abstractCriteria) {
        SearchTermFactory builder = new SearchTermFactory(abstractCriteria.getParent());
        abstractCriteria.accept(builder);
        return builder.getIsomorph();
    }

    protected SearchTerm createNotCriteriaIsomorph(SearchTerm concreteCriteria) {
        return new NotTerm(concreteCriteria);
    }

    protected SearchTerm createAndCriteriaIsomorph(List<SearchTerm> concreteCriterias) {
        return new AndTerm(concreteCriterias.toArray(new SearchTerm[0]));
    }

    protected SearchTerm createOrCriteriaIsomorph(List<SearchTerm> concreteCriterias) {
        return new OrTerm(concreteCriterias.toArray(new SearchTerm[0]));
    }

    protected SearchTerm createRecipientCriteriaIsomorph(RecipientCriteria criteria) {
        return new RecipientStringTerm(Message.RecipientType.TO,criteria.getValue());
    }

    protected SearchTerm createReplyToAddressCriteriaIsomorph(ReplyToAddressCriteria criteria) {
        return new HeaderTerm("Reply-To",criteria.getValue());
    }

    protected SearchTerm createSecondaryRecipientCriteriaIsomorph(SecondaryRecipientCriteria criteria) {
        return new RecipientStringTerm(Message.RecipientType.CC,criteria.getValue());
    }

    protected SearchTerm createSenderCriteriaIsomorph(SenderCriteria criteria) {
        return new FromStringTerm(criteria.getValue());
    }

    protected SearchTerm createSubjectCriteriaIsomorph(SubjectCriteria criteria) {
        return new SubjectTerm(criteria.getValue());
    }

    protected SearchTerm createReceivedDateCriteriaIsomorph(ReceivedDateCriteria criteria) {
        return new ReceivedDateTerm(convertComparison(criteria.getComparison()),criteria.getValue());
    }

    protected SearchTerm createTypeCriteriaIsomorph(TypeCriteria criteria) {

        switch(criteria.getValue()) {
            case VOICE:
                return VOICE_SEARCHTERM;
            case VIDEO:
                return VIDEO_SEARCHTERM;
            case FAX:
                return FAX_SEARCHTERM;
            default:
                return EMAIL_SEARCHTERM;
        }
    }

    protected SearchTerm createConfidentialCriteriaIsomorph(ConfidentialCriteria criteria) {
        if(criteria.getValue()) {
            return CONFIDENTIAL_SEARCHTERM;
        } else {
            return new NotTerm(CONFIDENTIAL_SEARCHTERM);
        }
    }

    protected SearchTerm createUrgentCriteriaIsomorph(UrgentCriteria criteria) {
        if(criteria.getValue()) {
            return URGENT_SEARCHTERM;
        } else {
            return new NotTerm(URGENT_SEARCHTERM);
        }
    }

    private int convertComparison(QuantitativeValueCriteria.Comparison comparison) {
        int result = 0;
        switch(comparison) {
            case EQ: result = ComparisonTerm.EQ; break;
            case GE: result = ComparisonTerm.GE; break;
            case GT: result = ComparisonTerm.GT; break;
            case LE: result = ComparisonTerm.LE; break;
            case LT: result = ComparisonTerm.LT; break;
            case NE: result = ComparisonTerm.NE; break;
        }
        return result;
    }

    private static final SearchTerm newTerm =
            new FlagTerm(NEW_NOT_SET_FLAGS,false);

    private static final SearchTerm savedTerm =
            new AndTerm(new FlagTerm(SAVED_SET_FLAGS,true),new FlagTerm(SAVED_NOT_SET_FLAGS,false));

    private static final SearchTerm readTerm
            = new AndTerm(new FlagTerm(READ_SET_FLAGS,true),new FlagTerm(READ_NOT_SET_FLAGS,false));

    private static final SearchTerm deletedTerm
            = new FlagTerm(DELETED_SET_FLAGS,true);

    protected SearchTerm createStateCriteriaIsomorph(StateCriteria criteria) {

        switch(criteria.getValue()) {
            case NEW:
                return newTerm;
            case SAVED:
                return savedTerm;
            case READ:
                return readTerm;
            default:
                return deletedTerm;
        }

    }

	@Override
	protected SearchTerm createLanguageCriteriaIsomorph(
			LanguageCriteria criteria) {
		//Not implemented
		return null;
	}

}
