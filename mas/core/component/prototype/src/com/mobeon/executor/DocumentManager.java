/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor;

import com.mobeon.application.graph.Node;
import com.mobeon.application.graph.VXMLNode;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class DocumentManager {
    public static Logger logger = Logger.getLogger("com.mobeon");
    private static DocumentManager ourInstance = new DocumentManager();
    private HashMap documents = new HashMap();
    public static DocumentManager getInstance() {
        return ourInstance;
    }

    private DocumentManager() {
    }

    public void addDocument(String URI,Node rootNode){
        logger.debug("Adding document " + URI + " to the document manager");
        documents.put(URI, rootNode);
    }

    public Node getDocument(String URI) {
        logger.debug("Retrieving document " + URI);
        return (Node) documents.get(URI);
    }
}
