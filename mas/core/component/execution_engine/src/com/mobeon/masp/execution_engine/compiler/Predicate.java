/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;


/**
 * Predicate
 *
 * @author Mikael Andersson
 */
public interface Predicate extends Product  {
    boolean eval(ExecutionContext ex);

    void addToPredicate(Executable pred);

    void setCond(String cond);

    String getCond();

    void setExpr(String expr);

    String getExpr();
}
