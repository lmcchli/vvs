/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.voicexml.compiler.Prompt;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;

public class PromptTest extends NodeCompilerCase {
    Prompt prompt;

    public PromptTest() {
        super("Prompt", Prompt.class, "prompt");
        commonSetup();
    }

    private void commonSetup() {
        setCompilerPasses(Compiler.VXML_PASSES);
    }


    /**
     * Test compilation of the simples form of a prompt
     * @throws Exception
     */
    public void testSimpleCompile() throws Exception {
        String form = "<prompt>Hello World</prompt>";
        element = readDocument(form);
        Product result = compile();
        String mnemonic = result.toMnemonic();
        System.out.println(mnemonic);
        assertTrue(result instanceof PredicateImpl);
        assertTrue(mnemonic.equals("Predicate([])[\n  Text_P('Hello World')\n]"));
    }

    /**
     * Test compilation of a prompt with a condition
     * @throws Exception
     */
    public void testCondCompile() throws Exception {
        String form = "<prompt cond='1>0'>Hello World</prompt>";
        element = readDocument(form);
        Product result = compile();
        String mnemonic = result.toMnemonic();
        System.out.println(mnemonic);
        assertTrue(result instanceof PredicateImpl);
        assertTrue(mnemonic.equals("Predicate([EvaluateECMA_P('1>0')])[\n  Text_P('Hello World')\n]"));
    }

    /**
     * Test compilation of a prompt, verifying the bargein attribute for handling of correct and
     * errorous values.
     *
     * @throws Exception
     */
    public void testBargeinCompile() throws Exception {
        String form = "<prompt bargein='true'>Hello World</prompt>";
        element = readDocument(form);
        Product result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 0);
        }
        commonSetup();
        form = "<prompt bargein='false'>Hello World</prompt>";
        element = readDocument(form);
        result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 0);
        }

        commonSetup();
        form = "<prompt bargein='errorous_value'>Hello World</prompt>";
        element = readDocument(form);
        result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 1);
        }
    }


    /**
     * Test compilation of a prompt, verifying the bargeintype attribute
     *
     * @throws Exception
     */
    public void testBargeintypeCompile() throws Exception {
        String form = "<prompt bargein='true' bargeintype='speech'>Hello World</prompt>";
        element = readDocument(form);
        Product result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 0);
        }

        commonSetup();
        form = "<prompt bargein='true' bargeintype='hotword'>Hello World</prompt>";
        element = readDocument(form);
        result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 0);
        }

        commonSetup();
        form = "<prompt bargein='true' bargeintype='errorous_value'>Hello World</prompt>";
        element = readDocument(form);
        result = compile();
        if (result instanceof PredicateImpl) {
            PredicateImpl product = (PredicateImpl) result;
            assertTrue(module.getCompilationEvents().size() == 1);
        }
    }

}