/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.dummies.ExecutionContextDummy;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistryImpl;
import com.mobeon.masp.execution_engine.runtime.values.EventValue;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import junit.framework.Test;
import junit.framework.TestSuite;

public class StoreDialogId_TPTest extends CCXMLOperationCase {

    public static Test suite() {
        return new TestSuite(StoreDialogId_TPTest.class);
    }

    public StoreDialogId_TPTest(String name) {
        super(name);
    }

    /**
     * Validates that the id of the dialog start event on the
     * stack, is placed in the ECMA variable named by the
     * StoreDialogId operation.
     * It's worth noting that it never actually removes anything
     * from the stack, it only examines what's present.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {
        String id = "id";

        DialogStartEvent dse = createDialogStartEvent();
        EventValue onStack = new EventValue(dse);
        expect_ValueStack_peek(onStack);

        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        final Scope sc = scopeRegistry.createNewScope("thescope");
        sc.evaluate("var id;");

        op = new StoreDialogId_TP(id);

        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(sc));
        op.execute(getExecutionContext());
        if(!sc.evaluate(id).equals(dse.getDialogId().toString()))
            die("Expected an id variable containing "+dse.getDialogId()+" but found "+sc.evaluate(id));

    }


    public void testToMnemonic() throws Exception {
        op = new StoreDialogId_TP("id");
        validateMnemonic("StoreDialogId_TP('id')");
    }
}