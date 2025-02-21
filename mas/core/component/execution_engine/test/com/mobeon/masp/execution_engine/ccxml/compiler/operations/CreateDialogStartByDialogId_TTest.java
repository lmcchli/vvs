package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.runtime.values.TextValue;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CreateDialogStartByDialogId_T Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/06/2005</pre>
 */
public class CreateDialogStartByDialogId_TTest extends CCXMLOperationCase
{

    public CreateDialogStartByDialogId_TTest(String name)
    {
        super(name);
    }

    /**
     * Validates that given a previously allocated dialogId, a valid
     * DialogStartEvent is placed on the stack.
     *
     * @throws Exception
     */
    public void testExecute() throws Exception {
        String dialogId = "12345";

        TextValue onStack = new TextValue(dialogId);
        expect_ValueStack_pop(onStack);
        DialogStartEvent event = createDialogStartEvent();
        expect_ExecutionContext_fetchDialogEvent(event, dialogId);
        expect_ValueStack_push(event);
        op = new CreateDialogStartByDialogId_T();
        op.execute(getExecutionContext());
    }

    public void testToMnemonic() {
        op = new CreateDialogStartByDialogId_T();
        validateMnemonic("CreateDialogStartByDialogId_T()");
    }

    public static Test suite()
    {
        return new TestSuite(CreateDialogStartByDialogId_TTest.class);
    }
}
