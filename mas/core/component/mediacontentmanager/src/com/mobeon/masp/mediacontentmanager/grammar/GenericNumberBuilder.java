/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;

/**
 * Default implementation of the
 * {@link com.mobeon.masp.mediacontentmanager.IGenericNumberBuilder} interface.
 *
 * @author mmawi
 */
public class GenericNumberBuilder implements IGenericNumberBuilder {
    /**
     * The logger used.
     */
    private static ILogger LOGGER =
            ILoggerFactory.getILogger(GenericNumberBuilder.class);

    //javadoc in interface
    public List<String> buildNumber(IRulesRecord rule, Long numberToDecompose) {
        List<INumberRule> numberRuleList = new ArrayList<INumberRule>(rule.getNumberRules().values());
        if (numberRuleList.size() == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No NumberRules found in this RulesRecord.");
            }
            return null;
        }

        if (numberToDecompose == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Number is null.");
            }
            return null;
        }

        List<IActionElement> actionList = new ArrayList<IActionElement>();

        executeRule(0, numberToDecompose, numberRuleList, actionList);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Actions before postprocessing: " + actionList.toString());
        }

        List<String> result = postProcessResult(actionList);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Result after postprocessing: " + result.toString());
        }
        return result;
    }

    /**
     * Translates a list of {@link IActionElement}s to a list of Strings
     * containing spoken words.
     * Dependent of the type of action, the elements may change position or be
     * skipped in the resulting list.
     *
     * @param actionList   The list of <code>IActionElement</code>s.
     * @return A list of spoken words in correct order.
     */
    private List<String> postProcessResult(List<IActionElement> actionList) {
        List<String> resultList = new ArrayList<String>();
        ListIterator<IActionElement> iter = actionList.listIterator();
        int swap = 0;
        boolean skipNext = false;
        boolean select = false;
        while (iter.hasNext()) {
            IActionElement currentAction = iter.next();

            switch (currentAction.getType()) {

                case mediafile:
                    // Add the media file to the list if it is not first.
                    // If it is the first element, add it only if it not should be skipped.
                    if (!skipNext || resultList.size() > 0) {
                        // Check if we must replace the previous media file with this
                        // media file.
                        if (select) {
                            //If there are no more media files, replace the previous.
                            if (!iter.hasNext()) {
                                resultList.set(resultList.size() - 1, currentAction.getMediaFileName());
                            }
                        }
                        else {
                            resultList.add(currentAction.getMediaFileName());
                            // Check if we need to change position of the element.
                            if (swap != 0) {
                                int fromIndex = resultList.size() - 1;
                                int toIndex = fromIndex + swap;
                                if (toIndex < 0) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Invalid value of swap. " +
                                                "Cannot move element before the beginning of list," +
                                                "swapping with first element.");
                                    }
                                    toIndex = 0;
                                }
                                String toElement = resultList.get(toIndex);
                                String fromElement = resultList.get(fromIndex);
                                resultList.set(toIndex, fromElement);
                                resultList.set(fromIndex, toElement);

                                // If the element just moved to position 0 should be skipped,
                                // remove it.
                                if (toIndex == 0 && skipNext) {
                                    resultList.remove(0);
                                }
                            }
                        }
                    }
                    skipNext = false;
                    select = false;
                    swap = 0;
                    break;
                case skip:
                    // The next media file should not be first in the list.
                    skipNext = true;
                    break;
                case swap:
                    // The next media file should change position with the
                    // one at position -swap in the list.
                    swap = currentAction.getSwapValue();
                    break;
                case select:
                    // The previous media file will be replaced by the next
                    // element if the next one is the last.
                    select = true;
                    break;
            }
        }
        return resultList;
    }

    /**
     * Recursive function that decomposes the number.
     * This function will traverse through the {@link NumberRule}s and
     * execute the division and check the {@link NumberRuleCondition}s for
     * each rule.
     *
     * @param index             The position in the list of
     *                          <code>NumberRule</code>s to start at.
     * @param number            The number to decompose.
     * @param numberRuleList    The <code>NumberRules</code>s that should be
     *                          used for testing the number.
     * @param resultList        A list of <code>IActionElement</code>s that is
     *                          built during the decomposition.
     */
    private boolean executeRule(int index, Long number,
                                List<INumberRule> numberRuleList,
                                List<IActionElement> resultList) {
        try
        {
            INumberRule numberRule = numberRuleList.get(index);

            List<INumberRuleCondition> conditionList = numberRule.getConditionList();

            Long divisor = numberRule.getDivisor();
            Long quotient = number / divisor;
            Long remainder = number % divisor;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("n=" + number + " d=" + divisor + " q=" + quotient + " r=" + remainder);
            }

            for (INumberRuleCondition condition : conditionList) {
                if (condition.isSatified(quotient, remainder)){
                    if (condition.isDivide()) {
                        if (condition.isAtomic()) {
                            number = number - divisor;
                            for (IActionElement element : condition.getActionElementList()) {
                                resultList.add(element);
                            }
                            return true;
                        }
                        else {
                            if (condition.isTerminal()) {
                                number = remainder;
                                for (IActionElement element : condition.getActionElementList()) {
                                    resultList.add(element);
                                }
                                break;
                            }
                            else {
                                number = remainder;
                                int recIndex = index + 1;
                                executeRule(recIndex, quotient, numberRuleList, resultList);
                                for (IActionElement element : condition.getActionElementList()) {
                                    resultList.add(element);
                                }
                                break;
                            }
                        }
                    }
                    else {
                        for (IActionElement element : condition.getActionElementList()) {
                            resultList.add(element);
                        }
                    }
                }
            }
            return executeRule(++index, number, numberRuleList, resultList);
        }
        catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}
