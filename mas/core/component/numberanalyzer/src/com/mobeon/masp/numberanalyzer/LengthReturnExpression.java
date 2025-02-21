/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import java.util.regex.Matcher;

/**
 * ReturnExpression class that checks length on the number
 *
 * @author ermmaha
 */
class LengthReturnExpression extends ReturnExpression {
    private int min;
    private int max;

    /**
     * Constructor
     *
     * @param returnExpr
     * @param matcher
     */
    LengthReturnExpression(String returnExpr, Matcher matcher) throws NumberAnalyzerException {
        super(returnExpr);
        String tmp = matcher.group(1);
        min = Integer.parseInt(tmp);
        tmp = matcher.group(3);
        max = Integer.parseInt(tmp);

        if (min > max) {
            throw new NumberAnalyzerException(
                    "Invalid return-expression minlength can't be larger than maxlength " + returnExpr);
        }
    }

    /**
     * Retrieves min allowed length for the number
     *
     * @return min length
     */
    public int getMin() {
        return min;
    }

    /**
     * Retrieves max allowed length for the number
     *
     * @return max length
     */
    public int getMax() {
        return max;
    }
}

