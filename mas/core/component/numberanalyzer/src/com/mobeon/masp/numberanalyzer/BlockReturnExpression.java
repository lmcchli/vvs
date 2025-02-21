/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

/**
 * ReturnExpression class that says that the number is blocked if the Rule matches
 */
class BlockReturnExpression extends ReturnExpression {
    BlockReturnExpression(String returnExpr) {
        super(returnExpr);
    }
}
