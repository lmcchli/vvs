/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;
import junit.framework.*;
import com.mobeon.masp.execution_engine.ccxml.compiler.CCXMLCompilerDispatcher;
import com.mobeon.masp.execution_engine.compiler.NodeCompiler;
import com.mobeon.masp.execution_engine.compiler.NullCompiler;
import com.mobeon.masp.execution_engine.Case;


/**
 * @author Mikael Andersson
 */
public class CCXMLCompilerDispatcherTest extends Case {
    CCXMLCompilerDispatcher compilerDispatcher;

    public CCXMLCompilerDispatcherTest(String name) {
        super(name);
    }

    public void testDispatch() throws Exception {
        NodeCompiler nodeCompiler;

        CCXMLCompilerDispatcher cd = CCXMLCompilerDispatcher.getInstance();

        nodeCompiler = cd.dispatch("ccxml");
        if (!(nodeCompiler instanceof CCXML)) {
            die("CompilerDispatcher does not dispatch a CCXML compiler");
            return;
        }
        nodeCompiler = cd.dispatch("log");
        if (!(nodeCompiler instanceof Log)) {
            die("CompilerDispatcher does not dispatch a Log compiler");
            return;
        }
        nodeCompiler = cd.dispatch("dialogstart");
        if (!(nodeCompiler instanceof DialogStart)) {
            die("CompilerDispatcher does not dispatch a DialogStart compiler");
            return;
        }
        nodeCompiler = cd.dispatch("proxy");
        if (!(nodeCompiler instanceof Proxy)) {
            die("CompilerDispatcher does not dispatch a NullCompiler for null elements");
            return;
        }

        nodeCompiler = cd.dispatch("nonexistant-ccxml-node");
        if (!(nodeCompiler instanceof NullCompiler)) {
            die("CompilerDispatcher does not dispatch a NullCompiler for nonexistant nodes");
            return;
        }
        nodeCompiler = cd.dispatch(null);
        if (!(nodeCompiler instanceof NullCompiler)) {
            die("CompilerDispatcher does not dispatch a NullCompiler for null elements");
            return;
        }
    }
}