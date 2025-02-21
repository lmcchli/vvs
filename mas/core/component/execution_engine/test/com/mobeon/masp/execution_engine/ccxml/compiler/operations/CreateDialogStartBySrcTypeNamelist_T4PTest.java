/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.ConnectionImpl;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.values.TextArrayValue;
import com.mobeon.masp.execution_engine.runtime.values.TextValue;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.builder.NameMatchBuilder;
import org.jmock.Mock;

/**
 * @author Mikael Anderssons
 */
public class CreateDialogStartBySrcTypeNamelist_T4PTest extends CCXMLOperationCase {

    public CreateDialogStartBySrcTypeNamelist_T4PTest(String name) {
        super(name);
    }
    public static class ConnectionStub extends ConnectionImpl {
        public ConnectionStub(ExecutionContext executionContext) {
            super(executionContext);
        }

    }

    public static class DialogStub extends Dialog {
        public DialogStub(Id<BridgeParty> id, String src, String mimeType, CCXMLExecutionContext originatingContext) {
            super(id, src, mimeType, originatingContext);
        }

    }

    public static Test suite() {
        return new TestSuite(CreateDialogStartBySrcTypeNamelist_T4PTest.class);
    }

    /**
     * Validates execution with state names supplied.
     * The end result is that a valid DialogStartEvent is
     * placed on the stack.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {
        String src = "http://test.com/src.vxml";
        String name1 = "name1";
        String name2 = "name2";
        String name3 = "name3";
        IdGeneratorImpl.IdImpl<BridgeParty> id = new IdGeneratorImpl.IdImpl<BridgeParty>(IdGeneratorImpl.PARTY_GENERATOR, 2);

        CCXMLEvent event = createEvent(getExecutionContext());

        expect_ExecutionContext_getEventVar(event);

        expect_ValueStack_pop(new TextValue(src));
        expect_ValueStack_pop(new TextValue(Constants.MimeType.VOICEXML_MIMETYPE));
        expect_ValueStack_pop(new TextArrayValue(name1,name2,name3));

        expect_ScopeContext_getValue(name1,name1);
        expect_ScopeContext_getValue(name2,name2);
        expect_ScopeContext_getValue(name3,name3);

        IdGeneratorImpl.PARTY_GENERATOR.reset();
        ConnectionStub conn = new ConnectionStub(getExecutionContext());
        DialogStartEvent dse = createDialogStartEvent(id, src, Constants.MimeType.VOICEXML_MIMETYPE,getExecutionContext(),conn);

        Dialog dialog = dse.getDialog();
        dialog.addParameter(name1,name1);
        dialog.addParameter(name2,name2);
        dialog.addParameter(name3,name3);
        expect_ValueStack_push(dse);
        expect_ValueStack_popAsString(conn.getBridgePartyId());
        IdGeneratorImpl.IdImpl<BridgeParty> idOfConnection = new IdGeneratorImpl.IdImpl<BridgeParty>(IdGeneratorImpl.PARTY_GENERATOR, 1);

        expect_ConnectionManager_findConnection(idOfConnection.toString(), conn);
        expect_ConnectionManager_createDialog(new DialogStub(id, src, Constants.MimeType.VOICEXML_MIMETYPE, getExecutionContext()));

        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(getCurrentScope()));


        op = new CreateDialogStartBySrcTypeNamelist_T4P();
        op.execute(getExecutionContext());
    }

    protected void receive_ExecutionContext_addDialog(NameMatchBuilder self) {
        self.method("addDialog").with(not(eq(null)));
    }

    protected void expect_ExecutionContext_addDialog() {
        receive_ExecutionContext_addDialog(mockExecutionContext.expects(once()));
    }


    /**
     * Validates execution when no state names are supplied.
     * The end result is that a valid DialogStartEvent is
     * placed on the stack.
     *
     * @throws Exception
     */
    public void testExecuteNoNames() throws Exception {
        String src = "http://test.com/src.vxml";
        IdGeneratorImpl.IdImpl<BridgeParty> id = new IdGeneratorImpl.IdImpl<BridgeParty>(IdGeneratorImpl.PARTY_GENERATOR, 3);

        CCXMLEvent event = createEvent(getExecutionContext());

        expect_ExecutionContext_getEventVar(event);
        expect_ValueStack_pop(new TextValue(src));
        expect_ValueStack_pop(new TextValue(Constants.MimeType.VOICEXML_MIMETYPE));
        expect_ValueStack_pop(new TextArrayValue());
        expect_ValueStack_popAsString(null);


        IdGeneratorImpl.PARTY_GENERATOR.reset();
        DialogStartEvent dse = createDialogStartEvent(id, src, Constants.MimeType.VOICEXML_MIMETYPE,getExecutionContext(),new ConnectionStub(getExecutionContext()));
        expect_ValueStack_push(dse);

        Dialog dialog = new Dialog(id, src, Constants.MimeType.VOICEXML_MIMETYPE, getExecutionContext());

        mockEventSourceManager.expects(once()).method("createDialog").with(eq(src), eq(Constants.MimeType.VOICEXML_MIMETYPE)).will(returnValue(dialog));
//        expect_ConnectionManager_createDialog(new DialogStub(id, src, Constants.MimeType.VOICEXML_MIMETYPE, getExecutionContext()));

        op = new CreateDialogStartBySrcTypeNamelist_T4P();
        op.execute(getExecutionContext());
    }

    public void testToMnemonic() {
        op = new CreateDialogStartBySrcTypeNamelist_T4P();
        validateMnemonic("CreateDialogStartBySrcTypeNamelist_T4P()");
    }
}