/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.operations.EvaluateECMA_P;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;

/**
 * Test the Block compiler
 */
public class BlockTest extends NodeCompilerCase {
    private static final ILogger logger = ILoggerFactory.getILogger(BlockTest.class);

    private static final String simple = "<block>I am a string in a block element" +
            "<log>I am a string in a log element</log></block>";
    private static final String named = "<block name='theName'>I am a string in a block element" +
            "<log>I am a string in a log element</log></block>";
    private static final String with_cond_true = "<block cond='true'>I am a string in a block element" +
            "<log>I am a string in a log element</log></block>";
    private static final String with_cond_false = "<block cond='false'>I am a string in a block element" +
            "<log>I am a string in a log element</log></block>";


    public BlockTest() {
        super("block", Block.class, "block");

    }

    public void setUp() throws Exception {
        super.setUp();
        setCompilerPasses(Compiler.CCXML_PASSES);
    }

    public void testCompileNoAttribs() throws Exception {
        compileDocument(simple);
        Product product = compile();
        ProductImpl child = new ProductImpl(product, null, null);
        child.add(Ops.createLogMessage("I am a string in a log element"));

        validateResultAndParent(product, parent);
        validateOperations(product,
                Ops.newScope(""),
                Ops.text_P("I am a string in a block element"),
                child);

    }

    public void testCompileNamed() throws Exception {
        //element.addAttribute("name", "theName");
        compileDocument(named);
        // TODO: get gotoable product not implemented yet. Test it when it os
        Product product = compile();
        ProductImpl child = new ProductImpl(product, null, null);
        child.add(Ops.createLogMessage("I am a string in a log element"));
        validateResultAndParent(product, parent);
        validateOperations(product,
                Ops.newScope(""),
                Ops.text_P("I am a string in a block element"),
                child);
    }

    public void testCompileCond() throws Exception {

        compileDocument(with_cond_true);
        Product product = compile();
        ProductImpl child = new ProductImpl(product, null, null);
        child.add(Ops.createLogMessage("I am a string in a log element"));
        validateResultAndParent(product, parent);
        validateOperations(product,
                Ops.newScope(""),
                Ops.text_P("I am a string in a block element"),
                child);

        // check predicate
        assertTrue(product instanceof PredicateImpl);
        PredicateImpl predicate = (PredicateImpl) product;
        List<Executable> list = predicate.freezeAndGetPredicate();
        assertTrue(list.get(0) instanceof EvaluateECMA_P);
        logger.debug(((EvaluateECMA_P) list.get(0)).toMnemonic());

        //checkTree(product);
    }


    public void testCompileExpr() throws Exception {
        element.addAttribute(Constants.VoiceXML.EXPR, "47+11");
        Product product = compile();
        //checkTree(product);
        ProductImpl child = new ProductImpl(product, null, null);
        child.add(Ops.createLogMessage("I am a string in a log element"));
        validateResultAndParent(product, parent);
        validateOperations(product,
                Ops.newScope(""),
                Ops.text_P("I am a string in a block element"),
                child);

        // check predicate
        assertTrue(product instanceof PredicateImpl);
        PredicateImpl predicate = (PredicateImpl) product;
        List<Executable> list = predicate.freezeAndGetPredicate();

        assertTrue(list.get(0) instanceof AtomicExecutable);
        AtomicExecutable atomic = (AtomicExecutable) list.get(0);
        List<? extends Executable> list2 = atomic.getOperations();
        assertTrue(list2.get(0) instanceof EvaluateECMA_P);
//        assertTrue(list2.get(1) instanceof IsUndef_TP);


    }

    private void compileDocument(String vxml) {
        element = readDocument(vxml);
    }

}