/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;
import com.mobeon.masp.execution_engine.compiler.Var;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Operation;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

import java.net.URI;

public class VarTest extends NodeCompilerCase {
    Var var;
     public VarTest(){
        super("VarTest", Var.class, "var");
    }

    /**
     * This test case compiles <var> including initial value,
     * and verifies that the result of the compilation is as expected.
     * @throws Exception
     */
    public void testCompileWithInitialValue() throws Exception {
        String valueOfNameAttribute = "flavor";
        String valueOfExprAttribute = "'chocolate'";

        element.addAttribute("name", valueOfNameAttribute);
        element.addAttribute("expr", valueOfExprAttribute);
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.introduceECMAVariable(valueOfNameAttribute,null),
                Ops.evaluateECMA_P(valueOfExprAttribute, new URI("a"), 1),
                Ops.assignECMAVar_T(valueOfNameAttribute));
        // No destructors should be defined
        Operation destructors[] = new Operation[0];
        validateDestructors(result,destructors);
    }

    /**
     * This test case compiles <var>, not including initial value,
     * and verifis that the result of the compilation is as expected.
     * @throws Exception
     */
    public void testCompileWithoutInitialValue() throws Exception {
        String valueOfNameAttribute = "flavor";

        element.addAttribute("name", valueOfNameAttribute);
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.introduceECMAVariable(valueOfNameAttribute,null));
        // No destructors should be defined
        Operation destructors[] = new Operation[0];
        validateDestructors(result,destructors);
    }

}