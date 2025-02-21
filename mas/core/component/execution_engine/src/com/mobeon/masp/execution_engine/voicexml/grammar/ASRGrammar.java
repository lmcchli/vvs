/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.compiler.Constants;

import java.net.URI;

import org.dom4j.Node;

public class ASRGrammar extends Grammar {
    private CompilerElement srgs_grammar;

    public ASRGrammar(CompilerElement node, Grammar.SCOPE scope) {
        super(InputMode.VOICE, scope);
        
        this.srgs_grammar = node;
    }

    /**
     * Returns the SRGS content surrounded with what ever stuff needed by ASR enginge
     * @return the srgs content
     */               
    public String getSRGSContent() {
        // TODO: Append and prepend proper xml stuff for ASR engine
        String srgs = this.srgs_grammar.asXML();
        // Removing the namespace ...
        srgs = srgs.replaceFirst("xmlns=\"[:.a-zA-Z0-9 /]*\"", "");
        return srgs;
    }

}
