/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.Module;

import java.util.List;

import org.dom4j.Node;

/**
* @author Mikael Andersson
 */
public interface NodeCompiler {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content);
}
