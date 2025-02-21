/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import junit.framework.Test;
import junit.framework.TestSuite;

public class EventVar_PTest extends CCXMLOperationCase {

    public static Test suite() {
        return new TestSuite(EventVar_PTest.class);
    }

    public EventVar_PTest(String name) {
        super(name);
    }

    /**
     * Validates that the current event var is retrieved
     * and pushed onto the value stack.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {
        CCXMLEvent event = createEvent(getExecutionContext());
        expect_ExecutionContext_getEventVar(event);
        expect_ValueStack_push(event);

        op = new EventVar_P();
        op.execute(getExecutionContext());
    }

    public void testToMnemonic() throws Exception {
        op = new EventVar_P();
        validateMnemonic("EventVar_P()");
    }
}