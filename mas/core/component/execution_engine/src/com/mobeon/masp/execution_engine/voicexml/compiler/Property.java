/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.List;

public class Property extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass cd, Product parent, CompilerElement element, List content) {

        String name = element.attributeValue(Constants.VoiceXML.NAME);
        String value = element.attributeValue(Constants.VoiceXML.VALUE);
        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));
        
        if(name != null &&  !name.equals("") &&  value != null && !value.equals("")) {
            product.add(Ops.setProperty(name, value));
        }
        parent.add(product);
        return product;
    }
}
