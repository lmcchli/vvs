/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;

import java.net.URI;

public class VirtualRootGrammarScopeNode extends GrammarScopeNode {
    public VirtualRootGrammarScopeNode(URI document_uri) {
        super(null, document_uri);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("Virtual Root Grammar Scope Node [" +
                CompilerMacros.getFileName(getDocumentURI()) + "] children {");

        if (children != null) {
            for (GrammarScopeNode c : children) {
                buf.append(c.toString());
            }
        }
        buf.append("}");

        return buf.toString();

    }
}
