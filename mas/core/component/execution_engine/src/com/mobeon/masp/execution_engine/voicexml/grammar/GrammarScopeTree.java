/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;
import java.net.URI;

import org.dom4j.Element;

public class GrammarScopeTree {
    private ILogger log = ILoggerFactory.getILogger(GrammarScopeTree.class);
    private GrammarScopeNode documentScopeGrammars = null;
    private GrammarScopeNode root = null;
    private GrammarScopeNode current = null;
    private Map<Element, GrammarScopeNode> grammar_node_hash = new HashMap<Element, GrammarScopeNode>();
    private static GrammarScopeNode always_match = new GrammarScopeNode(new GrammarAlwaysMatch(null ,null), null); // TODO put this singleton guy somewhere else


    public void setDocumentScopeGrammar(GrammarScopeNode gnode) {
        GrammarScopeNode new_node = null;


        if (gnode instanceof VirtualRootGrammarScopeNode) {
            new_node = new VirtualRootGrammarScopeNode(gnode.getDocumentURI());
        } else {
            new_node = new GrammarScopeNode(gnode.getGrammar(), gnode.getDocumentURI());
        }


        if (documentScopeGrammars == null) {
            new_node.setParent(null);
            documentScopeGrammars = new_node;
        } else {
            documentScopeGrammars.addChild(new_node);
            new_node.setParent(documentScopeGrammars);
            //documentScopeGrammars = new_node; //TODO :BUGG
        }
    }

    private GrammarScopeNode cloneNode(GrammarScopeNode gnode, GrammarScopeNode parent) {
        GrammarScopeNode g = new GrammarScopeNode(gnode.getGrammar(), gnode.getDocumentURI());

        g.setParent(parent);

        return g;
    }

    public GrammarScopeNode getDocumentScopeGrammars() {
        if (documentScopeGrammars == null) {
            return null;
        }

        GrammarScopeNode root = cloneNode(documentScopeGrammars, null);
        GrammarScopeNode g = documentScopeGrammars;
        GrammarScopeNode n;
        while (g.children != null) {
            g = g.children.get(0);
            n = cloneNode(g, root);
            root.addChild(n);
            root = n;
        }

        return root;
    }

    // TODO: this is the bug
    public void hangInApplicationGrammar(GrammarScopeNode app_grammar_node) {
        app_grammar_node.addChild(root); // TODO: test
        root.setParent(app_grammar_node);
        while (app_grammar_node.getParent() != null) {
            app_grammar_node = app_grammar_node.getParent();

        }
        //root.setParent(app_grammar_node);
        root = app_grammar_node;
    }

    public void setAlwaysMatchGrammar(Element element) {
        grammar_node_hash.put(element, always_match);
    }

    public GrammarScopeNode getGrammars(Element element) {
        return grammar_node_hash.get(element);
    }


    public  Map<Element, GrammarScopeNode> getGrammarNodeHash () {
        return grammar_node_hash;
    }
    
    public GrammarScopeNode getRoot() {
        return root;
    }

    public void setRoot(GrammarScopeNode root) {
        this.root = root;
    }

    public GrammarScopeNode getCurrent() {
        return this.current;
    }

    public void setCurrent(GrammarScopeNode current) {
        this.current = current;
    }

    private boolean isDocumentScope(Element parent, CompilerElement grammar) {
        if (Constants.VoiceXML.VXML.equals(parent.getName()))
            return true;
        if (Constants.VoiceXML.DOCUMENT.equals(grammar.attributeValue(Constants.VoiceXML.SCOPE))) {
            return true;
        }
        if (Constants.VoiceXML.FORM.equals(parent.getName())
                && Constants.VoiceXML.DOCUMENT.equals(parent.attributeValue(Constants.VoiceXML.SCOPE))) {
            return true;
        }

        return false;
    }


    public void createVirtualDocRootGrammar(Element parentElem, URI uri) {
        this.root = this.current = new VirtualRootGrammarScopeNode(uri); //TODO: bugg?
        grammar_node_hash.put(parentElem, this.current);
        // setDocumentScopeGrammar(this.root); // no use to set a virtualroot to be a ap grammar
    }

    public void handleGrammars(List<CompilerElement> grammarNodes, Element parentElem, URI uri) {
        if (grammarNodes == null) {
            return;
        }
        // incase of multiple grammars, handle them in reverse document order so matching will be in doc.order
        Collections.reverse(grammarNodes);
        for (CompilerElement grammar : grammarNodes) {
            // mode = dtmf?
            if (Constants.VoiceXML.DTMF.equals(grammar.attributeValue(Constants.VoiceXML.MODE))) {
                if (isDocumentScope(parentElem, grammar)) {
                    hangInGrammar(new DTMFGrammar(grammar, Grammar.SCOPE.DOCUMENT), uri, grammar.getLine());
                } else {
                    hangInGrammar(new DTMFGrammar(grammar, Grammar.SCOPE.LOCAL), uri, grammar.getLine());
                }
            } else { // mode is voice since it is deafult
                if (isDocumentScope(parentElem, grammar)) {
                    hangInGrammar(new ASRGrammar(grammar, Grammar.SCOPE.DOCUMENT), uri, grammar.getLine());
                } else {
                    hangInGrammar(new ASRGrammar(grammar, Grammar.SCOPE.LOCAL), uri, grammar.getLine());
                }
            }
        }

        grammar_node_hash.put(parentElem, this.current);
    }

    public void hangInGrammar(Grammar grammar, URI uri, int line_no) {
        GrammarScopeNode node = new GrammarScopeNode(grammar, uri);
        grammar.setGrammar_id(uri.toString()+ "?" + Integer.toString(line_no));
        hangin(grammar, node);
    }


    private void hangin(Grammar grammar, GrammarScopeNode node) {
        // if grammar is in doc scope, hang it to that list
        if (grammar.getScope() == Grammar.SCOPE.DOCUMENT) {
            setDocumentScopeGrammar(node);
        }
        if (this.current == null) { // we have a brand new tree
            this.current = root = node;
            return;
        }

        this.current.addChild(node);
        node.setParent(this.current);
        this.current = node;
        return;
    }

    public String toString() {
        if (root != null)
            return root.toString();
        return "";
    }

}

