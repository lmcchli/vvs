/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.graph.Node;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: HemPC
 * Date: 2005-mar-19
 * Time: 16:08:16
 * To change this template use File | Settings | File Templates.
 */
public class SuspendHandler {
    private ECMAExecutor ecmaExecutor = null;
    private Scope exceptionScope= null;
    private Scope grammarScope= null;
    private Scope fields= null;
    private int docScopeCounter = 0;
    private int rootScopeCounter = 0;
    private boolean inRootDoc = false;
    private String rootURI = null;

    private HashMap dialogGotoables ;
    private HashMap docGotoables ;
    private HashMap subdialogParameters = null;
    private Node next = null;
    private String subdialogName = null;

    public  SuspendHandler(Traverser traverser, Node n, String subDialogName) {
        ecmaExecutor = traverser.getEcmaExecutor();
        exceptionScope = traverser.getExceptionScope();
        grammarScope = traverser.getGrammarScope();
        fields = traverser.getFields();
        docScopeCounter = traverser.getDocScopeCounter();
        rootScopeCounter = traverser.getRootScopeCounter();
        inRootDoc = traverser.isInRootDoc();
        rootURI = traverser.getRootURI();
        dialogGotoables = traverser.getDialogGotoables();
        docGotoables = traverser.getDocGotoables();
        subdialogParameters = traverser.getSubdialogParamerters();
        next = n;
        subdialogName = subDialogName;
    }

    public void resume(Traverser traverser) {
        traverser.setEcmaExecutor(ecmaExecutor);
        traverser.setExceptionScope(exceptionScope);
        traverser.setGrammarScope(grammarScope);
        traverser.setFields(fields);
        traverser.setDocScopeCounter(docScopeCounter);
        traverser.setRootScopeCounter(rootScopeCounter);
        traverser.setRootURI(rootURI);
        traverser.setDialogGotoables(dialogGotoables);
        traverser.setDocGotoables(docGotoables);
        traverser.setSubdialogParamerters(subdialogParameters);
    }

    public Node getNext() {
        return next;
    }

    public String getSubDialogName() {
        return subdialogName;
    }

}
