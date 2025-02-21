/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;

/**
 * @author David Looberger
 */
public class FilledPredicateImpl extends PredicateImpl {
    String namelist = null;
    public FilledPredicateImpl(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public FilledPredicateImpl(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    public void setNamelist(String namelist) {
        this.namelist = namelist;
    }

    public String getNamelist() {
        return namelist;
    }
}
