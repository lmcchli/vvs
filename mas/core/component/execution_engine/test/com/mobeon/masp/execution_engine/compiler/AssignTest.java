/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URI;

public class AssignTest extends NodeCompilerCase {
    Assign assign;

    public AssignTest(String name){
        super(name, Assign.class, "assign");
    }

    /**
     * Simple test case compiling <assign>
     * @throws Exception
     */
    public void testCompile() throws Exception {
        String valueOfNameAttribute = "flavor";
        String valueOfExprAttribute = "'chocolate'";

        element.addAttribute("name", valueOfNameAttribute);
        element.addAttribute("expr", valueOfExprAttribute);
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.evaluateECMA_P(valueOfExprAttribute,new URI("a"),1),
                Ops.assignECMAVar_T(valueOfNameAttribute));
        // No destructors should be defined
        Operation destructors[] = new Operation[0];
        validateDestructors(result,destructors);
    }

    public static Test suite() {
        return new TestSuite(AssignTest.class);
    }
}