/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Clear extends NodeCompilerBase {
    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        Product clear = createProduct(parent, element);
        clear.add(Ops.logElement(element));

        clear.setTagType(Constants.VoiceXML.CLEAR);

        String namelist = element.attributeValue(Constants.VoiceXML.NAMELIST);

        // Send a connection.disconnect.hangup event
        clear.add(Ops.clearFormItems(namelist));

        // The <clear> tag has no children, hence, it is only placed as a child to its parent without
        // recursion

        parent.add(clear);


        return clear;
    }
}
