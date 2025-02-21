/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.values.TextValue;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ConnectionAccept_TTest extends CCXMLOperationCase {

    public ConnectionAccept_TTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ConnectionAccept_TTest.class);
    }

    /**
     * Validate that a connection id is popped from the stack and
     * that the Connection.accept gets called.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {

        String idStr = "0";

        expect_ConnectionManager_findConnection(idStr, createConnection());
        expect_Connection_accept();
        expect_ValueStack_popAsString(idStr);

        op = new ConnectionAccept_T();
        op.execute(getExecutionContext());
    }


    public void testToMnemonic() {
        op = new ConnectionAccept_T();
        validateMnemonic("ConnectionAccept_T()");
    }
}