/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;

import org.dom4j.Node;

/**
 * @author Mikael Andersson
 */
public class NullCompiler extends NodeCompilerBase {

    private static ILogger log = ILoggerFactory.getILogger(NullCompiler.class) ;

    /**
     * @logs.error <URI> line <line>: Unrecognized tag name <tag> encountered. Entire contents will be ignored." - The tag <tag> on line <line> was encountered during compilation of <URI>, but it is not recognized as a supported tag.
     * @param app
     * @param compilerPass
     * @param parent
     * @param element
     * @param content
     * @return
     */
    public Product compile(Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        log.error( app.getDocumentURI() + " line " + element.getLine() + ": " +
                "Unrecognized tag name " + element.getName() +
                    " encountered. Entire contents will be ignored.");
        return null;
    }
}
