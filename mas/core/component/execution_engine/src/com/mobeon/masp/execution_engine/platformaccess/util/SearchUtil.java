/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.compare.*;
import com.mobeon.masp.mailbox.search.*;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.execution_engine.platformaccess.SearchCriteriaException;


import java.util.*;

/**
 * Utility class used to calculate search criteria and search comparator from the string arguments
 * that comes from the ECMA application when searching for messages.
 * Date: 2005-okt-28
 *
 * @author ermmaha
 */
@SuppressWarnings("unchecked")
public class SearchUtil {
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
    private final static String LIFO = "lifo";

    private Criteria<MessagePropertyCriteriaVisitor> searchCriteria;
    private StoredMessageComparatorSequence searchComparator = new StoredMessageComparatorSequence();

    private TypeComparator typeComparator;
    private StateComparator stateComparator;
    private UrgentComparator urgentComparator;

    /**
     * Constructor
     * These params could be set as null or an empty string if they are not going to be used
     * in the search criteria.
     *
     * @param types      defines TypeCriteria
     * @param states     defines StateCriteria
     * @param priorities defines UrgentCriteria
     * @param orders     (may be null)
     * @param timeOrder  fifo or lifo (may be null)
     * @param language	used for selecting correct languages for broadcast announcements
     * @throws com.mobeon.masp.execution_engine.platformaccess.SearchCriteriaException
     *          if some of the arguments have illegal values
     */
	public SearchUtil(String types, String states, String priorities, String orders, String timeOrder, String language)
            throws SearchCriteriaException {

        ArrayList<Criteria<MessagePropertyCriteriaVisitor>> critList = new ArrayList<Criteria<MessagePropertyCriteriaVisitor>>();
        if (types != null && types.length() > 0) {
            Criteria<MessagePropertyCriteriaVisitor> criteria = getTypeCriteria(types);
            if (criteria != null) critList.add(criteria);
        }
        if (states != null && states.length() > 0) {
            Criteria<MessagePropertyCriteriaVisitor> criteria = getStateCriteria(states);
            if (criteria != null) critList.add(criteria);
        }
        if (priorities != null && priorities.length() > 0) {
            Criteria<MessagePropertyCriteriaVisitor> criteria = getPrioritesCriteria(priorities);
            if (criteria != null) critList.add(criteria);
        }

        if(language != null) {
        	Criteria<MessagePropertyCriteriaVisitor> criteria = getLanguageCriteria(language);
            if (criteria != null) critList.add(criteria);
        }

        if (!critList.isEmpty()) {
            if (critList.size() == 1) {
                searchCriteria = critList.get(0);
            } else {
                searchCriteria = new AndCriteria(critList.toArray(new Criteria[critList.size()]));
            }
        }

        if (orders != null) {
            calculateSortOrder(orders);
        }

        if (timeOrder != null) {
            setTimeOrder(timeOrder);
        }


    }

    /**
     * Retrieves the Criteria object used in the search.
     *
     * @return Criteria<MessagePropertyCriteriaVisitor>
     */
    public Criteria<MessagePropertyCriteriaVisitor> getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * Retrieves the Comparator<IStoredMessage> object used to sort search results.
     *
     * @return Comparator<IStoredMessage> (empty if no orders and no timeorders was defined in the constructor)
     */
    public Comparator<IStoredMessage> getSearchComparator() {
        return searchComparator;
    }

    /**
     * Parses the type string for TypeCriterias. The order is saved in a TypeComparator.
     *
     * @param types
     * @return A TypeCriteria. If more than 1 the criterias is put in an AndCriteria. Null if no TypeCriteria
     * @throws SearchCriteriaException If invalid type string
     */
    private Criteria<MessagePropertyCriteriaVisitor> getTypeCriteria(String types)
            throws SearchCriteriaException {

        typeComparator = new TypeComparator();
        ArrayList<TypeCriteria> typeCriterias = new ArrayList<TypeCriteria>();
        List<String> list = splitString(types);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String type = it.next();
            if (type.equals(VOICE)) {
                typeCriterias.add(TypeCriteria.VOICE);
                typeComparator.add(MailboxMessageType.VOICE);
            } else if (type.equals(VIDEO)) {
                typeCriterias.add(TypeCriteria.VIDEO);
                typeComparator.add(MailboxMessageType.VIDEO);
            } else if (type.equals(FAX)) {
                typeCriterias.add(TypeCriteria.FAX);
                typeComparator.add(MailboxMessageType.FAX);
            } else if (type.equals(EMAIL)) {
                typeCriterias.add(TypeCriteria.EMAIL);
                typeComparator.add(MailboxMessageType.EMAIL);
            } else {
                throw new SearchCriteriaException("Invalid typecriteria " + type);
            }
        }

