/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.vxml.VXML;
import com.mobeon.application.vxml.Transformer;
import com.mobeon.application.vxml.LateBinder;
import com.mobeon.application.BuildGraph;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;
import com.mobeon.executor.Traverser;
import com.mobeon.executor.DocumentManager;
import com.mobeon.util.ErrorCodes;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import org.w3.x2001.vxml.VxmlDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.log4j.Logger;

// todo: This is a temporary class that will be removed once the VXML compiler is implemented
public class GraphFactory {
    public static Logger logger = Logger.getLogger("com.mobeon");

    private Node root;
    private HashMap namedGraphs;
    private String documentBase;
    private String startDoc;

    private static GraphFactory ourInstance = new GraphFactory();

    public static GraphFactory getInstance() {
        return ourInstance;
    }

    private GraphFactory() {
        documentBase = System.getProperty("VXML_ROOTDIR");
//        root = buildDemoGraph();
        this.startDoc =System.getProperty("VXML_STARTDOC");;
        root = buildApplication(documentBase, startDoc);

        namedGraphs = new HashMap();
        namedGraphs.put("root", root);
    }

    public Node getApplication(){
        return root;
    }

    public Node getNamedGraph(String name) {
        return (Node) namedGraphs.get(name);
    }

    public String getDocumentBase() {
        return documentBase;
    }

    public Node buildApplication(String startdir, String startdocument){
        com.mobeon.application.graph.Node root = null;
        try {
            logger.debug("Transforming " + startdir);
            VXML[] vxml_arr = vxml_arr = Transformer.transformPath(new File(startdir));

            BuildGraph bg = new BuildGraph();

            for (int i = 0; i < vxml_arr.length; i++) {
                bg.compileDocument(vxml_arr[i]);
            }
            root = DocumentManager.getInstance().getDocument(startdocument);
        } catch (IOException e) {
            logger.error("Cannot open " + startdir);
            System.exit(ErrorCodes.GENERAL_ERROR);
        }
        return root;
    }

    public Node buildDemoGraph() {
        Node root = null;
        Node current = null;
        Node next = null;
        Node child = null;
        Node endScope = null;

        root = new  VXMLNode();

        current = new CatchNode();
        root.setNext(current);
        next = new LogNode();
        ((CatchNode) current).setExceptionHandlerNode(next);
        next = new VarNode("foo", new Expression("'your mother'"));
        current.setNext(next);
        current = next;
        next = new VarNode("bar", new Expression("'your daddy'"));
        current.setNext(next);
        current = next;
        next = new ScriptNode("function test() {return false;}; function getAudio(audio) {if (audio == 1) return '1.wav'; else return '2.wav';}");
        current.setNext(next);
        current = next;
        next = new FormNode();
        current.setNext(next);
        current = next;
        next = new BlockNode();
        current.setNext(next);
        current = next;

        next = new FieldNode();
        ((FieldNode) next).setName("q1");
        current.setNext(next);
        current = next;

        next = new PromptNode();
        current.setNext(next);
        current = next;

        next = new AudioNode();
        ((AudioNode) next).setSrc("1.wav");
        current.setNext(next);
        current = next;


        next = new GrammarNode();
        ((GrammarNode) next).addRule("1");
        ((GrammarNode) next).addRule("2");
        current.setNext(next);
        current = next;

        next = new ValueRetrievalNode();
        ((ValueRetrievalNode) next).setVarName("q1");
        current.setNext(next);
        current = next;


        IfNode ifnode = new IfNode();
        endScope = new EndScopeNode();
        ifnode.setCond(new Cond("test()"));
        current.setNext(ifnode);
        current = ifnode;

        next = new PromptNode();
        current.setNext(next);
        current = next;

        next = new AudioNode();
        current.setNext(next);
        ((AudioNode) next).setExpr(new Expression("getAudio(q1)"));
        next.setNext(endScope);


        next = new PromptNode();
        ifnode.setNext(next);
        current = next;

        next = new AudioNode();
        current.setNext(next);
        ((AudioNode) next).setExpr(new Expression("getAudio(q1)"));
        next.setNext(endScope);

        current = endScope;
        next = new PromptNode();
        current.setNext(next);
        current = next;

        next = new AudioNode();
        ((AudioNode) next).setSrc("2.wav");
        current.setNext(next);
        current = next;
        next = new EndScopeNode();
        current.setNext(next);
        current = next;
        next = new EndScopeNode();
        current.setNext(next);
        return root;
    }
}
