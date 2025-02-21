package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;

/**
 * @author Mikael Andersson
 */


public class BinaryExecutable extends ProductSupport implements EngineCallable {
    private static final ILogger log = ILoggerFactory.getILogger(BinaryExecutable.class);
    private String condition;
    private List<Executable> predicateList = new ArrayList<Executable>();
    private EngineCallable trueCallable;
    private EngineCallable falseCallable;

    public BinaryExecutable(String localName, DebugInfo info) {
        super(info, localName);
    }

    public void setCondition(String condition, URI uri, int lineNumber) {
        this.condition = condition;
        // Make sure we clear the PredicateList of old stuff
        predicateList = new ArrayList<Executable>();
        if (condition != null) {
            predicateList.add(Ops.evaluateECMA_P(condition, uri, lineNumber));
        }
    }

    public void appendExtraSections(
            StringAccumulator sa, int count, String lineSep) {
        sa.append(lineSep);
        appendSection(sa, "<predicate>", predicateList, lineSep, count);
        sa.append(lineSep);
        if(trueCallable != null) trueCallable.appendExtraSections(sa, count, lineSep);
        if(falseCallable != null) falseCallable.appendExtraSections(sa, count, lineSep);
    }

    public String getCondition() {
        return condition;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        // Execute either trueOperationList or falseOperationList depending
        // on the condition. condition == null means true.

        if (eval(ex)) {
            if (log.isDebugEnabled()) log.debug("Evaluating condition: " + condition + " was true");
            if (trueCallable != null) trueCallable.execute(ex);
        } else {
            if (log.isDebugEnabled()) log.debug("Evaluating condition: " + condition + " was false");
            if (falseCallable != null) falseCallable.execute(ex);
        }
    }


    public boolean eval(ExecutionContext ex) {
        if (predicateList.isEmpty()) {
            return true;
        }
        try {
            ex.executeAtomic(predicateList);
            Value v = ex.getValueStack().pop();
            if (v == null) {
                // Nothing on the stack! Some earlier evaluation failed!
                return false;
            }
            return (Boolean) v.accept(ex, Visitors.getAsBooleanVisitor());
        } catch (InterruptedException e) {
            ex.wasInterrupted();
        }
        return false;
    }

    public void addToPredicate(Executable executable) {
        predicateList.add(executable);

    }

    public Class<?> getCanonicalClass() {
        return BinaryExecutable.class;
    }

    public EngineCallable getFalseCallable() {
        return falseCallable;
    }

    public void setFalseCallable(EngineCallable falseCallable) {
        this.falseCallable = falseCallable;
    }

    public EngineCallable getTrueCallable() {
        return trueCallable;
    }

    public void setTrueCallable(EngineCallable trueCallable) {
        this.trueCallable = trueCallable;
    }
}


