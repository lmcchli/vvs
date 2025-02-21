/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.DocumentManager;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;
import com.mobeon.application.graph.util.URIValidator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-07
 * Time: 18:07:21
 * To change this template use File | Settings | File Templates.
 */
public class GotoNode extends Node {
    private Node next;
    private Expression expr = null;
    private String URI = null;

    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        Node gotoNode = null;
        if (URI != null) {
            if (URIValidator.isDocumentURI(URI)) {  // Are we going to a new document
                Node nextRoot = URIValidator.getDocumentRoot(URIValidator.getDocumentPart(URI)); // Get VXML node of the new document
                if (nextRoot != null ) { // Document found in the Goto-map
                    String dialogPart = URIValidator.getDialogPart(URI);  // Get the dialog part of the URIValidator
                    if (dialogPart != null) {
                        // Need to load scopes etc. since not starting at the top of the document
                        nextRoot.load(traverser);
                        // Get the dialog node in the new document
                        gotoNode = traverser.getDocGotoable(dialogPart);
                    }
                    else {  // No dialog part, going direct to the VXMLNode of the new document
                        gotoNode = nextRoot;
                    }
                } else {
                    gotoNode = null;
                }
            }
            else if (URIValidator.isDialogURI(URI)){  // Are we going to another dialog in this document?
                String dialogURI = URIValidator.getDialogPart(URI);
                gotoNode = traverser.getDocGotoable(dialogURI);
                traverser.leaveScope(false);   // todo: Should more than one scope be pop:ed?
            }
            else { // We are going to an entity in the same dialog
                gotoNode = traverser.getDialogGotoable(URI);
                traverser.leaveScope(false);   // todo: Should more than one scope be pop:ed?
            }
        }
        if (gotoNode == null)
            logger.error("Failed to locate the URIValidator  " + URI + " , bailing out!");
        return gotoNode;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }


}
