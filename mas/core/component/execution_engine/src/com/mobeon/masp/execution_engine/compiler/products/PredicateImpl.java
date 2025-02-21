/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.compiler.PromptImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mikael Andersson
 */
public class PredicateImpl extends ProductImpl implements Predicate {

    private Executable[] predicate;

    protected List<Executable> predicateList = new ArrayList<Executable>();
    private boolean predicateListFrozen;
    private String cond;
    protected final List<PromptImpl> prompts = new ArrayList<PromptImpl>();
    private String expr = null;

    public PredicateImpl(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public PredicateImpl(Product parent, DebugInfo debugInfo) {
        super(parent,debugInfo);
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        if(eval(ex)) {
            super.execute(ex);
        }
    }

    public void addPrompt(PromptImpl prompt) {
        prompts.add(prompt);
    }

    public List<PromptImpl> getPrompts() {
        return prompts;
    }    

    public boolean eval(ExecutionContext ex) {
        Boolean result = Boolean.TRUE;
        try {
            List<Executable> list = freezeAndGetPredicate();

            for(Executable e : list) {
                    e.execute(ex);
                    Value v = ex.getValueStack().pop();
                    result = (Boolean) v.accept(ex, Visitors.getAsBooleanVisitor());
                    if(!result) return false;
            }

        } catch (InterruptedException e) {
            ex.wasInterrupted();
        }
        return true;
    }

    public void addToPredicate(Executable pred) {
        predicateList.add(pred);
    }

    public synchronized List<Executable> freezeAndGetPredicate() {
        if(!predicateListFrozen) {
            predicateList = Collections.unmodifiableList(predicateList);
            predicateListFrozen = true;
        }
        return predicateList;
    }

    public void appendExtraSections(StringAccumulator buf, int indent, String lineSep) {
        super.appendExtraSections(buf,indent,lineSep);
        buf.append(lineSep);
        String section = "<predicate>";
        appendSection(buf, section, predicateList, lineSep, indent);
        buf.append(lineSep);
    }

    public Class getCanonicalClass() {
        return Predicate.class;
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getExpr() {
        return expr;
    }

}
