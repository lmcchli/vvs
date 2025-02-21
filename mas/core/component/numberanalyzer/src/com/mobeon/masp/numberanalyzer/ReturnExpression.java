/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for the return expressions that can be set in a Rule
 *
 * @author ermmaha
 */
public class ReturnExpression {
    static final String GROUPIDENTIFIER = "$i";

    private static Pattern returnTypeLengthPattern = Pattern.compile("(\\d+)(,)(\\d+)", Pattern.COMMENTS);
    protected String returnExpr;

    /**
     * Constructor
     * @param returnExpr
     */
    ReturnExpression(String returnExpr) {
        this.returnExpr = returnExpr;
    }

    /**
     * Factory method for creating ReturnExpression objects depending on the returnExpr string
     * @param returnExpr
     * @return a ReturnExpression object
     * @throws NumberAnalyzerException if a ReturnExpression could not be created from the string
     */
    public static ReturnExpression createReturnExpression(String returnExpr) throws NumberAnalyzerException {
        Matcher lengthMatcher = returnTypeLengthPattern.matcher(returnExpr);
        if (lengthMatcher.find()) {
            return new LengthReturnExpression(returnExpr, lengthMatcher);
        } else if (returnExpr.equalsIgnoreCase("block")) {
            return new BlockReturnExpression(returnExpr);
        } else if (returnExpr.indexOf(GROUPIDENTIFIER) > -1) {
            return new GroupReturnExpression(returnExpr);
        } else if (returnExpr.matches("\\d*")) {
            return new GroupReturnExpression(returnExpr);
        }
        throw new NumberAnalyzerException("Invalid return-expression " + returnExpr);
    }

    /**
     * Retrieves the return expression string
     * @return return expression string
     */
    public String toString() {
        return returnExpr;
    }
}