        if (typeCriterias.size() == 1) {
            return typeCriterias.get(0);
        }
        return new OrCriteria(typeCriterias.toArray(new TypeCriteria[typeCriterias.size()]));
    }

    /**
     * Parses the states string for StateCriterias. The order is saved in a StateComparator.
     *
     * @param states
     * @return A StateComparator. If more than 1 the criterias is put in an AndCriteria. Null if no StateComparator
     * @throws SearchCriteriaException If invalid state string
     */
    private Criteria<MessagePropertyCriteriaVisitor> getStateCriteria(String states)
            throws SearchCriteriaException {

        stateComparator = new StateComparator();
        ArrayList<StateCriteria> stateCriterias = new ArrayList<StateCriteria>();
        List<String> list = splitString(states);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String state = it.next();
            if (state.equals(NEW)) {
                stateCriterias.add(StateCriteria.NEW);
                stateComparator.add(StoredMessageState.NEW);
            } else if (state.equals(READ)) {
                stateCriterias.add(StateCriteria.READ);
                stateComparator.add(StoredMessageState.READ);
            } else if (state.equals(DELETED)) {
                stateCriterias.add(StateCriteria.DELETED);
                stateComparator.add(StoredMessageState.DELETED);
            } else if (state.equals(SAVED)) {
                stateCriterias.add(StateCriteria.SAVED);
                stateComparator.add(StoredMessageState.SAVED);
            } else {
                throw new SearchCriteriaException("Invalid statecriteria " + state);
            }
        }

        if (stateCriterias.size() == 1) {
            return stateCriterias.get(0);
        }
        return new OrCriteria(stateCriterias.toArray(new StateCriteria[stateCriterias.size()]));
    }

    private Criteria<MessagePropertyCriteriaVisitor> getLanguageCriteria(String language) {

    	return new LanguageCriteria(language);

    }

    /**
     * Parses the priorities string for UrgentCriterias. The order is saved in a UrgentComparator.
     *
     * @param priorities
     * @return A UrgentCriteria. If more than 1 the criterias is put in an AndCriteria. Null if no UrgentCriteria
     * @throws SearchCriteriaException If invalid priorities string
     */
    private Criteria<MessagePropertyCriteriaVisitor> getPrioritesCriteria(String priorities)
            throws SearchCriteriaException {

        ArrayList<UrgentCriteria> prioritiesCriterias = new ArrayList<UrgentCriteria>();
        List<String> list = splitString(priorities);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String priority = it.next();
            if (priority.equals(URGENT)) {
                prioritiesCriterias.add(UrgentCriteria.URGENT);
                urgentComparator = new UrgentComparator(false);
            } else if (priority.equals(NONURGENT)) {
                prioritiesCriterias.add(UrgentCriteria.NON_URGENT);
                urgentComparator = new UrgentComparator(true);
            } else {
                throw new SearchCriteriaException("Invalid prioritycriteria " + priority);
            }
        }

        if (prioritiesCriterias.size() == 1) {
            return prioritiesCriterias.get(0);
        }
        return new OrCriteria(prioritiesCriterias.toArray(new UrgentCriteria[prioritiesCriterias.size()]));
    }

    /**
     * Calculates sort order depending on the the order string and the saved Comparators.
     *
     * @param order
     * @throws SearchCriteriaException
     */
    private void calculateSortOrder(String order) throws SearchCriteriaException {
        List<String> list = splitString(order);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String orderTmp = it.next();
            if (orderTmp.equals(TYPE)) {
                if (typeComparator == null) {
                    throw new SearchCriteriaException("Invalid ordercriteria (No type criteria defined)");
                }
                searchComparator.add(typeComparator);
            } else if (orderTmp.equals(STATE)) {
                if (stateComparator == null) {
                    throw new SearchCriteriaException("Invalid ordercriteria (No state criteria defined)");
                }
                searchComparator.add(stateComparator);
            } else if (orderTmp.equals(PRIO)) {
                if (urgentComparator == null) {
                    throw new SearchCriteriaException("Invalid ordercriteria (No prio criteria defined)");
                }
                searchComparator.add(urgentComparator);
            } else {
                throw new SearchCriteriaException("Invalid ordercriteria " + orderTmp);
            }
        }
    }

    private void setTimeOrder(String timeOrder) throws SearchCriteriaException {
        if (timeOrder.equalsIgnoreCase(LIFO)) {
            searchComparator.add(ReceivedDateComparator.NEWEST_FIRST);
        } else {
            searchComparator.add(ReceivedDateComparator.OLDEST_FIRST);
        }
    }

    private List<String> splitString(String str) {
        ArrayList<String> list = new ArrayList<String>();
        StringTokenizer z = new StringTokenizer(str, ",");
        while (z.hasMoreTokens()) {
            list.add(z.nextToken());
        }
        return list;
    }
}
