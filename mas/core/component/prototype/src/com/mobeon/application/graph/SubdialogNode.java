/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.DocumentManager;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;
import com.mobeon.application.graph.util.URIValidator;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by IntelliJ IDEA.
 * User: HemPC
 * Date: 2005-mar-19
 * Time: 10:21:06
 * To change this template use File | Settings | File Templates.
 */
public class SubdialogNode extends Node {
    private Node next = null;
    private Cond cond  = null;
    private Expression expr = null;
    private String name = null;
    private String src = null;
    private String srcExpression = null;
    private ArrayList children = new ArrayList();
    private HashMap params = new HashMap();

    public Node execute(Traverser traverser) {
        logger.debug("Executing (Calldepth is " + (traverser.getDocScopeCounter() + traverser.getRootScopeCounter()) + ")");
        Node subdialogNode = null;
                // "Suspend" execution of the current flow
        traverser.suspend(next, name);

        HashMap parameters = new HashMap();
        for (Iterator it = params.keySet().iterator(); it.hasNext();){
            String name = (String) it.next();
            Expression expr = (Expression) params.get(name);
            Object value = expr.eval(traverser.getEcmaExecutor());
            parameters.put(name,value);
        }
        traverser.setSubdialogParamerters(parameters);

        String URI = null;
        if (src != null)
            URI = src;
        else if (srcExpression != null)
            URI =  (String) traverser.getEcmaExecutor().exec(srcExpression);

        if (URI != null) {
            if (URIValidator.isDocumentURI(src)) {  // Are we going to a new document
                Node nextRoot = URIValidator.getDocumentRoot(URIValidator.getDocumentPart(URI)); // Get VXML node of the new document
                if (nextRoot != null ) { // Document found in the Goto-map
                    String dialogPart = URIValidator.getDialogPart(URI);  // Get the dialog part of the URIValidator
                    if (dialogPart != null) {
                        // Need to load scopes etc. since not starting at the top of the document
                        nextRoot.load(traverser);
                        // Get the dialog node in the new document
                        subdialogNode = traverser.getDocGotoable(dialogPart);
                    }
                    else {  // No dialog part, going direct to the VXMLNode of the new document
                        subdialogNode = nextRoot;
                    }
                } else {
                    subdialogNode = null;
                }
            }
            else if (URIValidator.isDialogURI(URI)){  // Are we going to another dialog in this document?
                String dialogURI = URIValidator.getDialogPart(URI);
                subdialogNode = traverser.getDocGotoable(dialogURI);
                traverser.leaveScope(false);   // todo: Should more than one scope be pop:ed?
            }
            else { // We are going to an entity in the same dialog
                subdialogNode = traverser.getDialogGotoable(URI);
                traverser.leaveScope(false);   // todo: Should more than one scope be pop:ed?
            }
        }
        if (subdialogNode == null)
            logger.error("Failed to locate the subdialog, bailing out!");
        return subdialogNode;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrcExpression() {
        return srcExpression;
    }

    public void setSrcExpression(String srcExpression) {
        this.srcExpression = srcExpression;
    }

    public HashMap getParams() {
        return params;
    }

    public void addParam(String name, Expression expr) {
        params.put(name, expr);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }


}
