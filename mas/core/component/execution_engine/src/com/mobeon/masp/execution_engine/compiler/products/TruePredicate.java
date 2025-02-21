/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * A predicate which always succeed.
 *
 * @author Mikael Andersson
 */
public class TruePredicate extends PredicateImpl  {
    public TruePredicate(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public TruePredicate(Product parent,DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    /**
     * Evaluates this predicate.
     * In this implementation it will always return <code>true</code>
     * @param ex Current {@link ExecutionContext}
     * @return true
     */
    public boolean eval(ExecutionContext ex) {
        return true;
    }

    /*public void appendMnemonic(
    StringAccumulator accumulator, int indent, boolean recurse, String lineSep, String entrySep) {
        appendMnemonics(accumulator,indent, recurse, "TruePredicate",getName(),"", operationList, lineSep,entrySep);
    }*/

    public Class getCanonicalClass() {
        return TruePredicate.class;
    }

}
