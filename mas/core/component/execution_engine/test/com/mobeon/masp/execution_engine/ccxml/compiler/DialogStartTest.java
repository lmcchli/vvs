/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Constants;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URI;

/**
 * DialogStart Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class DialogStartTest extends NodeCompilerCase {

    public DialogStartTest(String name) {
        super(name, DialogStart.class, "dialogstart");
    }

    public void testCompileWithConnectionId() throws Exception {
        element.addAttribute(Constants.CCXML.PREPARED_DIALOG_ID, "myId");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.evaluateECMA_P("myId",new URI("a"), 1),
                Ops.createDialogStartByDialogID_T(),
                Ops.sendEvent_T());
    }

    public void testCompileWithSrc() throws Exception {
        element.addAttribute(Constants.CCXML.SRC, "'srcParam'");
        element.addAttribute(Constants.CCXML.NAMELIST, "name1 name2 name3");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.evaluateECMA_P("'srcParam'", new URI("a"), 1),
                Ops.text_P("application/xml+vxml"),
                Ops.textArray_P("name1", "name2", "name3"),
                Ops.text_P(null),
                Ops.createDialogStartBySrcTypeNamelist_T4P(),
                Ops.sendEvent_T());
    }

    public void testCompileWithSrc2() throws Exception {
        element.addAttribute(Constants.CCXML.SRC, "'srcParam'");
        element.addAttribute(Constants.CCXML.TYPE, "'application/custom'");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.evaluateECMA_P("'srcParam'", new URI("a"), 1),
                Ops.evaluateECMA_P("'application/custom'", new URI("a"),1),
                Ops.textArray_P(),
                Ops.text_P(null),
                Ops.createDialogStartBySrcTypeNamelist_T4P(),
                Ops.sendEvent_T());
    }


    public void testCompileWithConflictingAttributes() throws Exception {
        element.addAttribute(Constants.CCXML.PREPARED_DIALOG_ID, "myId");
        element.addAttribute(Constants.CCXML.SRC, "'srcParam'");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.sendEvent(Constants.Event.ERROR_SEMANTIC, "", null));
    }

    public static Test suite() {
        return new TestSuite(DialogStartTest.class);
    }
}
