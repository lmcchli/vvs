/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.compiler.Else;
import com.mobeon.masp.execution_engine.voicexml.compiler.Script;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;


/**
 * CCXML specific {@link com.mobeon.masp.execution_engine.compiler.CompilerDispatcher} implementation.
 * <p/>
 * <b>Supported tags:</b>
 * <ul>
 * <li>&lt;ccxml&gt;</li>
 * <li>&lt;log&gt;</li>
 * <li>&lt;dialogstart&gt;</li>
 * </ul>
 * <b>Threading: </b>This class is safe to use from any number of threads, in any way.
 *
 * @author Mikael Andersson
 */
public class CCXMLCompilerDispatcher implements com.mobeon.masp.execution_engine.compiler.CompilerDispatcher {

    private static NodeCompiler nullCompiler = new NullCompiler();

    private static HashMap<String, NodeCompiler> dispatcher;
    private Text textCompiler;
    private static ILogger log = ILoggerFactory.getILogger(CCXMLCompilerDispatcher.class) ;

    /**
     * Singleton factory method.
     *
     * @return The created CompilerDispatcher instance
     */
    public synchronized static CCXMLCompilerDispatcher getInstance() {
        return new CCXMLCompilerDispatcher();
    }

    private CCXMLCompilerDispatcher() {
        dispatcher = new HashMap<String, NodeCompiler>();
        dispatcher.put("ccxml", new CCXML());
        dispatcher.put("transition", new Transition());
        dispatcher.put("eventprocessor", new EventProcessor());
        dispatcher.put("dialogstart", new DialogStart());
        dispatcher.put("accept", new Accept());
        dispatcher.put("log", new Log());
        dispatcher.put("var", new Var());
        dispatcher.put("assign", new Assign());
        dispatcher.put("if", new If());
        dispatcher.put("else",new Else());
        dispatcher.put("exit", new Exit());
        dispatcher.put("script",new Script());
        dispatcher.put("reject",new Reject());
        dispatcher.put("createcall",new CreateCall());
        dispatcher.put("send",new Send());
        dispatcher.put("disconnect",new Disconnect());
        dispatcher.put("join",new Join());
        dispatcher.put("unjoin",new Unjoin());
        dispatcher.put("cancel",new Cancel());
        dispatcher.put("proxy", new Proxy());
        dispatcher.put("redirect", new Redirect());
        textCompiler = new Text();
    }

    public NodeCompiler dispatch(String nodeName) {
        NodeCompiler nc = dispatcher.get(nodeName);
        if (nc == null) {
            nc = nullCompiler;
        }
        return nc;
    }

    public TextCompiler dispatchText() {
        return textCompiler;
    }

}
