/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Filled extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        String mode = element.attributeValue(Constants.VoiceXML.MODE);
        if (mode == null) {
            mode = Constants.VoiceXML.ALL;
        }
        String namelist = element.attributeValue(Constants.VoiceXML.NAMELIST);
        // Check if the parent form item has been filled, otherwise rewind and run the parent again.

        FilledPredicateImpl filled = new FilledPredicateImpl(parent, null, DebugInfo.getInstance(element));
        filled.add(Ops.logElement(element));

        if ( element.getTagType().isLeafTypeOf(Constants.VoiceXML.FORM)) {
            filled.setNamelist(namelist);
            FormPredicate realParent = (FormPredicate) parent.getParent();
            // We should only execute the filled if all needed items have been filled
            filled.addToPredicate(new AtomicExecutable(Ops.areAllNeededFormItemsDone_P(realParent, namelist, mode))); 
        } else {
            namelist = parent.getName();
            filled.setName(namelist);
            filled.addToPredicate(new AtomicExecutable(Ops.areAllNeededFormItemsDone_P(null, namelist, mode)));
                                                     //  Ops.not_TP(),
                                                     //  Ops.unwindOrContinue_TP(parent)));
        }


        CompilerMacros.addScope(filled,null);
        compileChildren(module, compilerPass, parent,filled, element.content());
        return filled;

    }


}
