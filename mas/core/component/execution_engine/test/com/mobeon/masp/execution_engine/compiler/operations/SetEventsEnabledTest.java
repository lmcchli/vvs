/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.compiler.operations.CCXMLOperationCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SetEventsEnabledTest extends CCXMLOperationCase {

    public static Test suite() {
        return new TestSuite(SetEventsEnabledTest.class);
    }

    public SetEventsEnabledTest(String name) {
        super(name);
    }

    /**
     * Validates that invocation results in that the EventProcessor
     * enters the enabled state.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {

        mockExecutionContext.stubs().method("getEventProcessor").will(returnValue(mockEventProcessor.proxy()));

        expect_EventProcessor_isEnabled(false);
        expect_EventProcessor_setEnabled(true);

        op = new SetEventsEnabled(true);
        op.execute(getExecutionContext());

    }

    public void testToMnemonic() throws Exception {
        op = new SetEventsEnabled(true);
        validateMnemonic("SetEventsEnabled(true)");
    }
}