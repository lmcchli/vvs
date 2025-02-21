/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.DocumentManager;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:15:57
 * To change this template use File | Settings | File Templates.
 */
public class VXMLNode extends Node implements Gotoable {
    private Node next = null;
    private String application;
    private String base;
    private String lang;
    private ArrayList children = new ArrayList();

    public Node execute(Traverser traverser) {
        boolean applScope = false;
        if (base != null)
            logger.debug("Executing " + base);
        else
            logger.debug("Executing");

        // Clear the Gotoables lists
        traverser.clearDocGotoables();
        traverser.clearDialogGotoables();

        for (int i = 0; i < traverser.getDocScopeCounter();i++)
            traverser.leaveScope(applScope); // Pop all root scopes before changing root document

        if (application != null && !application.equals(traverser.getRootURI())) {
            VXMLNode vn = (VXMLNode) DocumentManager.getInstance().getDocument(application);
            for (int i = 0; i < traverser.getRootScopeCounter();i++)
                traverser.leaveScope(true); // Pop all root scopes before changing root document
            vn.load(traverser);
        } else if(application == null) {
            if (!base.equals(traverser.getRootURI())) {
                traverser.setRootURI(base);
                traverser.setInRootDoc(true);
                applScope = true;
            }
        }
        else {
            traverser.setInRootDoc(false);
        }
        traverser.newScope(applScope);

        for (Iterator it = children.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            // n.load(traverser);
            if (n instanceof Gotoable)
                traverser.addDocGotoable(((Gotoable) n).getName(), n);
        }
        // If we where called as a subdialog, retrieve the parameters
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        HashMap params = traverser.getSubdialogParamerters() ;
        if (params != null)  {
            for (Iterator it = params.keySet().iterator(); it.hasNext();){
                String name = (String) it.next();
                Object value = params.get(name);
                ecma.putIntoScope(name, value);
            }
        }
        return next;
    }

    public Node load(Traverser traverser) {

         if (base != null)
            logger.debug("Loading " + base);
        else
            logger.debug("Loading");

        // Clear the Gotoables lists
        traverser.clearDocGotoables();
        traverser.clearDialogGotoables();
        for (int i = 0; i < traverser.getDocScopeCounter();i++)
            traverser.leaveScope(false); // Pop all doc scopes before changing root document

        if (application != null && !application.equals(traverser.getRootURI())) {
            VXMLNode vn = (VXMLNode) DocumentManager.getInstance().getDocument(application);
            for (int i = 0; i < traverser.getScopeCount();i++)
                traverser.leaveScope(true); // Pop all root scopes before changing root document
            vn.load(traverser);
        } else if(application == null) {
            if (base.equals(traverser.getRootURI())) {
                return null; // we are a root application and allready loaded
            }
        }
        traverser.newScope(false);
        for (Iterator it = children.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            n.load(traverser);
        }
        return null;
    }

    public void setNext(Node next) {
        this.next = next;
    }

     public Node getNext() {
        return next;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void addChild(Node n) {
        children.add(n);
    }

    public ArrayList getChildren() {
        return children;
    }

    public String getName() {
        return base;
    }

    public void setName(String name) {
        base = name;
    }
}
