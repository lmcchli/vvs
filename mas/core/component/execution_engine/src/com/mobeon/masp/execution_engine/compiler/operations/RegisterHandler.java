/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.Selector;

/**
 * @author Mikael Andersson
 */
public class RegisterHandler extends OperationBase {
    final private String[] states;
    final private Selector sel;
    final private Predicate predicate;

    public RegisterHandler(String[] states, Selector sel, Predicate predicate) {

        this.states = states;
        this.sel = sel;
        this.predicate = predicate;
    }

    public String arguments() {
        StringBuffer buf = new StringBuffer();
        if (states != null)
            buf.append(arrayToString(states));
        else
            buf.append("[ ... ]");
        buf.append(", ");
        buf.append(sel);
        buf.append(", ");
        buf.append(predicate.identity());
        return buf.toString();
    }

    public void appendMnemonic(
    StringAccumulator sa, int indent, boolean recurse, String lineSep, String entrySep) {
        String mnemonic = classToMnemonic(getClass());

        indent(sa,indent);
        sa.append(mnemonic);
        sa.append("(");
        sa.append(arguments());
        sa.append(")");
        appendMnemonics(sa,predicate.freezeAndGetExecutables(),indent,false,"",entrySep);
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ex.registerHandler(states, sel, predicate);
    }
}
