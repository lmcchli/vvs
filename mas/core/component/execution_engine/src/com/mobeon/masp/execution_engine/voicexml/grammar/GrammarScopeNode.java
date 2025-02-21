/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
   
public  class GrammarScopeNode {
    ILogger log = ILoggerFactory.getILogger(this.getClass());
    protected URI document_uri = null;
    protected Grammar grammar = null;


    protected GrammarScopeNode parent = null;
    protected List<GrammarScopeNode> children = null;


    public GrammarScopeNode(Grammar grammar, URI document_uri) {
        this.document_uri = document_uri;
        this.grammar = grammar;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public URI getDocumentURI() {
        return document_uri;
    }

    public void setDocument_uri(URI document_uri) {
        this.document_uri = document_uri;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    public GrammarScopeNode getParent() {
        return parent;
    }

    public void setParent(GrammarScopeNode parent) {
        this.parent = parent;
    }

    public void addChild(GrammarScopeNode gnode) {
        if (children == null) {
            children = new ArrayList<GrammarScopeNode>();
        }
        children.add(gnode);
    }

    /**
     * @logs.error "Application Grammar ERROR" - An internal error regarding grammar representation
     * @param node
     */
    public void setLeaf(GrammarScopeNode node) {

        if (children == null) {

            addChild(node);
            return;
        } else {
            if (children.size() != 1) { // sanity check - remove
                //if (log.isDebugEnabled()) log.debug("<CHECKOUT>Application Grammar ERROR");
                log.error("Application Grammar ERROR");

            }
            children.get(0).setLeaf(node); // recursive call
        }
    }

    public String toString() {
        String fname = "UNKNOWN";
        if (document_uri != null) {
            fname = CompilerMacros.getFileName(document_uri);
        }

        StringBuffer strbuf = new StringBuffer("Grammar [" + fname + "] children {");

        if (children != null) {

            for (Iterator<GrammarScopeNode> iterator = children.iterator(); iterator.hasNext();) {
                GrammarScopeNode node = iterator.next();
                strbuf.append(node.toString());
            }
        }
        strbuf.append(" } ");

        return strbuf.toString();
    }

    

}
