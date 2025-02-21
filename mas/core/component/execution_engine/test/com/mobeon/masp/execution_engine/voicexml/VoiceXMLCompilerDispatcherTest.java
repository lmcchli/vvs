/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.compiler.Assign;
import com.mobeon.masp.execution_engine.compiler.CompilerDispatcher;
import com.mobeon.masp.execution_engine.compiler.NodeCompiler;
import com.mobeon.masp.execution_engine.compiler.Var;
import com.mobeon.masp.execution_engine.voicexml.compiler.*;


public class VoiceXMLCompilerDispatcherTest extends Case {
    VoiceXMLCompilerDispatcher compilerDispatcher;

    public VoiceXMLCompilerDispatcherTest(String name) {
        super(name);
    }

    public void testDispatch() throws Exception {
        NodeCompiler nodeCompiler;

        CompilerDispatcher cd = VoiceXMLCompilerDispatcher.getInstance();

        nodeCompiler = cd.dispatch("vxml");
        if (!(nodeCompiler instanceof VXML)) {
            die("CompilerDispatcher does not dispatch a VXML compiler");
            return;
        }
        nodeCompiler = cd.dispatch("assign");
        if (!(nodeCompiler instanceof Assign)) {
            die("CompilerDispatcher does not dispatch a Assign compiler");
            return;
        }
        nodeCompiler = cd.dispatch("block");
        if (!(nodeCompiler instanceof Block)) {
            die("CompilerDispatcher does not dispatch a Block compiler");
            return;
        }
        nodeCompiler = cd.dispatch("form");
        if (!(nodeCompiler instanceof Form)) {
            die("CompilerDispatcher does not dispatch a Form compiler");
            return;
        }
        nodeCompiler = cd.dispatch("log");
        if (!(nodeCompiler instanceof Log)) {
            die("CompilerDispatcher does not dispatch a Log compiler");
            return;
        }
        nodeCompiler = cd.dispatch("prompt");
        if (!(nodeCompiler instanceof Prompt)) {
            die("CompilerDispatcher does not dispatch a Prompt compiler");
            return;
        }
        nodeCompiler = cd.dispatch("var");
        if (!(nodeCompiler instanceof Var)) {
            die("CompilerDispatcher does not dispatch a Var compiler");
            return;
        }
    }
}