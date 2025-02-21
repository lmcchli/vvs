/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mikael Andersson
 */
public class AtomicExecutable extends ExecutableBase {

    protected List<Operation> operationList = new ArrayList<Operation>();

    //This constructor is sooo braindamaged that i can't even
    //start to descripe it. But it's all because of how fantastically
    //perfectly incomprehenibly unusable generics is implemented in Java
    public AtomicExecutable(Operation ... ops) {
        for(int i=0;i<ops.length;i++) {
            operationList.add(ops[i]);
        }
        operationList =  Collections.unmodifiableList(operationList);
    }


    public void execute(ExecutionContext ex) throws InterruptedException {
        ex.executeAtomic(operationList);
    }

    public List<? extends Executable> getOperations() {
        return operationList;
    }

    public void appendMnemonic(
    StringAccumulator sb, int indent, boolean recurse, String lineSep, String entrySep) {
        indent(sb, indent);
        appendMnemmonicPrologue(sb,"Atomic", null, null,null);
        sb.append("{ ");
        appendMnemonics(sb, operationList, indent, true, lineSep, entrySep);
        sb.append(" }");
    }

    public void freeze() {
        //TODO: could freeze be implemeted ni Exec.Base?

    }


}
