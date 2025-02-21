/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import org.dom4j.Text;


/**
 * @author Mikael Andersson
 */
public abstract class TextCompilerBase implements TextCompiler {

    public abstract void compile(Module module, Product parent, Text text);

}
