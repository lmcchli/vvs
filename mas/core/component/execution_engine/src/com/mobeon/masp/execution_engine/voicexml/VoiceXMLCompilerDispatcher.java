package com.mobeon.masp.execution_engine.voicexml;

import com.mobeon.masp.execution_engine.ccxml.compiler.Text;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.compiler.Record;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mikael Andersson
 */
public class VoiceXMLCompilerDispatcher implements CompilerDispatcher {


    private static final int INITIAL_CAPACITY = 40;
    private static Map<String, NodeCompiler> dispatcher = new HashMap<String, NodeCompiler>(INITIAL_CAPACITY);
    private TextCompiler textCompiler;
    private static NodeCompiler nullCompiler = new NullCompiler();
    private static ILogger log = ILoggerFactory.getILogger(VoiceXMLCompilerDispatcher.class);

    public static CompilerDispatcher getInstance() {
        return new VoiceXMLCompilerDispatcher();
    }

    private VoiceXMLCompilerDispatcher() {
        dispatcher.put("vxml", new VXML());
        dispatcher.put("assign", new Assign());
        dispatcher.put("block", new Block());
        dispatcher.put("form", new Form());
        dispatcher.put("log", new Log());
        dispatcher.put("prompt", new Prompt());
        dispatcher.put("var", new Var());
        dispatcher.put("if", new If());
        dispatcher.put("audio", new Audio());
        dispatcher.put("field", new Field());
        dispatcher.put("filled", new Filled());
        dispatcher.put("initial", new Initial());
        dispatcher.put("record", new Record());
        dispatcher.put("script", new Script());
        dispatcher.put("reprompt", new Reprompt());
        dispatcher.put("catch", new Catch());
        dispatcher.put("transfer", new Transfer());

        // kese commented this out to force a warning for unimplemented stuff:
        //dispatcher.put("choice", new Choice());
        //dispatcher.put("link", new Link());

        dispatcher.put("exit", new Exit());
        dispatcher.put("object", new VXMLObject());
        dispatcher.put("mark", new Mark());
        dispatcher.put("throw", new Throw());
        dispatcher.put("goto", new Goto());
        dispatcher.put("value", new Value());
        dispatcher.put("subdialog", new Subdialog());
        dispatcher.put("param", new Param());
        dispatcher.put("return", new Return());
        dispatcher.put("property", new Property());
        dispatcher.put("disconnect", new Disconnect());
        dispatcher.put("clear", new Clear());


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
