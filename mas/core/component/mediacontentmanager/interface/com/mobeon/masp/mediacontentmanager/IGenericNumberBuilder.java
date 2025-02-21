package com.mobeon.masp.mediacontentmanager;

import java.util.List;

/**
 * Decomposes a number into its spoken words according to the grammar rules
 * of a language.
 * <p/>
 * The input number is parsed by applying a sequence of divisions to it and
 * analyse the result if each division. The parsing algorithm is generic, it
 * uses a grammar file defining the division rules. Each division rule
 * consists of a divisor and value range tests (conditions) to apply to the
 * results of the division (quotient and remainder) to determine if the rule
 * succeeded. If the test succeeded, that condition's defined
 * {@link IActionElement}s are added to the resulting list. The conditions
 * are subsetted by divisor, and once a condition is satisfied within a
 * divisor subset, no more conditions are applied from that subset unless
 * the condition is "not divide", then the next condition is tested also.
 * <p/>
 * The number is now broken down in a quotient and a remainder. The quotient
 * and remainder are then subjected to other divisor subsets and the process
 * continues recursively until the number has been consumed (divided down to
 * 0).
 * <p/>
 * The list of <code>IActionElement</code> is post processed to take care of
 * some special rules. For example, one element may change position with
 * another to get the correct output, another must be skipped if it is the
 * first element in the list.
 * <p/>
 * The result after the post processing is a list of
 * file names, in correct order, that represents the number.
 *
 * @author mmawi
 */
public interface IGenericNumberBuilder {
    /**
     * Builds a list of digits or spoken words from a number.
     *
     * @param rule                  The <code>RulesRecord</code> to use for
     *                              the decomposition.
     * @param numberToDecompose     The number that shall be decomposed.
     * @return  A list of digits or words that constitutes the number.
     */
    List<String> buildNumber(IRulesRecord rule,
                             Long numberToDecompose);
}
